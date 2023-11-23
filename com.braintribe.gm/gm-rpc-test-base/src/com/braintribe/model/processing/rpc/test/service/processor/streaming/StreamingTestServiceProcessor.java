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
package com.braintribe.model.processing.rpc.test.service.processor.streaming;

import java.util.Date;

import com.braintribe.model.processing.rpc.test.commons.StreamingTools;
import com.braintribe.model.processing.rpc.test.commons.StreamingTools.RandomData;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resource.Resource;

public class StreamingTestServiceProcessor implements ServiceProcessor<StreamingTestServiceProcessorRequest, StreamingTestServiceProcessorResponse> {

	@Override
	public StreamingTestServiceProcessorResponse process(ServiceRequestContext requestContext, StreamingTestServiceProcessorRequest request)
			throws ServiceProcessorException {

		StreamingTestServiceProcessorResponse response = StreamingTestServiceProcessorResponse.T.create();

		Resource resource1 = request.getResource1();
		Resource resource2 = request.getResource2();

		try {

			StreamingTools.checkResource(resource1);
			StreamingTools.checkResource(resource2);

			response.setResource1(StreamingTools.createResource());
			response.setResource2(StreamingTools.createResource());

			response.setResponseDate(new Date());
			
			RandomData capture1Data = StreamingTools.createRandomData();
			RandomData capture2Data = StreamingTools.createRandomData();
			
			response.setCapture1Md5(capture1Data.md5);
			response.setCapture2Md5(capture2Data.md5);

			if (request.getRespondEagerly()) {
				response.setEager(true);
				requestContext.notifyResponse(response);
			}
				
			StreamingTools.serveCapture(capture1Data, request.getCapture1());
			StreamingTools.serveCapture(capture2Data, request.getCapture2());

		} catch (Exception e) {
			throw new ServiceProcessorException("Failed to process streaming request: " + e.getMessage(), e);
		}

		if (request.getRespondEagerly()) {
			return null;
		}

		return response;

	}

}
