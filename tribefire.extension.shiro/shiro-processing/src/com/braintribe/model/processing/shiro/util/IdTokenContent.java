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
package com.braintribe.model.processing.shiro.util;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.braintribe.utils.StringTools;

public class IdTokenContent {

	public String token;
	public String firstName;
	public String lastName;
	public String fullName;
	public String username;
	public String email;
	public String subject;
	public String audience;
	public Date expiration;
	public Date issuedAt;
	public String issuer;
	public Set<String> roles;
	public Map<String, Object> claims;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Subject: " + subject + "\n");
		sb.append("Audience: " + audience + "\n");
		sb.append("Expiration: " + expiration + "\n");
		sb.append("Issed At: " + issuedAt + "\n");
		sb.append("Issuer: " + issuer + "\n");
		sb.append("Roles: " + roles + "\n");
		sb.append("Claims: " + claims + "\n");
		sb.append("Username: " + username + "\n");
		sb.append("First Name: " + firstName + "\n");
		sb.append("Last Name: " + lastName + "\n");
		sb.append("Full Name: " + fullName + "\n");
		sb.append("Email: " + email + "\n");
		return StringTools.asciiBoxMessage(sb.toString());
	}
}