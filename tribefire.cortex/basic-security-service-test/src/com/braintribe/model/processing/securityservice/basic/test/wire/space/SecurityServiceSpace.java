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
package com.braintribe.model.processing.securityservice.basic.test.wire.space;

import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.model.processing.securityservice.basic.HardwiredDispatchingAuthenticator;
import com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor;
import com.braintribe.model.securityservice.credentials.ExistingSessionCredentials;
import com.braintribe.model.securityservice.credentials.GrantedCredentials;
import com.braintribe.model.securityservice.credentials.TrustedCredentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class SecurityServiceSpace implements WireSpace {

	private static final String serviceId = "SECURITY";

	@Import
	private AuthExpertsSpace authExperts;

	@Import
	private AuthContextSpace authContext;

	@Import
	private UserSessionServiceSpace userSessionService;

	@Import
	private CommonServiceProcessingContract commonServiceProcessing;

	public String serviceId() {
		return serviceId;
	}

	@Managed
	public SecurityServiceProcessor service() {
		SecurityServiceProcessor bean = new SecurityServiceProcessor();
		bean.setUserSessionService(userSessionService.service());
		bean.setEvaluator(commonServiceProcessing.evaluator());
		bean.setSessionMaxIdleTime(userSessionService.defaultMaxIdleTime());
		bean.setUserSessionService(userSessionService.service());
		bean.setSessionMaxAge(null);
		return bean;
	}

	@Managed
	public HardwiredDispatchingAuthenticator authenticator() {
		HardwiredDispatchingAuthenticator bean = new HardwiredDispatchingAuthenticator();
		bean.registerAuthenticator(UserPasswordCredentials.T, authExperts.userPasswordCredentials());
		bean.registerAuthenticator(GrantedCredentials.T, authExperts.grantedCredentials());
		bean.registerAuthenticator(ExistingSessionCredentials.T, authExperts.existingSessionCredentials());
		bean.registerAuthenticator(TrustedCredentials.T, authExperts.trustedCredentials());
		return bean;
	}
}
