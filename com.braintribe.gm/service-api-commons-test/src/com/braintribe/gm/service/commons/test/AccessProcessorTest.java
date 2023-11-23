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
package com.braintribe.gm.service.commons.test;

import org.junit.Test;

import com.braintribe.gm.service.commons.test.model.EvalTestAccessRequest;
import com.braintribe.gm.service.commons.test.model.EvalTestAccessAuthRequest;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 *
 * This test class needs to be improved and moved to another artifact. Committing only because of time limitations and code handover.
 *
 * @author Neidhart.Orlich
 * @author Dirk Scheffler
 *
 */
public class AccessProcessorTest extends CompositeServiceProcessorTestBase {
	@Test
	public void test() {
		EvalTestAccessRequest request = EvalTestAccessRequest.T.create();
		request.setDomainId("test.access");
		Object result = evaluator.eval(request).get();
		Assertions.assertThat(result).isEqualTo("test.access");
	}

	@Test
	public void test2() {
		UserPasswordCredentials credentials = UserPasswordCredentials.forUserName("tester", "7357");

		OpenUserSession openUserSession = OpenUserSession.T.create();
		openUserSession.setCredentials(credentials);
		String sessionId = openUserSession.eval(evaluator).get().getUserSession().getSessionId();

		EvalTestAccessAuthRequest request = EvalTestAccessAuthRequest.T.create();
		request.setDomainId("test.access");
		request.setSessionId(sessionId);
		Object result = evaluator.eval(request).get();
		Assertions.assertThat(result).isEqualTo("tester");
	}
}
