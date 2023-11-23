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

import java.util.Date;
import java.util.Map;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * This request is meant to be external: it authenticates to any delegate authentication service and open a user session
 * if the authentication was successful.
 * 
 * This request is completely generic, and agnostic to the type of credential, see {@link SimplifiedOpenUserSession} for
 * simple requests.
 * 
 * @see Authenticate
 * @see SimplifiedOpenUserSession
 * @see OpenUserSessionWithUserAndPassword
 */
public interface OpenUserSession extends SecurityRequest {

	EntityType<OpenUserSession> T = EntityTypes.T(OpenUserSession.class);

	String getLocale();
	void setLocale(String locale);

	Credentials getCredentials();
	void setCredentials(Credentials credentials);

	Map<String, String> getProperties();
	void setProperties(Map<String, String> properties);

	Date getExpiryDate();
	void setExpiryDate(Date expiryDate);

	@Override
	EvalContext<? extends OpenUserSessionResponse> eval(Evaluator<ServiceRequest> evaluator);

}
