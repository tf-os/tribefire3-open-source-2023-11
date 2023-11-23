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
package com.braintribe.model.processing.deployment.hibernate.test.mapping.xmlusage;

import org.hibernate.dialect.DerbyTenFiveDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle10gDialect;

import com.braintribe.processing.test.db.derby.DerbyServerControl;

/**
 * <p>
 * Central point of database control/configuration for tests/labs.
 * 
 */
public abstract class DatabaseTest {

	public static boolean useInternallyManagedDerbyInstance = true;
	
	protected static String driver = "oracle.jdbc.OracleDriver";
	protected static String user = "braintribe";
	protected static String password = "1234";
	protected static String url = "jdbc:oracle:thin:@localhost:1521:xe";
	protected static Class<? extends Dialect> dialect = Oracle10gDialect.class;

	private static final String derbyDriver = "org.apache.derby.jdbc.ClientDriver";
	private static final String derbyUser = "cortex";
	private static final String derbyPassword = "cortex";
	private static final String derbyUrl = "jdbc:derby://localhost:1527/res/db/hibernateAccessTest;create=true";
	private static final Class<? extends Dialect> derbyDialect = DerbyTenFiveDialect.class;
	

	private static DerbyServerControl derbyServerControl = null;

	public static void initializeDatabaseContext() throws Exception {
		
		if (useInternallyManagedDerbyInstance) {
			driver = derbyDriver;
			user = derbyUser;
			password = derbyPassword;
			url = derbyUrl;
			dialect = derbyDialect;
			derbyServerControl = new DerbyServerControl();
			derbyServerControl.start();
		}


	}

	public static void destroyDatabaseContext() throws Exception {
		if (derbyServerControl != null) {
			derbyServerControl.stop();
		}
	}

}
