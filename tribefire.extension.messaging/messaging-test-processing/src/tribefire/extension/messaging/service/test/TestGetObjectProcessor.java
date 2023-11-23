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
package tribefire.extension.messaging.service.test;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;

import tribefire.extension.messaging.model.test.TestObject;
import tribefire.extension.messaging.service.test.model.TestGetObjectRequest;
import tribefire.extension.messaging.service.test.model.TestGetObjectResult;

public class TestGetObjectProcessor implements ServiceProcessor<TestGetObjectRequest, TestGetObjectResult> {
	private PersistenceGmSessionFactory factory;

	@Override
	public TestGetObjectResult process(ServiceRequestContext requestContext, TestGetObjectRequest request) {
		// This is a stub processor to fulfill the requirements for integration testing
		PersistenceGmSession session = factory.newSession("cortex");
		EntityQuery query = EntityQueryBuilder.from(TestObject.T).where().property(TestObject.id).ilike(request.getRelatedObjId()).done();
		TestObject result = session.query().entities(query).unique();
		return TestGetObjectResult.build(result);
	}

	@Required
	@Configurable
	public void setFactory(PersistenceGmSessionFactory factory) {
		this.factory = factory;
	}
}
