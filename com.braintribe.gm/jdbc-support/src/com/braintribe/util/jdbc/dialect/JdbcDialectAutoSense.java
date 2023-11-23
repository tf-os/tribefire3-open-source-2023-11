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
package com.braintribe.util.jdbc.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.regex.Matcher;

import javax.sql.DataSource;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.util.jdbc.dialect.JdbcDialectMappings.JdbcDialectMapping;

/**
 * @author peter.gazdik
 */
public class JdbcDialectAutoSense {

	private static final Logger log = Logger.getLogger(JdbcDialectAutoSense.class);

	public static String getProductNameAndVersion(DataSource dataSource) {
		try (Connection c = dataSource.getConnection()) {
			DatabaseMetaData dmd = c.getMetaData();
			String dName = dmd.getDriverName();
			String dVersion = dmd.getDriverVersion();
			int dMajorVersion = dmd.getDriverMajorVersion();
			int dMinorVersion = dmd.getDriverMinorVersion();
			String pName = dmd.getDatabaseProductName();
			String pVersion = dmd.getDatabaseProductVersion();

			log.debug("Database driver: " + dName + ", version: " + dVersion + " (major: " + dMajorVersion + ",minor: " + dMinorVersion + ")");
			log.debug("Database used: " + pName + ", version: " + pVersion);

			String nameAndVersion = pName + " Version:" + pVersion;

			return nameAndVersion.replaceAll("\n", " ");

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not get database metadata information.");
		}
	}
	
	/* package */ static JdbcDialect findJdbcDialect(DataSource dataSource) {
		return autoSenseDialect(getProductNameAndVersion(dataSource));
	}

	private static JdbcDialect autoSenseDialect(String productNameAndVersion) {
		for (JdbcDialectMapping mapping : JdbcDialectMappings.dialectMappings())
			if (matches(mapping, productNameAndVersion))
				return logEndReturnDialect(mapping);

		return JdbcDialect.defaultDialect();
	}

	private static boolean matches(JdbcDialectMapping mapping, String productNameAndVersion) {
		Matcher m = mapping.pattern.matcher(productNameAndVersion);
		boolean matches = m.matches();
		return matches;
	}

	private static JdbcDialect logEndReturnDialect(JdbcDialectMapping mapping) {
		String variant = mapping.dialect.dbVariant();
		String dialect = mapping.dialect.hibernateDialect();

		log.debug("Auto-sensed variant " + variant + ", hibernate dialect " + dialect);

		return mapping.dialect;
	}

}
