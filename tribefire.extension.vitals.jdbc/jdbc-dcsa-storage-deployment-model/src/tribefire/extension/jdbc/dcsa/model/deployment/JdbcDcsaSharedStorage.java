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
package tribefire.extension.jdbc.dcsa.model.deployment;

import com.braintribe.model.dcsadeployment.DcsaSharedStorage;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface JdbcDcsaSharedStorage extends DcsaSharedStorage {

	EntityType<JdbcDcsaSharedStorage> T = EntityTypes.T(JdbcDcsaSharedStorage.class);

	void setProject(String project);
	String getProject();

	DatabaseConnectionPool getConnectionPool();
	void setConnectionPool(DatabaseConnectionPool connectionPool);

	void setAutoUpdateSchema(Boolean autoUpdateSchema);
	Boolean getAutoUpdateSchema();

	void setParallelFetchThreads(Integer parallelFetchThreads);
	Integer getParallelFetchThreads();

	// TODO remove
	// void setUrl(String url);
	// String getUrl();
	//
	// void setDriver(String driver);
	// String getDriver();
	//
	// void setUsername(String username);
	// String getUsername();
	//
	// void setPassword(String password);
	// String getPassword();

}
