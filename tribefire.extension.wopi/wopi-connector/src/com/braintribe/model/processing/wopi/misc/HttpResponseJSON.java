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
package com.braintribe.model.processing.wopi.misc;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 
 * Base class of JSON response conversion (bean -> JSON)
 */
@JsonInclude(Include.NON_NULL)
public abstract class HttpResponseJSON implements HttpResponse {

	@Override
	public final void close() throws Exception {
		// NOP
	}

	@Override
	public final void write(HttpServletResponse response) throws IOException {
		response.setContentType(ContentType.APPLICATION_JSON.toString());
		JsonUtils.writeValue(response.getOutputStream(), this);
	}

	@Override
	public final String toString() {
		try {
			return JsonUtils.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

}
