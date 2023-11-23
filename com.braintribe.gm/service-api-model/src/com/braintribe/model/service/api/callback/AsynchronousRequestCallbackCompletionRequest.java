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
import com.braintribe.model.service.api.result.Failure;

@Description("This request is invoked when an asynchronous request has finished.")
public interface AsynchronousRequestCallbackCompletionRequest extends AsynchronousRequestCallbackRequest {

	final EntityType<AsynchronousRequestCallbackCompletionRequest> T = EntityTypes.T(AsynchronousRequestCallbackCompletionRequest.class);
	
	void setResult(Object result);
	@Name("Result")
	@Description("The result of the asynchronous request, or null in the case of an error.")
	Object getResult();

	void setFailure(Failure failure);
	@Name("Failure")
	@Description("When the processing of the asynchronous request resulted in an exception, it will be provided here.")
	Failure getFailure();

	@Override
	EvalContext<Boolean> eval(Evaluator<ServiceRequest> evaluator);

}
