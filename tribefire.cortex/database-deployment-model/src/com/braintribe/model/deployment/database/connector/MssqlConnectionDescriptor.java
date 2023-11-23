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
package com.braintribe.model.deployment.database.connector;

import java.util.Map;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface MssqlConnectionDescriptor extends DatabaseConnectionDescriptor {

	EntityType<MssqlConnectionDescriptor> T = EntityTypes.T(MssqlConnectionDescriptor.class);

	String getHost();
	void setHost(String host);

	@Initializer("1433")
	Integer getPort();
	void setPort(Integer port);

	String getDatabase();
	void setDatabase(String database);

	String getInstance();
	void setInstance(String instance);

	MssqlVersion getVersion();
	void setVersion(MssqlVersion version);

	@Initializer("enum(com.braintribe.model.deployment.database.connector.MssqlDriver,MicrosoftJdbc4Driver)")
	MssqlDriver getDriver();
	void setDriver(MssqlDriver driver);

	Map<String, String> getProperties();
	void setProperties(Map<String, String> properties);

	@Override
	default String describeConnection() {
		StringBuilder sb = new StringBuilder();
		sb.append(getHost());
		if (getPort() != null)
			sb.append(":" + getPort());
		return sb.toString();
	}
}
