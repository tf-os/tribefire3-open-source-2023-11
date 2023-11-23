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
package com.braintribe.gwt.gmresource.session;

import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.fileapi.client.Blob;
import com.braintribe.gwt.fileapi.client.FormData;
import com.braintribe.gwt.fileapi.client.ProgressHandler;
import com.braintribe.gwt.fileapi.client.XMLHttpRequest2;
import com.braintribe.gwt.genericmodel.client.codec.dom4.GmXmlCodec;
import com.braintribe.gwt.genericmodel.client.resource.Resources;
import com.braintribe.gwt.gmresource.client.ResourceProcessingException;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.resource.ResourceUpdateBuilder;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.model.resourceapi.persistence.UpdateResource;
import com.braintribe.model.resourceapi.persistence.UploadResourceResponse;
import com.braintribe.processing.async.api.JsPromise;
import com.google.gwt.xhr.client.XMLHttpRequest;

@SuppressWarnings("unusable-by-js")
public class RestBasedResourceUpdateBuilder implements ResourceUpdateBuilder {

	private final String accessId;
	private final Supplier<String> sessionProvider;
	private final String streamBaseUrl;

	private final Resource resource;

	private String mimeType;
	private String useCase;
	private Set<String> tags;
	private EntityType<? extends ResourceSource> sourceType;
	private ResourceSpecification specification;
	private String name;
	private boolean deleteOldSource = true;

	private ProgressHandler progressHandler;

	public RestBasedResourceUpdateBuilder(String accessId, String streamBaseUrl, Supplier<String> sessionIdProvider, Resource resource) {
		this.accessId = accessId;
		this.streamBaseUrl = streamBaseUrl;
		this.sessionProvider = sessionIdProvider;
		this.resource = resource;
	}

	@Override
	public ResourceUpdateBuilder mimeType(String mimeType) {
		this.mimeType = mimeType;
		return this;
	}

	@Override
	public ResourceUpdateBuilder md5(String md5) {
		return this;
	}

	@Override
	public ResourceUpdateBuilder useCase(String useCase) {
		this.useCase = useCase;
		return this;
	}

	@Override
	public ResourceUpdateBuilder tags(Set<String> tags) {
		this.tags = tags;
		return this;
	}

	@Override
	public ResourceUpdateBuilder sourceType(EntityType<? extends ResourceSource> sourceType) {
		this.sourceType = sourceType;
		return this;
	}

	@Override
	public ResourceUpdateBuilder specification(ResourceSpecification specification) {
		this.specification = specification;
		return this;
	}

	@Override
	public ResourceUpdateBuilder name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public ResourceUpdateBuilder withProgressHandler(ProgressHandler progressHandler) {
		this.progressHandler = progressHandler;
		return this;
	}

	@Override
	public ResourceUpdateBuilder deleteOldResourceSource(boolean deleteOldSource) {
		this.deleteOldSource = deleteOldSource;
		return this;
	}

	@Override
	public JsPromise<Resource> withBlobJs(Blob blob) {
		return withBlob(blob).toJsPromise();
	}

	@Override
	public JsPromise<Resource> withTextJs(String text) {
		return withText(text).toJsPromise();
	}

	@Override
	public Future<Resource> withText(String text) {
		return withBlob(Blob.createFromString(text, "plain/text;charset=UTF-8"));
	}

	@Override
	public Future<Resource> withBlob(Blob blob) {
		return buildFromBlob(blob);
	}

	private final Codec<Object, String> codec = new GmXmlCodec<>();

	private Future<Resource> buildFromBlob(Blob blob) {
		UpdateResource request = buildUpdateRequest(blob);
		String requestAsString = codec.encode(request);
		Blob requestBlob = Blob.createFromString(requestAsString, "application/xml;charset=UTF-8");

		Future<Resource> future = new Future<>();

		XMLHttpRequest2 xhr = XMLHttpRequest2.create();
		try {
			xhr.open("PUT", buildUploadUrl());
		} catch (RuntimeException e1) {
			future.onFailure(new ResourceProcessingException("error while building upload url for update", e1));
			return future;
		}
		xhr.setRequestHeader("Accept", "application/xml");
		xhr.setOnReadyStateChange(xhr1 -> {
			if (xhr1.getReadyState() != XMLHttpRequest.DONE)
				return;

			if (xhr1.getStatus() != 200) {
				future.onFailure(new ResourceProcessingException(
						"error while updating the resource; error code=" + xhr1.getStatus() + ": " + xhr1.getStatusText()));
				return;
			}

			String xml = xhr1.getResponseText();
			try {
				UploadResourceResponse result = (UploadResourceResponse) codec.decode(xml);
				future.onSuccess(result.getResource());

			} catch (CodecException e) {
				future.onFailure(new ResourceProcessingException("error while decoding upload results", e));
			}
		});

		if (progressHandler != null)
			xhr.getUpload().setProgressHandler(progressHandler);

		FormData formData = FormData.create();
		formData.append("serialized-request", requestBlob);
		formData.append(request.getResource().getResourceSource().getGlobalId(), blob);

		xhr.send(formData);

		return future;
	}

	private UpdateResource buildUpdateRequest(Blob blob) {
		Resource r = Resources.fromBlob(blob);
		r.setId(resource.getId());
		r.setName(name);
		r.setTags(tags);
		
		if (mimeType != null)
			r.setMimeType(mimeType);

		r.setSpecification(specification);

		UpdateResource result = UpdateResource.T.create();
		result.setResource(r);
		result.setUseCase(useCase);
		result.setDeleteOldResourceSource(deleteOldSource);
		if (sourceType != null)
			result.setSourceType(sourceType.getTypeSignature());

		return result;
	}

	private String buildUploadUrl() throws RuntimeException {
		StringBuilder sb = new StringBuilder(streamBaseUrl);

		sb.append("upload?");
		sb.append("domainId=" + accessId);
		sb.append("&sessionId=" + sessionProvider.get());

		return sb.toString();
	}

}
