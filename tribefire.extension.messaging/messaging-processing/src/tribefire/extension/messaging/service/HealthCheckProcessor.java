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
package tribefire.extension.messaging.service;

import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class HealthCheckProcessor implements CheckProcessor {

	private static final Logger logger = Logger.getLogger(HealthCheckProcessor.class);

	private Supplier<PersistenceGmSession> cortexSessionSupplier;
	private DeployRegistry deployRegistry;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {
		CheckResult response = CheckResult.T.create();

		PersistenceGmSession cortexSession = cortexSessionSupplier.get();

	/*	//@formatter:off TODO Would probably not be further used @dmiex check when testing HealthCheck task: https://document-one.atlassian.net/browse/D1-3311
		EntityQuery query = EntityQueryBuilder.from(MessagingConnectorDeployable.T)
				.where()
					.property(MessagingConnectorDeployable.deploymentStatus).eq(DeploymentStatus.deployed)
				.done();
		//@formatter:on
		List<MessagingConnectorDeployable> messagingConnectorDeployables = cortexSession.query().entities(query).list();

		List<CheckResultEntry> entries = response.getEntries();
		for (MessagingConnectorDeployable messagingConnectorDeployable : messagingConnectorDeployables) {
			DeployedUnit unit = deployRegistry.resolve(messagingConnectorDeployable);

			if (unit == null) {
				throw new IllegalStateException("'" + messagingConnectorDeployable.entityType().getTypeName() + "' deployedUnit is null.");
			}

			// Here we get the component implementation and query the health() method to get the status
			//@formatter:off
			unit.getComponents().entrySet().stream()
					.filter(c -> c.getKey().getSuperTypes().contains(MessagingConnectorDeployable.T))
					.map(Map.Entry::getValue)
					.map(v -> (MessagingConnector) v.exposedImplementation())
					.map(c->{
						CheckResultEntry health = c.health();
						health.setId(messagingConnectorDeployable.getExternalId());
						health.setName(messagingConnectorDeployable.getName());
						return health;
					})
					.forEach(entries::add);
			//@formatter:on
		}*/

		/*logger.info(() -> "Executed messaging health check on: "
				+ messagingConnectorDeployables.stream().map(HasExternalId::getExternalId).collect(Collectors.joining(", ")));*/

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
