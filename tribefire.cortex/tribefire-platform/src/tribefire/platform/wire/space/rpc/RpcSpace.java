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
package tribefire.platform.wire.space.rpc;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.execution.virtual.VirtualThreadExecutor;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.gm.marshaller.processing.MarshallerProcessor;
import com.braintribe.gm.model.marshaller.api.request.AbstractMarshallRequest;
import com.braintribe.gm.model.persistence.reflection.api.PersistenceReflectionRequest;
import com.braintribe.gm.model.user_session_service.CleanupUserSessions;
import com.braintribe.gm.model.user_session_service.UserSessionRequest;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessapi.GmqlRequest;
import com.braintribe.model.accessory.ModelRetrievingRequest;
import com.braintribe.model.bapi.AvailableAccessesRequest;
import com.braintribe.model.bapi.CurrentUserInformationRequest;
import com.braintribe.model.check.service.CompositeCheck;
import com.braintribe.model.cortexapi.access.GarbageCollectionRequest;
import com.braintribe.model.cortexapi.access.SetupAccessRequest;
import com.braintribe.model.cortexapi.connection.CreateModelFromDbSchema;
import com.braintribe.model.cortexapi.connection.DbSchemaRequest;
import com.braintribe.model.cortexapi.connection.TestConnectionRequest;
import com.braintribe.model.cortexapi.model.ModelRequest;
import com.braintribe.model.cortexapi.service.SetRuntimeProperty;
import com.braintribe.model.cortexapi.workbench.CreateServiceRequestTemplate;
import com.braintribe.model.deploymentapi.check.request.CheckBundlesRequest;
import com.braintribe.model.deploymentapi.request.DeploymentRequest;
import com.braintribe.model.deploymentapi.request.InternalDeploymentRequest;
import com.braintribe.model.deploymentreflection.request.DeploymentReflectionRequest;
import com.braintribe.model.exchangeapi.ExchangeRequest;
import com.braintribe.model.execution.persistence.ExecutionPersistenceRequest;
import com.braintribe.model.extensiondeployment.HardwiredWorker;
import com.braintribe.model.license.service.AbstractLicenseRequest;
import com.braintribe.model.logs.request.LogsRequest;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.modelnotification.InternalModelNotificationRequest;
import com.braintribe.model.modelnotification.ModelNotificationRequest;
import com.braintribe.model.platform.service.ExternalTribefireRequest;
import com.braintribe.model.platformreflection.request.PlatformReflectionRequest;
import com.braintribe.model.platformsetup.api.request.PlatformAssetRequest;
import com.braintribe.model.platformsetup.api.request.TrunkAssetRequestForAccess;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.core.expert.api.MutableDenotationMap;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.model.processing.mqrpc.server.GmMqRpcServer;
import com.braintribe.model.processing.securityservice.api.ServiceUserSessionScoping;
import com.braintribe.model.processing.securityservice.commons.scope.StandardServiceUserSessionScoping;
import com.braintribe.model.processing.securityservice.commons.service.AuthorizingServiceInterceptor;
import com.braintribe.model.processing.securityservice.commons.service.ExecuteAuthorizedServiceProcessor;
import com.braintribe.model.processing.securityservice.commons.utils.AuthenticationExceptionFactory;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.common.CompositeServiceProcessor;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.service.common.ElapsedTimeMeasuringInterceptor;
import com.braintribe.model.processing.service.common.RoleBasedAuthorizingInterceptor;
import com.braintribe.model.processing.service.common.ThreadNamingInterceptor;
import com.braintribe.model.processing.service.common.UnicastProcessor;
import com.braintribe.model.processing.service.common.eval.AuthorizingServiceRequestEvaluator;
import com.braintribe.model.processing.service.common.eval.ConfigurableServiceRequestEvaluator;
import com.braintribe.model.prototyping.api.StaticPrototyping;
import com.braintribe.model.prototyping.impl.StaticPrototypingProcessor;
import com.braintribe.model.resource.api.MimeTypeRegistry;
import com.braintribe.model.resourceapi.base.BinaryRequest;
import com.braintribe.model.resourceapi.base.ResourceSourceRequest;
import com.braintribe.model.resourceapi.persistence.ManageResource;
import com.braintribe.model.resourceapi.persistence.UploadResources;
import com.braintribe.model.resourceapi.request.FixSqlSources;
import com.braintribe.model.resourceapi.stream.DownloadResource;
import com.braintribe.model.resourceapi.stream.DownloadSource;
import com.braintribe.model.securityservice.AuthenticateCredentials;
import com.braintribe.model.securityservice.Logout;
import com.braintribe.model.securityservice.SecurityRequest;
import com.braintribe.model.securityservice.SimplifiedOpenUserSession;
import com.braintribe.model.service.api.AsynchronousRequest;
import com.braintribe.model.service.api.CompositeRequest;
import com.braintribe.model.service.api.ExecuteAuthorized;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.PushRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.UnicastRequest;
import com.braintribe.model.service.api.result.AsynchronousResponse;
import com.braintribe.model.service.cortex.publish.PublishModelRequest;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.api.util.Sets;

