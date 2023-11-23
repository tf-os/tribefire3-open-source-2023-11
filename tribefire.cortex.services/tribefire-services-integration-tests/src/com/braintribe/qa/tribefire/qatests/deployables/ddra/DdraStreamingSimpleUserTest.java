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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.resourceapi.stream.GetBinary;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.qa.tribefire.qatests.QaTestHelper;
import com.braintribe.testing.internal.tribefire.helpers.http.HttpPostHelperEx;

public class DdraStreamingSimpleUserTest extends AbstractDdraStreamingTest {

	private static ImpApi imp;

	@BeforeClass
	public static void init() {
		imp = apiFactory().build();

		ensureTestUser();
		ensureTestAccess();
		authenticate();
	}

	@AfterClass
	public static void cleanUp() {
		imp.deployable().access(domainId).delete();
		imp.commit();
	}

	public static void authenticate() {
		try {
			Map<String, Object> authRequest = new HashMap<>();
			authRequest.put("user", TEST_USER_NAME);
			authRequest.put("password", TEST_USER_NAME);
			HttpPostHelperEx httpHelper = new HttpPostHelperEx(TRIBEFIRE_SERVICES_URL + "/api/v1/authenticate", authRequest);
	
			String result = httpHelper.getContent();
			System.out.println(result);
			sessionId = result.replaceAll("\"", "").trim();
		} catch (IOException e) {
			throw new RuntimeException("Could not authenticate in tribefire via rest", e);
		}
	}

	private static void ensureTestAccess() {
		domainId = "ddra-simple-test-access";

		QaTestHelper.ensureSmoodAccess(imp, domainId, imp.model(FileSystemSource.T.getModel().name()).get());
	}

	@Test
	public void executeBinaryRequestTest() {
		// Refuse to access binary requests at all (Expect 403 response)
		factory.get() //
			.path(requestUrlPrefixWithDomain() + GetBinary.T.getTypeSignature()) //
			.urlParameter("serviceId", "binaryProcessor.fileSystem") //
			.urlParameter("resource", "@r") //
			.urlParameter("@s", "com.braintribe.model.resource.source.FileSystemSource") //
			.urlParameter("s.path", "../../setup/data/config.json") //
			.urlParameter("r.resourceSource", "@s") //
			.urlParameter("downloadResource", "true") //
			.urlParameter("domainId", domainId) //
			.urlParameter("sessionId", sessionId)
			.execute(403);
	}

}