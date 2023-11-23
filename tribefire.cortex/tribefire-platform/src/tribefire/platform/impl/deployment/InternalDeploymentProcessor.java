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
package tribefire.platform.impl.deployment;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deploymentapi.request.InternalDeploy;
import com.braintribe.model.deploymentapi.request.InternalDeploymentRequest;
import com.braintribe.model.deploymentapi.request.InternalUndeploy;
import com.braintribe.model.deploymentapi.response.InternalDeployResponse;
import com.braintribe.model.deploymentapi.response.InternalDeploymentResponse;
import com.braintribe.model.deploymentapi.response.InternalUndeployResponse;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.DeployContext;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.DeploymentService;
import com.braintribe.model.processing.deployment.api.DeploymentServiceContext;
import com.braintribe.model.processing.deployment.api.UndeployContext;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;

/**
 * <p>
 * Central processor of {@link InternalDeploymentRequest} instances. Such requests are broadcasted by the master cartridge and processed by every
 * cartridge instance (master and extensions) in order to work the internal deployment and undeployment operations.
 * 
 */
public class InternalDeploymentProcessor extends AbstractDispatchingServiceProcessor<InternalDeploymentRequest, InternalDeploymentResponse> {

	// constants
	private static final Logger log = Logger.getLogger(InternalDeploymentProcessor.class);

	// configurable
	private Supplier<PersistenceGmSession> cortexSessionProvider;
	private DeploymentService deploymentService;
	private DeployRegistry deployRegistry;
	private Supplier<ActivationState> activationStateSupplier;

	@Required
	public void setCortexSessionProvider(Supplier<PersistenceGmSession> cortexSessionProvider) {
		this.cortexSessionProvider = cortexSessionProvider;
	}

