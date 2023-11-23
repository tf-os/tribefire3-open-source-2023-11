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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;

public class HttpPostHelperEx extends AbstractHttpHelper<HttpPost> {

	public HttpPostHelperEx(String url) throws ClientProtocolException, IOException {
		super(new HttpPost(url));
	}

	public HttpPostHelperEx(String url, Object body) throws ClientProtocolException, IOException {
		super(generateRequest(url, body));
	}

	private static HttpPost generateRequest(String url, Object body) throws UnsupportedEncodingException {
		HttpPost request = new HttpPost(url);
		request.setHeader("Content-Type", "application/json");
		request.setEntity(new StringEntity(objectToJson(body)));

		return request;
	}

	private static String objectToJson(Object value) {
		JsonStreamMarshaller jsonMarshaller = new JsonStreamMarshaller();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		jsonMarshaller.marshall(stream, value);

		try {
			return stream.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 seems not to be supported", e);
		}
	}

}
