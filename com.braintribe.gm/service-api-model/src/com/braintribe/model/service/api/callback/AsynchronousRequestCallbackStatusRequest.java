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
package com.braintribe.model.service.api.callback;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

@Description("This request is invoked during the processing of an asynchronous request. There is no guarantee for status callbacks, though.")
public interface AsynchronousRequestCallbackStatusRequest extends AsynchronousRequestCallbackRequest {

	final EntityType<AsynchronousRequestCallbackStatusRequest> T = EntityTypes.T(AsynchronousRequestCallbackStatusRequest.class);
	
	String percentage = "percentage";

	void setPercentage(Double percentage);
	@Name("Percentage")
	@Description("Shows the degree of completion. The Job is completed when it reaches 1.")
	Double getPercentage();

	@Override
	EvalContext<Boolean> eval(Evaluator<ServiceRequest> evaluator);
}
