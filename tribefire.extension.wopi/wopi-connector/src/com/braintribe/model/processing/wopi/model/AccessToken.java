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
package com.braintribe.model.processing.wopi.model;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

import com.braintribe.model.processing.wopi.misc.JsonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;

//@formatter:off
/**
 * Token representation from Authorization Header from Request from MS Web App to TF (e.g. userName, sessionId (Token given in the Browser URL))
 * 
 * 
 * --------------       ---------------
 * | MS Web App | ----> | WOPI module |
 * --------------       ---------------
 * 
 */
//@formatter:on
@JsonInclude(Include.NON_DEFAULT)
public class AccessToken {

	private String userName;
	private String sessionId;

	@Override
	public String toString() {
		try {
			return JsonUtils.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Could not create '" + AccessToken.class.getSimpleName() + "' - this should never happen!", e);
		}
	}

	public String getUserName() {
		return userName;
	}

	public AccessToken setUserName(String userName) {
		this.userName = userName;
		return this;
	}

	public String getSessionId() {
		return sessionId;
	}

	public AccessToken setSessionId(String sessionId) {
		this.sessionId = sessionId;
		return this;
	}

	public String encode() {
		byte[] binaryData = toString().getBytes();
		return Base64.encodeBase64URLSafeString(binaryData);
	}

	public static AccessToken decode(String base64String) {
		try {
			String content = new String(Base64.decodeBase64(base64String));
			return JsonUtils.readValue(content, AccessToken.class);
		} catch (IOException e) {
			return null;
		}
	}
}
