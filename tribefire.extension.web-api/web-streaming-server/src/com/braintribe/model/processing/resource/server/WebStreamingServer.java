// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.processing.resource.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.logging.Logger;
import com.braintribe.model.cache.CacheControl;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.resource.server.request.ResourceDeleteRequest;
import com.braintribe.model.processing.resource.server.request.ResourceDownloadRequest;
import com.braintribe.model.processing.resource.server.request.ResourceStreamingRequest;
import com.braintribe.model.processing.resource.server.request.ResourceUploadRequest;
import com.braintribe.model.processing.resource.server.stream.InvalidRangeException;
import com.braintribe.model.processing.resource.server.stream.OnDemandSuppliedOutputStream;
import com.braintribe.model.processing.resource.server.stream.ReopenableInputStreamProviders;
import com.braintribe.model.processing.resource.server.stream.ReopenableInputStreamProviders.ReopenableInputStreamProvider;
import com.braintribe.model.processing.resource.streaming.ResourceStreamException;
import com.braintribe.model.processing.rpc.commons.impl.client.GmRpcExceptionContextualizer;
import com.braintribe.model.processing.rpc.commons.impl.logging.RpcServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.aspect.SummaryLoggerAspect;
import com.braintribe.model.processing.service.commons.NoOpServiceRequestSummaryLogger;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.condition.FingerprintMismatch;
import com.braintribe.model.resourceapi.stream.condition.ModifiedSince;
import com.braintribe.model.resourceapi.stream.condition.StreamCondition;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.util.servlet.stream.OnDemandOpeningOutputStream;
import com.braintribe.util.servlet.stream.PartInputStreamSupplier;
import com.braintribe.util.servlet.stream.RequestInputStreamSupplier;
import com.braintribe.util.servlet.util.ServletTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * <p>
 * {@link HttpServlet} used for the streaming of {@link Resource}(s).
 * 
 * <p>
 * Download ({@code GET}), upload ({@code POST}) and deletion ({@code DELETE}) operations are supported.
 * 
 */
public class WebStreamingServer extends HttpServlet {

	// constants
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(WebStreamingServer.class);
	private static final String logStepSession = "Opening GM session";
	private static final String logStepQuery = "Querying resource";
	private static final String logStepStream = "Transfering binary data";
	private static final String logStepDelete = "Deleting binary data";

	// configurable
	private PersistenceGmSessionFactory sessionFactory;
	private PersistenceGmSessionFactory systemSessionFactory;
	
	
	private ModelAccessoryFactory modelAccessoryFactory;
	
	private MarshallerRegistry marshallerRegistry;
	private String defaultUploadResponseType;

	private Set<String> systemAccessIds = Collections.singleton("cortex");
	
	@Required
	@Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Required
	@Configurable
	public void setSystemSessionFactory(PersistenceGmSessionFactory systemSessionFactory) {
		this.systemSessionFactory = systemSessionFactory;
	}
	
	@Required
	@Configurable
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	@Configurable
	public void setSystemAccessIds(Set<String> systemAccessIds) {
		this.systemAccessIds = systemAccessIds;
	}
	
