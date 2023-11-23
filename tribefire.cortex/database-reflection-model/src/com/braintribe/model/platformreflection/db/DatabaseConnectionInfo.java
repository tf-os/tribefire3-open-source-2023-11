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
package com.braintribe.model.platformreflection.db;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface DatabaseConnectionInfo extends GenericEntity {

	EntityType<DatabaseConnectionInfo> T = EntityTypes.T(DatabaseConnectionInfo.class);

	String getName();
	void setName(String name);

	String getConnectionDescription();
	void setConnectionDescription(String connectionDescription);

	Long getConnectionTimeout();
	void setConnectionTimeout(Long connectionTimeout);

	Long getIdleTimeout();
	void setIdleTimeout(Long idleTimeout);

	Long getMaxLifetime();
	void setMaxLifetime(Long maxLifetime);

	Integer getMinimumPoolSize();
	void setMinimumPoolSize(Integer minimumPoolSize);

	Integer getMaximumPoolSize();
	void setMaximumPoolSize(Integer maximumPoolSize);

	DatabaseConnectionPoolMetrics getMetrics();
	void setMetrics(DatabaseConnectionPoolMetrics metrics);
}
