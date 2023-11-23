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
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.fileapi.client.File;
import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.gwt.fileapi.client.ProgressHandler;
import com.braintribe.gwt.genericmodel.client.codec.dom4.GmXmlCodec;
import com.braintribe.gwt.gm.resource.api.client.ResourceBuilder;
import com.braintribe.gwt.gm.resource.api.client.ResourcesFromFilesBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.persistence.UploadResourceResponse;

public class RestBasedResourceProcessing implements ResourceBuilder {
	private Codec<UploadResourceResponse, String> resultCodec;
	private ManagedGmSession session;
	//private String streamBaseUrl;
	//private Supplier<String> sessionProvider;
	//private XMLHttpRequest2 xhr;
	
	@SuppressWarnings("unused")
	public void setStreamBaseUrl(String streamBaseUrl) {
		//this.streamBaseUrl = streamBaseUrl;
	}
	
	@SuppressWarnings("unused")
	public void setSessionProvider(Supplier<String> sessionProvider) {
		//this.sessionProvider = sessionProvider;
	}
	
	public Codec<UploadResourceResponse, String> getResultCodec() {
		if (resultCodec == null) {
			resultCodec = new GmXmlCodec<UploadResourceResponse>();
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
			private List<File> files = new ArrayList<File>();
			
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
		//if (xhr != null)
			//xhr.abort();
	}
	
	protected Future<List<Resource>> buildFromFiles(List<File> fileList, ProgressHandler progressHandler) {
		Future<List<Resource>> future = new Future<>();
		session.resources().create().withProgressHandler(progressHandler).store(fileList).andThen(result -> {
			session.merge().adoptUnexposed(true).suspendHistory(true).doFor(result, future);
		}).onError(future::onFailure);
		
		return future;
	}
}
