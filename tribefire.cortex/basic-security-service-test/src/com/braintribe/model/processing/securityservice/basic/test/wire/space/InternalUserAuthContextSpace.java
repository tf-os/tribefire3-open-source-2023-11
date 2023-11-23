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

import static com.braintribe.wire.api.util.Sets.set;

import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.model.processing.securityservice.commons.provider.SessionIdFromUserSessionProvider;
import com.braintribe.model.processing.securityservice.commons.provider.StaticUserSessionHolder;
import com.braintribe.model.processing.securityservice.commons.scope.StandardUserSessionScoping;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class InternalUserAuthContextSpace implements WireSpace {

	protected static final Set<String> internalRoles = set("tf-internal");

	@Import
	private SecurityServiceSpace securityService;

	@Import
	private UserSessionServiceSpace userSessionsService;

	@Import
	private CurrentUserAuthContextSpace currentUserAuthContext;
	
	@Import
	private CommonServiceProcessingContract commonServiceProcessing;

	@Managed
	public User user() {

		User bean = User.T.create();
		bean.setId("internal");
		bean.setName("internal");

		for (String internalRole : internalRoles) {
			Role tfInternal = Role.T.create();
			tfInternal.setId(internalRole);
			tfInternal.setName(internalRole);
			bean.getRoles().add(tfInternal);
		}


		return bean;

	}

	@Managed
	public StandardUserSessionScoping userSessionScoping() {
		StandardUserSessionScoping bean = new StandardUserSessionScoping();
		bean.setDefaultUserSessionSupplier(userSessionProvider());
		bean.setRequestEvaluator(commonServiceProcessing.evaluator());
		bean.setUserSessionStack(currentUserAuthContext.userSessionStack());
		return bean;
	}

	@Managed
	public StaticUserSessionHolder userSessionProvider() {
		StaticUserSessionHolder bean = new StaticUserSessionHolder();
		bean.setUserSession(userSessionsService.initialUserSession(user()));
		return bean;
	}

	@Managed
	public Supplier<String> userSessionIdProvider() {
		SessionIdFromUserSessionProvider bean = new SessionIdFromUserSessionProvider();
		bean.setUserSessionProvider(userSessionProvider());
		return bean;
	}

	@Managed
	public Supplier<Set<String>> rolesProvider() {
		return () -> {return internalRoles;};
	}
}
