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
package com.braintribe.transport.http.util;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.braintribe.logging.Logger;

public class ErrorHelper {

	private static Logger logger = Logger.getLogger(ErrorHelper.class);
	
	public static IOException processErrorResponse(String url, String method, HttpResponse response, Exception reason) {

		StatusLine status = null;
		String responseBody = null; 
		
		if (response != null) {

			status = response.getStatusLine();

			HttpEntity responseEntity = response.getEntity();
			if (responseEntity != null) {

				ContentType contentType = ContentType.get(responseEntity);
				if (contentType != null) {
					String mimeType = contentType.getMimeType();
					if (mimeType != null && mimeType.toLowerCase().startsWith("text/")) {
						try {
							responseBody = EntityUtils.toString(responseEntity);
						} catch (Exception e) {
							logger.error("Could not get response body", e);
						}	
					}
				}

				HttpTools.consumeResponse(url, response);

			}
		}

		StringBuilder sb = new StringBuilder(method);
		sb.append(" request to [ ");
		sb.append(url);
		sb.append(" ] failed");
		if (status != null) {
			sb.append(": ");
			sb.append(status);
		}
		if (responseBody != null) {
			sb.append('\n');
			sb.append(responseBody);
		}
		if (reason == null) {
			return new IOException(sb.toString());	
		} else {
			return new IOException(sb.toString(), reason);
		}
		
	}

}
