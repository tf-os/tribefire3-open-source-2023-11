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
package com.braintribe.gm.service.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.OutputStreamProvider;
import com.braintribe.model.processing.accessrequest.api.AbstractDispatchingAccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.BlobSource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resourceapi.base.ResourceRequest;
import com.braintribe.model.resourceapi.base.ResourceSourceRequest;
import com.braintribe.model.resourceapi.persistence.DeleteResource;
import com.braintribe.model.resourceapi.persistence.DeleteResourceResponse;
import com.braintribe.model.resourceapi.persistence.DeleteSource;
import com.braintribe.model.resourceapi.persistence.DeleteSourceResponse;
import com.braintribe.model.resourceapi.persistence.DeletionScope;
import com.braintribe.model.resourceapi.persistence.ManageResource;
import com.braintribe.model.resourceapi.persistence.ManageResourceResponse;
import com.braintribe.model.resourceapi.persistence.UpdateResource;
import com.braintribe.model.resourceapi.persistence.UploadResource;
import com.braintribe.model.resourceapi.persistence.UploadResourceResponse;
import com.braintribe.model.resourceapi.persistence.UploadResources;
import com.braintribe.model.resourceapi.persistence.UploadResourcesResponse;
import com.braintribe.model.resourceapi.persistence.UploadSource;
import com.braintribe.model.resourceapi.persistence.UploadSourceResponse;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;
import com.braintribe.model.resourceapi.stream.DownloadResource;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.GetResource;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.StreamResource;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.stream.CountingInputStream;
import com.braintribe.utils.stream.RangeInputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.stream.api.StreamPipes;

// Inspired by com.braintribe.cartridge.master.processing.streaming.ResourceManipulationProcessor
public class SimpleResourceProcessor extends AbstractDispatchingAccessRequestProcessor<ResourceRequest, ManageResourceResponse> {

	private Map<String, Map<String, StreamPipe>> resourcesPerAccess = new ConcurrentHashMap<>();

	private StreamPipeFactory pipeFactory = StreamPipes.fileBackedFactory();

	@Override
	protected void configureDispatching(DispatchConfiguration dispatching) {
		dispatching.register(UploadResource.T, this::upload);
		dispatching.register(UpdateResource.T, this::update);
		dispatching.register(GetResource.T, this::getResource);
		dispatching.register(StreamResource.T, this::streamResource);
		dispatching.register(DeleteResource.T, this::delete);
		dispatching.register(UploadResources.T, this::bulkUpload);

		//@formatter:off
		/* Note: this is not yet supported
		 * 
		dispatching.register(GetSource.T, this::getSource);
		dispatching.register(StreamSource.T, this::streamSource);
		dispatching.register(ManipulateSource.T, this::manipulateSource);
		dispatching.register(DeleteSource.T, this::deleteSource);
		dispatching.register(UploadSource.T, this::uploadSource);
		 */
		//@formatter:on
	}

	protected UploadResourceResponse update(AccessRequestContext<UpdateResource> context) {

		PersistenceGmSession session = context.getSession();
		UpdateResource originalRequest = context.getOriginalRequest();
		Resource resource = originalRequest.getResource();

		Resource uploadedResource = uploadResource(resource, context, session, false);
		Resource sessionResource = getSessionResource(context);

		ResourceSource oldResourceSource = sessionResource.getResourceSource();
		sessionResource.setFileSize(uploadedResource.getFileSize());
		sessionResource.setMd5(uploadedResource.getMd5());
		sessionResource.setMimeType(uploadedResource.getMimeType());
		sessionResource.setResourceSource(uploadedResource.getResourceSource());

		if (originalRequest.getDeleteOldResourceSource() && oldResourceSource != null) {
			session.deleteEntity(oldResourceSource);
			resourcesPerAccess.getOrDefault(context.getDomainId(), Collections.emptyMap()).get(oldResourceSource.getId());
		}

		UploadResourceResponse response = UploadResourceResponse.T.create();
		response.setResource(sessionResource);

		return response;

	}

	protected GetBinaryResponse getResource(AccessRequestContext<GetResource> context) {

		StreamPipe pipe = pipeFactory.newPipe("get-resource");
		Resource resource = Resource.createTransient(pipe::openInputStream);

		GetBinaryResponse result = streamResource(context, GetBinaryResponse.T, pipe::openOutputStream);
		result.setResource(resource);
		return result;

	}

	protected StreamBinaryResponse streamResource(AccessRequestContext<StreamResource> context) {

		StreamBinaryResponse result = streamResource(context, StreamBinaryResponse.T, context.getOriginalRequest().getCapture()::openStream);

		return result;
	}

