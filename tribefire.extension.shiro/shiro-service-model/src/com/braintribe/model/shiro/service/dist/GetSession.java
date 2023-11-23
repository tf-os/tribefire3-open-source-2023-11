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
package com.braintribe.model.shiro.service.dist;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.shiro.service.ShiroRequest;

@SelectiveInformation("Get Shiro Session")
public interface GetSession extends ShiroRequest {

	EntityType<GetSession> T = EntityTypes.T(GetSession.class);

	String getShiroSessionId();
	void setShiroSessionId(String shiroSessionId);

	@Override
	EvalContext<? extends SerializedSession> eval(Evaluator<ServiceRequest> evaluator);

}