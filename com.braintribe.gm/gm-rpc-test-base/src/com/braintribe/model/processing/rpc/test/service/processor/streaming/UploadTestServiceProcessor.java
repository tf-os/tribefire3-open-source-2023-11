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

import com.braintribe.model.processing.rpc.test.commons.StreamingTools.RandomDataStore;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

public class UploadTestServiceProcessor implements ServiceProcessor<UploadTestServiceProcessorRequest, UploadTestServiceProcessorResponse> {

	@Override
	public UploadTestServiceProcessorResponse process(ServiceRequestContext requestContext, UploadTestServiceProcessorRequest request) throws ServiceProcessorException {

		String uploadedId = null;
		try {
			long s = System.nanoTime();
			uploadedId = RandomDataStore.upload(request.getResource());
			long e = System.nanoTime();
			double d = (e-s) / 1_000_000D;
			System.out.println("resource storage took: " + d + "ms");
		} catch (Exception e) {
			throw new ServiceProcessorException("Failed to upload resource: "+e.getMessage(), e);
		}
		
		UploadTestServiceProcessorResponse response = UploadTestServiceProcessorResponse.T.create();
		response.setResourceId(uploadedId);

		if (request.getRespondEagerly()) {
			response.setEager(true);
			requestContext.notifyResponse(response);
			return null;
		}

		return response;
		
	}

}
