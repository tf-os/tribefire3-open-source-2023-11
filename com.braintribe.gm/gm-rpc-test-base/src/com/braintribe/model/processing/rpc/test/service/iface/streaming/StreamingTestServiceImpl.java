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
package com.braintribe.model.processing.rpc.test.service.iface.streaming;

import java.util.Date;

import com.braintribe.model.processing.rpc.test.commons.StreamingTools;
import com.braintribe.model.resource.Resource;

public class StreamingTestServiceImpl implements StreamingTestService {

	@Override
	public StreamingTestServiceResponse test(StreamingTestServiceRequest request) throws Exception {

		StreamingTestServiceResponse response = StreamingTestServiceResponse.T.create();

		response = test(request, response);

		return response;

	}

	@Override
	public StreamingTestServiceResponseEncrypted testEncrypted(StreamingTestServiceRequest request) throws Exception {

		StreamingTestServiceResponseEncrypted response = StreamingTestServiceResponseEncrypted.T.create();

		response = test(request, response);

		return response;

	}

	private static <T extends StreamingTestServiceResponse> T test(StreamingTestServiceRequest request, T response) throws Exception {

		Resource resource1 = request.getResource1();
		Resource resource2 = request.getResource2();

		StreamingTools.checkResource(resource1);
		StreamingTools.checkResource(resource2);

		response.setCapture1Md5(StreamingTools.serveCapture(request.getCapture1()));
		response.setCapture2Md5(StreamingTools.serveCapture(request.getCapture2()));

		response.setResource1(StreamingTools.createResource());
		response.setResource2(StreamingTools.createResource());

		response.setResponseDate(new Date());

		return response;

	}

}
