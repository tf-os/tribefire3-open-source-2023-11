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
package com.braintribe.model.processing.rpc.test.service.iface.basic;

import java.util.Date;

public class BasicTestServiceImpl implements BasicTestService {

	@Override
	public BasicTestServiceResponse test(BasicTestServiceRequest request) throws Exception {

		BasicTestServiceResponse response = BasicTestServiceResponse.T.create();

		response = test(request, response);

		return response;
	}

	@Override
	public BasicTestServiceResponseEncrypted testEncrypted(BasicTestServiceRequest request) throws Exception {

		BasicTestServiceResponseEncrypted response = BasicTestServiceResponseEncrypted.T.create();

		response = test(request, response);

		return response;

	}

	private static <T extends BasicTestServiceResponse> T test(BasicTestServiceRequest request, T response) throws Exception {

		response.setRequestId(request.getRequestId());
		response.setResponseDate(new Date());

		return response;

	}

}
