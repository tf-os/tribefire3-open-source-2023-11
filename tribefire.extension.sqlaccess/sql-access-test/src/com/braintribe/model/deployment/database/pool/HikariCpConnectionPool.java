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
package com.braintribe.model.deployment.database.pool;

import java.util.List;

import com.braintribe.model.deployment.database.JdbcTransactionIsolationLevel;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

// Copy of extracted Hikari entity - to make sure this artifact compiles
// The SQL access should be extracted to an extension too, later, that one can depend on hikari extension for tests
public interface HikariCpConnectionPool extends ConfiguredDatabaseConnectionPool {

	EntityType<HikariCpConnectionPool> T = EntityTypes.T(HikariCpConnectionPool.class);

	@Initializer("60000")
	Integer getCheckoutTimeout();
	void setCheckoutTimeout(Integer checkoutTimeout);

	@Initializer("3")
	Integer getMaxPoolSize();
	void setMaxPoolSize(Integer maxPoolSize);

	@Initializer("1")
	Integer getMinPoolSize();
	void setMinPoolSize(Integer minPoolSize);

	String getPreferredTestQuery();
	void setPreferredTestQuery(String preferredTestQuery);

	Integer getMaxStatements();
	void setMaxStatements(Integer maxStatements);

	@Initializer("60000")
	Integer getMaxIdleTime();
	void setMaxIdleTime(Integer maxIdleTime);

	@Initializer("10")
	Integer getLoginTimeout();
	void setLoginTimeout(Integer loginTimeout);

	String getDataSourceName();
	void setDataSourceName(String dataSourceName);

	List<String> getInitialStatements();
	void setInitialStatements(List<String> initialStatements);

	@Initializer("true")
	boolean getEnableJmx();
	void setEnableJmx(boolean enableJmx);

	@Initializer("true")
	boolean getEnableMetrics();
	void setEnableMetrics(boolean enableMetrics);

	@Initializer("60000")
	Integer getValidationTimeout();
	void setValidationTimeout(Integer validationTimeout);

	Integer getInitializationFailTimeout();
	void setInitializationFailTimeout(Integer initializationFailTimeout);

	String getSchema();
	void setSchema(String schema);

	JdbcTransactionIsolationLevel getTransactionIsolationLevel();
	void setTransactionIsolationLevel(JdbcTransactionIsolationLevel transactionIsolationLevel);

}