	@Required
	public void setDeploymentService(DeploymentService deploymentService) {
		this.deploymentService = deploymentService;
	}

	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}

	@Configurable
	public void setActivationStateSupplier(Supplier<ActivationState> activationStateSupplier) {
		this.activationStateSupplier = activationStateSupplier;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<InternalDeploymentRequest, InternalDeploymentResponse> dispatching) {
		dispatching.register(InternalDeploy.T, (c, r) -> deploy(r));
		dispatching.register(InternalUndeploy.T, (c, r) -> undeploy(r));
	}
	
	private InternalDeploymentResponse deploy(InternalDeploy deploy) throws DeploymentException {
		List<Deployable> deployables = deploy.getDeployables();
		if (isEmpty(deployables))
			throw new DeploymentException("No deployable given in " + deploy);

		if (activationStateSupplier != null) {
			ActivationState state = activationStateSupplier.get();
			if (!active(state)) {
				log.info(() -> "Server is " + state + ". The deployment call will be ignored");
				return handleNonActiveDeployment(deployables);
			} else {
				log.trace(() -> "Server is " + state + ". The deployment call will processed");
			}

		}

		PersistenceGmSession session = cortexSessionProvider.get();
		if (session == null)
			return null;

		deployables = fullyFetchDeployables(session, deployables);

		InternalDeployContext deployContext = new InternalDeployContext(session, deployables);

		deploymentService.deploy(deployContext);

		return createResponse(deployContext, InternalDeployResponse.T);
	}

	private boolean active(ActivationState state) {

		if (state == null) {
			log.warn(() -> "Activation state is not available. It is assumed it is inactive.");
			return false;
		}

		switch (state) {
			case inactive:
			case unauthorized:
			case deactivated:
				return false;
			default:
				return true;
		}
	}

	private InternalDeployResponse handleNonActiveDeployment(List<Deployable> deployables) {
		InternalDeployResponse response = InternalDeployResponse.T.create();
		ResponseEnvelope envelope = ResponseEnvelope.T.create();

		for (Deployable d : deployables)
			if (d != null && d.getExternalId() != null)
				response.getResults().put(d.getExternalId(), envelope);

		return response;
	}

	private InternalUndeployResponse undeploy(InternalUndeploy undeploy) throws DeploymentException {
		if (activationStateSupplier != null && log.isTraceEnabled()) {
			ActivationState state = activationStateSupplier.get();
			log.trace(() -> "Server is " + state + ". The undeployment call will processed");
		}

		List<String> externalIds = undeploy.getExternalIds();
		if (isEmpty(externalIds))
			throw new DeploymentException("No deployable given in " + undeploy);

		List<Deployable> deployables = resolveDeployablesToUndeploy(externalIds);

		InternalUndeployContext undeployContext = new InternalUndeployContext(deployables);

		deploymentService.undeploy(undeployContext);

		return createResponse(undeployContext, InternalUndeployResponse.T);
	}

	private List<Deployable> resolveDeployablesToUndeploy(List<String> externalIds) {
		List<Deployable> result = newList(externalIds.size());
		List<Deployable> currentDeployables = deployRegistry.getDeployables();

		for (int i = currentDeployables.size() - 1; i >= 0 && !externalIds.isEmpty(); i--) {
			Deployable currentDeployable = currentDeployables.get(i);
			if (externalIds.remove(currentDeployable.getExternalId()))
				result.add(currentDeployable);
		}

		return result;
	}

	private <T extends InternalDeploymentResponse> T createResponse(InternalDeploymentContext context, EntityType<T> responseType) {
		T response = responseType.create();
		Map<String, ServiceResult> results = response.getResults();

		for (Entry<Deployable, ServiceResult> e : context.results.entrySet())
			results.put(e.getKey().getExternalId(), e.getValue());

		return response;
	}

	private List<Deployable> fullyFetchDeployables(PersistenceGmSession session, List<Deployable> deployables) {
		Set<String> externalIds = deployables.stream() //
				.map(Deployable::getExternalId) //
				.collect(Collectors.toSet());

		SelectQuery query = new SelectQueryBuilder() //
				.from(Deployable.T, "d") //
				.where().property("d", Deployable.externalId).in(externalIds) //
				.tc(PreparedTcs.everythingTc) //
				.done();

		SelectQueryResult result = session.queryDetached().select(query).result();
		return (List<Deployable>) (List<?>) result.getResults();
	}

	private static class InternalDeploymentContext implements DeploymentServiceContext {

		public final List<Deployable> deployables;
		public final Map<Deployable, ServiceResult> results;

		protected InternalDeploymentContext(List<Deployable> deployables) {
			this.deployables = deployables;
			this.results = newMap(deployables.size());
		}

		// @formatter:off
		@Override public List<Deployable> deployables() { return deployables; }
		@Override public void succeeded(Deployable deployable) { results.put(deployable, ResponseEnvelope.T.create()); }
		@Override public boolean areDeployablesFullyFetched() { return getClass() == InternalDeployContext.class; /* deploy = true, undeploy = false*/ }
		// @formatter:on

		@Override
		public void failed(Deployable deployable, Throwable throwable) {
			log.error("Deployment failed for " + deployable, throwable);

			Failure failure = toFailure(throwable);
			if (failure != null)
				results.put(deployable, failure);
		}

		private Failure toFailure(Throwable throwable) {
			try {
				return FailureCodec.INSTANCE.encode(throwable);
			} catch (Exception e) {
				log.error("Failed to convert " + throwable + " to " + Failure.T.getTypeSignature(), e);
				return null;
			}
		}

	}

	private static class InternalDeployContext extends InternalDeploymentContext implements DeployContext {

		private final PersistenceGmSession session;

		protected InternalDeployContext(PersistenceGmSession session, List<Deployable> deployables) {
			super(deployables);
			this.session = session;
		}

		@Override
		public PersistenceGmSession session() {
			return session;
		}

	}

	private static class InternalUndeployContext extends InternalDeploymentContext implements UndeployContext {

		protected InternalUndeployContext(List<Deployable> deployables) {
			super(deployables);
		}

	}

}
