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
package com.braintribe.model.processing.deployment.expert.connectionpool;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.regex.Matcher;

import javax.sql.DataSource;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.StringTools;

/**
 * This class holds the list of {@link DialectMapping}s. It's main purpose is to
 * increase the expressiveness of the configuration (for EM).
 * 
 * @author michael.lafite
 * 
 * @param <D> The dialect type
 */
public class DialectAutoSense<D> {

	private static Logger logger = Logger.getLogger(DialectAutoSense.class);

	private List<DialectMapping> dialectMappings;

	public List<DialectMapping> getDialectMappings() {
		return this.dialectMappings;
	}

	@Required
	public void setDialectMappings(List<DialectMapping> dialectMappings) {
		this.dialectMappings = dialectMappings;
	}

	public Class<? extends D> senseDialect(DataSource connectionPool) {
		try (Connection c = connectionPool.getConnection()) {
			DatabaseMetaData dmd = c.getMetaData();
			String driverName = dmd.getDriverName();
			String driverVersion = dmd.getDriverVersion();
			int driverMajorVersion = dmd.getDriverMajorVersion();
			int driverMinorVersion = dmd.getDriverMinorVersion();
			String productName = dmd.getDatabaseProductName();
			String productVersion = dmd.getDatabaseProductVersion();
			logger.debug("Database driver: " + driverName + ", version: " + driverVersion + " (major: "
					+ driverMajorVersion + ",minor: " + driverMinorVersion + ")");
			logger.debug("Database used: " + productName + ", version: " + productVersion);

			return this.autoSenseDialect(productName + " Version:" + productVersion);

		} catch (Exception e) {
			throw new IllegalStateException("Could not get database metadata information.", e);
		}
	}
	@SuppressWarnings("unchecked")
	protected Class<? extends D> autoSenseDialect(String productNameAndVersion) throws Exception {

		if (StringTools.isEmpty(productNameAndVersion)) {
			throw new Exception(
					"The auto-sense feature of the connection pool does not work. Could not determine the database product name.");
		}

		productNameAndVersion = productNameAndVersion.replaceAll("\n", " ");

		if (!CollectionTools.isEmpty(dialectMappings)) {
			for (DialectMapping mapping : dialectMappings) {
				Matcher m = mapping.getProductMatcher().matcher(productNameAndVersion);
				if (m.matches()) {
					String variant = mapping.getVariant();
					String dialect = mapping.getDialect();

					logger.debug("Auto-sensed variant " + variant + " with dialect " + dialect);

						try {
							return (Class<? extends D>) this.getClass().getClassLoader().loadClass(dialect);
						} catch (Exception e) {
							throw new Exception("Cannot load dialect class " + dialect, e);
						}
					}
				}
		}

		throw new Exception("The auto-sense feature of the connection pool does not work. No pattern for database "+ productNameAndVersion + " configured.");
	}

	public static String getBuildVersion() {

		return "$Build_Version$ $Id: DialectAutoSense.java 96852 2017-02-16 19:48:29Z andre.goncalves $";
	}
}
