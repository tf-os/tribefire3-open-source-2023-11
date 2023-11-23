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
package com.braintribe.model.processing.service.common.eval;

import java.util.concurrent.ExecutorService;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * Standard local {@link Evaluator} of {@link ServiceRequest}(s).
 * 
 */
public class ConfigurableServiceRequestEvaluator extends AbstractServiceRequestEvaluator {

	@Override
	@Configurable
	@Required
	public void setServiceProcessor(ServiceProcessor<ServiceRequest, Object> serviceProcessor) {
		super.setServiceProcessor(serviceProcessor);
	}

	@Override
	@Configurable
	@Required
	public void setExecutorService(ExecutorService executorService) {
		super.setExecutorService(executorService);
	}

}