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
package com.braintribe.model.processing.check.jdbc;

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

public class DatabaseConnectionsCheck extends ParallelDatabaseConnectionCheckProcessor implements CheckProcessor {

	private static Logger logger = Logger.getLogger(DatabaseConnectionsCheck.class);

	private Supplier<PersistenceGmSession> cortexSessionFactory;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest checkRequest) {
		EntityQuery query = EntityQueryBuilder.from(DatabaseConnectionPool.T) //
				.where().property(Deployable.deploymentStatus).eq(DeploymentStatus.deployed) //
				.done();

		PersistenceGmSession session = cortexSessionFactory.get();
		List<DatabaseConnectionPool> cpList = session.query().entities(query).list();
		logger.debug(() -> "Found " + (cpList != null ? cpList.size() : "0") + " deployed connection pools to check.");

		return super.performCheck(cpList);
	}

	@Configurable
	public void setCortexSessionFactory(Supplier<PersistenceGmSession> cortexSessionFactory) {
		this.cortexSessionFactory = cortexSessionFactory;
	}

}
