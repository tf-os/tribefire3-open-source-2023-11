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
package com.braintribe.model.processing.webrpc.client;

import com.braintribe.model.processing.rpc.commons.api.config.GmRpcClientConfig;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.utils.stream.api.StreamPipeFactory;

public class BasicGmWebRpcClientConfig extends GmRpcClientConfig {

	private String url;
	private String contentType;
	private Long retryInterval;
	private Long callTimeout;
	private HttpClientProvider httpClientProvider;
	private Integer socketTimeout;
	private StreamPipeFactory streamPipeFactory;

	public Integer getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Long getRetryInterval() {
		return retryInterval;
	}

	public void setRetryInterval(Long retryInterval) {
		this.retryInterval = retryInterval;
	}

	public Long getCallTimeout() {
		return callTimeout;
	}

	public void setCallTimeout(Long callTimeout) {
		this.callTimeout = callTimeout;
	}

	public HttpClientProvider getHttpClientProvider() {
		return httpClientProvider;
	}

	public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
		this.httpClientProvider = httpClientProvider;
	}

	public StreamPipeFactory getStreamPipeFactory() {
		return streamPipeFactory;
	}

	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

}
