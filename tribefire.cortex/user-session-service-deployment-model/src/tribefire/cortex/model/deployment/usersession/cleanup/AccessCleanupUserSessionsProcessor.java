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

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * {@link CleanupUserSessionsProcessor} based on an {@link IncrementalAccess}.
 * <p>
 * This one works with any access, but if the user sessions access is JDBC based, {@link JdbcCleanupUserSessionsProcessor} is better.
 * <p>
 * Not that the deployable does not need to reference the access, as the user sessions access is just one and known.
 * 
 * @author peter.gazdik
 */
public interface AccessCleanupUserSessionsProcessor extends CleanupUserSessionsProcessor {

	EntityType<AccessCleanupUserSessionsProcessor> T = EntityTypes.T(AccessCleanupUserSessionsProcessor.class);

}
