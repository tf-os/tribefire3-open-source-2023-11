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

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cartridge.common.api.topology.ApplicationHeartbeatListener;
import com.braintribe.cartridge.common.api.topology.ApplicationLifecycleListenerContext;
import com.braintribe.cartridge.common.api.topology.ApplicationShutdownListener;
import com.braintribe.cartridge.common.api.topology.LiveInstances;
import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.deploymentapi.data.InstanceDeployment;
import com.braintribe.model.deploymentapi.request.Deploy;
import com.braintribe.model.deploymentapi.request.DeployWithDeployables;
import com.braintribe.model.deploymentapi.request.DeploymentMode;
import com.braintribe.model.deploymentapi.request.DeploymentOperation;
import com.braintribe.model.deploymentapi.request.DeploymentOperationWithDeployables;
import com.braintribe.model.deploymentapi.request.DeploymentRequest;
import com.braintribe.model.deploymentapi.request.InternalDeploy;
import com.braintribe.model.deploymentapi.request.InternalDeploymentRequest;
import com.braintribe.model.deploymentapi.request.InternalUndeploy;
import com.braintribe.model.deploymentapi.request.NotifyManualDeployments;
import com.braintribe.model.deploymentapi.request.Redeploy;
import com.braintribe.model.deploymentapi.request.RedeployWithDeployables;
import com.braintribe.model.deploymentapi.request.ResolveDeployablesWithExternalIds;
import com.braintribe.model.deploymentapi.request.ResolveDeployablesWithQuery;
import com.braintribe.model.deploymentapi.request.Undeploy;
import com.braintribe.model.deploymentapi.request.UndeployWithDeployables;
import com.braintribe.model.deploymentapi.response.DeployResponse;
import com.braintribe.model.deploymentapi.response.DeploymentResponse;
import com.braintribe.model.deploymentapi.response.DeploymentResponseMessage;
import com.braintribe.model.deploymentapi.response.InternalDeploymentResponse;
import com.braintribe.model.deploymentapi.response.RedeployResponse;
import com.braintribe.model.deploymentapi.response.ResolveDeployablesResponse;
import com.braintribe.model.deploymentapi.response.UndeployResponse;
import com.braintribe.model.deploymentreflection.request.GetDeployedDeployables;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GmReflectionTools;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.notification.CommandNotification;
import com.braintribe.model.notification.HasNotifications;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.core.commons.EntityHashingComparator;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.DeploymentPreProcessor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.securityservice.api.UserSessionScope;
import com.braintribe.model.processing.securityservice.api.UserSessionScoping;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.UnicastRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.service.api.result.ServiceResultType;
import com.braintribe.model.uicommand.Refresh;
import com.braintribe.utils.lcd.StopWatch;

/**
 * <p>
 * Central processor of {@link DeploymentRequest} instances.
 * 
 * <p>
 * This processor centralizes the processing of {@link DeploymentRequest} instances, broadcasting the deployment and
 * undeployment operations they represent to every cartridge instance (master and extensions).
 * 
 */
