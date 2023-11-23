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
package com.braintribe.model.processing.email;

import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.email.service.CheckConnections;
import com.braintribe.model.email.service.ConnectionCheckResult;
import com.braintribe.model.email.service.ConnectionCheckResultEntry;
import com.braintribe.model.email.service.reason.ConfigurationMissing;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;

public class HealthCheckProcessor implements CheckProcessor {

	private static final Logger logger = Logger.getLogger(HealthCheckProcessor.class);

	private Evaluator<ServiceRequest> systemServiceRequestEvaluator;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {

		CheckResult response = CheckResult.T.create();
		List<CheckResultEntry> entries = response.getEntries();

		CheckConnections cc = CheckConnections.T.create();
		Maybe<? extends ConnectionCheckResult> reasoned = cc.eval(systemServiceRequestEvaluator).getReasoned();

		if (reasoned.isUnsatisfied()) {
			if (reasoned.isUnsatisfiedBy(ConfigurationMissing.T)) {
				logger.debug(() -> "Check is ok only because there is no email connector configured.");
				CheckResultEntry cre = CheckResultEntry.T.create();
				cre.setCheckStatus(CheckStatus.ok);
				cre.setName("Email Connnectors");
				cre.setMessage("No connectors available.");
				entries.add(cre);
			} else {
				final String explanation = reasoned.whyUnsatisfied().stringify();
				logger.debug(() -> "Check is not ok because of: " + explanation);
				CheckResultEntry cre = CheckResultEntry.T.create();
				cre.setCheckStatus(CheckStatus.fail);
				cre.setName("Email Connnectors");
				cre.setMessage("Error while trying to establish the connection status.");
				cre.setDetails(explanation);
				entries.add(cre);
			}
		} else {
			ConnectionCheckResult checkResult = reasoned.get();
			for (ConnectionCheckResultEntry entry : checkResult.getEntries()) {
				CheckResultEntry cre = CheckResultEntry.T.create();
				cre.setName(entry.getName() + " (" + entry.getType().name().toLowerCase() + ")");
				if (entry.getSuccess()) {
					cre.setCheckStatus(CheckStatus.ok);
					cre.setDetails(entry.getDetails());
				} else {
					cre.setCheckStatus(CheckStatus.fail);
					cre.setDetails(entry.getErrorMessage());
				}
				entries.add(cre);
			}
		}

		return response;
	}

	@Configurable
	@Required
	public void setSystemServiceRequestEvaluator(Evaluator<ServiceRequest> systemServiceRequestEvaluator) {
		this.systemServiceRequestEvaluator = systemServiceRequestEvaluator;
	}
}
