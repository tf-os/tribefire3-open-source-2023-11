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
package tribefire.platform.wire.space.common;

import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.model.processing.securityservice.commons.provider.SessionIdFromUserSessionProvider;
import com.braintribe.model.processing.service.common.context.UserSessionStack;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.rpc.GmWebRpcClientMetaDataProvider;

@Managed
public class CurrentUserSpace implements WireSpace {
	
	public Supplier<UserSession> userSessionProvider() {
		return userSessionStack();
	}

	@Managed
	public UserSessionStack userSessionStack() {
		UserSessionStack bean = new UserSessionStack();
		
		return bean;
	}

	@Managed
	public Supplier<String> userSessionIdProvider() {
		SessionIdFromUserSessionProvider bean = new SessionIdFromUserSessionProvider();
		bean.setUserSessionProvider(userSessionStack());
		return bean;
	}

	@Managed
	public Supplier<Map<String, Object>> clientMetaDataProvider() {
		GmWebRpcClientMetaDataProvider bean = new GmWebRpcClientMetaDataProvider();
		bean.setSessionIdProvider(userSessionIdProvider());
		bean.setIncludeNdc(true);
		bean.setIncludeNodeId(true);
		bean.setIncludeThreadName(true);
		return bean;
	}

}
