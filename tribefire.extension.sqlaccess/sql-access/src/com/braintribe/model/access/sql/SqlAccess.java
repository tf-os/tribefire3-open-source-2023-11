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
package com.braintribe.model.access.sql;

import com.braintribe.cfg.Required;
import com.braintribe.model.access.AccessBase;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.sql.manipulation.SqlManipulationApplicator;
import com.braintribe.model.access.sql.manipulation.SqlManipulationReportImpl;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.support.QueryAdaptingTools;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

/**
 * @author peter.gazdik
 */
public class SqlAccess extends AccessBase {

	private GmMetaModel metaModel;
	private CmdResolver cmdResolver;
	private ModelOracle modelOracle;
	private SqlAccessDriver sqlAccessDriver;

	@Required
	public void setSqlAccessDriver(SqlAccessDriver sqlAccessDriver) {
		this.sqlAccessDriver = sqlAccessDriver;
		this.cmdResolver = sqlAccessDriver.getCmdResolver();
		this.modelOracle = cmdResolver.getModelOracle();
		this.metaModel = modelOracle.getGmMetaModel();
	}

	@Override
	public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
		return sqlAccessDriver.query(query);
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery entityQuery) throws ModelAccessException {
		return QueryAdaptingTools.queryEntities(entityQuery, this);
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery propertyQuery) throws ModelAccessException {
		return QueryAdaptingTools.queryProperties(propertyQuery, this);
	}

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
		SqlManipulationReportImpl report = SqlManipulationApplicator.appy(this, manipulationRequest.getManipulation());

		return sqlAccessDriver.applyManipulation(report);		
	}

	@Override
	public GmMetaModel getMetaModel() throws GenericModelException {
		return metaModel;
	}

}
