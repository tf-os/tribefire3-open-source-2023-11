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
package tribefire.platform.wire.space.cortex.deployment;

import static com.braintribe.wire.api.util.Lists.list;
import static com.braintribe.wire.api.util.Sets.set;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cartridge.common.processing.deployment.DeploymentScope;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.HardwiredCollaborativeAccess;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.extensiondeployment.HardwiredServiceProcessor;
import com.braintribe.model.extensiondeployment.check.HardwiredCheckProcessor;
import com.braintribe.model.extensiondeployment.check.HardwiredParameterizedCheckProcessor;
import com.braintribe.model.platformsetup.api.request.TransferAsset;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.check.api.ParameterizedAccessCheckProcessor;
import com.braintribe.model.processing.check.hw.MemoryCheckProcessor;
import com.braintribe.model.processing.check.jdbc.DatabaseConnectionsCheck;
import com.braintribe.model.processing.check.jdbc.SelectedDatabaseConnectionsCheck;
import com.braintribe.model.processing.deployment.DynamicDeployableResolver;
import com.braintribe.model.processing.deployment.api.DeploymentScoping;
import com.braintribe.model.processing.deployment.api.DeploymentService;
import com.braintribe.model.processing.deployment.api.SchrodingerBean;
import com.braintribe.model.processing.platformsetup.PlatformSetupManager;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.check.BaseConnectivityCheckProcessor;
import tribefire.platform.impl.check.BaseFunctionalityCheckProcessor;
import tribefire.platform.impl.check.BaseVitalityCheckProcessor;
import tribefire.platform.impl.deployment.BasicDeployRegistry;
import tribefire.platform.impl.deployment.DeploymentProcessor;
import tribefire.platform.impl.deployment.HardwiredComponent;
import tribefire.platform.impl.deployment.HardwiredDenotationBinding;
import tribefire.platform.impl.deployment.InternalDeploymentProcessor;
import tribefire.platform.impl.deployment.ParallelDeploymentService;
import tribefire.platform.impl.deployment.SystemDeploymentListenerRegistry;
import tribefire.platform.impl.deployment.WireDeploymentScoping;
import tribefire.platform.impl.deployment.proxy.ProxyingDeployedComponentResolver;
import tribefire.platform.impl.deployment.reflection.DeploymentReflectionProcessor;
import tribefire.platform.impl.lifecycle.TribefireServerActivation;
import tribefire.platform.wire.space.MasterResourcesSpace;
import tribefire.platform.wire.space.common.BindersSpace;
import tribefire.platform.wire.space.common.CartridgeInformationSpace;
import tribefire.platform.wire.space.common.HttpSpace;
import tribefire.platform.wire.space.common.MarshallingSpace;
import tribefire.platform.wire.space.common.MessagingSpace;
import tribefire.platform.wire.space.common.ResourceProcessingSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.cortex.accesses.HardwiredAccessSpaceBase;
import tribefire.platform.wire.space.cortex.accesses.PlatformSetupAccessSpace;
import tribefire.platform.wire.space.cortex.accesses.SystemAccessCommonsSpace;
import tribefire.platform.wire.space.cortex.accesses.SystemAccessesSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.DeployablesSpace;
import tribefire.platform.wire.space.cortex.services.WorkerSpace;
import tribefire.platform.wire.space.messaging.MulticastSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;
import tribefire.platform.wire.space.security.AuthenticatorsSpace;
import tribefire.platform.wire.space.streaming.ResourceAccessSpace;
import tribefire.platform.wire.space.system.ChecksSpace;
import tribefire.platform.wire.space.system.MetaSpace;
import tribefire.platform.wire.space.system.SystemTasksSpace;
import tribefire.platform.wire.space.system.TopologySpace;

@Managed
public class DeploymentSpace implements WireSpace {

	private static final Logger logger = Logger.getLogger(DeploymentSpace.class);

	// @formatter:off
	@Import private AuthContextSpace authContext;
	@Import private AuthenticatorsSpace authenticators;
	@Import private BindersSpace binders;
	@Import private CartridgeInformationSpace cartridgeInformation;
	@Import private ChecksSpace checks;
	@Import private CortexAccessSpace cortexAccess;
	@Import private DeployablesSpace deployables;
	@Import private DeploymentScope deploymentScope;
	@Import private HttpSpace http;
	@Import private MarshallingSpace marshalling;
	@Import private MasterResourcesSpace resources;
	@Import private MessagingSpace messaging;
	@Import private MetaSpace meta;
	@Import private MulticastSpace multicast;
	@Import private PlatformSetupAccessSpace platformSetupAccess;
	@Import private ResourceAccessSpace resourceAccess;
	@Import private ResourceProcessingSpace resourceProcessing;
	@Import private RpcSpace rpc;
	@Import private SystemAccessCommonsSpace systemAccessCommons;
	@Import private SystemAccessesSpace systemAccesses;
	@Import private SystemTasksSpace systemTasks;
	@Import private TopologySpace topology;
	@Import private WorkerSpace worker;
	// @formatter:on

