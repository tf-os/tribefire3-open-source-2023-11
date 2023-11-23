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
package com.braintribe.model.processing.check;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.check.service.CompositeCheck;
import com.braintribe.model.check.service.CompositeCheckResult;
import com.braintribe.model.extensiondeployment.check.CheckProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.CompositeRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.CompositeResponse;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;

public class CompositeCheckProcessor implements ServiceProcessor<CompositeCheck,CompositeCheckResult> {

	@Override
	public CompositeCheckResult process(ServiceRequestContext requestContext, CompositeCheck request) {
		
		List<ServiceRequest> requests = new ArrayList<>();
		for (CheckProcessor checkProcessor : request.getCheckProcessors()) {
			CheckRequest cr = CheckRequest.T.create();
			cr.setServiceId(checkProcessor.getExternalId());
			requests.add(cr);
		}
		requests.addAll(request.getParameterizedChecks());
		
		CompositeRequest compositeRequest = CompositeRequest.T.create();
		compositeRequest.setRequests(requests);
		compositeRequest.setContinueOnFailure(true);
		compositeRequest.setParallelize(request.getParallelize());
		CompositeResponse compositeResult = compositeRequest.eval(requestContext).get();
		
		CompositeCheckResult result = CompositeCheckResult.T.create();
		
		List<ServiceResult> results = compositeResult.getResults();
		int i = 0;
		for (; i<results.size(); ++i) {

			CheckResult checkResult = null;
			CheckResultEntry cre = null;
			
			ServiceResult serviceResult = results.get(i);
			switch(serviceResult.resultType()) {
				case failure:
					Failure failure = (Failure) serviceResult;
					checkResult = CheckResult.T.create();
					cre = CheckResultEntry.T.create();
					cre.setCheckStatus(CheckStatus.fail);
					cre.setMessage(failure.getMessage());
					checkResult.getEntries().add(cre);
					break;
				case success:
					ResponseEnvelope ssr = (ResponseEnvelope) serviceResult;
					checkResult = (CheckResult) ssr.getResult();
					break;
				default:
					checkResult = CheckResult.T.create();
					cre = CheckResultEntry.T.create();
					cre.setCheckStatus(CheckStatus.fail);
					cre.setMessage("Unexpected service result type "+serviceResult.resultType());
					checkResult.getEntries().add(cre);
			}

			if (i < request.getCheckProcessors().size()) {
				result.getCheckProcessorResults().add(checkResult);
			} else {
				result.getParameterizedCheckResults().add(checkResult);
			}
		}
		
		return result;
	}

}
