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
package tribefire.extension.webapi.web_api_server_initializer;

import java.util.function.Consumer;

import com.braintribe.model.accessapi.GetModel;
import com.braintribe.model.accessapi.GmqlRequest;
import com.braintribe.model.bapi.AvailableAccessesRequest;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeInitializers;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeStageData;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeStageStats;
import com.braintribe.model.cortexapi.access.collaboration.GetModifiedModelsForStage;
import com.braintribe.model.cortexapi.access.collaboration.MergeCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.MergeCollaborativeStageToPredecessor;
import com.braintribe.model.cortexapi.access.collaboration.RenameCollaborativeStage;
import com.braintribe.model.cortexapi.model.CreateModel;
import com.braintribe.model.ddra.DdraConfiguration;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.ddra.DdraUrlMethod;
import com.braintribe.model.ddra.endpoints.OutputPrettiness;
import com.braintribe.model.ddra.endpoints.api.v1.ApiV1DdraEndpoint;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregationKind;
import com.braintribe.model.deploymentapi.check.request.RunAimedCheckBundles;
import com.braintribe.model.deploymentapi.check.request.RunCheckBundles;
import com.braintribe.model.deploymentapi.check.request.RunDistributedCheckBundles;
import com.braintribe.model.deploymentapi.check.request.RunHealthChecks;
import com.braintribe.model.deploymentapi.check.request.RunVitalityCheckBundles;
import com.braintribe.model.deploymentapi.request.Deploy;
import com.braintribe.model.deploymentapi.request.Redeploy;
import com.braintribe.model.deploymentapi.request.Undeploy;
import com.braintribe.model.deploymentreflection.request.GetDeploymentStatus;
import com.braintribe.model.extensiondeployment.ServicePostProcessor;
import com.braintribe.model.extensiondeployment.ServicePreProcessor;
import com.braintribe.model.extensiondeployment.StateChangeProcessor;
import com.braintribe.model.extensiondeployment.meta.OnChange;
import com.braintribe.model.extensiondeployment.meta.OnCreate;
import com.braintribe.model.extensiondeployment.meta.OnDelete;
import com.braintribe.model.extensiondeployment.meta.PostProcessWith;
import com.braintribe.model.extensiondeployment.meta.PreProcessWith;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.prompt.Embedded;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.platformreflection.DiagnosticPackage;
import com.braintribe.model.platformreflection.HeapDump;
import com.braintribe.model.platformreflection.request.CollectDiagnosticPackages;
import com.braintribe.model.platformreflection.request.GetDiagnosticPackage;
import com.braintribe.model.platformreflection.request.GetHostInformation;
import com.braintribe.model.platformreflection.request.GetHotThreads;
import com.braintribe.model.platformreflection.request.GetProcesses;
import com.braintribe.model.platformreflection.request.GetSystemInformation;
import com.braintribe.model.platformreflection.request.GetThreadDump;
import com.braintribe.model.platformreflection.request.GetTribefireInformation;
import com.braintribe.model.platformreflection.request.ReflectPlatform;
import com.braintribe.model.processing.ddra.endpoints.api.v1.WebApiConfigurationInitializer;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.prototyping.api.StaticPrototyping;
import com.braintribe.model.resourceapi.persistence.DeleteResource;
import com.braintribe.model.resourceapi.persistence.DeletionScope;
import com.braintribe.model.resourceapi.persistence.UpdateResource;
import com.braintribe.model.resourceapi.persistence.UploadResources;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.GetResource;
import com.braintribe.model.resourceapi.stream.HasStreamCondition;
import com.braintribe.model.resourceapi.stream.HasStreamRange;
import com.braintribe.model.securityservice.GetCurrentUser;
import com.braintribe.model.securityservice.Logout;
import com.braintribe.model.securityservice.OpenUserSessionWithUserAndPassword;
import com.braintribe.model.securityservice.ValidateUserSession;
import com.braintribe.model.service.api.PlatformRequest;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.util.Lists;
import com.braintribe.wire.api.util.Sets;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.cortex.model.check.CheckCoverage;
import tribefire.extension.webapi.web_api_server_initializer.wire.WebApiServerInitializerWireModule;
import tribefire.extension.webapi.web_api_server_initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.webapi.web_api_server_initializer.wire.contract.WebApiServerInitializerMainContract;

