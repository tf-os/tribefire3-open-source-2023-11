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
package com.braintribe.model.access.sql.main;

import java.io.File;
import java.io.IOException;

import javax.sql.DataSource;

import com.braintribe.model.access.rdbms.deploy.RdbmsDriverDeployer;
import com.braintribe.model.access.sql.SqlAccess;
import com.braintribe.model.access.sql.SqlAccessDriver;
import com.braintribe.model.access.sql.main.assembly.RdbmsAssemblyProvider;
import com.braintribe.model.access.sql.test.base.SqlAccessTestTools;
import com.braintribe.model.deployment.database.pool.HikariCpConnectionPool;
import com.braintribe.utils.FileTools;

import tribefire.extension.sqlaccess.model.RdbmsDriver;

/**
 * @author peter.gazdik
 */
public class RdbmsMain {

	public static void main(String[] args) throws Exception {
		removeDbFolder();

		RdbmsDriver sqlDriverDenotation = RdbmsAssemblyProvider.provide();
		DataSource dataSource = SqlAccessTestTools.deploy((HikariCpConnectionPool) sqlDriverDenotation.getConnectionPool());
		SqlAccessDriver sqlAccessDriver = RdbmsDriverDeployer.deploy(sqlDriverDenotation, dataSource);

		SqlAccess sqlAccess = new SqlAccess();
		sqlAccess.setSqlAccessDriver(sqlAccessDriver);
		
		System.out.println("Rdbms sqlAccessDriver ready to be used:" + sqlAccessDriver);
	}

	private static void removeDbFolder() throws IOException {
		File file = new File("res/main/RdbmsMain");
		if (!file.isDirectory())
			return;

		System.out.println("Deleting folder: " + file.getAbsolutePath());
		FileTools.deleteDirectoryRecursively(file);
	}
	
}