	@Managed
	public HardwiredDenotationBinding hardwiredBindings() {

		StopWatch stopWatch = new StopWatch();

		HardwiredDenotationBinding bean = new HardwiredDenotationBinding();
		bean.setInterfaceBindings(deployables.interfaceBindings());

		// @formatter:off
		// bind system accesses
		List<HardwiredAccessSpaceBase> accessSpaces = list(
				// Keep cortex first, deploying any other access first triggers cortex anyway, this way troubleshooting is simpler
				systemAccesses.cortex(),
				systemAccesses.cortexWorkbench(),
				systemAccesses.workbench(),
				systemAccesses.authWorkbench(),
				systemAccesses.userSessionsWorkbench(),
				systemAccesses.userStatisticsWorkbench(),
				systemAccesses.platformSetup(),
				systemAccesses.platformSetupWorkbench(),
				systemAccesses.transientMessagingDataWorkbenchAccess());
		// @formatter:on

		stopWatch.intermediate("Access Spaces List");

		for (HardwiredAccessSpaceBase accessSpace : accessSpaces) {
			if (accessSpace.isCollaborativeAccess())
				bean.bind((HardwiredCollaborativeAccess) accessSpace.hardwiredDeployable()) //
						.component(binders.incrementalAccess(), accessSpace::aopAccess) //
						.component(binders.collaborativeAccess(), accessSpace::collaborativeAccess);
			else
				bean.bind(accessSpace.hardwiredDeployable()) //
						.component(binders.incrementalAccess(), accessSpace::aopAccess);
		}

		stopWatch.intermediate("Access Spaces Binding");

		// marshallers
		marshalling.hardwiredMarshallers().forEach( //
				(denotation, marshaller) -> bean.bind(denotation).component(binders.marshaller(), () -> marshaller));

		stopWatch.intermediate("Marshallers");

		// bind binary processors
		bean.bind(resourceAccess.defaultBinaryPersistenceDeployable()) //
				.component(binders.binaryPersistenceProcessor(), resourceAccess::defaultBinaryPeristenceProcessor);

		bean.bind(resourceAccess.fileSystemBinaryProcessorDeployable()) //
				.component(binders.binaryPersistenceProcessor(), resourceAccess::fileSystemBinaryProcessor) //
				.component(binders.binaryRetrievalProcessor(), resourceAccess::fileSystemBinaryProcessor);

		bean.bind(systemAccessCommons.moduleSourceBinaryRetrievalDeployable()) //
				.component(binders.binaryRetrievalProcessor(), systemAccessCommons::moduleSourceBinaryRetrieval);

		if (systemAccessCommons.isDistributedSetup())
			bean.bind(systemAccessCommons.csaBinaryRetrievalDeployable()) //
					.component(binders.binaryRetrievalProcessor(), systemAccessCommons::csaBinaryRetrieval);

		bean.bind(resourceAccess.templateBinaryRetrievalDeployable()).component(binders.binaryRetrievalProcessor(),
				resourceAccess::templateRetrieval);

		stopWatch.intermediate("Binary Processors");

		// bind resource enrichers
		bean.bind(resourceAccess.mimeTypeDetectingEnricherDeployable()) //
				.component(binders.resourceEnricherProcessor(), resourceAccess::mimeTypeDetectingEnricher);
		bean.bind(resourceAccess.mimeBasedDispatchingResourceEnricherDeployable()) //
				.component(binders.resourceEnricherProcessor(), resourceAccess::mimeBasedDispatchingResourceEnricher);

		bean.bind(resourceAccess.standardPrePersistenceEnricherDeployable()) //
				.component(binders.resourceEnricherProcessor(), resourceAccess::standardPrePersistenceEnricher);
		bean.bind(resourceAccess.standardPostPersistenceEnricherDeployable()) //
				.component(binders.resourceEnricherProcessor(), resourceAccess::standardPostPersistenceEnricher);

		stopWatch.intermediate("Enrichers");

		// bind check processors
		bindParameterizedAccessCheck(bean, SelectedDatabaseConnectionsCheck.class, checks::selectedDatabaseConnectionsCheck);
		bindParameterizedAccessCheck(bean, MemoryCheckProcessor.class, checks::memoryCheckProcessor);

		bindCheck(bean, BaseFunctionalityCheckProcessor.class, checks::baseFunctionalityCheckProcessor);
		bindCheck(bean, BaseConnectivityCheckProcessor.class, checks::baseConnectivityCheckProcessor);
		bindCheck(bean, BaseVitalityCheckProcessor.class, checks::baseVitalityCheckProcessor);
		bindCheck(bean, DatabaseConnectionsCheck.class, checks::databaseConnectionCheck);

		stopWatch.intermediate("Checks");

		// workers
		bean.bind(multicast.consumerDeployable()).component(binders.worker(), multicast::consumer);
		bean.bind(rpc.mqServerDeployable()).component(binders.worker(), rpc::mqServer);
		bean.bind(topology.heartbeatManagerDeployable()).component(binders.worker(), topology::heartbeatManager);

		stopWatch.intermediate("Workers");

		// @formatter:on

		// register hardwired deployed authenticators
		for (HardwiredComponent<HardwiredServiceProcessor, ServiceProcessor<?, ?>> component : authenticators.hardwiredComponents().values()) {
			bean.bind(component.getTransientDeployable()).component(binders.serviceProcessor(), component.getExpertSupplier());
		}

		logger.debug(() -> "Creating HardwiredDenotationBinding took: " + stopWatch);

		return bean;
	}

