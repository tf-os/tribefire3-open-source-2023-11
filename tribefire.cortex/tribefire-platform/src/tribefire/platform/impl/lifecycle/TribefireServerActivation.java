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
package tribefire.platform.impl.lifecycle;

import static tribefire.platform.impl.deployment.ActivationState.activated;
import static tribefire.platform.impl.deployment.ActivationState.activating;
import static tribefire.platform.impl.deployment.ActivationState.deactivated;
import static tribefire.platform.impl.deployment.ActivationState.deactivating;
import static tribefire.platform.impl.deployment.ActivationState.inactive;
import static tribefire.platform.impl.deployment.ActivationState.unauthorized;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.apache.http.impl.conn.ConnectionShutdownException;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.exception.CanceledException;
import com.braintribe.exception.CommunicationException;
import com.braintribe.exception.Exceptions;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.deploymentapi.request.ResolveDeployablesWithQuery;
import com.braintribe.model.deploymentapi.response.ResolveDeployablesResponse;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.deployment.api.DeployContext;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.DeploymentService;
import com.braintribe.model.processing.deployment.api.DeploymentServiceContext;
import com.braintribe.model.processing.deployment.api.UndeployContext;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.securityservice.api.UserSessionScope;
import com.braintribe.model.processing.securityservice.api.UserSessionScoping;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.processing.worker.api.WorkerManagerControl;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.lcd.StopWatch;

import tribefire.platform.impl.deployment.ActivationState;
import tribefire.platform.impl.deployment.SystemDeploymentListenerRegistry;
import tribefire.platform.impl.topology.CartridgeLiveInstances;
import tribefire.platform.impl.topology.HeartbeatManager;

public class TribefireServerActivation implements DestructionAware {

	// constants
	private static final Logger log = Logger.getLogger(TribefireServerActivation.class);
	private static final String unauthorizedMessage = "%s is unauthorized to communicate with tribefire services and therefore couldn't be activated. It might not have been detected yet.";

	// configurable
	protected InstanceId processingInstanceId;
	protected UserSessionScoping userSessionScoping;
	private Evaluator<ServiceRequest> requestEvaluator;
	private WorkerManagerControl workerManagerControl;
	private DeployRegistry deployRegistry;
	private DeploymentService deploymentService;
	private SystemDeploymentListenerRegistry systemDeploymentListenerRegistry;
	private Supplier<PersistenceGmSession> cortexSessionProvider;
	private boolean skipDeployablesDeployment = false;
	private EntityQuery deployablesQuery = null;

	// internal
	protected ActivationState state = inactive;
	protected Object stateMonitor = new Object();

	protected AtomicInteger activationTries = new AtomicInteger(0);
	protected AtomicLong activationUnsucessfulDurations = new AtomicLong(0);

	protected ExecutorService executor = VirtualThreadExecutorBuilder.newPool() //
			.concurrency(5) //
			.threadNamePrefix("activation") //
			.description("Activation") //
			.build();
	private CartridgeLiveInstances liveInstances;
	private Instant firstActivationTryInstant = null;
	private long lastActivationTryEndTime = -1L;
	private final AtomicLong totalTimeBetweenTries = new AtomicLong(0);