import tribefire.platform.impl.bootstrapping.AvailableAccessesRequestProcessor;
import tribefire.platform.impl.bootstrapping.CurrentUserInformationRequestProcessor;
import tribefire.platform.impl.bootstrapping.PersistenceReflectionServiceProcessor;
import tribefire.platform.impl.deployment.HardwiredServiceProcessorRegistration;
import tribefire.platform.impl.push.PushProcessor;
import tribefire.platform.impl.rpc.GmWebRpcClientMetaDataProvider;
import tribefire.platform.impl.service.AsynchronousServiceProcessor;
import tribefire.platform.impl.service.ServiceDomainMappedDispatchingInterceptor;
import tribefire.platform.impl.service.StandardMetaDataResolverProvider;
import tribefire.platform.impl.service.StandardRequestContextThreadContextScopeSupplier;
import tribefire.platform.impl.service.async.CallbackExpert;
import tribefire.platform.impl.service.async.PersistingAsynchronousServiceProcessor;
import tribefire.platform.impl.service.async.ServiceRequestPersistence;
import tribefire.platform.wire.space.common.BindersSpace;
import tribefire.platform.wire.space.common.CartridgeInformationSpace;
import tribefire.platform.wire.space.common.HttpSpace;
import tribefire.platform.wire.space.common.MarshallingSpace;
import tribefire.platform.wire.space.common.MessagingSpace;
import tribefire.platform.wire.space.common.ResourceProcessingSpace;
import tribefire.platform.wire.space.common.RuntimeSpace;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.cortex.ModelNotificationSpace;
import tribefire.platform.wire.space.cortex.ServicesSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.streaming.BinaryProcessorsSpace;
import tribefire.platform.wire.space.cortex.services.AccessServiceSpace;
import tribefire.platform.wire.space.messaging.MulticastSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;
import tribefire.platform.wire.space.security.accesses.AuthAccessSpace;
import tribefire.platform.wire.space.security.services.SecurityServiceSpace;
import tribefire.platform.wire.space.security.services.SimpleSecurityServiceSpace;
import tribefire.platform.wire.space.security.services.UserSessionServiceSpace;
import tribefire.platform.wire.space.security.servlets.SecurityServletSpace;
import tribefire.platform.wire.space.streaming.MimeTypeSpace;
import tribefire.platform.wire.space.streaming.ResourceAccessSpace;
import tribefire.platform.wire.space.system.ChecksSpace;
import tribefire.platform.wire.space.system.ExecutionPersistenceSpace;
import tribefire.platform.wire.space.system.LicenseSpace;
import tribefire.platform.wire.space.system.SystemInformationSpace;
import tribefire.platform.wire.space.system.servlets.SystemServletsSpace;

@Managed
public class RpcSpace implements WireSpace {

	private static final Logger logger = Logger.getLogger(RpcSpace.class);

	// @formatter:off
	@Import private AccessServiceSpace accessService;
	@Import private AuthAccessSpace authAccess;
	@Import private AuthContextSpace authContext;
	@Import private BinaryProcessorsSpace binaryProcessor;
	@Import private BindersSpace binders;
	@Import private CartridgeInformationSpace cartridgeInformation;
	@Import private ChecksSpace checks;
	@Import private CortexAccessSpace cortexAccess;
	@Import private DeploymentSpace deployment;
	@Import private ExecutionPersistenceSpace executionPersistence;
	@Import private GmSessionsSpace gmSessions;
	@Import private HttpSpace http;
	@Import private LicenseSpace license;
	@Import private MarshallingSpace marshalling;
	@Import private MessagingSpace messaging;
	@Import private MimeTypeSpace mimeType;
	@Import private ModelNotificationSpace modelNotification;
	@Import private MulticastSpace multicast;
	@Import private ResourceAccessSpace resourceAccess;
	@Import private ResourceProcessingSpace resourceProcessing;
	@Import private RuntimeSpace runtime;
	@Import private SecurityServiceSpace securityService;
	@Import private SecurityServletSpace securityServlet;
	@Import private ServicesSpace services;
	@Import private SimpleSecurityServiceSpace simpleSecurityService;
	@Import private SystemInformationSpace systemInformation;
	@Import private SystemServletsSpace systemServlets;
	@Import private UserSessionServiceSpace sessionService;
	// @formatter:on

