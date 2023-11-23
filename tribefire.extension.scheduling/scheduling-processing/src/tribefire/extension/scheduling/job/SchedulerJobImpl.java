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
package tribefire.extension.scheduling.job;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.job.api.JobRequest;
import com.braintribe.model.job.api.JobResponse;
import com.braintribe.model.processing.service.api.Job;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.scheduling.model.api.UpdateRegistry;

public class SchedulerJobImpl implements Job {

	private static final Logger logger = Logger.getLogger(SchedulerJobImpl.class);

	private Evaluator<ServiceRequest> systemServiceRequestEvaluator;
	private String accessId;

	@Override
	public JobResponse process(ServiceRequestContext requestContext, JobRequest request) {
		logger.debug(() -> "Initiating periodic Cleanup service.");

		UpdateRegistry req = UpdateRegistry.T.create();
		req.setDomainId(accessId);

		req.eval(systemServiceRequestEvaluator).get();

		return JobResponse.T.create();
	}

	@Required
	@Configurable
	public void setSystemServiceRequestEvaluator(Evaluator<ServiceRequest> systemServiceRequestEvaluator) {
		this.systemServiceRequestEvaluator = systemServiceRequestEvaluator;
	}

	@Required
	@Configurable
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

}
