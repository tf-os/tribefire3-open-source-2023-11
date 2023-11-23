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
package com.braintribe.model.processing.shiro.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import com.braintribe.model.shiro.service.EnsureUserByIdToken;
import com.braintribe.model.shiro.service.EnsuredUser;
import com.braintribe.model.user.User;

import io.jsonwebtoken.Jwts;

public class ShiroServiceProcessorTest {

	@Test
	public void testIdTokenParsing() throws Exception {

		EnsureUserByIdToken request = EnsureUserByIdToken.T.create();

		Map<String, Object> claims = new HashMap<>();
		claims.put("email", "test@example.com");
		claims.put("first_name", "C.");
		claims.put("last_name", "Cortex");

		Instant now = Instant.now();
		//@formatter:off
		String jwt = Jwts.builder()
		        .setAudience("https://braintribe.okta.com/oauth2/v1/token")
		        .setIssuedAt(Date.from(now))
		        .setExpiration(Date.from(now.plus(5L, ChronoUnit.MINUTES)))
		        .setIssuer("0oa160j8din1F60Dh4x7")
		        .setSubject("testuser")
		        .setId(UUID.randomUUID().toString())
		        .addClaims(claims)
		        .compact();
		//@formatter:on

		request.setIdToken(jwt);

		ShiroServiceProcessor proc = new ShiroServiceProcessor() {
			@Override
			protected User ensureUser(String username, String firstName, String lastName, String email, Set<String> roles) {
				User newUser = User.T.create();
				newUser.setName(username);
				newUser.setFirstName(firstName);
				newUser.setLastName(lastName);
				newUser.setEmail(email);
				return newUser;
			}
		};

		EnsuredUser result = proc.ensureUserByIdToken(null, request);
		assertThat(result).isNotNull();
		assertThat(result.getSuccess()).isTrue();
		User ensuredUser = result.getEnsuredUser();
		assertThat(ensuredUser).isNotNull();
		assertThat(ensuredUser.getFirstName()).isEqualTo("C.");
		assertThat(ensuredUser.getLastName()).isEqualTo("Cortex");
		assertThat(ensuredUser.getEmail()).isEqualTo("test@example.com");
		assertThat(ensuredUser.getName()).isEqualTo("testuser");

	}
}
