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
package tribefire.platform.wire.space.cortex.accesses;

import static com.braintribe.wire.api.util.Lists.list;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.braintribe.model.access.collaboration.CollaborativeAccessManager;
import com.braintribe.model.access.collaboration.CollaborativeSmoodAccess;
import com.braintribe.model.access.collaboration.CsaStatePersistence;
import com.braintribe.model.access.collaboration.persistence.CortexManipulationPersistence;
import com.braintribe.model.processing.accessory.impl.DynamicModelAccessory;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.cortex.CortexModelNames;
import com.braintribe.model.processing.cortex.priming.TfEnvCsaPriming;
import com.braintribe.model.processing.cortex.processor.DeployableModuleAssigningScp;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.platform.impl.denotrans.AccessToAccessCleanupUserSessionsProcessorMorpher;
import tribefire.platform.impl.denotrans.AccessToAccessUserSessionServiceMorpher;
import tribefire.platform.impl.denotrans.CleanupUserSessionsProcessorEnricher;
import tribefire.platform.impl.denotrans.SystemAccessesDenotationEnricher;
import tribefire.platform.impl.denotrans.SystemDeployablesAutoDeployEnsuringEnricher;
import tribefire.platform.impl.denotrans.TransientMessagingAccessWithSqlBinaryProcessorEnricher;
import tribefire.platform.impl.denotrans.UserSessionServiceEnricher;
import tribefire.platform.impl.initializer.CortexConfigurationPostInitializer;
import tribefire.platform.impl.initializer.Edr2ccPostInitializer;
import tribefire.platform.impl.configuration.denotrans.DenotationTransformationExecutor;
import tribefire.platform.impl.configuration.denotrans.DenotationTransformerRegistryImpl;
import tribefire.platform.wire.space.common.MarshallingSpace;
import tribefire.platform.wire.space.common.ResourceProcessingSpace;
import tribefire.platform.wire.space.cortex.deployment.StateChangeProcessorsSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.DeployablesSpace;
import tribefire.platform.wire.space.module.ModuleInitializationSpace;
import tribefire.platform.wire.space.security.accesses.AuthAccessSpace;

@Managed
public class CortexAccessSpace extends CollaborativeSystemAccessSpaceBase {

	private static final String id = "cortex";
	private static final String name = "Cortex";
	private static final String modelName = CortexModelNames.TF_CORTEX_MODEL_NAME;
	private static final String serviceModelName = CortexModelNames.TF_CORTEX_SERVICE_MODEL_NAME;

	/* package */ static final String defaultServiceDomainId = "serviceDomain:default";

	// @formatter:off
	@Import private AuthAccessSpace authAccess;
	@Import private CortexAccessInitializersSpace cortexAccessInitializers;
	@Import private DeployablesSpace deployables;
	@Import private MarshallingSpace marshalling;
	@Import private ModuleInitializationSpace moduleInitialization;
	@Import private ResourceProcessingSpace resourceProcessing;
	@Import private StateChangeProcessorsSpace stateChangeProcessors;

	@Override public String id() { return id; }
	@Override public String name() { return name; }
	@Override public String modelName() { return modelName; }
	@Override public String serviceModelName() { return serviceModelName; }
	          public String defaultServiceDomainId() { return defaultServiceDomainId; }
	// @formatter:on

	@Override
	protected List<AccessAspect> aopAspects() {
		return list( //
				aspects.security(), //
				aspects.globalIdGenerator(), //
				stateProcessingAspect() //
		);
	}

	@Override
	@Managed
	public CollaborativeSmoodAccess access() {
		CollaborativeSmoodAccess bean = systemAccessCommons.newRegularOrDistributedCsa(id());
		bean.setReadWriteLock(new ReentrantReadWriteLock());
		bean.setDefaultTraversingCriteria(traversingCriteria.cortexDefaultMap());
		bean.setManipulationPersistence(manipulationPersistence());
		bean.setSelfModelName(CortexModelNames.TF_CORTEX_MODEL_NAME);
		bean.setModelAccessory(dynamicModelAccessory());
		bean.setCollaborativeRequestProcessor(collaborativeAccessManager());

		return bean;
	}

	public DynamicModelAccessory dynamicModelAccessory() {
		return systemAccessCommons.dynamicModelAccessory(id());
	}