	private final Object deploymentAwaitingMonitor = new Object();
	private boolean deploymentReached;
	private long deploymentAwaitingTimeout = Numbers.MILLISECONDS_PER_MINUTE * 5;
	private HeartbeatManager heartbeatManager;
	private static boolean threadRenamingEnabled = Boolean
			.valueOf(TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_THREAD_RENAMING, "true"));

	public TribefireServerActivation() {
	}

	public void waitForDeploymentStart() {
		if (deploymentReached)
			return;

		synchronized (deploymentAwaitingMonitor) {
			if (deploymentReached)
				return;

			try {
				deploymentAwaitingMonitor.wait(deploymentAwaitingTimeout);
				if (!deploymentReached)
					throw new com.braintribe.common.lcd.TimeoutException(
							"Timed out after waiting for deployment for " + deploymentAwaitingTimeout + "ms");
			} catch (InterruptedException e) {
				throw new CanceledException("Unexpected interupt while waiting for deployment", e);
			}
		}
	}

	/** configures the timeout in ms to wait for the deployment to have at least started */
	@Configurable
	public void setDeploymentAwaitingTimeout(long deploymentAwaitingTimeout) {
		this.deploymentAwaitingTimeout = deploymentAwaitingTimeout;
	}

	@Required
	@Configurable
	public void setProcessingInstanceId(InstanceId processingInstanceId) {
		this.processingInstanceId = processingInstanceId;
	}

	@Required
	@Configurable
	public void setUserSessionScoping(UserSessionScoping userSessionScoping) {
		this.userSessionScoping = userSessionScoping;
	}

	@Required
	@Configurable
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

	@Required
	@Configurable
	public void setWorkerManagerControl(WorkerManagerControl workerManagerControl) {
		this.workerManagerControl = workerManagerControl;
	}

	@Required
	@Configurable
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}

	@Required
	@Configurable
	public void setDeploymentService(DeploymentService deploymentService) {
		this.deploymentService = deploymentService;
	}

	@Required
	public void setSystemDeploymentListenerRegistry(SystemDeploymentListenerRegistry systemDeploymentListenerRegistry) {
		this.systemDeploymentListenerRegistry = systemDeploymentListenerRegistry;
	}

	@Required
	@Configurable
	public void setCortexSessionProvider(Supplier<PersistenceGmSession> cortexSessionProvider) {
		this.cortexSessionProvider = cortexSessionProvider;
	}

	@Configurable
	public void setSkipDeployablesDeployment(boolean skipDeployablesDeployment) {
		this.skipDeployablesDeployment = skipDeployablesDeployment;
	}

	@Configurable
	public void setDeployablesQuery(EntityQuery deployablesQuery) {
		this.deployablesQuery = deployablesQuery;
	}

	@Configurable
	@Required
	public void setLiveInstances(CartridgeLiveInstances liveInstances) {
		this.liveInstances = liveInstances;
	}

	@Configurable
	@Required
	public void setHeartbeatManager(HeartbeatManager heartbeatManager) {
		this.heartbeatManager = heartbeatManager;
	}

	public ActivationState activate() {
		StopWatch stopWatch = new StopWatch();

		long start = log.isTraceEnabled() ? System.currentTimeMillis() : 0;

		if (firstActivationTryInstant == null) {
			firstActivationTryInstant = NanoClock.INSTANCE.instant();
		}

		ActivationState previousStatus;
		ActivationState newStatus;

		synchronized (stateMonitor) {
			previousStatus = state;

			if (previousStatus == activated) {
				log.debug(() -> "Server is already activated. Ignoring activate() call");
				return state;
			}

			state = activating;

			int tries = activationTries.incrementAndGet();
			long tryStart = System.currentTimeMillis();

			try {
				UserSessionScope scope = userSessionScoping.forDefaultUser().push();

				stopWatch.intermediate("UserScope pushed");
				try {
					state = authorizedActivation(stopWatch);
					stopWatch.intermediate("Authorized Activation");

				} finally {
					scope.pop();
					stopWatch.intermediate("UserScope popped");
				}

				if (state == activated) {
					log.info("Server was activated");
				}

			} catch (AuthorizationException | SecurityServiceException e) {
				state = unauthorized;

				String authMsg = String.format(unauthorizedMessage, processingInstanceId.getApplicationId());

				log.warn(authMsg);

				throw e;

			} catch (CommunicationException e) {
				stopWatch.intermediate("Communication Exception");
				state = inactive;
				/* This was some previous weird impl with cartridges. You can get here for example when debugging and loading
				 * deployables takes longer than 30 seconds. We just log it as an error now, below. */

				// log.info(() -> "Activation for " + processingInstanceId
				// + " postponed as communication channel is not yet established. See more details on TRACE level.");
				// log.trace(() -> "Details for postponed activation: ", e);

				log.error("Could not start the server", e);

			} catch (Exception e) {
				state = inactive;

				String failureMsg = "Failed to activate " + processingInstanceId + (e.getMessage() != null ? ": " + e.getMessage() : "");

				if (e instanceof RuntimeException) {
					log.error(failureMsg);
					throw (RuntimeException) e;
				}

				throw new IllegalStateException(failureMsg, e);

			} finally {
				long tryDuration = System.currentTimeMillis() - tryStart;

				if (state == activated) {
					if (log.isDebugEnabled()) {

						long unsuccessfulDuration = activationUnsucessfulDurations.longValue();
						int unsuccessfulTries = (tries - 1);
						long unsuccessMsPerTry = unsuccessfulTries > 0 ? (unsuccessfulDuration / unsuccessfulTries) : 0;

						long totalTryDuration = unsuccessfulDuration + tryDuration;

						Instant startWasAt = liveInstances.getHeartbeatStartInstant(TribefireConstants.TRIBEFIRE_SERVICES_APPLICATION_ID);
						String timeFromHeartbeatToActivation = startWasAt != null ? StringTools.prettyPrintDuration(startWasAt, true, null) : "n/a";

						String timeSinceFirstActivationTry = StringTools.prettyPrintDuration(firstActivationTryInstant, true, null);
						String totalTimeBetweenTriesString = StringTools.prettyPrintDuration(totalTimeBetweenTries.longValue(), true, null);

						log.debug(
								"Activated " + processingInstanceId + " after a total " + totalTryDuration + " ms in " + tries + " tries (this try: "
										+ tryDuration + " ms, averaging: " + (totalTryDuration / tries) + " ms per try; unsuccessful tries: "
										+ unsuccessfulTries + ", unsuccessful avg: " + unsuccessMsPerTry + " ms per try, total time since heartbeat: "
										+ timeFromHeartbeatToActivation + ", total time since first activation try: " + timeSinceFirstActivationTry
										+ ", total time between tries: " + totalTimeBetweenTriesString + "; stop watch: " + stopWatch + ")");
					}

					heartbeatManager.startHeartbeatBroadcasting();

				} else {
					activationUnsucessfulDurations.addAndGet(tryDuration);
				}

				if (lastActivationTryEndTime != -1L) {
					long timeSinceLastTry = start - lastActivationTryEndTime;
					totalTimeBetweenTries.addAndGet(timeSinceLastTry);
				}

				lastActivationTryEndTime = System.currentTimeMillis();
			}

			newStatus = state;
		}

		log.trace(() -> processingInstanceId + " activation call (state change from " + previousStatus + " to " + newStatus + ") took "
				+ (System.currentTimeMillis() - start) + " ms (" + stopWatch + ")");

		log.pushContext(processingInstanceId.stringify());
		try {
			if (newStatus == ActivationState.activated)
				liveInstances.acceptCurrentInstance();
		} finally {
			log.popContext();
		}

		return newStatus;
	}

	public ActivationState deactivate() {
		long start = log.isTraceEnabled() ? System.currentTimeMillis() : 0;

		ActivationState previousStatus;
		ActivationState newStatus;

		synchronized (stateMonitor) {
			previousStatus = state;

			if (previousStatus != activated) {
				log.debug("The instance " + processingInstanceId + " is not activated. Ignoring deactivate() call");
				return state;
			}

			state = deactivating;

			if (!skipDeployablesDeployment) {
				// collect non-hardwired deployables in reverse deployment order
				LinkedList<Deployable> deployables = new LinkedList<>();

				for (Deployable deployable : deployRegistry.getDeployables())
					if (!(deployable instanceof HardwiredDeployable))
						deployables.addFirst(deployable);

				deploymentService.undeploy(new DeployerUndeployContext(deployables));
			}

			state = deactivated;

			newStatus = state;

			log.info("Deactivated " + processingInstanceId);
		}

		log.trace(() -> processingInstanceId + " deactivation call (state change from " + previousStatus + " to " + newStatus + ") took "
				+ (System.currentTimeMillis() - start) + " ms");

		return newStatus;
	}

	public ActivationState state() {
		return state;
	}

	private ActivationState authorizedActivation(StopWatch stopWatch) throws DeploymentException, InterruptedException {
		deployDeployables(stopWatch);

		stopWatch.intermediate("Deploy Deployables");

		log.debug(() -> "Starting workers of " + processingInstanceId);

		try {
			workerManagerControl.start();
		} catch (WorkerException e) {
			log.error("Failed to start workers of " + processingInstanceId + (e.getMessage() != null ? ": " + e.getMessage() : ""));
			throw e;
		}

		stopWatch.intermediate("Worker Manager");

		log.info("Started workers of " + processingInstanceId);

		return activated;
	}

	private void deployDeployables(StopWatch stopWatch) throws DeploymentException, InterruptedException {
		if (skipDeployablesDeployment) {
			log.debug(processingInstanceId + " activation won't deploy deployables as deployment is deactivated");
			return;
		}

		String appId = processingInstanceId.getApplicationId();
		log.info(() -> "Determining designated deployables for " + appId);
		stopWatch.intermediate("Get App Id");

		List<Deployable> resolveDeployables = resolveDeployables(stopWatch);
		if (resolveDeployables.isEmpty()) {
			log.info(() -> "No designated deployables found for " + appId);
			return;
		}
		stopWatch.intermediate("Resolve Deployables");

		log.debug(() -> "Trying to get a Cortex session");
		PersistenceGmSession session = cortexSessionProvider.get();
		stopWatch.intermediate("Get Session");

		deploymentService.deploy(new DeployerDeployContext(session, resolveDeployables, true));
		log.info(() -> "Finished deployment of " + resolveDeployables.size() + " designated deployable(s) to " + appId);
		stopWatch.intermediate("Deploy");

		systemDeploymentListenerRegistry.onSystemDeployed();
		stopWatch.intermediate("OnSystemDeployed");
	}

	private List<Deployable> resolveDeployables(StopWatch stopWatch) throws DeploymentException, InterruptedException {
		ResolveDeployablesWithQuery resolveDeployables = ResolveDeployablesWithQuery.T.create();
		resolveDeployables.setQuery(getQueryForDeployables());
		resolveDeployables.setShallowify(false);
		resolveDeployables.setIncludeHardwired(true);
		resolveDeployables.setId("BasicCartridgeActivation-" + processingInstanceId + "-" + RandomTools.newStandardUuid());

		Future<List<Deployable>> future = executor.submit(() -> {
			UserSessionScope scope = userSessionScoping.forDefaultUser().push();
			Thread currentThread = null;
			String oldName = null;
			if (threadRenamingEnabled) {
				currentThread = Thread.currentThread();
				oldName = currentThread.getName();
				currentThread.setName(oldName + "-" + resolveDeployables.getId());
			}
			try {
				stopWatch.intermediate("resolveDeployables-thread-" + Thread.currentThread().getName());
				ResolveDeployablesResponse response = resolveDeployables.eval(requestEvaluator).get();
				stopWatch.intermediate("Response");
				List<Deployable> deployables = response.getDeployables();
				stopWatch.intermediate("Get Deployables");
				return deployables;
			} finally {
				scope.pop();
				log.trace(() -> "Resolve deployables request (" + resolveDeployables.getId() + ") in " + processingInstanceId + ": " + stopWatch);
				if (threadRenamingEnabled) {
					currentThread.setName(oldName);
				}
			}
		});
		stopWatch.intermediate("Request Submitted");

		try {
			List<Deployable> list = future.get(30, TimeUnit.SECONDS);
			stopWatch.intermediate("Response from Future");
			future = null;
			return list;
		} catch (ExecutionException e) {
			stopWatch.intermediate("Execution Error");
			Throwable rootCause = Exceptions.getRootCause(e);
			if (rootCause instanceof SocketException || rootCause instanceof SocketTimeoutException
					|| rootCause instanceof ConnectionShutdownException) {
				String message = "Got a socket exception while trying to get a list of deployables.";
				log.trace(message, e);
				throw new CommunicationException(message);
			}
			throw new DeploymentException("Got an error while trying to get the list of deployables.", e);

		} catch (TimeoutException e) {
			stopWatch.intermediate("Timeout of ResolveDeployablesWithQuery-" + resolveDeployables.getId());
			throw new CommunicationException("Got a timeout while waiting for the list of deployables.");

		} catch (InterruptedException ie) {
			stopWatch.intermediate("Interrupted");
			throw ie;

		} finally {
			if (future != null) {
				try {
					future.cancel(true);
				} catch (Exception e) {
					log.debug(() -> "Error while trying to cancel getting the list of deployables.", e);
				}
			}
		}
	}

	private Query getQueryForDeployables() {
		if (deployablesQuery != null)
			return deployablesQuery;
		else
			// @formatter:off
			return EntityQueryBuilder.from(Deployable.class)
					.where()
						.property(Deployable.autoDeploy).eq(true)
					.tc(PreparedTcs.everythingTc)
					.done();
			// @formatter:on
	}

	/**
	 * <p>
	 * A base for {@link DeploymentServiceContext}(s) used by this {@link TribefireServerActivation}.
	 * 
	 *
	 */
	private static abstract class DeployerDeploymentContext implements DeploymentServiceContext {

		protected static final String deploy = "deploy";
		protected static final String undeploy = "undeploy";

		private final List<Deployable> deployables;
		private final boolean isFullyFetched;

		protected DeployerDeploymentContext(List<Deployable> deployables, boolean isFullyFetched) {
			this.deployables = deployables;
			this.isFullyFetched = isFullyFetched;
		}

		abstract String operation();

		@Override
		public List<Deployable> deployables() {
			return deployables;
		}

		@Override
		public void succeeded(Deployable d) {
			log.debug(() -> "Successfully " + operation() + "ed" + context(d));
		}

		@Override
		public void failed(Deployable d, Throwable failure) {
			log.error("Failed to " + operation() + context(d), failure);
		}

		private String context(Deployable d) {
			return " [" + d.getExternalId() + "] on " + (undeploy.equals(operation()) ? "de" : "") + "activation. Deployable " + d;
		}

		@Override
		public boolean areDeployablesFullyFetched() {
			return isFullyFetched;
		}

	}

	private class DeployerDeployContext extends DeployerDeploymentContext implements DeployContext {
		private final PersistenceGmSession session;

		protected DeployerDeployContext(PersistenceGmSession session, List<Deployable> deployables, boolean isFullyFetched) {
			super(deployables, isFullyFetched);
			this.session = session;
		}

		@Override
		public void deploymentStarted() {
			deploymentReached = true;
			synchronized (deploymentAwaitingMonitor) {
				deploymentAwaitingMonitor.notify();
			}
		}

		// @formatter:off
		@Override public void started(Deployable deployable) { /* NO OP */ }
		@Override public PersistenceGmSession session() { return session; }
		@Override final String operation() { return deploy; }
		// @formatter:on
	}

	private static class DeployerUndeployContext extends DeployerDeploymentContext implements UndeployContext {
		protected DeployerUndeployContext(List<Deployable> deployables) {
			super(deployables, false);
		}

		@Override
		final String operation() {
			return undeploy;
		}
	}

	@Override
	public void preDestroy() {
		deactivate();

		if (executor != null) {
			try {
				executor.shutdown();
			} catch (Exception e) {
				log.debug("Error while trying to shutdown executor.", e);
			} finally {
				executor = null;
			}
		}
	}

}