	@Managed
	public GmMqRpcServer mqServer() {
		GmMqRpcServer bean = new GmMqRpcServer();
		bean.setRequestEvaluator(serviceRequestEvaluator());
		bean.setMessagingSessionProvider(messaging.sessionProvider());
		bean.setRequestDestinationName(messaging.destinations().trustedRequestQueueName());
		bean.setRequestDestinationType(Queue.T);
		bean.setConsumerId(cartridgeInformation.instanceId());
		bean.setExecutor(mqServerThreadPool());
		bean.setThreadRenamer(runtime.threadRenamer());
		bean.setTrusted(true);
		bean.setMetaDataResolverProvider(metaDataResolverProvider());
		return bean;
	}

	@Managed
	public HardwiredWorker mqServerDeployable() {
		HardwiredWorker bean = HardwiredWorker.T.create();
		bean.setExternalId("rpc-mq-server");
		bean.setName("RPC Mq Server");
		bean.setGlobalId("hardwired:worker/" + bean.getExternalId());
		return bean;
	}

	@Managed
	public ServiceUserSessionScoping serviceUserSessionScoping() {
		StandardServiceUserSessionScoping bean = new StandardServiceUserSessionScoping();
		return bean;
	}

	@Managed
	public StandardMetaDataResolverProvider metaDataResolverProvider() {
		StandardMetaDataResolverProvider bean = new StandardMetaDataResolverProvider();
		bean.setModelAccessoryFactory(gmSessions.systemModelAccessoryFactory());
		return bean;
	}

	@Managed
	public ServiceProcessor<ServiceRequest, Object> hwServiceProcessor() {
		ConfigurableDispatchingServiceProcessor bean = new ConfigurableDispatchingServiceProcessor(hwServiceProcessorsRegistry());
		return bean;
	}

	@Managed
	private ConfigurableDispatchingServiceProcessor evaluatorServiceProcessor() {
		ConfigurableDispatchingServiceProcessor bean = new ConfigurableDispatchingServiceProcessor(hwServiceProcessorsRegistry());

		bean.registerInterceptor("thread-naming").register(threadNamingInterceptor());
		bean.registerInterceptor("time-measuring").register(ElapsedTimeMeasuringInterceptor.INSTANCE);
		bean.registerInterceptor("auth").registerWithPredicate(r -> r.supportsAuthentication(), authorizationInterceptor());
		bean.registerInterceptor("web-logout").registerForType(Logout.T, securityService.webLogoutInterceptor());
		bean.registerInterceptor("domain-mapped-dispatching").registerWithPredicate(r -> !r.system(), domainMappedDispatchingInterceptor());
		bean.registerInterceptor("require-system-user").registerForType(BinaryRequest.T, requireSystemUserInterceptor());

		return bean;
	}

	@Managed
	private RoleBasedAuthorizingInterceptor requireSystemUserInterceptor() {
		RoleBasedAuthorizingInterceptor bean = new RoleBasedAuthorizingInterceptor();
		bean.setAllowedRoles(Sets.set("tf-admin", "tf-internal"));

		return bean;
	}

	@Managed
	private ThreadNamingInterceptor threadNamingInterceptor() {
		ThreadNamingInterceptor bean = new ThreadNamingInterceptor();

		bean.setThreadRenamer(runtime.threadRenamer());

		return bean;
	}

	@Managed
	private ServiceDomainMappedDispatchingInterceptor domainMappedDispatchingInterceptor() {
		ServiceDomainMappedDispatchingInterceptor bean = new ServiceDomainMappedDispatchingInterceptor();

		bean.setModelAccessoryFactory(gmSessions.systemModelAccessoryFactory());
		bean.setDeployedComponentResolver(deployment.proxyingDeployedComponentResolver());

		return bean;
	}