	protected <T extends BinaryRetrievalResponse> T streamResource(AccessRequestContext<? extends DownloadResource> context, EntityType<T> resultType,
			OutputStreamProvider outProvider) {
		Resource sessionResource = getSessionResourceFromDownload(context);

		DownloadResource request = context.getOriginalRequest();

		ResourceSource resourceSource = sessionResource.getResourceSource();
		if (!(resourceSource instanceof BlobSource)) {
			throw new IllegalStateException("The resource has not a BlobSource attached: " + resourceSource);
		}
		BlobSource source = (BlobSource) resourceSource;
		String domainId = context.getDomainId();

		StreamPipe pipe = resourcesPerAccess.getOrDefault(domainId, Collections.emptyMap()).get(source.getId());
		if (pipe == null) {
			throw new IllegalStateException("The resource source is not backed by a pipe: " + resourceSource);
		}

		T result = resultType.create();

		try (OutputStream out = outProvider.openOutputStream(); InputStream in = pipe.openInputStream()) {

			StreamRange range = request.getRange();
			final InputStream wIn;

			if (range != null) {
				Long start = range.getStart();
				if (start == null) {
					start = 0L;
				}
				Long end = range.getEnd();
				if (end == null) {
					end = Long.MAX_VALUE;
				}

				// TODO: this is not very nice. The model lacks any documentation on the expected values
				// This should be checked with other implementations

				result.setRanged(true);
				result.setRangeStart(start);
				result.setRangeEnd(end);
				result.setSize(end - start + 1);

				wIn = new RangeInputStream(in, start, end - 1);

			} else {

				result.setRanged(false);
				result.setSize(sessionResource.getFileSize());

				wIn = in;
			}

			IOTools.transferBytes(wIn, out);

		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}

		return result;
	}

	@SuppressWarnings("fallthrough")
	protected DeleteResourceResponse delete(AccessRequestContext<DeleteResource> context) {
		DeleteResource request = context.getOriginalRequest();
		PersistenceGmSession session = context.getSession();

		DeletionScope deletionScope = request.getDeletionScope();
		Resource sessionResource = getSessionResource(context);

		ResourceSource resourceSource = sessionResource.getResourceSource();
		if (!(resourceSource instanceof BlobSource)) {
			throw new IllegalStateException("The resource has not a BlobSource attached: " + resourceSource);
		}
		BlobSource source = (BlobSource) resourceSource;
		String domainId = context.getDomainId();

		StreamPipe pipe = resourcesPerAccess.getOrDefault(domainId, Collections.emptyMap()).remove(source.getId());
		if (pipe == null) {
			throw new IllegalStateException("The resource source is not backed by a pipe: " + resourceSource);
		}

		switch (deletionScope) {
			case resource: {
				session.deleteEntity(sessionResource);
			}
			case source: {
				session.deleteEntity(source);
				break;
			}
			default:
				throw new IllegalStateException("Unsupported/unknown deletion scope: " + deletionScope);
		}

		return DeleteResourceResponse.T.create();
	}

	protected UploadResourceResponse upload(AccessRequestContext<UploadResource> context) {

		PersistenceGmSession session = context.getSession();
		UploadResource originalRequest = context.getOriginalRequest();
		Resource resource = originalRequest.getResource();

		Resource returnResource = uploadResource(resource, context, session, true);

		UploadResourceResponse response = UploadResourceResponse.T.create();
		response.setResource(returnResource);

		return response;
	}

	protected Resource uploadResource(Resource sourceResource, AccessRequestContext<? extends ResourceRequest> context, PersistenceGmSession session,
			boolean persistResource) {

		String domainId = context.getDomainId();

		StreamPipe pipe = pipeFactory.newPipe(sourceResource.getName());
		long filesize;
		String md5;

		try (InputStream in = sourceResource.openStream(); OutputStream os = pipe.acquireOutputStream()) {
			CountingInputStream cin = new CountingInputStream(in, false);
			DigestInputStream din = new DigestInputStream(cin, MessageDigest.getInstance("MD5"));

			IOTools.transferBytes(din, os);
			filesize = cin.getCount();
			md5 = StringTools.toHex(din.getMessageDigest().digest());

		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		} catch (NoSuchAlgorithmException n) {
			throw Exceptions.unchecked(n);
		}

		BlobSource source = session.create(BlobSource.T);
		source.setId(RandomTools.newStandardUuid());

		Map<String, StreamPipe> map = resourcesPerAccess.computeIfAbsent(domainId, i -> new ConcurrentHashMap<>());
		map.put(source.getId(), pipe);

		Resource returnResource = persistResource ? session.create(Resource.T) : Resource.T.create();
		returnResource.setId(source.getId());
		returnResource.setName(sourceResource.getName());
		returnResource.setMd5(md5);
		returnResource.setFileSize(filesize);
		returnResource.setCreated(new Date());
		returnResource.setCreator(context.getRequestorUserName());
		returnResource.getTags().addAll(sourceResource.getTags());

		String mimeType = sourceResource.getMimeType();
		if (mimeType != null) {
			mimeType = guessMimeType(sourceResource.getName());
		}
		returnResource.setMimeType(mimeType);
		returnResource.setResourceSource(source);

		return returnResource;
	}
	private String guessMimeType(String filename) {
		if (filename == null) {
			return null;
		}
		return URLConnection.guessContentTypeFromName(filename);
	}

