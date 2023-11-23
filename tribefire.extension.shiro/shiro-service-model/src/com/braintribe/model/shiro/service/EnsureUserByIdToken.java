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
package com.braintribe.model.shiro.service;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

@SelectiveInformation("Ensure User by ID Token")
public interface EnsureUserByIdToken extends ShiroRequest {

	EntityType<EnsureUserByIdToken> T = EntityTypes.T(EnsureUserByIdToken.class);

	String getIdToken();
	void setIdToken(String idToken);

	@Initializer("'preferred_username'")
	String getUsernameClaim();
	void setUsernameClaim(String usernameClaim);

	@Initializer("'roles'")
	String getRolesClaim();
	void setRolesClaim(String rolesClaim);

	@Initializer("'email'")
	String getEmailClaim();
	void setEmailClaim(String emailClaim);

	@Initializer("'name'")
	String getNameClaim();
	void setNameClaim(String nameClaim);

	@Initializer("'first_name'")
	String getFirstNameClaim();
	void setFirstNameClaim(String firstNameClaim);

	@Initializer("'last_name'")
	String getLastNameClaim();
	void setLastNameClaim(String lastNameClaim);

	@Override
	EvalContext<? extends EnsuredUser> eval(Evaluator<ServiceRequest> evaluator);

}
