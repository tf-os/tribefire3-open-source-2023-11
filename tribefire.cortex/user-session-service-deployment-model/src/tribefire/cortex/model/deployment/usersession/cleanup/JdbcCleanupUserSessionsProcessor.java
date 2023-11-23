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
package tribefire.cortex.model.deployment.usersession.cleanup;

import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * {@link CleanupUserSessionsProcessor} that based on JDBC, i.e. an optimized implementation to go alongside a JDBC based user sessions access
 * (currently that means that one is a HibernateAccess).
 * <p>
 * The implementation assumes the DB schema already exists and does not create one.
 * 
 * @author peter.gazdik
 */
public interface JdbcCleanupUserSessionsProcessor extends CleanupUserSessionsProcessor {

	EntityType<JdbcCleanupUserSessionsProcessor> T = EntityTypes.T(JdbcCleanupUserSessionsProcessor.class);

	DatabaseConnectionPool getConnectionPool();
	void setConnectionPool(DatabaseConnectionPool connectionPool);

}