	@Managed
	private AuthorizingServiceInterceptor authorizationInterceptor() {
		AuthorizingServiceInterceptor bean = new AuthorizingServiceInterceptor();
		return bean;
	}

	@Managed
	private HardwiredServiceProcessorRegistration hardwiredServiceProcessorRegistration() {
		HardwiredServiceProcessorRegistration bean = new HardwiredServiceProcessorRegistration();

		bean.setProcessorBinder(binders.accessRequestProcessor());

		// AccessRequest handlers that are AccessRequestProcessors
		bean.bindAccessRequest(PlatformAssetRequest.T, deployment.platformSetupManager());
		bean.bindAccessRequest(TrunkAssetRequestForAccess.T, deployment.platformSetupManager());
		bean.bindAccessRequest(ManageResource.T, resourceAccess.resourceManipulationProcessor());
		bean.bindAccessRequest(ResourceSourceRequest.T, resourceAccess.resourceManipulationProcessor());
		bean.bindAccessRequest(UploadResources.T, resourceAccess.resourceManipulationProcessor());
		bean.bindAccessRequest(DownloadResource.T, resourceAccess.resourceDownloadProcessor());
		bean.bindAccessRequest(DownloadSource.T, resourceAccess.resourceDownloadProcessor());
		bean.bindAccessRequest(PublishModelRequest.T, services.publishModelProcessor());

		bean.bindAccessRequest(GarbageCollectionRequest.T, services.garbageCollectionProcessor());
		bean.bindAccessRequest(SetupAccessRequest.T, services.setupAccessProcessor());
		bean.bindAccessRequest(ModelRequest.T, services.modelRequestProcessor());
		bean.bindAccessRequest(ExchangeRequest.T, services.exchangeRequestProcessor());
		bean.bindAccessRequest(TestConnectionRequest.T, services.createTestConnectionRequestProcessor());
		bean.bindAccessRequest(DbSchemaRequest.T, services.createDbSchemaRequestProcessor());
		// CreateModelFromDbSchema needs explicit registration since its type hierarchy is ambiguous.
		// (ModelRequest+DbSchemaRequest).
		bean.bindAccessRequest(CreateModelFromDbSchema.T, services.createDbSchemaRequestProcessor());
		bean.bindAccessRequest(FixSqlSources.T, binaryProcessor.fixSqlSourcesProcessor());

		bean.bindServiceRequest(ExternalTribefireRequest.T, services.externalTribefireRequestProcessor());
		bean.bindServiceRequest(SetRuntimeProperty.T, services.runtimePropertiesProcessor());

		// ServiceRequest handlers that are ServiceProcessors
		bean.bindServiceRequest(GmqlRequest.T, resourceAccess.gmqlProcessor());
		bean.bindServiceRequest(CompositeRequest.T, compositeServiceProcessor());
		bean.bindServiceRequest(AsynchronousRequest.T, asynchronousServiceProcessor());
		bean.bindServiceRequest(SecurityRequest.T, securityService.securityServiceProcessor());
		bean.bindServiceRequest(AuthenticateCredentials.T, securityService.authenticator());
		bean.bindServiceRequest(CleanupUserSessions.T, sessionService.cleanupService());
		bean.bindServiceRequest(UserSessionRequest.T, sessionService.wbService());
		bean.bindServiceRequest(SimplifiedOpenUserSession.T, simpleSecurityService.service());
		bean.bindServiceRequest(UnicastRequest.T, unicastProcessor());
		bean.bindServiceRequest(ExecuteAuthorized.T, ExecuteAuthorizedServiceProcessor.INSTANCE);
		bean.bindServiceRequest(MulticastRequest.T, multicast.processor());
		bean.bindServiceRequest(AbstractMarshallRequest.T, marshallProcessor());
		bean.bindServiceRequest(DeploymentRequest.T, deployment.processor());
		bean.bindServiceRequest(InternalDeploymentRequest.T, deployment.internalProcessor());
		bean.bindServiceRequest(DeploymentReflectionRequest.T, deployment.deploymentReflectionProcessor());
		bean.bindServiceRequest(PersistenceReflectionRequest.T, persistenceReflectionProcessor());
		bean.bindServiceRequest(AvailableAccessesRequest.T, availableAccessesProcessor());
		bean.bindServiceRequest(PlatformReflectionRequest.T, systemInformation.platformReflectionProcessor());
		bean.bindServiceRequest(AbstractLicenseRequest.T, license.licenseResourceProcessor());
		bean.bindServiceRequest(CheckBundlesRequest.T, systemInformation.checkBundlesProcessor());
		bean.bindServiceRequest(LogsRequest.T, systemServlets.logsProcessor());
		bean.bindServiceRequest(CurrentUserInformationRequest.T, currentUserInformationProcessor());
		bean.bindServiceRequest(ModelNotificationRequest.T, modelNotification.processor());
		bean.bindServiceRequest(InternalModelNotificationRequest.T, modelNotification.internalProcessor());

		bean.bindServiceRequest(PushRequest.T, pushProcessor());
		bean.bindServiceRequest(StaticPrototyping.T, staticPrototypingProcessor());

		// AccessRequest handlers that are ServiceProcessors directly
		bean.bindServiceRequest(CreateServiceRequestTemplate.T, services.createServiceRequestTemplateProcessor());
		bean.bindServiceRequest(CompositeCheck.T, checks.compositeCheckProcessor());

		bean.bindAccessRequest(ExecutionPersistenceRequest.T, executionPersistence.executionPersistenceServiceProcessor());

		bean.bindServiceRequest(ModelRetrievingRequest.T, gmSessions.modelRetrievingProcessor());

		return bean;
	}