	private <C extends ParameterizedAccessCheckProcessor<?>> void bindParameterizedAccessCheck(HardwiredDenotationBinding bean, Class<C> clazz,
			Supplier<C> supplier) {
		HardwiredParameterizedCheckProcessor deployable = ChecksSpace.parameterizedAccessCheckDeployable(clazz);
		bean.bind(deployable).component(binders.parameterizedAccessCheckProcessor(), supplier);
	}

	private <C extends CheckProcessor> void bindCheck(HardwiredDenotationBinding bean, Class<C> clazz, Supplier<C> supplier) {
		HardwiredCheckProcessor deployable = ChecksSpace.checkProcessorDeployable(clazz);
		bean.bind(deployable).component(binders.checkProcessor(), supplier);
	}

	@Managed
	public BasicDeployRegistry registry() {
		BasicDeployRegistry bean = new BasicDeployRegistry();
		return bean;
	}

	@Managed
	public SystemDeploymentListenerRegistry systemDeploymentListenerRegistry() {
		SystemDeploymentListenerRegistry bean = new SystemDeploymentListenerRegistry();
		bean.setExecutor(systemTasks.scheduledExecutor());

		return bean;
	}

	public DeploymentService service() {
		return parallelDeploymentService();
	}

	/**
	 * Those bindings below are a supplier, because we want to create the parallelDeploymentService as fast as possible. The reason is this is also
	 * needed for the {@link #inDeploymentBlocker()}.
	 * <p>
	 * The first thing accessed from the outside is {@link #proxyingDeployedComponentResolver()}, probably by some {@link SchrodingerBean}.
	 * <p>
	 * That one depends on {@link #inDeploymentBlocker()}, which depends on this method.
	 * <p>
	 * Before I changed this, we simply accessed deployables.bindings(), which in the end triggered deployment, and that meant that deployment is
	 * running while {@link #proxyingDeployedComponentResolver()} still doesn't have property {@link #inDeploymentBlocker()}.
	 * <p>
	 * As a consequence, hardwired beans which are proxies based on a SchrodingerBean would not wait on their deployable to be deployed, and if
	 * accessed they would throw the exception that their deployable is not deployed yet.
	 */
	@Managed
	public ParallelDeploymentService parallelDeploymentService() {
		ParallelDeploymentService bean = new ParallelDeploymentService();
		bean.setDenotationTypeBindings(deployables.bindings()); // see method comment
		bean.setDeploymentScoping(scoping());
		bean.setDeployRegistry(registry());
		bean.setDeployedComponentResolver(proxyingDeployedComponentResolver());

		String standardThreadCountString = TribefireRuntime.getProperty("TRIBEFIRE_DEPLOYMENT_STANDARD_THREADS", "5");
		int standardThreadCount = 5;
		try {
			standardThreadCount = Integer.parseInt(standardThreadCountString);
		} catch (Exception e) {
			logger.warn("Could not parse the TRIBEFIRE_DEPLOYMENT_STANDARD_THREADS value " + standardThreadCountString + " as a number.", e);
		}

		bean.setStandardParallelDeployments(standardThreadCount);
		bean.setThreadContextScoping(authContext.currentUser().threadContextScoping());
		return bean;
	}

	@Managed
	public DeploymentScoping scoping() {
		WireDeploymentScoping bean = new WireDeploymentScoping();
		bean.setScope(deploymentScope);
		return bean;
	}

