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
package com.braintribe.model.processing.ddra.endpoints.api.v1;

import static com.braintribe.ddra.endpoints.api.DdraEndpointsUtils.getPathInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang.StringUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.CharsetOption;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.url.UrlEncodingMarshaller;
import com.braintribe.ddra.endpoints.api.DdraEndpointAspect;
import com.braintribe.ddra.endpoints.api.DdraEndpointsUtils;
import com.braintribe.ddra.endpoints.api.api.v1.ApiV1EndpointContext;
import com.braintribe.ddra.endpoints.api.api.v1.DdraMappings;
import com.braintribe.ddra.endpoints.api.api.v1.SingleDdraMapping;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.reason.meta.HttpStatusCode;
import com.braintribe.gm.model.reason.meta.LogReason;
import com.braintribe.logging.Logger;
import com.braintribe.model.DdraBaseUrlPathParameters;
import com.braintribe.model.DdraEndpoint;
import com.braintribe.model.DdraEndpointDepth;
import com.braintribe.model.DdraEndpointHeaders;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.ddra.DdraConfiguration;
import com.braintribe.model.ddra.DdraUrlMethod;
import com.braintribe.model.ddra.endpoints.api.v1.ApiV1DdraEndpoint;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.logging.LogLevel;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.ddra.endpoints.AbstractDdraRestServlet;
import com.braintribe.model.processing.ddra.endpoints.api.v1.ApiV1RestServletUtils.DecodingTargetTraversalResult;
//import com.braintribe.model.processing.ddra.endpoints.api.v1.handlers.ResourceDeleteHandler;
//import com.braintribe.model.processing.ddra.endpoints.api.v1.handlers.ResourceDownloadHandler;
//import com.braintribe.model.processing.ddra.endpoints.api.v1.handlers.ResourceUploadHandler;
import com.braintribe.model.processing.ddra.endpoints.interceptors.HttpRequestSupplierImpl;
import com.braintribe.model.processing.ddra.endpoints.interceptors.HttpResponseConfigurerImpl;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.rpc.commons.api.RpcConstants;
import com.braintribe.model.processing.rpc.commons.impl.RpcUnmarshallingStreamManagement;
import com.braintribe.model.processing.service.api.HttpRequestSupplier;
import com.braintribe.model.processing.service.api.HttpRequestSupplierAspect;
import com.braintribe.model.processing.service.api.HttpResponseConfigurerAspect;
import com.braintribe.model.processing.service.api.TraversingCriterionAspect;
import com.braintribe.model.processing.service.api.aspect.HttpStatusCodeNotification;
import com.braintribe.model.processing.service.api.aspect.RequestTransportPayloadAspect;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.web.rest.HttpExceptions;
import com.braintribe.model.processing.web.rest.HttpRequestEntityDecoder;
import com.braintribe.model.processing.web.rest.HttpRequestEntityDecoderOptions;
import com.braintribe.model.processing.web.rest.StandardHeadersMapper;
import com.braintribe.model.processing.web.rest.UrlPathCodec;
import com.braintribe.model.processing.web.rest.impl.QueryParamDecoder;
import com.braintribe.model.prototyping.api.PrototypingRequest;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Unsatisfied;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.collection.api.ListMap;
import com.braintribe.utils.collection.impl.HashListMap;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.MultipartFormat;
import com.braintribe.web.multipart.api.MutablePartHeader;
import com.braintribe.web.multipart.api.PartReader;
import com.braintribe.web.multipart.api.PartWriter;
import com.braintribe.web.multipart.api.SequentialFormDataReader;
import com.braintribe.web.multipart.impl.MultipartSubFormat;
import com.braintribe.web.multipart.impl.Multiparts;
import com.braintribe.web.multipart.impl.SequentialParallelFormDataWriter;

/**
 * The HttpServlet for the /api/v1 path.
 * <p>
 * This class is thread-safe IF the injected {@code DdraMappings} is thread-safe.
 */