	@Managed
	public MutableDenotationMap<ServiceRequest, ServiceProcessor<? extends ServiceRequest, ?>> hwServiceProcessorsRegistry() {
		MutableDenotationMap<ServiceRequest, ServiceProcessor<? extends ServiceRequest, ?>> bean = new PolymorphicDenotationMap<>();
		hardwiredServiceProcessorRegistration().register(bean);

		return bean;
	}

	@Managed
	public CompositeServiceProcessor compositeServiceProcessor() {
		return new CompositeServiceProcessor();
	}

	public ServiceProcessor<AsynchronousRequest, AsynchronousResponse> asynchronousServiceProcessor() {
		String flag = TribefireRuntime.getProperty("TF_EXPERIMENTAL_ASYNC_PERSISTENCE");
		if (flag != null && flag.equalsIgnoreCase("false")) {
			return classicAsynchronousServiceProcessor();
		} else {
			return persistingAsynchronousServiceProcessor();
		}
	}

	@Managed
	public AsynchronousServiceProcessor classicAsynchronousServiceProcessor() {
		AsynchronousServiceProcessor bean = new AsynchronousServiceProcessor();
		bean.setThreadScoping(authContext.currentUser().threadContextScoping());
		bean.setExecutorService(serviceRequestExecutor());
		return bean;
	}

	@Managed
	public PersistingAsynchronousServiceProcessor persistingAsynchronousServiceProcessor() {
		PersistingAsynchronousServiceProcessor bean = new PersistingAsynchronousServiceProcessor();
		bean.setRequestEvaluator(serviceRequestEvaluator());
		bean.setMetaDataResolverProvider(metaDataResolverProvider());
		bean.setDeployedComponentResolver(deployment.proxyingDeployedComponentResolver());
		bean.setCallbackExpert(serviceRequestCallbackExpert());
		bean.setServicePersistence(serviceRequestPersistence());
		bean.setSessionFactory(gmSessions.sessionFactory());
		bean.setStringCodec(servicePersistenceCodec());
		bean.setThreadScoping(authContext.currentUser().threadContextScoping());
		return bean;
	}

	@Managed
	private CallbackExpert serviceRequestCallbackExpert() {
		CallbackExpert bean = new CallbackExpert();
		bean.setHttpClientProvider(http.clientProvider());
		bean.setMarshaller(marshalling.jsonMarshaller());
		bean.setRequestEvaluator(serviceRequestEvaluator());
		return bean;
	}

	@Managed
	private ServiceRequestPersistence serviceRequestPersistence() {
		ServiceRequestPersistence bean = new ServiceRequestPersistence();
		bean.setStringCodec(servicePersistenceCodec());
		return bean;
	}

	private HasStringCodec servicePersistenceCodec() {
		return marshalling.jsonMarshaller();
	}

	@Managed
	public AvailableAccessesRequestProcessor availableAccessesProcessor() {
		AvailableAccessesRequestProcessor bean = new AvailableAccessesRequestProcessor();
		bean.setCortexAccessSupplier(cortexAccess::access);
		return bean;
	}

