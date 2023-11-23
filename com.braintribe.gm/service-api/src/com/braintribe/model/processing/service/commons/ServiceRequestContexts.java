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
package com.braintribe.model.processing.service.commons;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.api.ServiceRequestContextBuilder;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.collection.impl.AttributeContexts;

public interface ServiceRequestContexts extends AttributeContexts {

	static ServiceRequestContextBuilder serviceRequestContext(Evaluator<ServiceRequest> evaluator) {
		return new StandardServiceRequestContextBuilder(evaluator);
	}
	
	static ServiceRequestContextBuilder serviceRequestContext(AttributeContext parent, Evaluator<ServiceRequest> evaluator) {
		return new StandardServiceRequestContextBuilder(parent, evaluator);
	}
}
