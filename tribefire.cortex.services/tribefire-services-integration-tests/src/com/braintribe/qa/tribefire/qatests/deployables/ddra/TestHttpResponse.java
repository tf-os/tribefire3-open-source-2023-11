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
package com.braintribe.qa.tribefire.qatests.deployables.ddra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.http.util.EntityUtils;

public class TestHttpResponse {

	private final org.apache.http.HttpResponse response;

	public TestHttpResponse(org.apache.http.HttpResponse response) {
		this.response = response;
	}

	public org.apache.http.HttpResponse getResponse() {
		return response;
	}

	public int getStatusCode() {
		return response.getStatusLine().getStatusCode();
	}

	public <T> T getContent() {
		try (InputStream content = response.getEntity().getContent()) {
			return (T) TestHttpRequest.MARSHALLER.unmarshall(content);
		} catch (UnsupportedOperationException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getContentStringValue() {
		try (InputStream content = response.getEntity().getContent()) {
			StringBuilder textBuilder = new StringBuilder();
			try (Reader reader = new BufferedReader(new InputStreamReader(content, Charset.forName(StandardCharsets.UTF_8.name())))) {
				int c = 0;
				while ((c = reader.read()) != -1) {
					textBuilder.append((char) c);
				}
			}

			return textBuilder.toString();
		} catch (UnsupportedOperationException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void consumeEntity() {
		EntityUtils.consumeQuietly(response.getEntity());
	}
}