	/**
	 * <p>
	 * Sets the {@link MarshallerRegistry} which will provide {@link Marshaller}(s) for unmarshalling the uploaded
	 * {@link Resource}(s).
	 * 
	 * @param marshallerRegistry
	 *            The source of the {@link Marshaller}(s) used for unmarshalling the uploaded {@link Resource}(s)
	 */
	@Configurable
	@Required
	public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}

	/**
	 * <p>
	 * Determines the default response mime type to be used in case no {@code responseMimeType} parameter is informed in
	 * the upload request.
	 * 
	 * <p>
	 * The configured mime type must have an equivalent {@link Marshaller} registered to {@link #marshallerRegistry}.
	 * 
	 * @param defaultUploadResponseType
	 *            The default response mime type to be used in case no {@code responseMimeType} parameter is informed in
	 *            the upload request
	 */
	@Configurable
	public void setDefaultUploadResponseType(String defaultUploadResponseType) {
		this.defaultUploadResponseType = defaultUploadResponseType;
	}

	/**
	 * <p>
	 * Processes resource streaming requests.
	 * 
	 * <p>
	 * The following request parameters are handled:
	 * 
	 * <ul>
	 * <li>{@code sessionId}</li>
	 * <li>{@code accessId}</li>
	 * <li>{@code resourceId}</li>
	 * <li>{@code noCache}</li>
	 * <li>{@code download}</li>
	 * <li>{@code fileName}</li>
	 * </ul>
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		final AttributeContext attributeContext = createContext();
		ServiceRequestSummaryLogger summaryLogger = attributeContext.findAttribute(SummaryLoggerAspect.class).orElse(NoOpServiceRequestSummaryLogger.INSTANCE);
		ResourceDownloadRequest streamingRequest = null;

		AttributeContexts.push(attributeContext);
		
		try {
			final PersistenceGmSession gmSession;
			final Resource resource;

			// Parsing the request
			streamingRequest = getResourceDownloadRequest(request);

			if (streamingRequest.getResourceId() == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			// Opening GM session
			summaryLogger.startTimer(logStepSession);
			try {
				gmSession = openSystemSession(streamingRequest.getAccessId());
			} finally {
				summaryLogger.stopTimer(logStepSession);
			}

			// Querying the Resource
			summaryLogger.startTimer(logStepQuery);
			try {
				resource = retrieveResource(gmSession, streamingRequest.getResourceId(), streamingRequest.getAccessId());
				if (resource == null) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			} finally {
				summaryLogger.stopTimer(logStepQuery);
			}

			// Streaming the binary data
			summaryLogger.startTimer(logStepStream);
			try {
				stream(streamingRequest, response, gmSession, resource);
			} finally {
				summaryLogger.stopTimer(logStepStream);
			}

		} catch (RuntimeException e) {
			throw GmRpcExceptionContextualizer.enhanceException(e, attributeContext, null);
		} catch (Exception e) {
			throw new ServletException(GmRpcExceptionContextualizer.enhanceException(e, attributeContext, null));
			
		} finally {
			AttributeContexts.pop();
			summaryLogger.log(this, null);
			summaryLogger.logOneLine("download", null);

		}
	}

	/**
	 * <p>
	 * Processes resource upload requests.
	 * 
	 * <p>
	 * The following request parameters are handled:
	 * 
	 * <ul>
	 * <li>{@code sessionId}</li>
	 * <li>{@code accessId}</li>
	 * <li>{@code responseMimeType}</li>
	 * <li>{@code fileName} (for non-multipart requests)</li>
	 * <li>{@code useCase}</li>
	 * <li>{@code sourceType}</li>
	 * </ul>
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		final AttributeContext attributeContext = createContext();
		ServiceRequestSummaryLogger summaryLogger = attributeContext.findAttribute(SummaryLoggerAspect.class).orElse(NoOpServiceRequestSummaryLogger.INSTANCE);

		AttributeContexts.push(attributeContext);
		
		try {
			final ResourceUploadRequest streamingRequest;
			final PersistenceGmSession gmSession;
			final List<Resource> resources;

			// Parsing the request
			streamingRequest = getResourceUploadRequest(request);

			setDefaultCacheControlHeaders(response);

			// Opening GM session
			summaryLogger.startTimer(logStepSession);
			try {
				gmSession = openSession(streamingRequest.getAccessId());
			} finally {
				summaryLogger.stopTimer(logStepSession);
			}

			// Streaming the binary data
			summaryLogger.startTimer(logStepStream);
			try {
				resources = uploadResources(gmSession, request, streamingRequest);
			} finally {
				summaryLogger.stopTimer(logStepStream);
			}

			// Writes the response
			writeUploadResponse(response, streamingRequest, resources);

		} catch (RuntimeException e) {
			throw GmRpcExceptionContextualizer.enhanceException(e, attributeContext, null);
		} catch (Exception e) {
			throw new ServletException(GmRpcExceptionContextualizer.enhanceException(e, attributeContext, null));

		} finally {
			AttributeContexts.pop();
			summaryLogger.log(this, null);
			summaryLogger.logOneLine("upload", null);

		}
	}

	/**
	 * <p>
	 * Processes resource deletion requests.
	 * 
	 * <p>
	 * The following request parameters are handled:
	 * 
	 * <ul>
	 * <li>{@code sessionId}</li>
	 * <li>{@code accessId}</li>
	 * <li>{@code resourceId}</li>
	 * </ul>
	 */
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		final AttributeContext attributeContext = createContext();
		ServiceRequestSummaryLogger summaryLogger = attributeContext.findAttribute(SummaryLoggerAspect.class).orElse(NoOpServiceRequestSummaryLogger.INSTANCE);
		ResourceDeleteRequest streamingRequest = null;

		AttributeContexts.push(attributeContext);
		
		try {
			final PersistenceGmSession gmSession;
			final Resource resource;

			// Parsing the request
			streamingRequest = getResourceDeleteRequest(request);

			if (streamingRequest.getResourceId() == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			// Opening GM session
			summaryLogger.startTimer(logStepSession);
			try {
				gmSession = openSession(streamingRequest.getAccessId());
			} finally {
				summaryLogger.stopTimer(logStepSession);
			}

			// Querying the Resource
			summaryLogger.startTimer(logStepQuery);
			try {
				resource = retrieveResource(gmSession, streamingRequest.getResourceId(), streamingRequest.getAccessId());
				if (resource == null) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			} finally {
				summaryLogger.stopTimer(logStepQuery);
			}

			// Deleting the binary data
			summaryLogger.startTimer(logStepDelete);
			try {
				// @formatter:off
				gmSession
					.resources()
						.delete(resource)
						.useCase(streamingRequest.getUseCase())
						.delete();
				// @formatter:on
			} finally {
				summaryLogger.stopTimer(logStepDelete);
			}

		} catch (RuntimeException e) {
			throw GmRpcExceptionContextualizer.enhanceException(e, attributeContext, null);
		} catch (Exception e) {
			throw new ServletException(GmRpcExceptionContextualizer.enhanceException(e, attributeContext, null));

		} finally {
			AttributeContexts.pop();
			summaryLogger.log(this, null);
			summaryLogger.logOneLine("deletion", null);

		}
	}

	/**
	 * <p>
	 * Determines whether the request given by {@code httpRequest} is just a ping request.
	 * 
	 * @param httpRequest
	 *            The {@link HttpServletRequest} to be checked
	 * @return {@code true} is the request given {@link HttpServletRequest} is a ping request, {@code false} otherwise.
	 */
	protected boolean isPingRequest(HttpServletRequest httpRequest) {
		return (httpRequest.getPathInfo() == null || httpRequest.getPathInfo().trim().isEmpty())
				&& (httpRequest.getParameterMap() == null || httpRequest.getParameterMap().isEmpty());
	}

	protected AttributeContext createContext() {

		AttributeContext context = AttributeContexts.peek();

		return context.derive()
			.set(SummaryLoggerAspect.class, RpcServiceRequestSummaryLogger.getInstance(log, context, null))
			.build();
	}

	/**
	 * <p>
	 * Upload the resources contained in {@link HttpServletRequest}'s payload using the {@link ResourceAccess}
	 * configured with the given {@link PersistenceGmSession}
	 */
	protected List<Resource> uploadResources(PersistenceGmSession gmSession, HttpServletRequest request, ResourceUploadRequest uploadRequest)
			throws ResourceStreamException {

		final List<Resource> resources = new ArrayList<>();
		final ResourceAccess resourceAccess = gmSession.resources();

		if (resourceAccess == null)
			throw new ResourceStreamException("No resource access was provided by the gm session");

		if (ServletTools.isMultipart(request)) {
			Collection<Part> parts = getParts(request);
			for (Part part : parts) {
				try {
					if (ServletTools.isContentPart(part)) {
						try (ReopenableInputStreamProvider in = ReopenableInputStreamProviders.create(new PartInputStreamSupplier(part)::get)) {
							uploadResource(resourceAccess, in, ServletTools.getFileName(part), getMimeType(part), null, uploadRequest, resources);
						} catch (IOException thrownWhenCloseFails) {
							log.warn("Could not close input stream provider from part " + part, thrownWhenCloseFails);
						} catch(Exception e) {
							throw new ResourceStreamException("Error while trying to upload part: "+part, e);
						}
					}
				} finally {
					try {
						part.delete();
					} catch (Exception e) {
						log.warn("Could not delete part: " + part, e);
					}
				}
			}
		} else {
			try (ReopenableInputStreamProvider in = ReopenableInputStreamProviders.create(new RequestInputStreamSupplier(request)::get)) {
				uploadResource(resourceAccess, in, getFileName(uploadRequest), uploadRequest.getMimeType(), uploadRequest.getMd5(), uploadRequest, resources);
			} catch (IOException thrownWhenCloseFails) {
				log.warn("Could not close input stream provider", thrownWhenCloseFails);
			}
		}

		return resources;
	}



	/**
	 * <p>
	 * Uploads the resource given by the {@link InputStreamProvider} through {@link ResourceAccess#create()}
	 */
	private void uploadResource(ResourceAccess resourceAccess, InputStreamProvider inputStreamSupplier, final String fileName,
			final String mimeType, final String md5,
			ResourceUploadRequest uploadRequest, final List<Resource> resources) {

		// @formatter:off
		Resource resource = 
				resourceAccess
					.create()
						.name(fileName)
						.useCase(uploadRequest.getUseCase())
						.mimeType(mimeType)
						.md5(md5)
						.sourceType(uploadRequest.getSourceType())
						.specification(uploadRequest.getSpecification())
						.tags(uploadRequest.getTags())
						.store(inputStreamSupplier);
		// @formatter:on

		resources.add(resource);

	}

	/**
	 * <p>
	 * Commits the given {@link PersistenceGmSession}.
	 * 
	 * @param gmSession
	 *            The {@link PersistenceGmSession} to be commited
	 * @throws ResourceStreamException
	 *             In case the given {@link PersistenceGmSession} fails to be commited
	 */
	protected void commitPersistenceGmSession(PersistenceGmSession gmSession) throws ResourceStreamException {

		if (!gmSession.getTransaction().hasManipulations())
			return;

		try {
			gmSession.commit();
			if (gmSession instanceof BasicManagedGmSession)
				((BasicManagedGmSession) gmSession).cleanup();
		} catch (GmSessionException e) {
			throw new ResourceStreamException("Failed to commit persistence GM session: " + e.getMessage(), e);
		}
	}

	/**
	 * <p>
	 * Creates a {@link ResourceDownloadRequest} instance based on the given {@linkplain HttpServletRequest request}
	 * parameter.
	 */
	protected ResourceDownloadRequest getResourceDownloadRequest(HttpServletRequest request) throws ResourceStreamException, InvalidRangeException {

		try {

			ResourceDownloadRequest resourceDownloadRequest = new ResourceDownloadRequest();
			resourceDownloadRequest.setSessionId(request.getParameter("sessionId"));
			resourceDownloadRequest.setAccessId(request.getParameter("accessId"));
			resourceDownloadRequest.setFileName(request.getParameter("fileName"));
			resourceDownloadRequest.setResourceId(request.getParameter("resourceId"));
			resourceDownloadRequest.setNoCache(Boolean.TRUE.toString().equalsIgnoreCase(request.getParameter("noCache")));
			resourceDownloadRequest.setDownload(Boolean.TRUE.toString().equalsIgnoreCase(request.getParameter("download")));
			resourceDownloadRequest.setIfNoneMatch(request.getHeader("If-None-Match"));
			parseContext(resourceDownloadRequest, request);

			
			long IfModifiedSinceLong = request.getDateHeader("If-Modified-Since");
			if (IfModifiedSinceLong > -1) {
				resourceDownloadRequest.setIfModifiedSince(new Date(IfModifiedSinceLong));
			}
			
			parseRangeHeader(request, resourceDownloadRequest);

			return resourceDownloadRequest;
		} catch (InvalidRangeException ire) {
			throw ire;
		} catch (Exception e) {
			throw new ResourceStreamException("Unable to parse download request: " + e.getMessage(), e);
		}

	}
	
	protected void parseRangeHeader(HttpServletRequest request, ResourceDownloadRequest resourceDownloadRequest) throws InvalidRangeException {
		
		String rangeHeader = request.getHeader("Range");
		if (StringTools.isBlank(rangeHeader)) {
			return;
		}
		
		try {
			
			int index = rangeHeader.indexOf('=');
			if (index == -1) {
				throw new InvalidRangeException("There is no '=' sign.");
			}
			String unit = rangeHeader.substring(0, index).trim();
			if (StringTools.isBlank(unit) || !unit.equalsIgnoreCase("bytes")) {
				throw new InvalidRangeException("Only unit 'bytes' is supported.");
			}
			String rangeSpec = rangeHeader.substring(index+1).trim();
			index = rangeSpec.indexOf('-');
			if (index == -1) {
				throw new InvalidRangeException("The range value "+rangeSpec+" does not contain '-'");
			}
			String start = rangeSpec.substring(0, index).trim();
			String end = null;
			if (index < rangeSpec.length()-1) {
				end = rangeSpec.substring(index+1).trim();
			}
			long startLong = Long.parseLong(start);
			long endLong = -1;
			if (!StringTools.isBlank(end)) {
				endLong = Long.parseLong(end);
			}
			
			resourceDownloadRequest.setRangeStart(startLong);
			resourceDownloadRequest.setRangeEnd(endLong);
		} catch(InvalidRangeException ire) {
			throw ire;
		} catch (Exception e) {
			throw new InvalidRangeException("Unable to parse Range header \""+rangeHeader+"\".", e);
		}
		
	}

	/**
	 * <p>
	 * Creates a {@link ResourceUploadRequest} instance based on the given {@linkplain HttpServletRequest request}
	 * parameter.
	 */
	protected ResourceUploadRequest getResourceUploadRequest(HttpServletRequest request) throws ResourceStreamException {

		try {

			ResourceUploadRequest resourceUploadRequest = new ResourceUploadRequest();
			resourceUploadRequest.setSessionId(request.getParameter("sessionId"));
			resourceUploadRequest.setAccessId(request.getParameter("accessId"));
			resourceUploadRequest.setFileName(request.getParameter("fileName"));
			resourceUploadRequest.setResponseMimeType(request.getParameter("responseMimeType"));
			resourceUploadRequest.setUseCase(request.getParameter("useCase"));
			resourceUploadRequest.setMimeType(request.getParameter("mimeType"));
			resourceUploadRequest.setMd5(request.getParameter("md5"));
			
			Marshaller marshaller = null;
			String encodedSpecification = request.getParameter("specification");
			if (encodedSpecification != null) {
				marshaller = marshallerRegistry.getMarshaller("application/json");
				if (marshaller instanceof HasStringCodec) {
					HasStringCodec codec = (HasStringCodec) marshaller;
					ResourceSpecification specification = (ResourceSpecification) codec.getStringCodec().decode(encodedSpecification);
					resourceUploadRequest.setSpecification(specification);
				}
			}
			
			String encodedTags = request.getParameter("tags");
			if (encodedTags != null) {
				if (marshaller == null) {
					marshaller = marshallerRegistry.getMarshaller("application/json");
				}
				if (marshaller instanceof HasStringCodec) {
					HasStringCodec codec = (HasStringCodec) marshaller;
					Set<String> tags = (Set<String>) codec.getStringCodec().decode(encodedTags);
					resourceUploadRequest.setTags(tags);
				}
			}
			
			parseContext(resourceUploadRequest, request);
			
			String sourceTypeSignature = request.getParameter("sourceType");
			if (sourceTypeSignature != null) {
				EntityType<? extends ResourceSource> sourceType = GMF.getTypeReflection().getEntityType(sourceTypeSignature);
				resourceUploadRequest.setSourceType(sourceType);
			}

			return resourceUploadRequest;

		} catch (Exception e) {
			throw new ResourceStreamException("Unable to parse upload request: " + e.getMessage(), e);
		}

	}
	
	protected static void parseContext(ResourceStreamingRequest streamingRequest, HttpServletRequest servletRequest) throws ResourceStreamException {
		String contextString = servletRequest.getParameter("context");
		if (contextString != null) {
			try {
				Map<String,String> contextMap = StringTools.decodeStringMapFromString(contextString);
				streamingRequest.setContext(contextMap);
			} catch(Exception e) {
				throw new ResourceStreamException("Could not parse context "+contextString, e);
			}
		}
	}

	/**
	 * <p>
	 * Creates a {@link ResourceDownloadRequest} instance based on the given {@linkplain HttpServletRequest request}
	 * parameter.
	 */
	protected ResourceDeleteRequest getResourceDeleteRequest(HttpServletRequest request) throws ResourceStreamException {

		try {

			ResourceDeleteRequest resourceDeleteRequest = new ResourceDeleteRequest();
			resourceDeleteRequest.setSessionId(request.getParameter("sessionId"));
			resourceDeleteRequest.setAccessId(request.getParameter("accessId"));
			resourceDeleteRequest.setResourceId(request.getParameter("resourceId"));
			resourceDeleteRequest.setUseCase(request.getParameter("useCase"));
			parseContext(resourceDeleteRequest, request);

			return resourceDeleteRequest;

		} catch (Exception e) {
			throw new ResourceStreamException("Unable to parse delete request: " + e.getMessage(), e);
		}

	}

	/**
	 * <p>
	 * Opens a {@link PersistenceGmSession} based on the given {@code accessId} parameter.
	 * 
	 * <p>
	 * The returned {@link PersistenceGmSession} is expected to support resource streaming (
	 * {@link PersistenceGmSession#resources()}).
	 * 
	 * @param accessId
	 *            Id of the target access for streaming operations
	 * @return {@link PersistenceGmSession} for manipulating the access given by the provided id
	 * @throws ResourceStreamException
	 *             In case a {@link PersistenceGmSession} fails to be provided from the underlying factory
	 */
	protected PersistenceGmSession openSession(String accessId) throws ResourceStreamException {
		try {
			return sessionFactory.newSession(accessId);
		} catch (GmSessionException e) {
			throw new ResourceStreamException("Error while creating session for access with id: " + accessId, e);
		}
	}

	/**
	 * <p>
	 * Opens a {@link PersistenceGmSession} based on the given {@code accessId} parameter.
	 * 
	 * <p>
	 * The returned {@link PersistenceGmSession} is expected to support resource streaming (
	 * {@link PersistenceGmSession#resources()}).
	 * 
	 * @param accessId
	 *            Id of the target access for streaming operations
	 * @return {@link PersistenceGmSession} for manipulating the access given by the provided id
	 * @throws ResourceStreamException
	 *             In case a {@link PersistenceGmSession} fails to be provided from the underlying factory
	 */
	protected PersistenceGmSession openSystemSession(String accessId) throws ResourceStreamException {
		try {
			if (systemSessionFactory != null && systemAccessIds.contains(accessId)) {
				return systemSessionFactory.newSession(accessId);
			} else {
				return openSession(accessId);
			}
			
		} catch (GmSessionException e) {
			throw new ResourceStreamException("Error while creating session for access with id: " + accessId, e);
		}
	}
	/**
	 * <p>
	 * Queries a {@link Resource} from be given {@link PersistenceGmSession} using the {@source resourceId} provided.
	 * 
	 * @param gmSession
	 *            The {@link PersistenceGmSession} used to query the {@link Resource}
	 * @param resourceId
	 *            Id of the {@link Resource} to be retrieved
	 * @return A {@link Resource} matching the requested id, or {@code null} if the resource is not found
	 * @throws ResourceStreamException
	 *             In case the query fails to be executed
	 * @throws NotFoundException
	 *             If no {@link Resource} is found
	 * @throws NullPointerException
	 *             If any argument is {@code null}
	 */
	protected Resource retrieveResource(PersistenceGmSession gmSession, String resourceId, String accessId) throws ResourceStreamException {

		Objects.requireNonNull(gmSession, "gmSession");
		Objects.requireNonNull(resourceId, "resourceId");

		EntityQuery query = EntityQueryBuilder.from(Resource.T).where().property(Resource.id).eq(resourceId).done();

		Resource resource = null;

		try {
			resource = gmSession.query().entities(query).unique();
		} catch (NotFoundException e) {
			throw e;
		} catch (GmSessionException e) {
			throw new ResourceStreamException(
					"Failed to obtain resource: [ " + resourceId + " ] from access [ " + gmSession.getAccessId() + " ]: " + e.getMessage(), e);
		}

		if (resource == null) {
			throw new NotFoundException("Resource [ " + resourceId + " ] not found");
		}

		if (this.modelAccessoryFactory != null) {
			ModelAccessory modelAccessory = this.modelAccessoryFactory.getForAccess(accessId);
			boolean visible = modelAccessory.getMetaData().entity(resource).is(Visible.T);
			if (!visible) {
				throw new AuthorizationException("Insufficient privileges to retrieve resource.");
			}
		}
		
		return resource;

	}
	
	

	protected void stream(ResourceDownloadRequest streamingRequest, HttpServletResponse response, PersistenceGmSession gmSession, Resource resource)
			throws ResourceStreamException {

		try {

			OutputStream outputStream = new OnDemandSuppliedOutputStream(forWriting -> {

				// if (forWriting) {
				// // Setting body related headers only when there is a body
				// setContentHeaders(response, resource, streamingRequest);
				// }

				try {
					return response.getOutputStream();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}

			});

			StreamCondition condition = streamCondition(streamingRequest);
			StreamRange range = streamRange(streamingRequest);

			// @formatter:off
			gmSession.resources()
						.retrieve(resource)
							.condition(condition)
							.onResponse(
								r -> {
									setDownloadHeaders(
										(StreamBinaryResponse)r, 
										response, 
										resource, 
										streamingRequest
									);
								}
							)
							.range(range)
							.stream(outputStream);
			// @formatter:on

			outputStream.flush();

		} catch (IOException e) {
			throw new ResourceStreamException("Failed to write response: " + e.getMessage(), e);
		}

	}

	private void setDefaultCacheControlHeaders(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-store, no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
	}

	private void setCacheControlHeaders(HttpServletResponse response, CacheControl cacheControl, boolean forceNoCache) {

		if (cacheControl == null || forceNoCache) {

			setDefaultCacheControlHeaders(response);

		} else {

			List<String> ccParts = new ArrayList<>();

			if (cacheControl.getType() != null) {
				switch (cacheControl.getType()) {
					case noCache:
						ccParts.add("no-cache");
						response.setHeader("Pragma", "no-cache");
						break;
					case noStore:
						ccParts.add("no-store");
						break;
					case privateCache:
						ccParts.add("private");
						break;
					case publicCache:
						ccParts.add("public");
						break;
				}
			}

			if (cacheControl.getMaxAge() != null) {
				ccParts.add("max-age=" + cacheControl.getMaxAge());
			}

			if (cacheControl.getMustRevalidate()) {
				ccParts.add("must-revalidate");
			}

			if (ccParts.isEmpty()) {
				setDefaultCacheControlHeaders(response);
			} else {
				response.setHeader("Cache-Control", String.join(", ", ccParts));
			}

			if (cacheControl.getLastModified() != null) {
				response.setDateHeader("Last-Modified", cacheControl.getLastModified().getTime());
			}

			if (cacheControl.getFingerprint() != null) {
				response.setHeader("ETag", cacheControl.getFingerprint());
			}

		}

	}

	/**
	 * <p>
	 * Prepares the {@link HttpServletResponse} for a resource streaming download request based on the {@link Resource}
	 * requested to be streamed and the initial {@link ResourceDownloadRequest} sent.
	 * 
	 * @param response
	 *            {@link HttpServletResponse} to be prepared
	 * @param resource
	 *            {@link Resource} about to be streamed
	 * @param streamingRequest
	 *            {@link ResourceDownloadRequest} holding resource streaming request parameters
	 */
	private void setContentHeaders(HttpServletResponse response, Resource resource, ResourceDownloadRequest streamingRequest, StreamBinaryResponse streamBinaryResponse) {

		String disposition = streamingRequest.isDownload() ? "attachment" : "inline";

		String fileName = streamingRequest.getFileName();

		if (fileName == null || fileName.isEmpty()) {
			fileName = resource.getName();
		}

		if (fileName != null) {
			final String normalizedFilename = FileTools.normalizeFilename(fileName, '_');
			
			// Because the Content-Disposition header is encoded in ISO-8859-1 we need to handle characters outside this encoding specially
			boolean needsToBeEncoded = ! Charset.forName("ISO-8859-1").newEncoder().canEncode(fileName);

			// Sometimes the 'filename*' attribute is not supported by a client. That's why we always send a normalized
			// simple 'filename' as a backup.
			// Only if the normalization changes the value we supply a 'filename*' variant which always provides the
			// original filename. If 'filename*' is supported, it should always have prevalence over the simple 'filename'.
			// See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition#Directives
			if (!normalizedFilename.equals(fileName) || needsToBeEncoded) {
				try {
					String urlEncodedFilename = URLEncoder.encode(fileName, "UTF-8");
					disposition += "; filename*=UTF-8''" + urlEncodedFilename;
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException("Could not UrlEncode filename for response header.");
				}
			}
			
			disposition += "; filename=\"" + normalizedFilename + "\"";
			
		} 

		response.setHeader("Content-Disposition", disposition);

		String mimeType = null;
		if (streamingRequest.isDownload()) {
			mimeType = "application/download";
		} else {
			mimeType = resource.getMimeType();
		}

		response.setContentType(mimeType);
		
		
		boolean ranged = streamBinaryResponse.getRanged();
		if (ranged) {
			Long size = streamBinaryResponse.getSize();
			String sizeString = (size != null) ? String.valueOf(size) : "*";
			String rangeStart = String.valueOf(streamBinaryResponse.getRangeStart());
			String rangeEnd = String.valueOf(streamBinaryResponse.getRangeEnd());
			String contentRange = "bytes ".concat(rangeStart).concat("-").concat(rangeEnd).concat("/").concat(sizeString);
			response.setHeader("Content-Range", contentRange);
			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		}

	}

	private void setDownloadHeaders(StreamBinaryResponse response, HttpServletResponse httpResponse, Resource resource,
			ResourceDownloadRequest streamingRequest) {

		if (response.getNotStreamed()) {
			httpResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		} else {
			setContentHeaders(httpResponse, resource, streamingRequest, response);
		}

		setCacheControlHeaders(httpResponse, response.getCacheControl(), streamingRequest.isNoCache());

	}

	private StreamCondition streamCondition(ResourceDownloadRequest streamingRequest) {

		if (streamingRequest.getIfNoneMatch() != null) {
			FingerprintMismatch condition = FingerprintMismatch.T.create();
			condition.setFingerprint(streamingRequest.getIfNoneMatch());
			return condition;
		} else if (streamingRequest.getIfModifiedSince() != null) {
			ModifiedSince condition = ModifiedSince.T.create();
			condition.setDate(streamingRequest.getIfModifiedSince());
			return condition;
		}

		return null;

	}
	
	private StreamRange streamRange(ResourceDownloadRequest streamingRequest) {

		long rangeStart = streamingRequest.getRangeStart();
		long rangeEnd = streamingRequest.getRangeEnd();
		
		if (rangeStart < 0 && rangeEnd < 0) {
			return null;
		}
		
		StreamRange range = StreamRange.T.create();
		if (rangeStart >= 0) {
			range.setStart(rangeStart);
		}
		if (rangeEnd >= 0) {
			range.setEnd(rangeEnd);
		}

		return range;

	}

	/**
	 * <p>
	 * Writes the marshalled representation of the uploaded {@link Resource}(s) to {@link HttpServletResponse}'s
	 * {@link OutputStream}.
	 * 
	 * <p>
	 * After using this method, the given {@link HttpServletResponse} should be considered to be committed and should
	 * not be written to.
	 * 
	 * @param response
	 *            Source of the writing {@link OutputStream} target
	 * @param streamingRequest
	 *            {@link ResourceUploadRequest} providing the upload request parameters
	 * @param resources
	 *            {@link Resource}(s) to have its marshalled representation written to {@link HttpServletResponse}'s
	 *            {@link OutputStream}
	 * @throws ResourceStreamException
	 *             If either the encoding or writing operation fail
	 */
	protected void writeUploadResponse(HttpServletResponse response, ResourceUploadRequest streamingRequest, List<Resource> resources)
			throws ResourceStreamException {

		MarshallerRegistryEntry marshallerRegistryEntry = getMarshallerRegistryEntry(streamingRequest.getResponseMimeType());
		response.setContentType(marshallerRegistryEntry.getMimeType());

		OutputStream out;
		try {
			out = new OnDemandOpeningOutputStream(response);
			marshallerRegistryEntry.getMarshaller().marshall(out, resources);
			out.flush();
		} catch (Exception e) {
			throw new ResourceStreamException("Failed to write response: " + e.getMessage(), e);
		}
	}

	/**
	 * Retrieves a {@link MarshallerRegistryEntry} from {@link #marshallerRegistry} based on the given mime type.
	 * <p>
	 * If the given mime type is not found in the registry, this method will try to fetch a
	 * {@link MarshallerRegistryEntry} based on the configured {@link #defaultUploadResponseType}
	 * 
	 * @param type
	 *            The mime type
	 * @return a {@link MarshallerRegistryEntry} correspondent to the given mime type, if configured
	 * @throws ResourceStreamException
	 *             If no {@link MarshallerRegistryEntry} is found for either {@code type} parameter or
	 *             {@link #defaultUploadResponseType}
	 */
	protected MarshallerRegistryEntry getMarshallerRegistryEntry(String type) throws ResourceStreamException {

		MarshallerRegistryEntry marshallerRegistryEntry = marshallerRegistry.getMarshallerRegistryEntry(type);

		if (marshallerRegistryEntry != null && marshallerRegistryEntry.getMarshaller() != null) {
			if (log.isDebugEnabled())
				log.debug("Found [ " + marshallerRegistryEntry.getMarshaller().getClass().getSimpleName() + " ] for given [ " + type + " ] type");
			return marshallerRegistryEntry;
		}

		marshallerRegistryEntry = marshallerRegistry.getMarshallerRegistryEntry(defaultUploadResponseType);

		if (marshallerRegistryEntry != null && marshallerRegistryEntry.getMarshaller() != null) {
			if (log.isDebugEnabled())
				log.debug("Found [ " + marshallerRegistryEntry.getMarshaller().getClass().getSimpleName() + " ] for default [ "
						+ defaultUploadResponseType + " ] type");
			return marshallerRegistryEntry;
		}

		throw new ResourceStreamException(
				"No marshaller configured for given mime-type [ " + type + " ] nor for default [ " + defaultUploadResponseType + " ] ");
	}
	
	/**
	 * Retrieves the mime type that might be set on the part. If the result is <code>application/octet-stream</code>, null will be returned instead.
	 * 
	 * @param part The part to be inspected.
	 * @return The MIME-type or null if none was set.
	 */
	private String getMimeType(Part part) {
		String mimeTypeFromContentType = HttpTools.getMimeTypeFromContentType(part.getHeader("Content-Type"), true);
		if (mimeTypeFromContentType != null && mimeTypeFromContentType.equalsIgnoreCase("application/octet-stream")) {
			//We can/should ignore this information and find out the real mime type later
			mimeTypeFromContentType = null;
		}
		return mimeTypeFromContentType;
	}

	/**
	 * <p>
	 * Retrieves the file name from a non-multipart request.
	 * 
	 * @param request
	 *            {@link ResourceUploadRequest} to be inspected for file name
	 * @return The file name as extracted from the given request
	 */
	private static String getFileName(ResourceUploadRequest request) {

		if (request.getFileName() == null) {
			if (log.isDebugEnabled()) {
				log.debug("No [ fileName ] request parameter was provided");
			}
			return null;
		}
		//There used to be this code here, but I don't get it....
		//The filename of the request is perfectly encoded because the filename in the POST URL was URL-encoded anyway
		//I tested this with German literals and Chinese letters... it works now
		//return new String(request.getFileName().getBytes("ISO-8859-1"), "UTF-8");

		return request.getFileName();
	}

	
	/**
	 * <p>
	 * Retrieves the {@link Part}(s) from the given multipart request.
	 */
	private static Collection<Part> getParts(HttpServletRequest request) throws ResourceStreamException {
		try {
			return request.getParts();
		} catch (Exception e) {
			throw new ResourceStreamException("Failed to get parts from multipart request: " + e.getMessage(), e);
		}
	}
}