	@Managed
	public CurrentUserInformationRequestProcessor currentUserInformationProcessor() {
		CurrentUserInformationRequestProcessor bean = new CurrentUserInformationRequestProcessor();
		bean.setAuthAccessSupplier(authAccess::access);
		bean.setUserSessionProvider(authContext.currentUser().userSessionSupplier());
		return bean;
	}

	@Managed
	public PersistenceReflectionServiceProcessor persistenceReflectionProcessor() {
		PersistenceReflectionServiceProcessor bean = new PersistenceReflectionServiceProcessor();
		bean.setAccessService(accessService.service());
		return bean;
	}

	@Managed
	public StandardRequestContextThreadContextScopeSupplier serviceRequestContextThreadContextScopeSupplier() {
		StandardRequestContextThreadContextScopeSupplier bean = new StandardRequestContextThreadContextScopeSupplier();
		return bean;
	}

	@Managed
	public ConfigurableServiceRequestEvaluator serviceRequestEvaluator() {
		ConfigurableServiceRequestEvaluator bean = new ConfigurableServiceRequestEvaluator();
		bean.setServiceProcessor(evaluatorServiceProcessor());
		bean.setExecutorService(serviceRequestExecutor());
		bean.setReasonExceptionFactory(new AuthenticationExceptionFactory());
		return bean;
	}

	@Managed
	public AuthorizingServiceRequestEvaluator systemServiceRequestEvaluator() {
		AuthorizingServiceRequestEvaluator bean = new AuthorizingServiceRequestEvaluator();
		bean.setUserSessionProvider(authContext.internalUser().userSessionProvider());
		bean.setDelegate(serviceRequestEvaluator());
		return bean;
	}

	@Managed
	public ExecutorService serviceRequestExecutor() {

		int threadPoolSize = 250;
		String threadPoolSizeString = TribefireRuntime.getProperty("TRIBEFIRE_RPC_THREAD_POOL_SIZE");
		if (!StringTools.isBlank(threadPoolSizeString)) {
			try {
				threadPoolSize = Integer.parseInt(threadPoolSizeString);
			} catch (NumberFormatException nfe) {
				logger.error("Could not parse the value " + threadPoolSizeString + " defined in variable TRIBEFIRE_RPC_THREAD_POOL_SIZE", nfe);
			}
		}

		VirtualThreadExecutor bean = VirtualThreadExecutorBuilder.newPool().concurrency(threadPoolSize)
				.threadNamePrefix("tribefire.master.evaluator.executor-").description("Master RPC Service").build();
		return bean;
	}

	@Managed
	public Supplier<Map<String, Object>> clientMetaDataProvider() {
		GmWebRpcClientMetaDataProvider bean = new GmWebRpcClientMetaDataProvider();
		bean.setSessionIdProvider(authContext.currentUser().userSessionIdProvider());
		bean.setIncludeNdc(true);
		bean.setIncludeNodeId(true);
		bean.setIncludeThreadName(true);
		return bean;
	}

	@Managed
	private ExecutorService mqServerThreadPool() {
		VirtualThreadExecutor bean = VirtualThreadExecutorBuilder.newPool().concurrency(20).threadNamePrefix("tribefire.trusted-master-")
				.description("Master Messaging Service").build();
		return bean;
	}

	@Managed
	private MarshallerProcessor marshallProcessor() {
		MarshallerProcessor bean = new MarshallerProcessor();

		bean.setMarshallerRegistry(marshalling.registry());
		bean.setStreamPipeFactory(resourceProcessing.streamPipeFactory());
		bean.setMimeTypeDetector(mimeType.detector());

		MimeTypeRegistry mimeTypeRegistry = mimeType.mimeTypeRegistry();

		bean.setMimeTypeExtensionResolver(m -> mimeTypeRegistry.getExtension(m).orElse("bin"));

		return bean;
	}

	@Managed
	private UnicastProcessor unicastProcessor() {
		UnicastProcessor bean = new UnicastProcessor();
		bean.setCurrentInstance(cartridgeInformation.instanceId());
		return bean;
	}

	@Managed
	public PushProcessor pushProcessor() {
		PushProcessor bean = new PushProcessor();
		return bean;
	}

	@Managed
	private StaticPrototypingProcessor staticPrototypingProcessor() {
		return new StaticPrototypingProcessor();
	}

}
