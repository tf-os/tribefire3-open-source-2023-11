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

import com.braintribe.model.access.sql.SqlAccessDriver;
import com.braintribe.model.access.sql.dialect.SqlDialect;
import com.braintribe.model.dbs.DbSchema;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

import tribefire.extension.sqlaccess.model.RdbmsDriver;

/**
 * @author peter.gazdik
 */
public class RdbmsDriverDeployer {

	public static SqlAccessDriver deploy(RdbmsDriver denotation, DataSource dataSource) {
		return new RdbmsDriverDeployer(denotation, dataSource).deploy();
	}

	// --------------------------------------------------------------------------------------------

	private final RdbmsDriver denotation;
	private final DataSource dataSource;
	private final ModelOracle modelOracle;
	private final CmdResolver cmdResolver;

	private SqlDialect sqlDialect;

	private DbSchema dbSchema;

	private RdbmsDriverDeployer(RdbmsDriver denotation, DataSource dataSource) {
		this.denotation = denotation;
		this.dataSource = dataSource;
		this.modelOracle = new BasicModelOracle(denotation.getMetaModel());
		this.cmdResolver = new CmdResolverImpl(modelOracle);
	}

	private SqlAccessDriver deploy() {
		prepareSqlDialect();
		prepareDbSchema();

		initializeDbs();

		return newRdbmsDriver();
	}

	private void prepareSqlDialect() {
		sqlDialect = RdbmsSqlDialectProvider.provide(denotation);
	}

	private void prepareDbSchema() {
		dbSchema = RdbmsDbSchemaProvider.provide(cmdResolver, sqlDialect);
	}

	private void initializeDbs() {
		RdbmsSchemaManager schemaManager = new RdbmsSchemaManager(denotation, dataSource, sqlDialect);

		schemaManager.ensureDatabase(denotation.getDatabaseName());
		schemaManager.ensureTables(dbSchema);
	}

	private SqlAccessDriver newRdbmsDriver() {
		com.braintribe.model.access.rdbms.RdbmsDriver result = new com.braintribe.model.access.rdbms.RdbmsDriver();
		result.setDataSource(dataSource);
		result.setCmdResolver(cmdResolver);
		result.setSqlDialect(sqlDialect);

		return result;
	}

}
