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
package com.braintribe.model.securityservice;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Confidential;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * A simplified implementation of the {@link OpenUserSession} request which takes a user and password.
 */
@Description("Simplified OpenUserSession request that takes user and password to build authentication credentials.")
public interface OpenUserSessionWithUserAndPassword extends SimplifiedOpenUserSession {

	EntityType<OpenUserSessionWithUserAndPassword> T = EntityTypes.T(OpenUserSessionWithUserAndPassword.class);

	@Mandatory
	String getUser();
	void setUser(String user);

	@Mandatory
	@Confidential
	String getPassword();
	void setPassword(String password);

	String getLocale();
	void setLocale(String locale);

	@Initializer("false")
	boolean getStaySignedIn();
	void setStaySignedIn(boolean staySignedIn);

	@Override
	EvalContext<? extends OpenUserSessionResponse> eval(Evaluator<ServiceRequest> evaluator);
}