public class WebApiServerInitializer extends AbstractInitializer<WebApiServerInitializerMainContract> {
	private static final String DDRA_MAPPING_TAG_SECURITY = "Security";
	private static final String DDRA_MAPPING_TAG_SESSIONS = "Sessions";
	private static final String DDRA_MAPPING_TAG_LICENSE = "License";
	private static final String DDRA_MAPPING_TAG_ACCESSES = "Deployed Accesses";
	private static final String DDRA_MAPPING_TAG_RESOURCES = "Resources";
	private static final String DDRA_MAPPING_TAG_MONITORING = "Monitoring";
	private static final String DDRA_MAPPING_TAG_QUERY = "Querying";
	private static final String DDRA_MAPPING_TAG_CORTEX_MODELS = "Models";
	private static final String DDRA_MAPPING_TAG_CORTEX_DEPLOYABLES = "Deployables";
	private static final String DDRA_MAPPING_TAG_CHECKS = "Checks";

	private static final String MODEL_NAME_CONFIGURED_WEB_API_CONFIGURATION = ExistingInstancesContract.GROUP_ID
			+ ":configured-web-api-configuration-model";

	private static final String ACCESS_ID_CORTEX = "cortex";

	@Override
	public WireTerminalModule<WebApiServerInitializerMainContract> getInitializerWireModule() {
		return WebApiServerInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<WebApiServerInitializerMainContract> initializerContext,
			WebApiServerInitializerMainContract initializerMainContract) {

		initializeDdraMappingMonitor(context, initializerMainContract);
		initializeStreamingAop(context, initializerMainContract);
		initializeDdraMappings(context, initializerMainContract);
	}

	private void initializeDdraMappingMonitor(PersistenceInitializationContext context, WebApiServerInitializerMainContract initializerMainContract) {
		ManagedGmSession session = context.getSession();
		StateChangeProcessor webApiConfigurationMonitor = initializerMainContract.existingInstances().webApiConfigurationMonitor();

		GmMetaModel model = session.create(GmMetaModel.T, "model:" + MODEL_NAME_CONFIGURED_WEB_API_CONFIGURATION);
		model.setName(MODEL_NAME_CONFIGURED_WEB_API_CONFIGURATION);

		model.getDependencies().add(initializerMainContract.existingInstances().webApiConfigurationModel());

		OnCreate onCreate = session.create(OnCreate.T, "095e701f-6e0f-4260-8aa6-0473b1ff5fda");
		OnChange onChange = session.create(OnChange.T, "e3b877fa-8dc0-44eb-b666-1749c16652bd");
		OnDelete onDelete = session.create(OnDelete.T, "db2c33b2-dff3-4971-8909-04a0afc1ff03");
		onCreate.setProcessor(webApiConfigurationMonitor);
		onChange.setProcessor(webApiConfigurationMonitor);
		onDelete.setProcessor(webApiConfigurationMonitor);

		ModelMetaDataEditor editor = BasicModelMetaDataEditor.create(model).withSession(session).done();

		editor.onEntityType(DdraConfiguration.T).addMetaData(onCreate, onDelete);
		editor.onEntityType(DdraMapping.T).addMetaData(onCreate, onDelete);
		editor.onEntityType(DdraConfiguration.T).addPropertyMetaData(onChange);
		editor.onEntityType(DdraMapping.T).addPropertyMetaData(onChange);

		GmMetaModel cortexModel = initializerMainContract.coreInstances().cortexModel();
		cortexModel.getDependencies().add(model);
	}