	@Managed
	public DeploymentProcessor processor() {
		DeploymentProcessor bean = new DeploymentProcessor();
		bean.setApplicationId(cartridgeInformation.applicationId());
		bean.setGmSessionProvider(cortexAccess.sessionProvider());
		bean.setDeploymentActivated(!TribefireRuntime.getExecutionMode().equals("design"));
		bean.setLiveInstances(topology.liveInstances());
		bean.setUserSessionScoping(authContext.internalUser().userSessionScoping());
		bean.setSystemEvaluator(rpc.serviceRequestEvaluator());
		return bean;
	}

	@Managed
	public InternalDeploymentProcessor internalProcessor() {
		InternalDeploymentProcessor bean = new InternalDeploymentProcessor();
		bean.setCortexSessionProvider(cortexAccess.sessionProvider());
		bean.setDeploymentService(service());
		bean.setDeployRegistry(registry());
		return bean;
	}

	@Managed
	public TribefireServerActivation activation() {
		// depends-on
		messaging.sessionProvider();

		TribefireServerActivation bean = new TribefireServerActivation();
		bean.setProcessingInstanceId(cartridgeInformation.instanceId());
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		bean.setCortexSessionProvider(cortexAccess.sessionProvider());
		bean.setDeploymentService(service());
		bean.setDeployRegistry(registry());
		bean.setSystemDeploymentListenerRegistry(systemDeploymentListenerRegistry());
		bean.setWorkerManagerControl(worker.manager());
		bean.setSkipDeployablesDeployment(TribefireRuntime.getExecutionMode().equals("design"));
		bean.setUserSessionScoping(authContext.internalUser().userSessionScoping());
		bean.setLiveInstances(topology.liveInstances());
		bean.setHeartbeatManager(topology.heartbeatManager());

		return bean;
	}

	public void configDynamicDeployableResolver(DynamicDeployableResolver<?> config) {
		config.setDeployRegistry(registry());
		config.setType(CortexConfiguration.class);
		config.setInstanceSelectorProperty(CortexConfiguration.id);
		config.setInstanceSelectorPropertyValue("singleton");
		config.setSessionProvider(cortexAccess::lowLevelSession);
	}

	public ProxyingDeployedComponentResolver proxyingDeployedComponentResolver() {
		/* We need this service to be initialized first, so that when setting the InDeploymentBlocker of proxyingDeployedComponentResolverBean(), it
		 * can be done.
		 * 
		 * The thing is, initializing parallelDeploymentService() leads back to this method via a SchrodingerBean. So if it is called first, we have
		 * at least a stub of this Service and can set the InDeploymentBlocker on the ProxyingResolver that we return.
		 * 
		 * If we first tried to instantiate the ProxyingResolver, we would get back here via a SchrodingerBean, that bean would get a stub of this
		 * ProxyingResolver without InDeploymentBlocker, and thus the proxy created would have an InDeploymentBlocker that doesn't wait for
		 * deployment. */
		parallelDeploymentService();

		return proxyingDeployedComponentResolverBean();
	}

	@Managed
	private ProxyingDeployedComponentResolver proxyingDeployedComponentResolverBean() {
		ProxyingDeployedComponentResolver bean = new ProxyingDeployedComponentResolver();
		bean.setDeployRegistry(registry());
		bean.setProcessingInstanceId(cartridgeInformation.instanceId());
		bean.setComponentInterfaceBindings(deployables.interfaceBindings());
		bean.setInDeploymentBlocker(inDeploymentBlocker());
		return bean;
	}

	private Consumer<String> inDeploymentBlocker() {
		return parallelDeploymentService()::waitForDeployableIfInDeployment;
	}

	@Managed
	public DeploymentReflectionProcessor deploymentReflectionProcessor() {
		DeploymentReflectionProcessor bean = new DeploymentReflectionProcessor();
		bean.setDeployRegistry(registry());
		bean.setBeanHolderLookup(meta.deploymentScopeOriginManager()::resolveBeanHolder);
		bean.setUserRolesProvider(authContext.currentUser().rolesProvider());
		bean.setProxyResolver(proxyingDeployedComponentResolver());
		bean.setAllowedRoles(set("tf-admin", "tf-internal"));
		return bean;
	}

	/**
	 * The keys for registered expert factories are match {@link TransferAsset#getTransferOperation()}, i.e. handling these OPs looks up the expert
	 * registry for these factories.
	 */
	@Managed
	public PlatformSetupManager platformSetupManager() {
		PlatformSetupManager bean = new PlatformSetupManager();
		bean.setPlatformSetupSessionProvider(platformSetupAccess.sessionProvider());
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		bean.setResourceBuilder(resourceProcessing.transientResourceBuilder());

		return bean;
	}

}
