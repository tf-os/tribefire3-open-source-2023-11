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
package tribefire.extension.sqlaccess.model;

import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface RdbmsDriver extends SqlAccessDriver {

	EntityType<RdbmsDriver> T = EntityTypes.T(RdbmsDriver.class);

	String getDatabaseName();
	void setDatabaseName(String value);

	DatabaseConnectionPool getConnectionPool();
	void setConnectionPool(DatabaseConnectionPool databaseConnectionPool);

	/**
	 * Information for the deployment expert whether it should also try to create the database. If set to false
	 * (default), the expert expects the DB to already exist.
	 * 
	 * Creating a DB on demand is convenient, but not every RDBMS supports it.
	 * 
	 * PGA: Let's see if this will make sense later.
	 */
	boolean getEnsureDatabase();
	void setEnsureDatabase(boolean ensureDatabase);

}
