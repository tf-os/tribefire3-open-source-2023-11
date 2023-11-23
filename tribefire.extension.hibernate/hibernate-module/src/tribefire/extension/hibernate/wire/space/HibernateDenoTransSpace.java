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
package tribefire.extension.hibernate.wire.space;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.accessdeployment.hibernate.HibernateEnhancedConnectionPool;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.model.deployment.usersession.cleanup.JdbcCleanupUserSessionsProcessor;
import tribefire.cortex.model.deployment.usersession.service.JdbcUserSessionService;
import tribefire.module.api.DenotationTransformationContext;
import tribefire.module.api.DenotationTransformerRegistry;
import tribefire.module.wire.contract.WebPlatformHardwiredExpertsContract;

@Managed
public class HibernateDenoTransSpace implements WireSpace {

	@Import
	private WebPlatformHardwiredExpertsContract hardwiredExperts;

	public void bind() {
		DenotationTransformerRegistry registry = hardwiredExperts.denotationTransformationRegistry();

		registry.registerStandardMorpher(DatabaseConnectionPool.T, HibernateAccess.T, this::dbConnectionPoolToHibernateAccess);
		registry.registerStandardMorpher(HibernateAccess.T, JdbcUserSessionService.T, this::accessToJdbcUserSessionService);
		registry.registerStandardMorpher(HibernateAccess.T, JdbcCleanupUserSessionsProcessor.T, this::accessToJdbcCleanupUserSessionsProcessor);
	}

	private Maybe<HibernateAccess> dbConnectionPoolToHibernateAccess(DenotationTransformationContext context, DatabaseConnectionPool pool) {
		HibernateAccess hibernateAccess = context.create(HibernateAccess.T);
		hibernateAccess.setConnector(pool);

		return Maybe.complete(hibernateAccess);
	}

	private Maybe<JdbcUserSessionService> accessToJdbcUserSessionService(DenotationTransformationContext context, HibernateAccess access) {
		JdbcUserSessionService service = context.create(JdbcUserSessionService.T);
		service.setConnectionPool(acquireConnectionPoolForAccess(context, access));

		return Maybe.complete(service);
	}

	private Maybe<JdbcCleanupUserSessionsProcessor> accessToJdbcCleanupUserSessionsProcessor(DenotationTransformationContext context,
			HibernateAccess access) {

		JdbcCleanupUserSessionsProcessor service = context.create(JdbcCleanupUserSessionsProcessor.T);
		service.setConnectionPool(acquireConnectionPoolForAccess(context, access));

		return Maybe.complete(service);
	}

	private HibernateEnhancedConnectionPool acquireConnectionPoolForAccess(DenotationTransformationContext context, HibernateAccess access) {
		String id = "connection-pool:hibernate-enhanced/" + access.getExternalId();

		HibernateEnhancedConnectionPool connectionPool = context.findEntityByGlobalId(id);
		if (connectionPool == null) {
			connectionPool = context.create(HibernateEnhancedConnectionPool.T);
			connectionPool.setGlobalId(id);
			connectionPool.setExternalId(id);
			connectionPool.setHibernateComponent(access);
		}
		return connectionPool;
	}

}
