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
package com.braintribe.model.processing.shiro.service;

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.utils.lcd.StringTools;

public class HealthCheckProcessor implements CheckProcessor {

	private static final Logger logger = Logger.getLogger(HealthCheckProcessor.class);

	private Supplier<String> authAccessIdSupplier;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {

		CheckResult response = CheckResult.T.create();

		List<CheckResultEntry> entries = response.getEntries();
		
		String authId = authAccessIdSupplier.get();
		
		CheckResultEntry entry = CheckResultEntry.T.create();
		entry.setCheckStatus(!StringTools.isBlank(authId) ? CheckStatus.ok : CheckStatus.fail);
		entry.setName("Authentication");
		entry.setDetails("Connected to authentication access: "+authId);
		
		entries.add(entry);

		return response;
	}

	@Required
	public void setAuthAccessIdSupplier(Supplier<String> authAccessIdSupplier) {
		this.authAccessIdSupplier = authAccessIdSupplier;
	}


}
