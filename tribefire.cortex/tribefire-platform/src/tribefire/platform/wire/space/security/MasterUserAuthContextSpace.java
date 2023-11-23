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
package tribefire.platform.wire.space.security;

import java.util.function.Supplier;

import com.braintribe.model.processing.securityservice.commons.provider.SessionIdFromUserSessionProvider;
import com.braintribe.model.processing.securityservice.commons.provider.StaticUserSessionHolder;
import com.braintribe.model.processing.securityservice.commons.scope.StandardUserSessionScoping;
import com.braintribe.model.user.User;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.MasterUserAuthContextContract;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.security.services.UserSessionServiceSpace;

@Managed
public class MasterUserAuthContextSpace implements MasterUserAuthContextContract {

	@Import
	private RpcSpace rpc;

	@Import
	private UserSessionServiceSpace userSessionsService;

	@Import
	private CurrentUserAuthContextSpace currentUserAuthContext;

	@Managed
	public User user() {
		User bean = User.T.create();
		bean.setId("master");
		bean.setName("master");
		return bean;
	}

	@Override
	@Managed
	public StandardUserSessionScoping userSessionScoping() {
		StandardUserSessionScoping bean = new StandardUserSessionScoping();
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		bean.setDefaultUserSessionSupplier(userSessionProvider());
		bean.setUserSessionStack(currentUserAuthContext.userSessionStack());
		return bean;
	}

	@Managed
	public StaticUserSessionHolder userSessionProvider() {
		StaticUserSessionHolder bean = new StaticUserSessionHolder();
		bean.setUserSession(userSessionsService.internalUserSession(user()));
		return bean;
	}

	@Managed
	public Supplier<String> userSessionIdProvider() {
		SessionIdFromUserSessionProvider bean = new SessionIdFromUserSessionProvider();
		bean.setUserSessionProvider(userSessionProvider());
		return bean;
	}

}
