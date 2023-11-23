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
package com.braintribe.model.processing.securityservice.usersession.basic.test.wire.space;

import java.util.function.Supplier;

import com.braintribe.common.MutuallyExclusiveReadWriteLock;
import com.braintribe.gm.model.usersession.PersistenceUserSession;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.securityservice.api.UserSessionService;
import com.braintribe.model.processing.securityservice.usersession.service.AccessUserSessionService;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class AccessBasedTestSpace extends BaseTestSpace {

	@Override
	public UserSessionService userSessionService() {
		return incrementalAccessService();
	}

	@Managed
	private UserSessionService incrementalAccessService() {
		AccessUserSessionService bean = new AccessUserSessionService();

		bean.setPersistenceUserSessionGmSessionProvider(this::lowLevelSession);
		bean.setSessionIdProvider(userSessionIdFactory());
		bean.setDefaultUserSessionMaxIdleTime(defaultMaxIdleTime());

		return bean;
	}

	private PersistenceGmSession lowLevelSession() {
		BasicPersistenceGmSession bean = new BasicPersistenceGmSession();
		bean.setIncrementalAccess(rawAccess());
		return bean;
	}

	@Managed
	private IncrementalAccess rawAccess() {
		Smood bean = new Smood(new MutuallyExclusiveReadWriteLock());
		bean.setAccessId("user-sessions");
		return bean;
	}

	@Managed
	private Supplier<GmMetaModel> metaModelProvider() {
		return () -> PersistenceUserSession.T.getModel().getMetaModel();
	}

}
