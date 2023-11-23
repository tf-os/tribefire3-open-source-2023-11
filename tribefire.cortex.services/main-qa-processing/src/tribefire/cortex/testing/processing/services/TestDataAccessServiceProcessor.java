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
package tribefire.cortex.testing.processing.services;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.qa.cartridge.main.model.data.Address;
import com.braintribe.qa.cartridge.main.model.service.TestAccessDataRequest;
import com.braintribe.qa.cartridge.main.model.service.TestAccessDataResponse;
import com.braintribe.utils.genericmodel.GMCoreTools;

public class TestDataAccessServiceProcessor implements AccessRequestProcessor<TestAccessDataRequest, TestAccessDataResponse> {

	private static Logger logger = Logger.getLogger(TestDataAccessServiceProcessor.class);

	@Override
	public TestAccessDataResponse process(AccessRequestContext<TestAccessDataRequest> context) {
		// log detailed info on trace level
		// (instead of checking if logger.isTraceEnabled, we "guard" using lambda expression)
		TestAccessDataRequest request = context.getRequest();
		logger.trace(() -> "Processing request " + GMCoreTools.getDescription(request));

		TestAccessDataResponse response = TestAccessDataResponse.T.create();
		
		EntityQuery query = EntityQueryBuilder.from(Address.T).where().property(Address.street).eq(request.getText()).done();
		Address address = context.getSession().query().entities(query).first();
		address.setStreetNumber(17);
		context.getSession().commit();
		response.setEcho(address.getStreetNumber().toString());
		return response;
	}

}
