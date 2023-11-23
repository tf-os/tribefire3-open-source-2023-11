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
package com.braintribe.model.processing.dmbrpc.client;

import java.util.Objects;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.ServiceRequest;

public class GmDmbRpcEvaluator extends GmDmbRpcClientBase implements Evaluator<ServiceRequest> {

	private static final Logger logger = Logger.getLogger(GmDmbRpcEvaluator.class);

	@Required
	@Configurable
	@Override
	public void setConfig(BasicGmDmbRpcClientConfig config) {
		super.setConfig(config);
		logClientConfiguration(logger, true);
	}

	@Override
	public <T> EvalContext<T> eval(ServiceRequest serviceRequest) {
		Objects.requireNonNull(serviceRequest, "serviceRequest must not be null");
		return new RpcEvalContext<T>(serviceRequest);
	}

	public static GmDmbRpcEvaluator create(BasicGmDmbRpcClientConfig config) {
		GmDmbRpcEvaluator evaluator = new GmDmbRpcEvaluator();
		evaluator.setConfig(config);
		return evaluator;
	}

	@Override
	protected Logger logger() {
		return logger;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [objectName=" + getObjectName() + "]";
	}

}
