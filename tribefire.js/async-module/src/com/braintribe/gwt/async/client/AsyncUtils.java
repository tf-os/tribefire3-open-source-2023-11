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
package com.braintribe.gwt.async.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

/**
 * This utility class contain helper methods for performing some common asynchronous tasks.
 * @author Dirk.
 *
 */
public class AsyncUtils {
	
	/**
	 * This method loads the string resource, from the URL passed as the parameter.
	 * @param url - URL of the resource to be read.
	 * @return - a String Future with the file contents.
	 */
	public static Future<String> loadStringResource(String url) {
		final Future<String> future = new Future<String>();
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == 200) {
						future.onSuccess(response.getText());
					} else {
						future.onFailure(new StatusCodeException(response.getStatusCode(),
								response.getStatusText(), response.getText()));
					}
				}
				
				@Override
				public void onError(Request request, Throwable exception) {
					future.onFailure(exception);
				}
			});
		} catch (RequestException e) {
			future.onFailure(e);
		}
		
		return future;
	}
	
	/**
	 * This method loads the string resource, from the URL passed as the parameter.
	 * @param url - URL of the resource to be read.
	 * @return - a String Future with the file contents.
	 */
	public static Future<String> loadStringResource(String url, final String defaultContent) {
		final Future<String> future = new Future<String>();
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request, Response response) {
					int sc = response.getStatusCode();
					if (sc == 200) {
						future.onSuccess(response.getText());
					} 
					else if (sc == 404) {
						future.onSuccess(defaultContent);
					}
					else {
						future.onFailure(new StatusCodeException(sc,
								response.getStatusText(), response.getText()));
					}
				}
				
				@Override
				public void onError(Request request, Throwable exception) {
					future.onFailure(exception);
				}
			});
		} catch (RequestException e) {
			future.onFailure(e);
		}
		
		return future;
	}
}
