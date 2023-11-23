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
package com.braintribe.model.processing.ddra.endpoints.api.v1.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;
import com.braintribe.ddra.endpoints.api.api.v1.ApiV1EndpointContext;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.processing.ddra.endpoints.api.v1.ReopenableInputStreamProviders;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resourceapi.request.ResourceStreamingRequest;
import com.braintribe.model.resourceapi.request.ResourceUploadRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.util.servlet.stream.OnDemandOpeningOutputStream;
import com.braintribe.util.servlet.stream.PartInputStreamSupplier;
import com.braintribe.util.servlet.stream.RequestInputStreamSupplier;
import com.braintribe.util.servlet.util.ServletTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.StopWatch;

public class ResourceUploadHandler extends ResourceHandler {

	private static final Logger log = Logger.getLogger(ResourceUploadHandler.class);

	public static boolean handleRequest(ApiV1EndpointContext context, MarshallerRegistry marshallerRegistry, ServiceRequest service,
			PersistenceGmSessionFactory gmSession) {
		if (!ResourceUploadRequest.T.isInstance(service))
			return false;

		return new ResourceUploadHandler(context, marshallerRegistry, (ResourceUploadRequest) service, gmSession).handle();
	}

	private final ApiV1EndpointContext context;
	private final ResourceUploadRequest resourceUploadRequest;
	private final MarshallerRegistry marshallerRegistry;
	private final StopWatch stopWatch;

	public ResourceUploadHandler(ApiV1EndpointContext context, MarshallerRegistry marshallerRegistry, ResourceUploadRequest resourceUploadRequest,
			PersistenceGmSessionFactory gmSession) {
		this.context = context;
		this.resourceUploadRequest = resourceUploadRequest;
		if (this.resourceUploadRequest.getAccessId() == null)
			this.resourceUploadRequest.setAccessId(context.getServiceDomain());
		this.sessionFactory = gmSession;
		this.marshallerRegistry = marshallerRegistry;
		this.stopWatch = context.getStopWatch();
	}

	private boolean handle() {
		HttpServletRequest request = context.getRequest();
		parseContext(resourceUploadRequest, request);

		writeResourceResponse(uploadResources(openSession(resourceUploadRequest.getAccessId()), request, resourceUploadRequest));

		return true;
	}

	private static void parseContext(ResourceStreamingRequest streamingRequest, HttpServletRequest servletRequest) {
		String contextString = servletRequest.getParameter("context");
		if (contextString != null) {
			try {
				Map<String,String> contextMap = StringTools.decodeStringMapFromString(contextString);
				streamingRequest.setContext(contextMap);
			} catch(Exception e) {
				throw new RuntimeException("Could not parse context "+contextString, e);
			}
		}
	}

	protected List<Resource> uploadResources(PersistenceGmSession gmSession, HttpServletRequest request, ResourceUploadRequest uploadRequest) {

		stopWatch.intermediate("Upload Processing Start");
		
		final List<Resource> resources = new ArrayList<>();
		final ResourceAccess resourceAccess = gmSession.resources();

		if (resourceAccess == null)
			throw new RuntimeException("No resource access was provided by the gm session");

		if (ServletTools.isMultipart(request)) {
			Collection<Part> parts = getParts(request);
			stopWatch.intermediate("Get Parts");
			for (Part part : parts) {
				try {
					if (ServletTools.isContentPart(part)) {
						try (ReopenableInputStreamProviders.ReopenableInputStreamProvider in = ReopenableInputStreamProviders
								.create(new PartInputStreamSupplier(part)::get)) {
							String filename = ServletTools.getFileName(part);
							uploadResource(resourceAccess, in, filename, getMimeType(part), null, uploadRequest, resources);
							stopWatch.intermediate("Upload "+filename);
							
						} catch (IOException thrownWhenCloseFails) {
							log.warn("Could not close input stream provider from part " + part, thrownWhenCloseFails);
						} catch (Exception e) {
							throw new RuntimeException("Error while trying to upload part: " + part, e);
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
			try (ReopenableInputStreamProviders.ReopenableInputStreamProvider in = ReopenableInputStreamProviders
					.create(new RequestInputStreamSupplier(request)::get)) {
				String filename = getFileName(uploadRequest);
				uploadResource(resourceAccess, in, filename, uploadRequest.getMimeType(), uploadRequest.getMd5(), uploadRequest,
						resources);
				stopWatch.intermediate("Upload "+filename);
				
			} catch (IOException thrownWhenCloseFails) {
				log.warn("Could not close input stream provider", thrownWhenCloseFails);
			}
		}

		return resources;
	}

	private void uploadResource(ResourceAccess resourceAccess, InputStreamProvider inputStreamSupplier, final String fileName, final String mimeType,
			final String md5, ResourceUploadRequest uploadRequest, final List<Resource> resources) {

		EntityType<? extends ResourceSource> sourceType = null;
		String sourceTypeSignature = uploadRequest.getSourceType();
		if (sourceTypeSignature != null)
			sourceType = GMF.getTypeReflection().getEntityType(sourceTypeSignature);

		// @formatter:off
		Resource resource =
				resourceAccess
					.create()
						.name(fileName)
						.useCase(uploadRequest.getUseCase())
						.mimeType(mimeType)
						.md5(md5)
						.sourceType(sourceType)
						.store(inputStreamSupplier);
		// @formatter:on

		resources.add(resource);

	}

	private String getMimeType(Part part) {
		String mimeTypeFromContentType = HttpTools.getMimeTypeFromContentType(part.getHeader("Content-Type"), true);
		if (mimeTypeFromContentType != null && mimeTypeFromContentType.equalsIgnoreCase("application/octet-stream")) {
			//We can/should ignore this information and find out the real mime type later
			mimeTypeFromContentType = null;
		}
		return mimeTypeFromContentType;
	}

	private static String getFileName(ResourceUploadRequest request) {

		if (request.getFileName() == null) {
			if (log.isDebugEnabled())
				log.debug("No [ fileName ] request parameter was provided");

			return null;
		}

		return request.getFileName();
	}

	private static Collection<Part> getParts(HttpServletRequest request) {
		try {
			return request.getParts();
		} catch (Exception e) {
			throw new RuntimeException("Failed to get parts from multipart request: " + e.getMessage(), e);
		}
	}

	private void writeResourceResponse(List<Resource> resources) {
		HttpServletResponse response = context.getResponse();

		MarshallerRegistryEntry marshallerRegistryEntry = marshallerRegistry.getMarshallerRegistryEntry(resourceUploadRequest.getResponseMimeType());
		response.setContentType(marshallerRegistryEntry.getMimeType());
		OutputStream out;
		try {
			out = new OnDemandOpeningOutputStream(response);
			marshallerRegistryEntry.getMarshaller().marshall(out, resources);
			out.flush();
		} catch (Exception e) {
			throw new RuntimeException("Failed to write response: " + e.getMessage(), e);
		}

		return;
	}

}
