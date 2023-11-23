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
package com.braintribe.testing.internal.tribefire.helpers.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;

abstract class AbstractHttpHelper<H extends HttpUriRequest> {
	private final HttpClient client = HttpClientBuilder.create().build();

	protected final H request;

	private final HttpResponse response;
	private String content;

	AbstractHttpHelper(H request) throws ClientProtocolException, IOException {
		this.request = request;
		
		response = client.execute(request);
		
		assertThat(response).as("No HTTP Response recieved").isNotNull();
	}
	
	public String getContent() throws UnsupportedOperationException, IOException {
		if (content == null) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line + "\n");
			}
			
			content = result.toString();
		}
		
		return content;
	}
	
	public <T> T getUnmarshalledGmObject() throws MarshallException, UnsupportedOperationException, IOException {
		JsonStreamMarshaller jsm = new JsonStreamMarshaller();
		@SuppressWarnings("unchecked")
		T unmarshalled = (T) jsm.unmarshall(new ByteArrayInputStream(getContent().getBytes("UTF-8")));
		return unmarshalled;
	}
	
	public int getStatusCode() {
		return response.getStatusLine().getStatusCode();
	}

}
