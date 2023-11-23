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
package com.braintribe.model.access.rdbms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.braintribe.cfg.Required;
import com.braintribe.model.access.rdbms.context.RdbmsManipulationContext;
import com.braintribe.model.access.rdbms.manipulation.RdbmsManipulationApplicator;
import com.braintribe.model.access.sql.SqlAccessDriver;
import com.braintribe.model.access.sql.SqlManipulationReport;
import com.braintribe.model.access.sql.dialect.SqlDialect;
import com.braintribe.model.access.sql.query.analysis.JdbcQueryAnalyzer;
import com.braintribe.model.access.sql.query.eval.EvalJdbcQueryPlan;
import com.braintribe.model.access.sql.query.oracle.JdbcQuery;
import com.braintribe.model.access.sql.query.planner.JdbcPlannerContext;
import com.braintribe.model.access.sql.query.planner.JdbcQueryPlanner;
import com.braintribe.model.access.sql.tools.SqlMappingExpert;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.support.QueryResultBuilder;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.sql.plan.JdbcQueryPlan;

/**
 * @author peter.gazdik
 */
public class RdbmsDriver implements SqlAccessDriver, RdbmsManipulationContext {

	protected DataSource dataSource;
	protected ModelOracle modelOracle;
	protected CmdResolver cmdResolver;
	protected SqlMappingExpert sqlMappingExpert;
	protected SqlDialect sqlDialect;

	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Required
	public void setCmdResolver(CmdResolver cmdResolver) {
		this.modelOracle = cmdResolver.getModelOracle();
		this.cmdResolver = cmdResolver;
		this.sqlMappingExpert = new SqlMappingExpert(cmdResolver);
	}

	@Override
	public CmdResolver getCmdResolver() {
		return cmdResolver;
	}

	@Required
	public void setSqlDialect(SqlDialect sqlDialect) {
		this.sqlDialect = sqlDialect;
	}

	@Override
	public SelectQueryResult query(SelectQuery selectQuery) {
		JdbcQuery query = JdbcQueryAnalyzer.analyze(selectQuery);

		JdbcPlannerContext context = null; // TODO
		JdbcQueryPlan queryPlan = JdbcQueryPlanner.plan(query, context);

		//
		EvalJdbcQueryPlan evalPlan = null/* TODO SOMETHING */;
		// TODO think if we really want to use the QueryResultBuilder and TupleSets here...
		SelectQueryResult result = QueryResultBuilder.buildQueryResult(evalPlan.getTuples(), evalPlan.getResultSize());

		return result;
	}

	@Override
	public ManipulationResponse applyManipulation(SqlManipulationReport localReport) {
		return new RdbmsManipulationApplicator(this, localReport).apply();
	}

	// ########################################################
	// ## . . . . . . RdbmsDriverContext methods . . . . . . ##
	// ########################################################

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public SqlMappingExpert getSqlMappingExpert() {
		return sqlMappingExpert;
	}

	@Override
	public SqlDialect getSqlDialect() {
		return sqlDialect;
	}

	Map<EntityType<?>, List<Property>> directProperties = new ConcurrentHashMap<>();

	@Override
	public List<Property> getDirectvaluePropertiesFor(EntityType<?> entityType) {
		return directProperties.computeIfAbsent(entityType, this::computeDirectValueProperties);
	}

	private List<Property> computeDirectValueProperties(EntityType<?> entityType) {
		return entityType.getProperties().stream() //
				.filter(RdbmsDriver::isDirectValueProperty) //
				.collect(Collectors.toList()); //
	}

	private static boolean isDirectValueProperty(Property p) {
		if (p.isIdentifier())
			return true;

		if (p.isGlobalId())
			return false;

		GenericModelType type = p.getType();
		return type.isScalar() || type.isEntity();
	}

}
