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
package tribefire.extension.tracing.service;

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

import tribefire.extension.tracing.model.deployment.connector.TracingConnector;

public class HealthCheckProcessor implements CheckProcessor {

	private static final Logger logger = Logger.getLogger(HealthCheckProcessor.class);

	private Supplier<PersistenceGmSession> cortexSessionSupplier;
	private DeployRegistry deployRegistry;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {
		CheckResult response = CheckResult.T.create();

		PersistenceGmSession cortexSession = cortexSessionSupplier.get();

		//@formatter:off
		EntityQuery query = EntityQueryBuilder.from(TracingConnector.T)
				.where()
					.property(TracingConnector.deploymentStatus).eq(DeploymentStatus.deployed)
				.done();
		//@formatter:on
		List<TracingConnector> tracingConnectors = cortexSession.query().entities(query).list();

		List<CheckResultEntry> entries = response.getEntries();
		for (TracingConnector tracingConnector : tracingConnectors) {
			DeployedUnit unit = deployRegistry.resolve(tracingConnector);
			if (unit == null) {
				throw new IllegalStateException("'" + tracingConnector.entityType().getTypeName() + "' deployedUnit is null.");
			}
			tribefire.extension.tracing.connector.api.TracingConnector impl = unit.getComponent(TracingConnector.T);

			CheckResultEntry tracingConnectorHealth = impl.health();
			entries.add(tracingConnectorHealth);
		}

		logger.debug(() -> "Executed tracing health check");

		return response;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Required
	@Configurable
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionSupplier = cortexSessionSupplier;
	}

	@Configurable
	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}

}
