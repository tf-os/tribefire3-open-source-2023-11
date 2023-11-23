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
package tribefire.extension.demo.processing;

import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

public class DemoHealthCheckProcessor implements CheckProcessor {

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {
		
		CheckResult response  = CheckResult.T.create();
		
		CheckResultEntry entry = CheckResultEntry.T.create();
		entry.setName("Demo Health Check");
		entry.setDetails("Successfully executed demo health check.");
		entry.setCheckStatus(CheckStatus.ok);
		
		response.getEntries().add(entry);
		
		return response;
		
	}

}