	private DeleteSourceResponse deleteSource(AccessRequestContext<DeleteSource> context) {
		DeleteSource originalRequest = context.getOriginalRequest();

		DeletionScope deletionScope = originalRequest.getDeleteSourceEntity() //
				? DeletionScope.source //
				: DeletionScope.binary;

		DeleteResource deleteBinary = delegateRequest(originalRequest, DeleteResource.T);
		deleteBinary.setDeletionScope(deletionScope);

		deleteBinary.eval(context.getSession()).get();

		return DeleteSourceResponse.T.create();
	}

	private UploadSourceResponse uploadSource(AccessRequestContext<UploadSource> context) {
		UploadSource originalRequest = context.getOriginalRequest();

		UploadResource uploadResource = delegateRequest(originalRequest, UploadResource.T);
		uploadResource.setSourceType(originalRequest.getSourceType());
		uploadResource.setUseCase(originalRequest.getUseCase());

		UploadResourceResponse uploadResourceResponse = uploadResource.eval(context.getSession()).get();

		UploadSourceResponse response = UploadSourceResponse.T.create();
		response.setResource(uploadResourceResponse.getResource());

		return response;
	}

	private <T extends ManageResource> T delegateRequest(ResourceSourceRequest originalRequest, EntityType<T> delegateRequestType) {
		Resource resource = Resource.T.create();
		resource.setResourceSource(originalRequest.getResourceSource());

		T delegateRequest = delegateRequestType.create();
		delegateRequest.setResource(resource);

		return delegateRequest;
	}

	protected UploadResourcesResponse bulkUpload(AccessRequestContext<UploadResources> context) {
		UploadResources originalRequest = context.getOriginalRequest();
		Boolean detectMimeType = originalRequest.getDetectMimeType();

		UploadResourcesResponse response = UploadResourcesResponse.T.create();

		for (Resource resource : originalRequest.getResources()) {
			UploadResource uploadResource = UploadResource.T.create();
			uploadResource.setDomainId(context.getDomainId());
			uploadResource.setResource(resource);
			uploadResource.setSourceType(originalRequest.getSourceType());
			uploadResource.setUseCase(originalRequest.getUseCase());

			UploadResourceResponse uploadResourceResponse = evalUploadResource(context.getSession(), resource, uploadResource, detectMimeType);

			response.getResources().add(uploadResourceResponse.getResource());
		}

		return response;
	}

	private UploadResourceResponse evalUploadResource(PersistenceGmSession session, Resource resource, UploadResource uploadResource,
			Boolean detectMimeType) {
		String requestResourceMimeType = resource.getMimeType();

		if (detectMimeType == Boolean.TRUE) {
			resource.setMimeType(requestResourceMimeType);
		} else if (detectMimeType == Boolean.FALSE && resource.getMimeType() == null) {
			resource.setMimeType("application/octet-stream");
		}

		UploadResourceResponse uploadResourceResponse = uploadResource.eval(session).get();

		Resource responseResource = uploadResourceResponse.getResource();

		if (detectMimeType == Boolean.TRUE && responseResource != null && responseResource.getMimeType() == null) {
			responseResource.setMimeType(requestResourceMimeType);
		}

		return uploadResourceResponse;
	}

	private Resource getSessionResource(AccessRequestContext<? extends ManageResource> context) {
		// The resource needs to be queried again because the caller might send invalid or malicious parts with the request, i.e. the ResourceSource
		Resource requestResource = context.getOriginalRequest().getResource();
		return context.getSession().query().entity(requestResource).require();
	}
	private Resource getSessionResourceFromDownload(AccessRequestContext<? extends DownloadResource> context) {
		// The resource needs to be queried again because the caller might send invalid or malicious parts with the request, i.e. the ResourceSource
		Resource requestResource = context.getOriginalRequest().getResource();
		return context.getSession().query().entity(requestResource).require();
	}

}
