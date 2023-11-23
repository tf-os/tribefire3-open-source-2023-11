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
package com.braintribe.gm.service.access.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.gm.service.access.test.wire.AccessRequestProcessingTestWireModule;
import com.braintribe.gm.service.access.test.wire.contract.AccessRequestProcessingTestContract;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.IOTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class TestAccessResource {

	protected static WireContext<AccessRequestProcessingTestContract> context;
	protected static Evaluator<ServiceRequest> evaluator;
	protected static AccessRequestProcessingTestContract testContract;

	@Before
	public void beforeClass() {
		context = Wire.context(AccessRequestProcessingTestWireModule.INSTANCE);
		testContract = context.contract();
		evaluator = testContract.evaluator();
	}

	@After
	public void afterClass() {
		context.shutdown();
	}

	@Test
	public void test() throws Exception {
		PersistenceGmSession session = testContract.sessionFactory().newSession(TestConstants.ACCESS_ID_RESOURCE_TEST);

		Resource r = session.resources().create().name("test.txt").store(os -> {
			os.write("hello".getBytes(StandardCharsets.UTF_8));
		});

		try (InputStream in = r.openStream()) {
			String check = IOTools.slurp(in, "UTF-8");
			System.out.println(check);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		r.writeToStream(baos);
		System.out.println(baos.toString("UTF-8"));

		session.deleteEntity(r);

	}
}
