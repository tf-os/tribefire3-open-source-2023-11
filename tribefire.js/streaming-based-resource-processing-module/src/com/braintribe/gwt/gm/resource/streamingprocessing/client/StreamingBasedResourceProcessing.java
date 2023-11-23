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
package com.braintribe.gwt.gm.resource.streamingprocessing.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.fileapi.client.File;
import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.gwt.fileapi.client.FormData;
import com.braintribe.gwt.fileapi.client.ProgressHandler;
import com.braintribe.gwt.fileapi.client.XMLHttpRequest2;
import com.braintribe.gwt.genericmodel.client.codec.dom4.GmXmlCodec;
import com.braintribe.gwt.gm.resource.api.client.ResourceBuilder;
import com.braintribe.gwt.gm.resource.api.client.ResourceProcessingException;
import com.braintribe.gwt.gm.resource.api.client.ResourcesFromFilesBuilder;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.HasAccessDescriptor;
import com.braintribe.model.resource.Resource;
import com.google.gwt.http.client.URL;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class StreamingBasedResourceProcessing implements ResourceBuilder {
	//private static final String META_DATA_PREFIX = "meta.";
	private Codec<List<Resource>, String> resultCodec;
	private ManagedGmSession session;
	private String streamingServletContext;
	private String streamingServletPath = "streaming";
	private Supplier<String> sessionProvider;
	private XMLHttpRequest2 xhr;
	//private String responseMimeType;
	//private String fileName;
	//private boolean interpretMetaData = false;
	
	public void setStreamingServletPath(String streamingServletPath) {
		this.streamingServletPath = streamingServletPath;
	}
	
	/*public void setInterpretMetaData(boolean interpretMetaData) {
		this.interpretMetaData = interpretMetaData;
	}*/
	
	@Required
	public void setSessionProvider(Supplier<String> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}
	
	@Required
	public void setStreamingServletContext(String streamingServletContext) {
		this.streamingServletContext = streamingServletContext;
	}
	
	public Codec<List<Resource>, String> getResultCodec() {
		if (resultCodec == null) {
			resultCodec = new GmXmlCodec<List<Resource>>();
		}

		return resultCodec;
	}
	
	@Override
	public void configureGmSession(ManagedGmSession gmSession) {
		session = gmSession;
	}
	
	@Override
	public ResourcesFromFilesBuilder fromFiles() {
		return new ResourcesFromFilesBuilder() {
			private ProgressHandler progressHandler;
			private final List<File> files = new ArrayList<File>();
			
			@Override
			public ResourcesFromFilesBuilder withProgressHandler(ProgressHandler progressHandler) {
				this.progressHandler = progressHandler;
				return this;
			}
			
			@Override
			public Future<List<Resource>> build() {
				return buildFromFiles(files, progressHandler);
			}
			
			@Override
			public ResourcesFromFilesBuilder addSource(File file) {
				files.add(file);
				return this;
			}
			
			@Override
			public ResourcesFromFilesBuilder addFiles(FileList fileList) {
				for (int i = 0; i < fileList.getLength(); i++) {
					File file = fileList.item(i);
					files.add(file);
				}
				return this;
			}
			
			@Override
			public ResourcesFromFilesBuilder addFiles(File... files) {
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					this.files.add(file);
				}
				return this;
			}
			
			@Override
			public ResourcesFromFilesBuilder addFiles(Iterable<File> fileList) {
				for (File file: fileList)
					files.add(file);
				return this;
			}
		};
	}
	
	@Override
	public void abortUpload() {
		if (xhr != null)
			xhr.abort();
	}
	
	protected Future<List<Resource>> buildFromFiles(List<File> files, ProgressHandler progressHandler) {
		final Future<List<Resource>> future = new Future<>();
		
		xhr = XMLHttpRequest2.create();
		try {
			xhr.open("POST", buildUploadUrl());
		} catch (RuntimeException e1) {
			future.onFailure(new ResourceProcessingException("error while building upload url", e1));
		}
		
		xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
			@Override
			public void onReadyStateChange(XMLHttpRequest xhr) {
				if (xhr.getReadyState() == XMLHttpRequest.DONE) {
					if (xhr.getStatus() == 200) {
						//String allHeaders = xhr.getAllResponseHeaders();
						//String mimeType = xhr.getResponseHeader("Content-Type");
						String xml = xhr.getResponseText(); 
						try {
							List<Resource> result = getResultCodec().decode(xml);
							future.onSuccess(result);
						} catch (CodecException e) {
							future.onFailure(new ResourceProcessingException("error while decoding results from upload", e));
						} 
					} else
						future.onFailure(new ResourceProcessingException("error while creating resources on bridge; error code=" + xhr.getStatus() + ": " + xhr.getStatusText()));
					
					StreamingBasedResourceProcessing.this.xhr = null;
				}
			}
		});
		
		if (progressHandler != null)
			xhr.getUpload().setProgressHandler(progressHandler);
		
		FormData formData = FormData.create();
		for (File file: files)
			formData.append("content", file);
		
		xhr.send(formData);
		
		return future;
	}
	
	private String buildUploadUrl() throws RuntimeException {
		Map<String, String> params = new HashMap<String, String>();
		fillParams(params);
		StringBuilder builder = new StringBuilder();
//		builder.append("/");
		builder.append(streamingServletContext);
//		builder.append("/");
		builder.append(streamingServletPath);
		
		appendQueryString(builder, params);
		return builder.toString();
	}

	private void fillParams(Map<String, String> params) throws RuntimeException {
		String accessId = null;

		// Specify access if possible
		if (session instanceof HasAccessDescriptor)
			accessId = ((HasAccessDescriptor)session).getAccessDescriptor().accessId();
		
		if (accessId != null)
			params.put("accessId", accessId);
		
		// specify sessionId
		params.put("sessionId", sessionProvider.get());
		
		params.put("responseMimeType", "text/xml");
		
		/*if(interpretMetaData){
			// specify meta data of upload
			Provider<Map<String, String>> metaDataProvider = CspCallServiceGlobalSettings.getMetaDataProvider();
			
			if (metaDataProvider != null) {
				Map<String, String> metaData = metaDataProvider.provide();
				if (metaData != null) {
					for (Map.Entry<String, String> entry: metaData.entrySet()) {
						String name = META_DATA_PREFIX + entry.getKey();
						String value = entry.getValue();
						params.put(name,  value);
					}
				}
			}
		}*/
	}
	
	private void appendQueryString(StringBuilder builder, Map<String, String> params) {
		if (!params.isEmpty()) {
			builder.append('?');
			for (Map.Entry<String, String> entry: params.entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue();
				builder.append('&');
				builder.append(URL.encodeQueryString(name));
				builder.append('=');
				builder.append(URL.encodeQueryString(value));
			}
		}
	}
}
