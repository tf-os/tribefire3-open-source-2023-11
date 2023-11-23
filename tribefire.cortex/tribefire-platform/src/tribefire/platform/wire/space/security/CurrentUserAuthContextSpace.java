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

import static com.braintribe.wire.api.util.Lists.list;

import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.model.processing.securityservice.commons.provider.RolesFromUserSessionProvider;
import com.braintribe.model.processing.securityservice.commons.provider.SessionIdFromUserSessionProvider;
import com.braintribe.model.processing.securityservice.commons.provider.UserIpAddressFromUserSessionProvider;
import com.braintribe.model.processing.securityservice.commons.provider.UserNameFromUserSessionProvider;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.thread.api.DeferringThreadContextScoping;
import com.braintribe.thread.impl.ThreadContextScopingImpl;
import com.braintribe.utils.collection.api.MinimalStack;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.wire.space.common.CurrentUserSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;

@Managed
public class CurrentUserAuthContextSpace implements WireSpace {

	protected InternalUserAuthContextSpace internalUserAuthContext;

	@Import
	private RpcSpace rpc;

	@Import
	private CurrentUserSpace currentUser;

	public Supplier<UserSession> userSessionSupplier() {
		return userSessionStack()::peek;
	}

	public MinimalStack<UserSession> userSessionStack() {
		return currentUser.userSessionStack();
	}
	
	@Managed
	public Supplier<String> userSessionIdProvider() {
		SessionIdFromUserSessionProvider bean = new SessionIdFromUserSessionProvider();
		bean.setUserSessionProvider(userSessionSupplier());
		return bean;
	}

	@Managed
	public Supplier<Set<String>> rolesProvider() {
		RolesFromUserSessionProvider bean = new RolesFromUserSessionProvider();
		bean.setUserSessionProvider(userSessionSupplier());
		return bean;
	}

	@Managed
	public Supplier<String> userNameProvider() {
		UserNameFromUserSessionProvider bean = new UserNameFromUserSessionProvider();
		bean.setUserSessionProvider(userSessionSupplier());
		return bean;
	}

	@Managed
	public Supplier<String> userIpProvider() {
		UserIpAddressFromUserSessionProvider bean = new UserIpAddressFromUserSessionProvider();
		bean.setUserSessionProvider(userSessionSupplier());
		return bean;
	}

	@Managed
	public DeferringThreadContextScoping threadContextScoping() {
		ThreadContextScopingImpl bean = new ThreadContextScopingImpl();
		bean.setScopeSuppliers(list(rpc.serviceRequestContextThreadContextScopeSupplier()));
		return bean;
	}
}