	@Managed
	private CortexManipulationPersistence manipulationPersistence() {
		CortexManipulationPersistence bean = new CortexManipulationPersistence();
		bean.setMergeModelAndData(mergeModelAndData());
		bean.setStaticInitializers(cortexAccessInitializers.initializers());
		bean.setStaticPostInitializers(cortexPostInitializers());
		bean.setStorageBase(storageBase());
		bean.setCsaStatePersistence(statePersistence());
		bean.setDataModelName(CortexModelNames.TF_CORTEX_MODEL_NAME);
		bean.setDataServiceModelName(CortexModelNames.TF_CORTEX_SERVICE_MODEL_NAME);
		bean.setGmmlErrorHandler(systemAccessCommons.gmmlErrorHandler(id()));
		bean.setManipulationFilter(DeploymentStateManipulationFilter.INSTANCE);
		bean.setCustomInitializerResolver(moduleInitialization.moduleBoundInitializerResolverFor(id));

		if (TribefireRuntime.getPlatformSetupSupport()) {
			bean.setAppendedManipulationListener(m -> deployment.platformSetupManager().notifyAppendedManipulation(access(), m));
		}

		return bean;
	}

	@Managed
	public File storageBase() {
		return systemAccessCommons.storageBase(id());
	}

	private static boolean mergeModelAndData() {
		String modelsFirst = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_CORTEX_MODELS_FIRST);
		return modelsFirst == null || !"true".equals(modelsFirst.toLowerCase());
	}

	private List<PersistenceInitializer> cortexPostInitializers() {
		List<PersistenceInitializer> result = TfEnvCsaPriming.getEnvironmentInitializersFor(id());
		result.add(edr2ccPostInitializer());
		result.add(cortexConfigurationPostInitializer());
		result.add(deployables.schrodingerBeanCoupler());
		return result;
	}

	@Managed
	private CortexConfigurationPostInitializer cortexConfigurationPostInitializer() {
		CortexConfigurationPostInitializer bean = new CortexConfigurationPostInitializer();

		return bean;
	}

	@Managed
	private Edr2ccPostInitializer edr2ccPostInitializer() {
		Edr2ccPostInitializer bean = new Edr2ccPostInitializer();
		bean.setTransformationExecutor(transformationExecutor());

		return bean;
	}

	@Managed
	private DenotationTransformationExecutor transformationExecutor() {
		DenotationTransformationExecutor bean = new DenotationTransformationExecutor();
		bean.setTransformerRegistry(transformerRegistry());

		return bean;
	}

	@Managed
	public DenotationTransformerRegistryImpl transformerRegistry() {
		DenotationTransformerRegistryImpl bean = new DenotationTransformerRegistryImpl();

		bean.registerEnricher(new SystemDeployablesAutoDeployEnsuringEnricher());
		bean.registerEnricher(new SystemAccessesDenotationEnricher());

		bean.registerMorpher(new AccessToAccessCleanupUserSessionsProcessorMorpher());
		bean.registerEnricher(new CleanupUserSessionsProcessorEnricher());

		bean.registerMorpher(new AccessToAccessUserSessionServiceMorpher());
		bean.registerEnricher(new UserSessionServiceEnricher());

		bean.registerEnricher(new TransientMessagingAccessWithSqlBinaryProcessorEnricher());

		return bean;
	}

	@Managed
	private CollaborativeAccessManager collaborativeAccessManager() {
		CollaborativeAccessManager bean = new CollaborativeAccessManager();
		bean.setAccess(access());
		bean.setCsaStatePersistence(statePersistence());
		bean.setSourcePathResolver(resourceAccess.accessPathResolver().pathResolverForDomain(id()));
		bean.setGmmlErrorHandler(systemAccessCommons.gmmlErrorHandler(id()));
		bean.setResourceBuilder(resourceProcessing.transientResourceBuilder());

		return bean;
	}

	private CsaStatePersistence statePersistence() {
		return systemAccessCommons.csaStatePersistence(id());
	}

	@Managed
	@Override
	protected List<StateChangeProcessorRule> stateChangeProcessorRules() {
		return list( //
				stateChangeProcessors.licenseUpload(), //
				stateChangeProcessors.licenseActivated(), //
				stateChangeProcessors.bidiProperty(), //
				stateChangeProcessors.metadata(), //
				stateChangeProcessors.cors(), //
				stateChangeProcessors.modelAccessoryNotifier(), //
				deployableModuleAutoAssigner());
	}

	@Managed
	DeployableModuleAssigningScp deployableModuleAutoAssigner() {
		DeployableModuleAssigningScp bean = new DeployableModuleAssigningScp();
		bean.setBindingModulesResolver(deployables.bindings()::resolveBindingModulesOf);
		return bean;
	}

	@Override
	public HardwiredAccessSpaceBase workbenchAccessSpace() {
		return systemAccesses.cortexWorkbench();
	}

}
