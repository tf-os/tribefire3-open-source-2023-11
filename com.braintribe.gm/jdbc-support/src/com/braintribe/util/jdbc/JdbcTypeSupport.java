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
package com.braintribe.util.jdbc;

import java.sql.Connection;

import javax.sql.DataSource;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.util.jdbc.dialect.JdbcDialect;
import com.braintribe.utils.IOTools;

public class JdbcTypeSupport {

	private static final Logger logger = Logger.getLogger(JdbcTypeSupport.class);

	protected static String getDatabaseSpecificType(String dbName, String typeName, String defaultValue) {
		String key = "TRIBEFIRE_JDBC_TYPE_" + typeName.toUpperCase() + "_" + dbName.toUpperCase().replace(' ', '_');
		String type = TribefireRuntime.getProperty(key);
		logger.debug(
				() -> "Specific mapping for type " + typeName + " with " + dbName + " (" + key + "): " + type + " (default: " + defaultValue + ")");
		if (type == null) {
			return defaultValue;
		}
		return type;
	}

	public static DatabaseTypes getDatabaseTypes(DataSource dataSource) {

		final String dbName = getDatabaseProductName(dataSource);
		logger.debug(() -> "Identified database: " + dbName);

		String clobType = "CLOB";
		if (dbName != null) {
			final String lowerCaseDbName = dbName.toLowerCase();
			if ((lowerCaseDbName.contains("mysql") || (lowerCaseDbName.contains("mariadb")))) {
				clobType = "LONGTEXT";
			} else if (lowerCaseDbName.contains("postgresql") || (lowerCaseDbName.contains("microsoft sql"))) {
				clobType = "TEXT";
			}
			clobType = getDatabaseSpecificType(dbName, "CLOB", clobType);
		}

		String blobType = "BLOB";
		if (dbName != null) {
			final String lowerCaseDbName = dbName.toLowerCase();
			if ((lowerCaseDbName.contains("mysql") || (lowerCaseDbName.contains("mariadb")))) {
				blobType = "MEDIUMBLOB";
			} else if (lowerCaseDbName.contains("postgresql")) {
				blobType = "oid";
			} else if (lowerCaseDbName.contains("microsoft sql")) {
				blobType = "IMAGE";
			}
			blobType = getDatabaseSpecificType(dbName, "BLOB", blobType);
		}

		String timestampType = "TIMESTAMP";
		if (dbName != null) {
			final String lowerCaseDbName = dbName.toLowerCase();
			if (lowerCaseDbName.contains("microsoft sql")) {
				timestampType = "DATETIME2";
			}
			timestampType = getDatabaseSpecificType(dbName, "TIMESTAMP", timestampType);
		}

		DatabaseTypes types = new DatabaseTypes(dbName, clobType, blobType, timestampType);
		logger.debug(() -> types.toString());

		return types;
	}

	public static JdbcDialect getJdbcDialect(DataSource dataSource) {
		return JdbcDialect.detectDialect(dataSource);
	}

	public static String getDatabaseProductName(DataSource dataSource) {
		Connection connection = null;

		try {
			connection = dataSource.getConnection();
			return connection.getMetaData().getDatabaseProductName();
		} catch (Exception e) {
			throw new RuntimeException("Could not get information about the database.", e);
		} finally {
			IOTools.closeCloseable(connection, logger);
		}
	}
}
