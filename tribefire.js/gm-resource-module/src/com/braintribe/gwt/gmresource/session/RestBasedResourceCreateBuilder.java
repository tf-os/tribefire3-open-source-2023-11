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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.fileapi.client.Blob;
import com.braintribe.gwt.fileapi.client.File;
import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.gwt.fileapi.client.FormData;
import com.braintribe.gwt.fileapi.client.ProgressHandler;
import com.braintribe.gwt.fileapi.client.XMLHttpRequest2;
import com.braintribe.gwt.genericmodel.client.codec.dom4.GmXmlCodec;
import com.braintribe.gwt.gmresource.client.ResourceProcessingException;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.resource.ResourceCreateBuilder;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.model.resourceapi.persistence.UploadResourcesResponse;
import com.braintribe.processing.async.api.JsPromise;
import com.google.gwt.xhr.client.XMLHttpRequest;

@SuppressWarnings("unusable-by-js")
public class RestBasedResourceCreateBuilder implements ResourceCreateBuilder {

	private final String accessId;
	private final Supplier<String> sessionProvider;
	private final String streamBaseUrl;
	private ProgressHandler progressHandler;

	public RestBasedResourceCreateBuilder(String accessId, String streamBaseUrl, Supplier<String> sessionIdProvider) {
		this.accessId = accessId;
		this.streamBaseUrl = streamBaseUrl;
		this.sessionProvider = sessionIdProvider;
	}

	@Override
	public ResourceCreateBuilder mimeType(String mimeType) {
		return this;
	}

	@Override
	public ResourceCreateBuilder md5(String md5) {
		return this;
	}

	@Override
	public ResourceCreateBuilder useCase(String useCase) {
		return this;
	}

	@Override
	public ResourceCreateBuilder tags(Set<String> tags) {
		return this;
	}

	@Override
	public ResourceCreateBuilder sourceType(EntityType<? extends ResourceSource> sourceType) {
		return this;
	}

	@Override
	public ResourceCreateBuilder specification(ResourceSpecification specification) {
		return this;
	}

	@Override
	public ResourceCreateBuilder name(String name) {
		return this;
	}

	@Override
	public ResourceCreateBuilder withProgressHandler(ProgressHandler progressHandler) {
		this.progressHandler = progressHandler;
		return this;
	}

	@Override
	public JsPromise<List<Resource>> store(String text) {
		return store(Blob.createFromString(text, "plain/text;charset=UTF-8"));
	}

	@Override
	public JsPromise<List<Resource>> store(Blob blob) {
		return buildFromFiles(Collections.singletonList(blob)).toJsPromise();
	}

	@Override
	public JsPromise<List<Resource>> store(File file) {
		return buildFromFiles(Collections.singletonList(file)).toJsPromise();
	}

	@Override
	public JsPromise<List<Resource>> store(FileList fileList) {
		List<File> files = newList();
		for (int i = 0; i < fileList.getLength(); i++)
			files.add(fileList.item(i));

		return buildFromFiles(files).toJsPromise();
	}

	@Override
	public JsPromise<List<Resource>> store(File[] files) {
		return buildFromFiles(Arrays.asList(files)).toJsPromise();
	}

	@Override
	public Future<List<Resource>> store(List<File> files) {
		return buildFromFiles(files);
	}

	private Future<List<Resource>> buildFromFiles(List<? extends Blob> fileList) {
		final Future<List<Resource>> future = new Future<>();

		XMLHttpRequest2 xhr = XMLHttpRequest2.create();
		try {
			xhr.open("POST", buildUploadUrl());
		} catch (RuntimeException e1) {
			future.onFailure(new ResourceProcessingException("error while building upload url", e1));
			return future;
		}
		xhr.setRequestHeader("Accept", "application/xml");
		xhr.setOnReadyStateChange(xhr1 -> {
			if (xhr1.getReadyState() != XMLHttpRequest.DONE)
				return;

			if (xhr1.getStatus() != 200) {
				future.onFailure(new ResourceProcessingException(
						"error while creating resources; error code=" + xhr1.getStatus() + ": " + xhr1.getStatusText()));
				return;
			}

			String xml = xhr1.getResponseText();
			try {
				Codec<UploadResourcesResponse, String> codec = new GmXmlCodec<>();
				UploadResourcesResponse result = codec.decode(xml);
				future.onSuccess(result.getResources());
			} catch (CodecException e) {
				future.onFailure(new ResourceProcessingException("error while decoding upload results", e));
			}
		});

		if (progressHandler != null)
			xhr.getUpload().setProgressHandler(progressHandler);

		xhr.send(toFormData(fileList));

		return future;
	}

	private String buildUploadUrl() throws RuntimeException {
		StringBuilder sb = new StringBuilder(streamBaseUrl);

		sb.append("upload?");
		sb.append("domainId=" + accessId);
		sb.append("&sessionId=" + sessionProvider.get());

		return sb.toString();
	}

	private FormData toFormData(List<? extends Blob> fileList) {
		FormData formData = FormData.create();
		for (Blob blob : fileList)
			formData.append("resources", blob);
		return formData;
	}

}