	private void initializeDdraMappings(PersistenceInitializationContext context, WebApiServerInitializerMainContract initializerMainContract) {
		WebApiConfigurationInitializer initializer = new WebApiConfigurationInitializer();
		initializer.setConfigurer((session, registry) -> {
			// Platform Domain
			registry.create("/authenticate", OpenUserSessionWithUserAndPassword.T, DdraUrlMethod.POST, "userSession.sessionId", "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_SECURITY));
			registry.create("/logout", Logout.T, DdraUrlMethod.POST, null, "application/json", PlatformRequest.platformDomainId,
					Sets.set(DDRA_MAPPING_TAG_SECURITY));

			registry.create("/gmql", GmqlRequest.T, DdraUrlMethod.GET, null, "application/json", PlatformRequest.platformDomainId,
					Sets.set(DDRA_MAPPING_TAG_QUERY));

			// TODO: remove commented code after checking feature loss
			// registry.create("/resources/upload", ResourceUploadRequest.T, DdraUrlMethod.POST, null, "application/json",
			// PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_RESOURCES));
			// registry.create("/resources/download", ResourceDownloadRequest.T, DdraUrlMethod.GET, null, "application/json",
			// PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_RESOURCES));
			// registry.create("/resources/delete", ResourceDeleteRequest.T, DdraUrlMethod.DELETE, null, "application/json",
			// PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_RESOURCES));

			// api/v2/my-access/resource GET-> download, POST-> create, PUT-> update, DELETE-> delete

			registry.create("/upload", UploadResources.T, DdraUrlMethod.POST, null, null, null, Sets.set(DDRA_MAPPING_TAG_RESOURCES));
			registry.create("/upload", UpdateResource.T, DdraUrlMethod.PUT, null, null, null, Sets.set(DDRA_MAPPING_TAG_RESOURCES));
			registry.create("/delete", DeleteResource.T, DdraUrlMethod.DELETE, null, null, null, Sets.set(DDRA_MAPPING_TAG_RESOURCES), m -> {
				DeleteResource deleteResource = DeleteResource.T.create();
				deleteResource.setDeletionScope(DeletionScope.resource);

				StaticPrototyping staticPrototyping = StaticPrototyping.T.create();
				staticPrototyping.setPrototype(deleteResource);

				m.setRequestPrototyping(staticPrototyping);
			});
			registry.create("/download", GetResource.T, DdraUrlMethod.GET, GetBinaryResponse.resource, null, null,
					Sets.set(DDRA_MAPPING_TAG_RESOURCES), m -> {
						ApiV1DdraEndpoint endpointPrototype = ApiV1DdraEndpoint.T.create();
						endpointPrototype.setDownloadResource(true);
						m.setEndpointPrototype(endpointPrototype);
					});

			registry.create("/accesses/deployedModel", GetModel.T, DdraUrlMethod.GET, null, "application/json", PlatformRequest.platformDomainId,
					Sets.set(DDRA_MAPPING_TAG_ACCESSES));
			registry.create("/accesses/modelEnvironment", com.braintribe.gm.model.persistence.reflection.api.GetModelAndWorkbenchEnvironment.T,
					DdraUrlMethod.GET, null, "application/json", PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_ACCESSES));
			registry.create("/accesses/available", AvailableAccessesRequest.T, DdraUrlMethod.GET, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_ACCESSES));

			registry.create("/accesses/smood/initializers", GetCollaborativeInitializers.T, DdraUrlMethod.GET, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_ACCESSES));
			registry.create("/accesses/smood/stageData", GetCollaborativeStageData.T, DdraUrlMethod.GET, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_ACCESSES));
			registry.create("/accesses/smood/stageStats", GetCollaborativeStageStats.T, DdraUrlMethod.GET, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_ACCESSES));
			registry.create("/accesses/smood/modifiedStageModels", GetModifiedModelsForStage.T, DdraUrlMethod.GET, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_ACCESSES));
			registry.create("/accesses/smood/mergeStages", MergeCollaborativeStage.T, DdraUrlMethod.POST, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_ACCESSES));
			registry.create("/accesses/smood/mergeStageToPredecessor", MergeCollaborativeStageToPredecessor.T, DdraUrlMethod.POST, null,
					"application/json", PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_ACCESSES));
			registry.create("/accesses/smood/renameStage", RenameCollaborativeStage.T, DdraUrlMethod.POST, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_ACCESSES));

			registry.create("/sessions/currentUser", GetCurrentUser.T, DdraUrlMethod.GET, null, "application/json", PlatformRequest.platformDomainId,
					Sets.set(DDRA_MAPPING_TAG_SESSIONS));
			registry.create("/sessions/validate", ValidateUserSession.T, DdraUrlMethod.GET, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_SESSIONS));

			// Platform reflection
			registry.create("/monitoring/platform-reflect", ReflectPlatform.T, DdraUrlMethod.GET, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_MONITORING));
			registry.create("/monitoring/diagnostic-package", CollectDiagnosticPackages.T, DdraUrlMethod.GET, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_MONITORING), m -> {
						m.setDefaultDownloadResource(true);
						m.setDefaultSaveLocally(true);
						m.setDefaultProjection(DiagnosticPackage.diagnosticPackage);
					});
			registry.create("/monitoring/heapdump", GetDiagnosticPackage.T, DdraUrlMethod.GET, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_MONITORING), m -> {
						m.setDefaultDownloadResource(true);
						m.setDefaultSaveLocally(true);
						m.setDefaultProjection(HeapDump.heapDump);
					});
			registry.create("/monitoring/host-information", GetHostInformation.T, DdraUrlMethod.GET, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_MONITORING));
			registry.create("/monitoring/hot-threads", GetHotThreads.T, DdraUrlMethod.GET, null, "application/json", PlatformRequest.platformDomainId,
					Sets.set(DDRA_MAPPING_TAG_MONITORING));
			registry.create("/monitoring/processes", GetProcesses.T, DdraUrlMethod.GET, null, "application/json", PlatformRequest.platformDomainId,
					Sets.set(DDRA_MAPPING_TAG_MONITORING));
			registry.create("/monitoring/system-information", GetSystemInformation.T, DdraUrlMethod.GET, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_MONITORING));
			registry.create("/monitoring/threaddump", GetThreadDump.T, DdraUrlMethod.GET, null, "application/json", PlatformRequest.platformDomainId,
					Sets.set(DDRA_MAPPING_TAG_MONITORING));
			registry.create("/monitoring/tribefire-information", GetTribefireInformation.T, DdraUrlMethod.GET, null, "application/json",
					PlatformRequest.platformDomainId, Sets.set(DDRA_MAPPING_TAG_MONITORING));

			// Cortex domain
			registry.create("/cortex/models/create", CreateModel.T, DdraUrlMethod.POST, null, "application/json", ACCESS_ID_CORTEX,
					Sets.set(DDRA_MAPPING_TAG_CORTEX_MODELS));
			registry.create("/cortex/deployables/deploy", Deploy.T, DdraUrlMethod.POST, null, "application/json", ACCESS_ID_CORTEX,
					Sets.set(DDRA_MAPPING_TAG_CORTEX_DEPLOYABLES));
			registry.create("/cortex/deployables/undeploy", Undeploy.T, DdraUrlMethod.POST, null, "application/json", ACCESS_ID_CORTEX,
					Sets.set(DDRA_MAPPING_TAG_CORTEX_DEPLOYABLES));
			registry.create("/cortex/deployables/redeploy", Redeploy.T, DdraUrlMethod.POST, null, "application/json", ACCESS_ID_CORTEX,
					Sets.set(DDRA_MAPPING_TAG_CORTEX_DEPLOYABLES));
			registry.create("/cortex/deployables/status", GetDeploymentStatus.T, DdraUrlMethod.GET, null, "application/json", ACCESS_ID_CORTEX,
					Sets.set(DDRA_MAPPING_TAG_CORTEX_DEPLOYABLES));
			registry.create("/cortex/deployables/summary", GetDeploymentStatus.T, DdraUrlMethod.GET, null, "application/json", ACCESS_ID_CORTEX,
					Sets.set(DDRA_MAPPING_TAG_CORTEX_DEPLOYABLES));

			// Checks
			ApiV1DdraEndpoint endpointPrototype = session.create(ApiV1DdraEndpoint.T, "690c670d-c419-4a94-bc98-97dce2ccc175");
			endpointPrototype.setDepth("reachable");
			endpointPrototype.setPrettiness(OutputPrettiness.high);

			Consumer<DdraMapping> configureReachabilityAndHighOutputPrettiness = m -> {
				m.setEndpointPrototype(endpointPrototype);
			};

			// TODO change default mime type application/json
			registry.create("/healthz", RunHealthChecks.T, DdraUrlMethod.GET, null, "application/json", ACCESS_ID_CORTEX,
					Sets.set(DDRA_MAPPING_TAG_CHECKS), configureReachabilityAndHighOutputPrettiness);
			registry.create("/checkVitality", RunVitalityCheckBundles.T, DdraUrlMethod.GET, null, "text/html;spec=check-bundles-response",
					ACCESS_ID_CORTEX, Sets.set(DDRA_MAPPING_TAG_CHECKS), configureReachabilityAndHighOutputPrettiness);
			registry.create("/check", RunCheckBundles.T, DdraUrlMethod.GET, null, "text/html;spec=check-bundles-response", ACCESS_ID_CORTEX,
					Sets.set(DDRA_MAPPING_TAG_CHECKS), configureReachabilityAndHighOutputPrettiness);
			registry.create("/checkDistributed", RunDistributedCheckBundles.T, DdraUrlMethod.GET, null, "text/html;spec=check-bundles-response",
					ACCESS_ID_CORTEX, Sets.set(DDRA_MAPPING_TAG_CHECKS), configureReachabilityAndHighOutputPrettiness);
			registry.create("/checkAimed", RunAimedCheckBundles.T, DdraUrlMethod.GET, null, "text/html;spec=check-bundles-response", ACCESS_ID_CORTEX,
					Sets.set(DDRA_MAPPING_TAG_CHECKS), configureReachabilityAndHighOutputPrettiness);

			RunDistributedCheckBundles run = session.create(RunDistributedCheckBundles.T, "b9265418-8e97-4424-9e0a-32153bf0d715");
			run.setIsPlatformRelevant(true);
			run.setCoverage(Sets.set(CheckCoverage.vitality));
			run.setAggregateBy(Lists.list(CbrAggregationKind.node));

			StaticPrototyping p = session.create(StaticPrototyping.T, "6d453267-7891-41a2-a649-31392e2b00d3");
			p.setPrototype(run);

			Consumer<DdraMapping> configureReachabilityAndHighOutputPrettinessAndRequestPrototype = configureReachabilityAndHighOutputPrettiness
					.andThen(m -> {
						m.setRequestPrototyping(p);
					});

			registry.create("/checkPlatform", RunDistributedCheckBundles.T, DdraUrlMethod.GET, null, "text/html;spec=check-bundles-response",
					ACCESS_ID_CORTEX, Sets.set(DDRA_MAPPING_TAG_CHECKS), configureReachabilityAndHighOutputPrettinessAndRequestPrototype);

		});

		initializer.initialize(context);
	}

	private void initializeStreamingAop(PersistenceInitializationContext context, WebApiServerInitializerMainContract initializerMainContract) {
		// TODO: direct applying of metadata on a skeleton model is not recommended - rethink where to actually put the metadata
		// on and how that model
		// is being respected during resource processing
		ManagedGmSession session = context.getSession();
		GmMetaModel model = session.findEntityByGlobalId("model:" + GetResource.T.getModel().name());

		ServicePreProcessor preProcessor = session.findEntityByGlobalId("hardwired:preprocessor/http.streaming");
		ServicePostProcessor postProcessor = session.findEntityByGlobalId("hardwired:postprocessor/http.streaming");

		BasicModelMetaDataEditor editor = BasicModelMetaDataEditor.create(model).withSession(session).done();

		UseCaseSelector usecaseDdra = session.create(UseCaseSelector.T);
		usecaseDdra.setUseCase("ddra");
		usecaseDdra.setGlobalId("554f083a-a05d-11ec-b909-0242ac120002");

		Embedded embedded = session.create(Embedded.T);
		embedded.setSelector(usecaseDdra);
		embedded.setGlobalId("554f0aba-a05d-11ec-b909-0242ac120002");

		PreProcessWith preProcessWith = session.create(PreProcessWith.T);
		preProcessWith.setProcessor(preProcessor);
		preProcessWith.setGlobalId("554f0bf0-a05d-11ec-b909-0242ac120002");

		PostProcessWith postProcessWith = session.create(PostProcessWith.T);
		postProcessWith.setProcessor(postProcessor);
		postProcessWith.setGlobalId("554f0cfe-a05d-11ec-b909-0242ac120002");

		editor.onEntityType(GetResource.T) //
				.addPropertyMetaData("resource", embedded).addMetaData(postProcessWith);
		editor.onEntityType(DeleteResource.T) //
				.addPropertyMetaData("resource", embedded);
		editor.onEntityType(HasStreamCondition.T) //
				.addMetaData(preProcessWith);
		editor.onEntityType(HasStreamRange.T) //
				.addMetaData(preProcessWith);
	}

}