public class WebApiV1Server extends AbstractDdraRestServlet<ApiV1EndpointContext> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(WebApiV1Server.class);

	private final static Set<String> requestAssemblyPartNames = CollectionTools.getSet(RpcConstants.RPC_MAPKEY_REQUEST,
			HttpRequestEntityDecoder.SERIALIZED_REQUEST);

	private static final StandardHeadersMapper<DdraEndpointHeaders> ENDPOINT_MAPPER = StandardHeadersMapper.mapToProperties(DdraEndpointHeaders.T);
	private static final UrlPathCodec<DdraBaseUrlPathParameters> URL_CODEC = UrlPathCodec.create(DdraBaseUrlPathParameters.T) //
			.mappedSegment("serviceDomain", true) //
			.mappedSegment("typeSignature");

	private static LogReason defaultLogging;

	static {
		defaultLogging = LogReason.T.create();
		defaultLogging.setLevel(LogLevel.ERROR);
		defaultLogging.setRecursive(true);
	}

	private String mappingsTimestamp;
	private DdraMappings mappings;
	private boolean pollMappings = true;

	private String defaultServiceDomain = "serviceDomain:default";
	private Predicate<String> accessAvailability = this::serviceDomainExists;
	private Supplier<String> sessionIdProvider;
	private ModelAccessoryFactory modelAccessoryFactory;
	private StreamPipeFactory streamPipeFactory;

	private ApiV1RestServletUtils restServletUtils;

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected void handleGet(ApiV1EndpointContext context) throws IOException {
		handleMethodWithoutBody(context);
	}

	@Override
	protected void handleDelete(ApiV1EndpointContext context) throws IOException {
		handleMethodWithoutBody(context);
	}

	@Override
	protected void handlePost(ApiV1EndpointContext context) throws IOException {
		handleMethodWithBody(context);
	}

	@Override
	protected void handlePut(ApiV1EndpointContext context) throws IOException {
		handleMethodWithBody(context);
	}

	@Override
	protected void handlePatch(ApiV1EndpointContext context) throws IOException {
		handleMethodWithBody(context);
	}

	@Override
	protected void handleOptions(ApiV1EndpointContext context) {

		Collection<String> mappedMethods = mappings.getMethods(getPathInfo(context));

		if (mappedMethods.isEmpty()) {
			decodePathAndFillContext(context);

			if (context.getServiceRequestType() == null) {
				throw new NotFoundException("No implicit or explicit mapping found for '" + getPathInfo(context) + "'.");
			}

			mappedMethods = Arrays.asList("GET", "POST");
		}

		context.getResponse().setStatus(204);
		DdraEndpointsUtils.setAllowHeader(context, mappedMethods);
	}

	@Override
	protected ApiV1EndpointContext createContext(HttpServletRequest request, HttpServletResponse response) {
		return new ApiV1EndpointContext(request, response, defaultServiceDomain);
	}

	@Override
	protected boolean fillContext(ApiV1EndpointContext context) {
		// check and load mappings if needed
		if (pollMappings)
			ensureDdraMappingInitialized();

		if ("OPTIONS".equals(context.getRequest().getMethod())) {
			handleOptions(context);
			return false;
		}

		// compute mapping of current request
		SingleDdraMapping mapping = computeDdraMapping(context);

		// get the entity type for the path or mapping
		if (mapping == null) {
			Collection<String> allowedHttpMethodsForMapping = mappings.getMethods(getPathInfo(context));

			if (!allowedHttpMethodsForMapping.isEmpty()) {
				// the mapping is available under a different method. Send 405
				context.getResponse().setStatus(405);
				DdraEndpointsUtils.setAllowHeader(context, allowedHttpMethodsForMapping);
				commitResponse(context);
				return false;
			}

			decodePathAndFillContext(context);
		}

		// get the out marshaller as early as possible to write exceptions with proper mimeType.
		ApiV1DdraEndpoint endpoint = restServletUtils.createDefaultEndpoint(mapping);
		context.setEndpoint(endpoint);

		return true;
	}

	private void commitResponse(ApiV1EndpointContext context) {
		try {
			// commit response so that nothing can be accidentally added afterwards
			context.getResponse().flushBuffer();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void handleMethodWithoutBody(ApiV1EndpointContext context) throws IOException {
		EntityType<? extends ServiceRequest> serviceRequestType = context.getServiceRequestType();

		ServiceRequest service = null;
		if (serviceRequestType != null) {
			service = createDefaultRequest(serviceRequestType, context.getMapping());
			decodeQueryAndFillContext(service, context);
		} else {
			throw new UnsatisfiedMaybeTunneling(Reasons.build(InvalidArgument.T).text("Missing service request type").toMaybe());
		}

		// TODO
		// if (ResourceDownloadHandler.handleRequest(context, service, userSessionFactory, modelAccessoryFactory))
		// return;
		// if (ResourceDeleteHandler.handleRequest(context, service, userSessionFactory, modelAccessoryFactory))
		// return;

		// get the transform request (from the mapping) if any
		service = restServletUtils.computeTransformRequest(context, service);

		// compute the output marshaller
		computeOutMarshallerFor(context, context.getDefaultMimeType());

		processRequestAndWriteResponse(context, service);
	}

	private void handleMethodWithBody(ApiV1EndpointContext context) throws IOException {
		EntityType<? extends ServiceRequest> serviceRequestType = context.getServiceRequestType();

		decodeQueryAndFillContext(null, context);

		// compute output marshaller
		computeOutMarshallerFor(context, context.getDefaultMimeType());

		// TODO: remove
		// if (serviceRequestType != null
		// && ResourceUploadHandler.handleRequest(context, marshallerRegistry, serviceRequestType.create(), userSessionFactory))
		// {
		// return;
		// }

		RpcUnmarshallingStreamManagement streamManagement = new RpcUnmarshallingStreamManagement("rest-api-v1", streamPipeFactory);
		context.setRequestStreamManagement(streamManagement);

		HttpServletRequest request = context.getRequest();

		GmDeserializationOptions options = GmDeserializationOptions.defaultOptions.derive() //
				.setInferredRootType(serviceRequestType) //
				.set(EntityVisitorOption.class, streamManagement.getMarshallingVisitor()) //
				.set(CharsetOption.class, request.getCharacterEncoding()) //
				.build();

		SingleDdraMapping mapping = context.getMapping();

		ApiV1DdraEndpoint endpoint = context.getEndpoint();

		ServiceRequest service = null;
		MultipartFormat requestMultipartFormat = context.getRequestMultipartFormat();
		ServletInputStream requestIn = request.getInputStream();
		if (requestMultipartFormat.getSubFormat() == MultipartSubFormat.formData) {
			String boundary = requestMultipartFormat.getParameter("boundary");

			if (boundary == null) {
				throw new IllegalArgumentException(
						"Illegal Request: Content-Type was 'multipart/form-data' but without the mandatory 'boundary' parameter.");
			}

			service = parseMultipartRequest(boundary, context);
		} else {

			if ("application/x-www-form-urlencoded".equals(endpoint.getContentType())) {
				ListMap<String, String> parameters = new HashListMap<>();
				request.getParameterMap().forEach((k, v) -> parameters.put(k, Arrays.asList(v)));

				UrlEncodingMarshaller.EntityTemplateFactory rootEntityFactory = l -> requestAssemblyPartNames.stream().map(l::getSingleElement)
						.filter(Objects::nonNull).findFirst().map(a -> {
							CharacterMarshaller jsonMarshaller = (CharacterMarshaller) marshallerRegistry
									.getMarshaller(DdraEndpointsUtils.APPLICATION_JSON);
							return (GenericEntity) jsonMarshaller.unmarshall(new StringReader(a), options);
						}).orElseGet(() -> createDefaultRequest(serviceRequestType, mapping));

				UrlEncodingMarshaller urlMarshaller = new UrlEncodingMarshaller(rootEntityFactory);
				service = urlMarshaller.create(parameters, serviceRequestType, options);
			} else {

				Marshaller inMarshaller = getInMarshallerFor(endpoint);

				boolean transportPayload = false;
				if (mapping != null) {
					Boolean preserveTransportPayload = mapping.getDefaultPreserveTransportPayload();
					if (preserveTransportPayload != null) {
						transportPayload = preserveTransportPayload.booleanValue();
					}
				}

				StreamPipe pipe = streamPipeFactory.newPipe("ddra-endpoint-request-payload");
				try (OutputStream pipeOut = pipe.acquireOutputStream(); InputStream in = new TeeInputStream(requestIn, pipeOut)) {

					if (transportPayload) {
						context.setRequestTransportPayload(pipe::openInputStream);
					}

					try {
						// Unmarshall the request from the body
						service = (ServiceRequest) inMarshaller.unmarshall(in, options);

					} catch (Exception e) {
						throw exceptionWithPipeContent(pipeOut, in, pipe, inMarshaller, e);
					}
				}
			}
		}

		if (service == null) {
			// If no body is provided at all the unmarshaller returns null. We supply a default in that case.
			service = createDefaultRequest(serviceRequestType, mapping);
		}
		decodeQueryAndFillContext(service, context);

		restServletUtils.ensureServiceDomain(service, context);
		// get the transform request (from the mapping) if any
		service = restServletUtils.computeTransformRequest(context, service);

		GenericModelType evaluatesTo = service.entityType().getEffectiveEvaluatesTo();
		if (evaluatesTo == null) {
			context.setExpectedResponseType(BaseType.INSTANCE);
		} else {
			context.setExpectedResponseType(evaluatesTo);
		}

		processRequestAndWriteResponse(context, service);
	}

	private RuntimeException exceptionWithPipeContent(OutputStream pipeOut, InputStream in, StreamPipe pipe, Marshaller inMarshaller, Throwable e) {
		// We have to close the streams here. Otherwise, we cannot read the content of the pipe
		IOTools.closeQuietly(pipeOut);
		IOTools.closeQuietly(in);
		String content = null;
		if (inMarshaller instanceof CharacterMarshaller && pipe != null) {
			try (InputStream pipeIn = pipe.openInputStream()) {
				content = com.braintribe.utils.IOTools.slurp(pipeIn, "UTF-8");
			} catch (Exception e2) {
				logger.debug(() -> "Could not read content from stream pipe.", e2);
				content = "<n/a>";
			}
		} else {
			content = "<binary>";
		}

		return Exceptions.unchecked(e, "Could not parse the request from the body: " + content);
	}

	private ServiceRequest createDefaultRequest(EntityType<? extends ServiceRequest> serviceRequestType, SingleDdraMapping mapping) {
		if (mapping == null || mapping.getRequestPrototyping() == null) {
			if (serviceRequestType == null) {
				throw new IllegalArgumentException("No service request declared in REST request. "
						+ "Can't supply a default request entity because there's no information about its supposed type.");
			}

			return serviceRequestType.create();
		}

		PrototypingRequest requestPrototyping = mapping.getRequestPrototyping();

		if (serviceRequestType != null && requestPrototyping.getPrototypeEntityType() == null) {
			GmEntityType serviceRequestGmType = GmEntityType.T.create();
			serviceRequestGmType.setTypeSignature(serviceRequestType.getTypeSignature());
			requestPrototyping.setPrototypeEntityType(serviceRequestGmType);
		}

		PersistenceGmSession cortexSession = userSessionFactory.newSession(cortexAccessId);
		return (ServiceRequest) cortexSession.eval(requestPrototyping).get();
	}

	private ServiceRequest parseMultipartRequest(String boundary, ApiV1EndpointContext context) {
		final ServiceRequest service;
		RpcUnmarshallingStreamManagement streamManagement = context.getRequestStreamManagement();
		EntityType<? extends ServiceRequest> serviceRequestType = context.getServiceRequestType();

		try (SequentialFormDataReader formDataReader = Multiparts.formDataReader(context.getRequest().getInputStream(), boundary).autoCloseInput()
				.sequential()) {
			PartReader part = formDataReader.next();

			if (part != null && requestAssemblyPartNames.contains(part.getName())) {
				StreamPipe pipe = streamPipeFactory.newPipe("Part-" + part.getName());
				try (OutputStream pipeOut = pipe.acquireOutputStream(); InputStream in = new TeeInputStream(part.openStream(), pipeOut)) {
					// get input marshaller
					Marshaller marshaller = getInMarshallerFor(part.getContentType());
					// Unmarshall the request from the body
					GmDeserializationOptions options = GmDeserializationOptions.defaultOptions.derive() //
							.setInferredRootType(serviceRequestType) //
							.set(EntityVisitorOption.class, streamManagement.getMarshallingVisitor()) //
							.build();

					try {
						service = (ServiceRequest) marshaller.unmarshall(in, options);
					} catch (Exception e) {
						throw exceptionWithPipeContent(pipeOut, in, pipe, marshaller, e);
					}
				}

				part = formDataReader.next();
			} else {
				service = createDefaultRequest(serviceRequestType, context.getMapping());
			}

			restServletUtils.ensureServiceDomain(service, context);

			ModelMdResolver mdResolver = modelAccessoryFactory.getForServiceDomain(context.getServiceDomain()) //
					.getMetaData() //
					.useCases("ddra");

			HttpRequestEntityDecoderOptions options = HttpRequestEntityDecoderOptions.defaults();
			QueryParamDecoder decoder = new QueryParamDecoder(options);
			decoder.registerTarget("service", service);

			Map<String, List<Resource>> resourceLists = new HashMap<>();
			Map<String, Set<Resource>> resourceSets = new HashMap<>();
			Map<String, DecodingTargetTraversalResult> resources = new HashMap<>();

			List<DecodingTargetTraversalResult> traversalResults = restServletUtils.traverseDecodingTarget(service, decoder, mdResolver);
			traversalResults.stream() //
					.forEach(r -> {
						GenericModelType propertyType = r.getProperty().getType();
						if (propertyType == Resource.T) {
							resources.put(r.prefixedPropertyName(), r);
						} else if (propertyType.isCollection() && ((CollectionType) propertyType).getCollectionElementType() == Resource.T) {
							Object ownValue = r.getOwnValue();
							if (ownValue instanceof List) {
								resourceLists.put(r.prefixedPropertyName(), (List<Resource>) ownValue);
							} else if (ownValue instanceof Set) {
								resourceSets.put(r.prefixedPropertyName(), (Set<Resource>) ownValue);
							}
						}
					});

			while (part != null) {
				String partName = part.getName();

				if (requestAssemblyPartNames.contains(partName)) {
					throw new IllegalStateException("Duplicate request assembly part: " + part);
				}

				TransientSource transientSourceWithId = streamManagement.getTransientSourceWithId(partName);
				if (transientSourceWithId != null) {
					restServletUtils.processResourcePart(streamManagement, part, transientSourceWithId);
				} else if (resourceLists.containsKey(partName)) {
					Resource resource = Resource.createTransient(null);
					resourceLists.get(partName).add(resource);
					restServletUtils.processResourcePart(streamManagement, part, (TransientSource) resource.getResourceSource());
				} else if (resourceSets.containsKey(partName)) {
					Resource resource = Resource.createTransient(null);
					resourceSets.get(partName).add(resource);
					restServletUtils.processResourcePart(streamManagement, part, (TransientSource) resource.getResourceSource());
				} else if (resources.containsKey(partName)) {
					DecodingTargetTraversalResult decodingTargetTraversalResult = resources.get(partName);
					Resource resource = (Resource) decodingTargetTraversalResult.ensureOwnEntity();
					ResourceSource resourceSource = resource.getResourceSource();
					if (resourceSource == null) {
						resource.assignTransientSource(null);
						resourceSource = resource.getResourceSource();
					} else if (!(resourceSource instanceof TransientSource)) {
						throw new IllegalArgumentException("Error while handling part '" + partName
								+ "'. Can't assign binary data to a resource that has already a non-transient ResourceSource." + resource);
					}

					restServletUtils.processResourcePart(streamManagement, part, (TransientSource) resourceSource);
				} else {
					decoder.decode(partName, part.getContentAsString());
				}

				part = formDataReader.next();
			}

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while reading multiparts");
		}

		streamManagement.checkPipeSatisfaction();
		return service;
	}

	private void processRequestAndWriteResponse(ApiV1EndpointContext context, ServiceRequest service) throws IOException {

		ApiV1DdraEndpoint endpoint = context.getEndpoint();

		if (!context.isMultipartResponse()) {

			getStandardResponse(context, endpoint, service);
		} else {
			getMultipartResponse(context, endpoint, service);
		}
	}

	private void getMultipartResponse(ApiV1EndpointContext context, ApiV1DdraEndpoint endpoint, ServiceRequest service) throws IOException {
		RpcUnmarshallingStreamManagement requestStreamManagement = context.getRequestStreamManagement();
		String boundary = Multiparts.generateBoundary();
		context.setMultipartResponseContentType(boundary);
		HttpServletResponse httpResponse = context.getResponse();
		FormDataWriter blobFormDataWriter = Multiparts.blobFormDataWriter(httpResponse.getOutputStream(), boundary);

		try (FormDataWriter formDataWriter = new SequentialParallelFormDataWriter(blobFormDataWriter, streamPipeFactory)) {
			context.setResponseOutputStreamProvider(() -> {
				MutablePartHeader header = Multiparts.newPartHeader();
				header.setName(RpcConstants.RPC_MAPKEY_RESPONSE);
				header.setContentType(context.getMimeType());
				PartWriter part = formDataWriter.openPart(header);

				return part.outputStream();
			});
			if (requestStreamManagement != null) {
				for (CallStreamCapture capture : requestStreamManagement.getCallStreamCaptures()) {
					capture.setOutputStreamProvider(() -> {
						MutablePartHeader header = Multiparts.newPartHeader();
						header.setName(capture.getGlobalId());
						header.setContentType("application/octet-stream");
						PartWriter part = formDataWriter.openPart(header);

						return part.outputStream();
					});
				}
			}
			Maybe<?> maybe = evaluateServiceRequest(service, context);

			if (maybe.isSatisfied()) {
				Object response = maybe.get();
				Object projectedResponse = restServletUtils.project(context, endpoint, response);
				writeResponse(context, projectedResponse, endpoint, false);
			} else {
				writeUnsatisfied(context, endpoint, maybe);
			}

			// writing transient resources
			restServletUtils.writeOutTransientSources(context, formDataWriter);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not prepare writing multipart response.");
		}
	}

	private void getStandardResponse(ApiV1EndpointContext context, ApiV1DdraEndpoint endpoint, ServiceRequest service) throws IOException {
		RpcUnmarshallingStreamManagement requestStreamManagement = context.getRequestStreamManagement();

		if (requestStreamManagement != null) {
			requestStreamManagement.disarmCallStreamCaptures();
		}

		String responseContentType = context.getEndpoint().getResponseContentType();
		if (responseContentType != null) {
			context.setMimeType(responseContentType);
		}

		Maybe<?> maybe = evaluateServiceRequest(service, context);

		if (maybe.isSatisfied()) {
			Object response = maybe.get();
			Object projectedResponse = restServletUtils.project(context, endpoint, response);

			if (context.isResourceDownloadResponse()) {
				handleResourceDownloadResponse(context, service, projectedResponse, responseContentType);
			} else {
				context.ensureContentDispositionHeader(null);
				writeResponse(context, projectedResponse, endpoint, false);
			}
		} else {
			writeUnsatisfied(context, endpoint, maybe);
		}
	}

	private void writeUnsatisfied(ApiV1EndpointContext context, ApiV1DdraEndpoint endpoint, Maybe<?> maybe) throws IOException {
		Reason reason = maybe.whyUnsatisfied();

		String domainId = context.getServiceDomain();

		ModelAccessory modelAccessory = modelAccessoryFactory.getForServiceDomain(domainId);

		EntityMdResolver reasonMdResolver = modelAccessory.getCmdResolver().getMetaData().lenient(true).entity(reason).useCase("ddra");

		// logging
		LogReason logReason = Optional.ofNullable(reasonMdResolver.meta(LogReason.T).exclusive()).orElse(defaultLogging);

		LogLevel logLevel = logReason.getLevel();

		if (logLevel != null) {
			String msg = logReason.getRecursive() ? reason.stringify() : reason.asString();
			if (reason instanceof InternalError) {
				InternalError ie = (InternalError) reason;
				logger.log(translateLogLevel(logLevel), msg, ie.getJavaException());
			} else {
				logger.log(translateLogLevel(logLevel), msg);
			}
		}

		// status code determination
		if (!context.getResponse().isCommitted()) {
			Optional<HttpStatusCode> statusOptional = Optional.ofNullable(reasonMdResolver.meta(HttpStatusCode.T).exclusive());
			int httpStatusCode = statusOptional.map(HttpStatusCode::getCode).orElse(500);

			context.getResponse().setStatus(httpStatusCode);
		}

		// marshaling reason
		writeResponse(context, Unsatisfied.from(maybe), endpoint, true);
	}

	private com.braintribe.logging.Logger.LogLevel translateLogLevel(LogLevel logLevel) {
		try {
			return com.braintribe.logging.Logger.LogLevel.valueOf(logLevel.name());
		} catch (Exception e) {
			logger.warn("Unsupported log level " + logLevel + ". Falling back to level ERROR.");
			return com.braintribe.logging.Logger.LogLevel.ERROR;
		}
	}

	protected Maybe<?> evaluateServiceRequest(ServiceRequest service, ApiV1EndpointContext context) {
		DdraEndpoint endpoint = context.getEndpoint();
		DdraEndpointDepth computedDepth = endpoint.getComputedDepth();
		TraversingCriterion criterion = (computedDepth != null) ? this.traversingCriteriaMap.getCriterion(computedDepth) : null;

		HttpRequestSupplier httpRequestSupplier = new HttpRequestSupplierImpl(service, context.getRequest());
		HttpResponseConfigurerImpl httpResponseConfigurer = new HttpResponseConfigurerImpl();

		//@formatter:off
		EvalContext<Object> evalContext = getEvaluator(service, context)
			.eval(service)
			.with(DdraEndpointAspect.class, endpoint)
			.with(TraversingCriterionAspect.class, criterion)
			.with(HttpStatusCodeNotification.class, context::setForceResponseCode)
			.with(HttpRequestSupplierAspect.class, httpRequestSupplier)
			.with(HttpResponseConfigurerAspect.class, httpResponseConfigurer);
		//@formatter:on

		if (context.getRequestTransportPayload() != null) {
			evalContext.setAttribute(RequestTransportPayloadAspect.class, context.getRequestTransportPayload());
		}
		Maybe<?> maybe = evalContext.getReasoned();

		if (maybe.isSatisfied()) {
			Object result = maybe.get();
			httpResponseConfigurer.consume(result, context.getResponse());
		}

		return maybe;
	}

	private Evaluator<ServiceRequest> getEvaluator(ServiceRequest service, ApiV1EndpointContext context) {

		if (userSessionFactory != null && context.useSessionEvaluation() && service instanceof AccessRequest) {
			String domainId = service.domainId();
			if (!accessExists(domainId)) {
				HttpExceptions.badRequest("Provided domainId %s for evaluating serviceRequest: %s is not an access.", domainId,
						service.entityType().getTypeSignature());
			}
			return this.userSessionFactory.newSession(domainId);
		}
		return this.evaluator;
	}

	private void handleResourceDownloadResponse(ApiV1EndpointContext context, ServiceRequest service, Object projectedResponse,
			String responseContentType) throws IOException {

		if (projectedResponse instanceof Resource) {
			Resource resource = (Resource) projectedResponse;
			context.ensureContentDispositionHeader(resource.getName());

			if (resource.getFileSize() != null)
				context.getResponse().setContentLength(resource.getFileSize().intValue());

			if (responseContentType == null) {
				String mimeType = resource.getMimeType();
				context.setMimeType(mimeType);
			}

			try (OutputStream responseOut = context.openResponseOutputStream()) {
				if (resource.isTransient()) {
					resource.writeToStream(responseOut);
				} else {
					// TODO: Abstract resource streaming to the service domain level
					PersistenceGmSession domainSession = userSessionFactory.newSession(service.domainId());
					domainSession.resources().writeToStream(resource, responseOut);
				}
			}
		} else {
			try (OutputStream responseOut = context.openResponseOutputStream()) {
				// Just open and close the stream, assuming that this means sending an empty response
			}
		}
	}

	private SingleDdraMapping computeDdraMapping(ApiV1EndpointContext context) {
		DdraUrlMethod method = DdraUrlMethod.valueOf(context.getRequest().getMethod().toUpperCase());
		String path = getPathInfo(context);
		SingleDdraMapping mapping = mappings.get(path, method);

		context.setMapping(mapping);
		return mapping;
	}

	private EntityType<? extends ServiceRequest> decodePathAndFillContext(ApiV1EndpointContext context) {

		DdraBaseUrlPathParameters pathParameters = DdraBaseUrlPathParameters.T.create();
		// No mapping found. Identify type and domain from Path
		URL_CODEC.decode(() -> pathParameters, getPathInfo(context));

		String serviceDomain = pathParameters.getServiceDomain();
		if (serviceDomain == null) {

			String typeSignature = pathParameters.getTypeSignature();
			if (!StringTools.isEmpty(typeSignature)) {
				serviceDomain = typeSignature;
				pathParameters.setServiceDomain(serviceDomain);
				pathParameters.setTypeSignature(null);
			} else {
				serviceDomain = defaultServiceDomain;
				pathParameters.setServiceDomain(serviceDomain);
				pathParameters.setIsDomainExplicit(false);
			}
		}

		context.setServiceDomain(serviceDomain);
		checkServiceDomain(context);

		// get the type signature from the path
		String typeSignature = pathParameters.getTypeSignature();
		if (StringUtils.isBlank(typeSignature)) {
			return null;
		}

		// get the entity type from the type signature
		EntityType<? extends ServiceRequest> entityType = restServletUtils.resolveTypeFromSignature(serviceDomain, typeSignature,
				modelAccessoryFactory);
		if (entityType == null) {
			HttpExceptions.notFound("Cannot find service request with type signature %s", typeSignature);
		}
		if (!ServiceRequest.T.isAssignableFrom(entityType)) {
			HttpExceptions.badRequest("Generic Entity %s is not a ServiceRequest.", typeSignature);
		}

		context.setServiceRequestType(entityType);
		return entityType;
	}

	private void decodeQueryAndFillContext(ServiceRequest service, ApiV1EndpointContext context) {
		HttpRequestEntityDecoderOptions options = HttpRequestEntityDecoderOptions.defaults();
		requestAssemblyPartNames.forEach(options::addIgnoredParameter);

		HttpRequestEntityDecoder decoder = HttpRequestEntityDecoder.createFor(context.getRequest(), options);
		if (service != null) {
			decoder.target("service", service); // TODO: remove or rename if possible
			// decoder.target("", service);

			String serviceDomain = context.getServiceDomain();

			ModelMdResolver metaDataResolver = modelAccessoryFactory //
					.getForServiceDomain(serviceDomain) //
					.getMetaData() //
					.useCase(WebApiUseCases.USECASE_DDRA);

			if (context.getMapping() != null && context.getMapping().getPath() != null) {
				String mappingSpecificUsecase = WebApiUseCases.mappingSpecificUseCase(context.getMapping().getPath());
				metaDataResolver.useCase(mappingSpecificUsecase);
			}

			restServletUtils.traverseDecodingTarget(service, decoder, metaDataResolver);
			restServletUtils.ensureServiceDomain(service, context);
		} else {
			options.setIgnoringUnmappedHeaders(true);
			options.setIgnoringUnmappedUrlParameters(true);
		}

		DdraEndpoint endpoint = context.getEndpoint();

		decoder.target("endpoint", endpoint, ENDPOINT_MAPPER).decode();

		DdraEndpointsUtils.computeDepth(endpoint);

	}

	private void checkServiceDomain(ApiV1EndpointContext context) {
		String serviceDomain = context.getServiceDomain();
		if (!accessAvailability.test(serviceDomain))
			HttpExceptions.notFound(
					"No ServiceDomain or DdraMapping found for name: " + serviceDomain + " and HTTP method: " + context.getRequest().getMethod());
	}

	public void ensureDdraMappingInitialized() throws StateChangeProcessorException {
		PersistenceGmSession session = systemSessionFactory.newSession("cortex");
		DdraConfiguration configuration = session.query().entity(DdraConfiguration.T, "ddra:config").withTraversingCriterion(PreparedTcs.scalarOnlyTc)
				.refresh();

		String queriedTimestamp = configuration.getLastChangeTimestamp();

		if (mappingsAreUpToDate(queriedTimestamp))
			return;

		synchronized (this) {
			if (!mappingsAreUpToDate(queriedTimestamp)) {
				configuration = session.query().entity(DdraConfiguration.T, "ddra:config")
						.withTraversingCriterion(TC.create().negation().joker().done()).refresh();

				mappings.setMappings(configuration.getMappings());
				mappings.setDdraMappingsInitialized(true);

				mappingsTimestamp = queriedTimestamp;
			}
		}
	}

	private boolean mappingsAreUpToDate(String queriedTimestamp) {
		return mappings.isDdraMappingsInitialized() && bothNullOrEqual(queriedTimestamp, mappingsTimestamp);
	}

	private static boolean bothNullOrEqual(String a, String b) {
		if (a == null) {
			return b == null;
		}

		return a.equals(b);
	}

	@Required
	@Configurable
	public void setMappings(DdraMappings mappings) {
		this.mappings = mappings;
	}

	@Required
	@Configurable
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	@Configurable
	public void setAccessAvailability(Predicate<String> accessAvailability) {
		this.accessAvailability = accessAvailability;
	}

	@Required
	@Configurable
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

	@Required
	@Configurable
	public void setSessionIdProvider(Supplier<String> sessionIdProvider) {
		this.sessionIdProvider = sessionIdProvider;
	}

	@Configurable
	public void setDefaultServiceDomain(String defaultServiceDomain) {
		this.defaultServiceDomain = defaultServiceDomain;
	}

	@Configurable
	public void setPollMappings(boolean pollMappings) {
		this.pollMappings = pollMappings;
	}

	@Configurable
	@Required
	public void setRestServletUtils(ApiV1RestServletUtils restServletUtils) {
		this.restServletUtils = restServletUtils;
	}

}
