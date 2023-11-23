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
package com.braintribe.model.access.rdbms.deploy;

import javax.sql.DataSource;

import com.braintribe.model.access.sql.dialect.SqlDialect;
import com.braintribe.model.dbs.DbSchema;

import tribefire.extension.sqlaccess.model.RdbmsDriver;

/**
 * @author peter.gazdik
 */
public class RdbmsSchemaManager {

	private final DataSource dataSource;
	private final RdbmsDriver driverDenotation;
	private final SqlDialect sqlDialect;
	private RdbmsDdlExpert ddlExpert;

	public RdbmsSchemaManager(RdbmsDriver driverDenotation, DataSource dataSource, SqlDialect sqlDialect) {
		this.driverDenotation = driverDenotation;
		this.dataSource = dataSource;
		this.sqlDialect = sqlDialect;
	}

	public void ensureDatabase(String databaseName) {
		ddlExpert = new RdbmsDdlExpert(dataSource, databaseName, sqlDialect);

		if (!driverDenotation.getEnsureDatabase())
			return;

		try {
			ddlExpert.ensureDatabase();

		} catch (Exception e) {
			throw new RuntimeException("Error while ensuring database: " + databaseName, e);
		}
	}

	public void ensureTables(DbSchema dbSchema) {
		try {
			ddlExpert.ensureMetaTable();

			ddlExpert.ensureSchema(dbSchema);

		} catch (Exception e) {
			throw new RuntimeException("Error while ensuring tables.", e);
		}
	}
}
