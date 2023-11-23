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
package com.braintribe.product.rat.imp.impl.deployable;

import com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.connector.MssqlConnectionDescriptor;
import com.braintribe.model.deployment.database.connector.MssqlDriver;
import com.braintribe.model.deployment.database.connector.MssqlVersion;
import com.braintribe.model.deployment.database.connector.OracleConnectionDescriptor;
import com.braintribe.model.deployment.database.connector.OracleVersion;
import com.braintribe.model.deployment.database.pool.ConfiguredDatabaseConnectionPool;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.lcd.CommonTools;

/**
 * A {@link BasicDeployableImp} specialized in {@link ConfiguredDatabaseConnectionPool}
 */
public class ConnectionImp<I extends ConfiguredDatabaseConnectionPool> extends BasicDeployableImp<I> {

	public ConnectionImp(PersistenceGmSession session, I connector) {
		super(session, connector);
	}

	public ConnectionImp<I> addOracleDescriptor(String user, String password, String dbName, String host, String service, int port,
			OracleVersion version) throws GmSessionException {

		logger.info("Adding oracle descriptor " + CommonTools.getParametersString("User", user, "Password", password) + " and "
				+ CommonTools.getParametersString("Service", service, "DB", dbName, "Port", port));

		OracleConnectionDescriptor oracleConnectionDescriptor = session().create(OracleConnectionDescriptor.T);
		oracleConnectionDescriptor.setUser(user);
		oracleConnectionDescriptor.setPassword(password);
		oracleConnectionDescriptor.setSid(service);
		oracleConnectionDescriptor.setHost(host);
		oracleConnectionDescriptor.setPort(port);
		oracleConnectionDescriptor.setVersion(version);
		this.instance.setConnectionDescriptor(oracleConnectionDescriptor);

		return this;
	}

	public ConnectionImp<I> addMssqlDescriptor(String user, String password, String dbName, MssqlDriver driver, String host, String instance,
			int port, MssqlVersion version) throws GmSessionException {
		logger.info("Adding mssql descriptor " + CommonTools.getParametersString("User", user, "Password", password) + " and "
				+ CommonTools.getParametersString("Instance", instance, "DB", dbName, "Port", port));

		MssqlConnectionDescriptor mssqlConnectionDescriptor = session().create(MssqlConnectionDescriptor.T);
		mssqlConnectionDescriptor.setUser(user);
		mssqlConnectionDescriptor.setPassword(password);
		mssqlConnectionDescriptor.setDatabase(dbName);
		mssqlConnectionDescriptor.setDriver(driver);
		mssqlConnectionDescriptor.setHost(host);
		mssqlConnectionDescriptor.setInstance(instance);
		mssqlConnectionDescriptor.setPort(port);
		mssqlConnectionDescriptor.setVersion(version);
		this.instance.setConnectionDescriptor(mssqlConnectionDescriptor);
		return this;
	}

	public ConnectionImp<I> addGenericDescriptor(String user, String password, String url, String driver) throws GmSessionException {
		logger.info("Adding oracle descriptor " + CommonTools.getParametersString("User", user, "Password", password) + " and "
				+ CommonTools.getParametersString("Url", url, "Driver", driver));

		GenericDatabaseConnectionDescriptor jdbcConnectionDescriptor = session().create(GenericDatabaseConnectionDescriptor.T);
		jdbcConnectionDescriptor.setUser(user);
		jdbcConnectionDescriptor.setPassword(password);
		jdbcConnectionDescriptor.setUrl(url);
		jdbcConnectionDescriptor.setDriver(driver);
		instance.setConnectionDescriptor(jdbcConnectionDescriptor);

		return this;
	}

}