public class DeploymentProcessor extends AbstractDispatchingServiceProcessor<DeploymentRequest, Object>
		implements ApplicationHeartbeatListener, ApplicationShutdownListener {

	private List<DeploymentPreProcessor> preProcessors = emptyList();

	// constants
	private static final Logger log = Logger.getLogger(DeploymentProcessor.class);

	// configurable
	private Supplier<PersistenceGmSession> gmSessionProvider;
	private boolean deploymentActivated = true;
	private Long requestTimeout;
	private final DeploymentStatusRegistry deploymentStatusRegistry = new DeploymentStatusRegistry();
	private LiveInstances liveInstances;
	private UserSessionScoping userSessionScoping;
	private Evaluator<ServiceRequest> systemEvaluator;

	private String applicationId;
	private static boolean threadRenamingEnabled = Boolean
			.valueOf(TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_THREAD_RENAMING, "true"));

	// @formatter:off
	private TraversingCriterion shallowifyingCriteria = 
			TC.create()
				.conjunction()
					.property()
					.typeCondition(or(isKind(TypeKind.collectionType), isKind(TypeKind.entityType)))
				.close()
			.done();
	// @formatter:on

	@Override
	protected void configureDispatching(DispatchConfiguration<DeploymentRequest, Object> dispatching) {
		dispatching.register(NotifyManualDeployments.T, (c, r) -> notifyManualDeployments(r));

		dispatching.register(ResolveDeployablesWithQuery.T, (c, r) -> resolveDeployablesWithQuery(r));
		dispatching.register(ResolveDeployablesWithExternalIds.T, (c, r) -> resolveDeployablesWithExternalIds(r));

		dispatching.register(DeployWithDeployables.T, this::deployWithDeployables);
		dispatching.register(Deploy.T, this::deploy);

		dispatching.register(UndeployWithDeployables.T, this::undeployWithDeployables);
		dispatching.register(Undeploy.T, this::undeploy);

		dispatching.register(RedeployWithDeployables.T, this::redeployWithDeployables);
		dispatching.register(Redeploy.T, this::redeploy);
	}

	@Required
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	@Required
	public void setGmSessionProvider(Supplier<PersistenceGmSession> gmSessionProvider) {
		this.gmSessionProvider = gmSessionProvider;
	}

	@Required
	public void setLiveInstances(LiveInstances liveInstances) {
		this.liveInstances = liveInstances;
	}

	@Required
	public void setUserSessionScoping(UserSessionScoping userSessionScoping) {
		this.userSessionScoping = userSessionScoping;
	}

	@Required
	public void setSystemEvaluator(Evaluator<ServiceRequest> systemEvaluator) {
		this.systemEvaluator = systemEvaluator;
	}

	@Required
	public void setPreProcessors(List<DeploymentPreProcessor> preprocessors) {
		this.preProcessors = preprocessors;
	}

	@Configurable
	public void setDeploymentActivated(boolean deploymentActivated) {
		this.deploymentActivated = deploymentActivated;
	}

	@Configurable
	public void setShallowifyingCriteria(TraversingCriterion shallowifyingCriteria) {
		this.shallowifyingCriteria = shallowifyingCriteria;
	}

	@Configurable
	public void setRequestTimeout(Long requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	private DeployResponse deployWithDeployables(ServiceRequestContext requestContext, DeployWithDeployables request) throws DeploymentException {
		Set<String> externalIds = validate(request);
		return deploy(requestContext, externalIds, request.getMode());
	}

	private DeployResponse deploy(ServiceRequestContext requestContext, Deploy request) throws DeploymentException {
		Set<String> externalIds = validate(request);
		return deploy(requestContext, externalIds, request.getMode());
	}

	protected DeployResponse deploy(ServiceRequestContext requestContext, Set<String> externalIds, DeploymentMode mode) throws DeploymentException {
		DeployResponse response = DeployResponse.T.create();

		PersistenceGmSession gmSession = null;

		if (deploymentActivated && DeploymentMode.bootstrappingOnly != mode) {

			gmSession = retrieveGmSession();

			// 1. resolve deployables with shallowify = true. (shallow since it's being broadcasted all over the place?)
			List<Deployable> resolvedDeployables = resolveDeployables(gmSession, true, externalIds);

			// 2. wrap results in InternalDeploy
			InternalDeploy internalRequest = InternalDeploy.T.create();
			internalRequest.setDeployables(resolvedDeployables);

			// 3. broadcast InternalDeploy
			MulticastResponse multicastResponse = broadcast(requestContext, internalRequest);

			if (log.isDebugEnabled())
				log.debug("Received response for " + internalRequest + ": " + multicastResponse);

			List<InstanceDeployment> deployments = fillResponseAndGetInstanceDeployments(response, multicastResponse.getResponses(), true);
			notifyDeployments(requestContext, deployments);
		}

		if (DeploymentMode.transientOnly != mode) {
			if (gmSession == null) {
				gmSession = retrieveGmSession();
			}
			flagAutoDeploy(gmSession, externalIds, true);
		}

		commitIfNecessary(gmSession);

		return finalizeResponse(response, createNotifications(response, "deploy"));
	}

	private UndeployResponse undeployWithDeployables(ServiceRequestContext requestContext, UndeployWithDeployables request)
			throws DeploymentException {
		Set<String> externalIds = validate(request);
		return undeploy(requestContext, externalIds, request.getMode());
	}

	private UndeployResponse undeploy(ServiceRequestContext requestContext, Undeploy request) throws DeploymentException {
		Set<String> externalIds = validate(request);
		return undeploy(requestContext, externalIds, request.getMode());
	}

	public UndeployResponse undeploy(ServiceRequestContext requestContext, Set<String> externalIds, DeploymentMode mode) throws DeploymentException {

		UndeployResponse response = UndeployResponse.T.create();

		PersistenceGmSession gmSession = null;
		if (deploymentActivated && DeploymentMode.bootstrappingOnly != mode) {

			gmSession = retrieveGmSession();

			InternalUndeploy internalRequest = InternalUndeploy.T.create();
			internalRequest.setExternalIds(new ArrayList<>(externalIds));

			MulticastResponse multicastResponse = broadcast(requestContext, internalRequest);

			if (log.isDebugEnabled())
				log.debug("Received response for " + internalRequest + ": " + multicastResponse);

			List<InstanceDeployment> deployments = fillResponseAndGetInstanceDeployments(response, multicastResponse.getResponses(), false);

			notifyDeployments(requestContext, deployments);

		}

		if (DeploymentMode.transientOnly != mode) {
			if (gmSession == null) {
				gmSession = retrieveGmSession();
			}
			flagAutoDeploy(gmSession, externalIds, false);
		}

		commitIfNecessary(gmSession);

		return finalizeResponse(response, createNotifications(response, "undeploy"));
	}

	private void notifyDeployments(ServiceRequestContext requestContext, List<InstanceDeployment> deployments) {
		NotifyManualDeployments notify = NotifyManualDeployments.T.create();
		notify.setDeployments(deployments);

		MulticastRequest multicastNotify = MulticastRequest.T.create();
		multicastNotify.setAddressee(InstanceId.of(null, applicationId));
		multicastNotify.setServiceRequest(notify);

		try {
			MulticastResponse notifyResponse = multicastNotify.eval(requestContext).get();

			List<Throwable> throwables = notifyResponse.getResponses().values().stream() //
					.filter(r -> r.resultType() == ServiceResultType.failure) //
					.map(r -> FailureCodec.INSTANCE.decode((Failure) r)) //
					.collect(Collectors.toList());

			if (!throwables.isEmpty()) {
				IllegalStateException e = new IllegalStateException("Could not notify all available instances about deployment operation.");
				throwables.stream().forEach(e::addSuppressed);
				throw e;
			}

		} catch (Exception e) {
			log.error("Error while notifying instances about deployment operation", e);
		}
	}

	private RedeployResponse redeployWithDeployables(ServiceRequestContext requestContext, RedeployWithDeployables request)
			throws DeploymentException {
		Set<String> externalIds = validate(request);
		return redeploy(requestContext, externalIds, request.getMode());
	}

	private RedeployResponse redeploy(ServiceRequestContext requestContext, Redeploy request) throws DeploymentException {
		Set<String> externalIds = validate(request);
		return redeploy(requestContext, externalIds, request.getMode());
	}

	protected RedeployResponse redeploy(ServiceRequestContext requestContext, Set<String> externalIds, DeploymentMode mode)
			throws DeploymentException {

		UndeployResponse undeployResponse = undeploy(requestContext, externalIds, mode);

		// Let's wait a bit to be sure ...
		sleep();

		DeployResponse deployResponse = deploy(requestContext, externalIds, mode);

		// Build response
		RedeployResponse response = RedeployResponse.T.create();
		List<DeploymentResponseMessage> results = response.getResults();
		List<Notification> notifications = response.getNotifications();

		results.addAll(deployResponse.getResults());
		results.addAll(undeployResponse.getResults());

		notifications.add(createMessageNotification("Redeployed Deployables. Click to check details...", Level.INFO));
		notifications.add(createRefreshNotification());

		addNonCommanNotifications(notifications, deployResponse);
		addNonCommanNotifications(notifications, undeployResponse);

		return response;
	}

	private void addNonCommanNotifications(List<Notification> notifications, HasNotifications hasNotifications) {
		hasNotifications.getNotifications().stream() //
				.filter(n -> !(n instanceof CommandNotification)) //
				.forEach(notifications::add);
	}

	// WTF is this?
	private void sleep() {
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			/* ignore */
		}
	}

	private void commitIfNecessary(PersistenceGmSession gmSession) {
		if (gmSession != null && gmSession.getTransaction().hasManipulations())
			gmSession.commit();
	}

	private ResolveDeployablesResponse resolveDeployablesWithExternalIds(ResolveDeployablesWithExternalIds request) throws DeploymentException {
		PersistenceGmSession gmSession = retrieveGmSession();

		List<Deployable> resolvedDeployables = resolveDeployables(gmSession, request.getShallowify(), request.getExternalIds());

		ResolveDeployablesResponse response = ResolveDeployablesResponse.T.create();
		response.setDeployables(resolvedDeployables);
		return response;

	}

	private ResolveDeployablesResponse resolveDeployablesWithQuery(ResolveDeployablesWithQuery request) throws DeploymentException {
		StopWatch stopWatch = new StopWatch();
		String id = request.getId();
		Thread currentThread = null;
		String oldName = null;
		if (threadRenamingEnabled) {
			currentThread = Thread.currentThread();
			oldName = currentThread.getName();
			currentThread.setName(oldName + "-" + id);
			stopWatch.intermediate(currentThread.getName());
		}

		try {
			PersistenceGmSession gmSession = retrieveGmSession();
			stopWatch.intermediate("Retrieve Session");

			List<Deployable> resolvedDeployables = resolveDeployables(gmSession, request.getShallowify(), request.getQuery(),
					request.getIncludeHardwired());
			stopWatch.intermediate("Resolved Deployables");

			ResolveDeployablesResponse response = ResolveDeployablesResponse.T.create();
			response.setDeployables(resolvedDeployables);

			log.trace(() -> "Server-side resolving of deployables (" + request.getId() + "): " + stopWatch);

			return response;
		} finally {
			if (threadRenamingEnabled) {
				currentThread.setName(oldName);
			}
		}
	}

	private Neutral notifyManualDeployments(NotifyManualDeployments request) {

		for (InstanceDeployment d : request.getDeployments()) {

			DeploymentStatusEntry entry = this.deploymentStatusRegistry.aquireEntry(InstanceId.of(d.getNodeId(), d.getApplicationId()));

			if (d.getDeployed()) {
				entry.addDeployedExternalId(d.getExternalId());
			} else {
				entry.removeDeployedExternalId(d.getExternalId());
			}

		}

		updateDeploymentStatus();

		return Neutral.NEUTRAL;
	}

	private void updateDeploymentStatus() {
		PersistenceGmSession session = retrieveGmSession();

		SelectQuery query = new SelectQueryBuilder() //
				.from(Deployable.T, "d") //
				.select("d") //
				.done();

		List<Deployable> result = session.query().select(query).list();

		Set<String> expectedInstances = this.liveInstances.liveInstances();

		for (Deployable deployable : result) {

			if (!(deployable instanceof HardwiredDeployable))
				updateDeploymentStatus(deployable, expectedInstances);
		}

		commitIfNecessary(session);
	}

	private void updateDeploymentStatus(Deployable deployable, Set<String> expectedInstances) {
		deployable.setDeploymentStatus(resolveDeploymentStatus(deployable, expectedInstances));
	}

	private DeploymentStatus resolveDeploymentStatus(Deployable deployable, Set<String> expectedInstances) {
		Set<String> deployedInInstances = getInstancesDeployableIsDeployedIn(deployable.getExternalId());

		deployedInInstances.retainAll(expectedInstances);

		if (deployedInInstances.isEmpty())
			return DeploymentStatus.undeployed;
		else
			return deployedInInstances.size() == expectedInstances.size() ? //
					DeploymentStatus.deployed : //
					DeploymentStatus.partiallyDeployed;

	}

	private Set<String> getInstancesDeployableIsDeployedIn(String externalId) {
		Set<String> instanceIds = new HashSet<>();
		for (DeploymentStatusEntry e : this.deploymentStatusRegistry.entries.values()) {
			if (e.isDeployed(externalId)) {
				InstanceId instanceId = e.getInstanceId();
				instanceIds.add(instanceId.getApplicationId() + "@" + instanceId.getNodeId());
			}
		}

		return instanceIds;
	}

	protected List<Deployable> resolveDeployables(PersistenceGmSession gmSession, boolean shallowify, Set<String> externalIds) {
		EntityQuery query = EntityQueryBuilder.from(Deployable.T).where().property(Deployable.externalId).in(externalIds).done();

		return resolveDeployables(gmSession, shallowify, query, false);
	}

	protected List<Deployable> resolveDeployables(PersistenceGmSession gmSession, boolean shallowify, Query query, boolean includeHardwired)
			throws DeploymentException {

		boolean trace = log.isTraceEnabled();
		StopWatch stopWatch = new StopWatch();

		List<Deployable> queryResult = null;
		try {
			query = GmReflectionTools.makeShallowCopy(query);

			if (!shallowify && query.getTraversingCriterion() == null)
				query.setTraversingCriterion(PreparedTcs.everythingTc);

			stopWatch.intermediate("Shallow copy");

			queryResult = gmSession.queryDetached().abstractQuery(query).list();
			stopWatch.intermediate("Query");

		} catch (GmSessionException e) {
			throw new DeploymentException("Failed to query the deployables to be resolved", e);
		}

		// filter only Deployables that are not hardwired based on includeHardwired
		if (!includeHardwired) {
			queryResult = queryResult.stream().filter(d -> !(d instanceof HardwiredDeployable)).collect(Collectors.toList());
			stopWatch.intermediate("Exclude Hardwired");
		}

		List<Deployable> deployables = new ArrayList<>(queryResult.size());

		for (Deployable deployable : queryResult) {
			deployable = preProcess(deployable);
			if (trace)
				stopWatch.intermediate("Pre-process " + deployable.getExternalId());

			if (shallowify && gmSession == deployable.session()) {
				deployable = shallowify(deployable);
				if (trace)
					stopWatch.intermediate("Shallowify " + deployable.getExternalId());
			}
			deployables.add(deployable);
		}

		log.trace(() -> " Resolve deployables: " + stopWatch);

		return deployables;
	}

	protected Deployable preProcess(Deployable deployable) {
		/* TODO replace this static algorithm by a new extension point base algorithm - DeployPreProcessor extends Deployable -
		 * DeployPreProcessWith extends EntityTypeMetaData - use the MD to fetch processors from deployregistry */

		for (DeploymentPreProcessor preProcessor : preProcessors)
			deployable = preProcessor.preProcess(deployable);

		return deployable;
	}

	/**
	 * <p>
	 * Creates a trimmed clone of the deployable that is easily transportable via rpc calls.
	 * 
	 * TODO: Review: this method should only shallowify deployables with persistence ids given and keep all the transient.
	 * 
	 * @param deployable
	 *            The deployable
	 * @return A clone of the deployable
	 */
	protected Deployable shallowify(Deployable deployable) {
		EntityType<? extends Deployable> entityType = deployable.entityType();

		StandardMatcher matcher = new StandardMatcher();
		matcher.setCriterion(shallowifyingCriteria);

		StandardCloningContext cloningContext = new StandardCloningContext();
		cloningContext.setAbsenceResolvable(true);
		cloningContext.setMatcher(matcher);

		Deployable exportedDeployable = (Deployable) entityType.clone(cloningContext, deployable, StrategyOnCriterionMatch.partialize);

		return exportedDeployable;
	}

	/**
	 * <p>
	 * Broadcasts the given {@link InternalDeploymentRequest} to every available cartridge instance.
	 * 
	 * @param evaluator
	 *            The master {@link Evaluator} of {@link ServiceRequest serviceRequests}
	 * @param request
	 *            The {@link InternalDeploymentRequest} to be broadcasted to every available cartridge instance.
	 * @return The wrapping the results from the reached cartridge instances.
	 * @throws DeploymentException
	 *             If the broadcasting fails.
	 */
	protected MulticastResponse broadcast(Evaluator<ServiceRequest> evaluator, InternalDeploymentRequest request) throws DeploymentException {
		MulticastRequest multicastRequest = MulticastRequest.T.create();
		multicastRequest.setServiceRequest(request);
		if (requestTimeout != null)
			multicastRequest.setTimeout(requestTimeout);

		return multicastRequest.eval(evaluator).get();
	}

	private List<InstanceDeployment> fillResponseAndGetInstanceDeployments(DeploymentResponse response,
			Map<InstanceId, ServiceResult> internalResponses, boolean isDeploy) {

		List<InstanceDeployment> deployments = new ArrayList<>();
		for (Entry<InstanceId, ServiceResult> entry : internalResponses.entrySet()) {

			InstanceId origin = entry.getKey();
			ServiceResult result = entry.getValue();

			try {
				switch (result.resultType()) {

					case success:

						InternalDeploymentResponse idr = (InternalDeploymentResponse) ((ResponseEnvelope) result).getResult();

						if (idr == null) {
							log.warn("Instance " + origin + " ignored the internal deployment request (returned null)");
							continue;
						}

						for (Entry<String, ServiceResult> instanceEntry : idr.getResults().entrySet()) {

							String externalId = instanceEntry.getKey();
							ServiceResult deployResult = instanceEntry.getValue();

							DeploymentResponseMessage message = DeploymentResponseMessage.T.create();
							message.setExternalId(externalId);
							message.setOriginId(origin);

							try {
								switch (deployResult.resultType()) {
									case success:
										message.setMessage("Success");
										message.setSuccessful(true);
										response.getResults().add(message);

										InstanceDeployment d = InstanceDeployment.T.create();
										d.setApplicationId(origin.getApplicationId());
										d.setNodeId(origin.getNodeId());
										d.setExternalId(externalId);
										d.setDeployed(isDeploy);
										deployments.add(d);

										break;
									case failure:
										Failure failure = (Failure) deployResult;
										message.setMessage(failure.getMessage());
										message.setDetails(failure.getType());
										message.setSuccessful(false);
										response.getResults().add(message);
										log.error("Instance " + origin + " failed to process the internal deployment request: " + failure.getType()
												+ ": " + failure.getMessage());
										break;
									default:
										log.warn("Unexpected type of response from " + origin + ": " + result);
								}

							} catch (Exception e) {
								log.error("Failed to process " + result + " from " + origin, e);
							}

						}

						break;

					case failure:

						DeploymentResponseMessage message = DeploymentResponseMessage.T.create();
						message.setOriginId(origin);

						Failure failure = (Failure) result;
						message.setMessage(failure.getMessage());
						message.setDetails(failure.getType());
						message.setSuccessful(false);
						response.getResults().add(message);
						log.error("Instance " + origin + " failed to process the internal deployment request: " + failure.getType() + ": "
								+ failure.getMessage());
						break;
					default:
						log.warn("Unexpected type of response from " + origin + ": " + result);
				}
			} catch (Exception e) {
				log.error("Failed to process " + result + " from " + origin, e);
			}

		}
		return deployments;

	}

	private Set<String> validate(DeploymentOperation request) throws DeploymentException {
		Set<String> externalIds = request.getExternalIds();
		validate(request, externalIds);

		return externalIds;
	}

	private Set<String> validate(DeploymentOperationWithDeployables request) {
		Set<String> externalIds = request.getDeployables().stream() //
				.map(Deployable::getExternalId) //
				.collect(Collectors.toSet());

		validate(request, externalIds);

		return externalIds;
	}

	private void validate(GenericEntity request, Set<String> externalIds) {
		if (log.isInfoEnabled())
			log.info("Processing " + request.entityType().getShortName() + " request for " + externalIds);

		if (externalIds.isEmpty())
			throw new DeploymentException("No external id was provided in the incoming request: " + request);
	}

	private void flagAutoDeploy(PersistenceGmSession gmSession, Set<String> externalIds, boolean autoDeploy) {
		EntityQuery query = EntityQueryBuilder.from(Deployable.T).where().property(Deployable.externalId).in(externalIds).done();

		try {
			List<Deployable> deployables = gmSession.query().entities(query).list();
			for (Deployable deployable : deployables)
				deployable.setAutoDeploy(autoDeploy);

			log.debug(() -> "Deployables " + externalIds + " flagged with autoDeploy=" + autoDeploy);

		} catch (GmSessionException e) {
			throw new DeploymentException("Failed to flag deployables [ " + externalIds + " ] as autoDeploy=" + autoDeploy, e);
		}
	}

	private PersistenceGmSession retrieveGmSession() throws DeploymentException {
		try {
			return gmSessionProvider.get();
		} catch (Exception e) {
			throw new DeploymentException("Failed to retrieve a gm session", e);
		}
	}

	private List<Notification> createNotifications(DeploymentResponse response, String description) {
		List<Notification> notifications = new ArrayList<>();
		StringBuilder m = new StringBuilder();
		String p = description.equals("deploy") ? " to " : " from ";

		boolean failure = false;
		for (DeploymentResponseMessage r : response.getResults()) {

			if (r.getSuccessful()) {

				StringBuilder message = new StringBuilder().append("Successfully ").append(description).append("ed ").append(r.getExternalId())
						.append(p).append(r.getOriginId());
				m.append(message.toString());

				addMessageNotification(notifications, message.toString(), Level.INFO);

			} else {
				failure = true;

				String message = "Failed to " + description + " " + r.getExternalId() + p + r.getOriginId() + ": " + r.getMessage();
				m.append(message);

				addMessageNotification(notifications, message, Level.ERROR);
			}
			m.append(". ");
		}

		String message = m.toString();

		if (log.isDebugEnabled())
			log.debug(message);

		CommandNotification commandNotification = createRefreshNotification();
		notifications.add(commandNotification);

		String operationString = description.equals("deploy") ? "Deployment" : "Undeployment";
		if (failure) {
			addMessageNotification(notifications, operationString + " failed!", Level.ERROR);
		} else {
			addMessageNotification(notifications, operationString + " succeeded!", Level.INFO);
		}

		return notifications;
	}

	private CommandNotification createRefreshNotification() {
		Refresh refreshCommand = Refresh.T.create();
		refreshCommand.setName("Refresh deployable");

		CommandNotification commandNotification = CommandNotification.T.create();
		commandNotification.setCommand(refreshCommand);
		return commandNotification;
	}

	private void addMessageNotification(List<Notification> notifications, String message, Level level) {
		MessageNotification notification = createMessageNotification(message, level);
		notifications.add(notification);
	}

	private MessageNotification createMessageNotification(String message, Level level) {
		MessageNotification notification = MessageNotification.T.create();
		notification.setMessage(message);
		notification.setLevel(level);
		return notification;
	}

	private <T extends DeploymentResponse> T finalizeResponse(T response, List<Notification> notifications) {
		Collections.reverse(notifications);
		response.setNotifications(notifications);
		return response;
	}

	private static EntityHashingComparator<InstanceId> instanceIdComparator = EntityHashingComparator //
			.build(InstanceId.T) //
			.addField(InstanceId.applicationId) //
			.addField(InstanceId.nodeId) //
			.done();

	private class DeploymentStatusRegistry {
		private final Map<EqProxy<InstanceId>, DeploymentStatusEntry> entries = new ConcurrentHashMap<>();

		public DeploymentStatusEntry aquireEntry(InstanceId instanceId) {
			return entries.computeIfAbsent(instanceIdComparator.eqProxy(instanceId), k -> new DeploymentStatusEntry(instanceId));
		}

		public DeploymentStatusEntry removeEntry(InstanceId instanceId) {
			return entries.remove(instanceIdComparator.eqProxy(instanceId));
		}

	}

	private class DeploymentStatusEntry {
		private final InstanceId instanceId;
		private final Set<String> deployedExternalIds = new HashSet<>();
		private ReentrantLock lock = new ReentrantLock();
		private boolean initialized;

		public DeploymentStatusEntry(InstanceId instanceId) {
			this.instanceId = instanceId;
		}

		public boolean isInitialized() {
			return initialized;
		}

		public InstanceId getInstanceId() {
			return instanceId;
		}

		public boolean isDeployed(String externalId) {
			lock.lock();
			try {
				return deployedExternalIds.contains(externalId);
			} finally {
				lock.unlock();
			}
		}

		// remove because it got undeployed
		public void removeDeployedExternalId(String externalId) {
			lock.lock();
			try {
				deployedExternalIds.remove(externalId);
			} finally {
				lock.unlock();
			}
		}

		public void addDeployedExternalId(String externalId) {
			lock.lock();
			try {
				deployedExternalIds.add(externalId);
			} finally {
				lock.unlock();
			}
		}

		public void setDeployedExternalIds(Set<String> externalIds) {
			lock.lock();
			try {
				deployedExternalIds.clear();
				deployedExternalIds.addAll(externalIds);
			} finally {
				lock.unlock();
			}
			initialized = true;
		}
	}

	//
	// Track live instances coming into existence and fading away again in order to
	// keep a proper deployment status.
	//

	@Override
	public void onShutdown(ApplicationLifecycleListenerContext context) {
		InstanceId instanceId = InstanceId.of(context.nodeId(), context.applicationId());
		deploymentStatusRegistry.removeEntry(instanceId);

		log.debug("Unregistered deployed deployables for " + instanceId.stringify() + " because no heartbeat has been received.");

		updateDeploymentStatus();
	}

	@Override
	public void onHeartbeat(ApplicationLifecycleListenerContext context) {
		InstanceId instanceId = InstanceId.of(context.nodeId(), context.applicationId());

		DeploymentStatusEntry entry = this.deploymentStatusRegistry.aquireEntry(instanceId);
		if (entry.isInitialized())
			return;

		UserSessionScope scope = userSessionScoping.forDefaultUser().push();

		GetDeployedDeployables getDeployedDeployables = GetDeployedDeployables.T.create();
		UnicastRequest request = UnicastRequest.T.create();
		request.setServiceRequest(getDeployedDeployables);
		request.setAddressee(instanceId);

		try {
			@SuppressWarnings("unchecked")
			List<String> deployedExternalIds = (List<String>) request.eval(systemEvaluator).get();

			entry.setDeployedExternalIds(new HashSet<>(deployedExternalIds));

			log.debug("Registered " + deployedExternalIds.size() + " deployed deployables: " + deployedExternalIds);

		} catch (Throwable t) {
			log.error("Error while retrieving deployment status for " + instanceId.stringify(), t);
		} finally {
			scope.pop();
		}

		updateDeploymentStatus();
	}

}