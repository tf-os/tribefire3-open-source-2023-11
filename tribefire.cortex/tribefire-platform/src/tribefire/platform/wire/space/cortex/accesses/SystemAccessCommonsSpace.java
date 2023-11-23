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

import java.io.File;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.model.access.collaboration.CollaborativeAccessManager;
import com.braintribe.model.access.collaboration.CollaborativeSmoodAccess;
import com.braintribe.model.access.collaboration.CsaStatePersistence;
import com.braintribe.model.access.collaboration.CsaStatePersistenceImpl;
import com.braintribe.model.access.collaboration.binary.CsaBinaryRetrieval;
import com.braintribe.model.access.collaboration.distributed.DistributedCollaborativeSmoodAccess;
import com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage;
import com.braintribe.model.access.collaboration.persistence.BasicManipulationPersistence;
import com.braintribe.model.accessdeployment.CollaborativeAccess;
import com.braintribe.model.extensiondeployment.HardwiredBinaryRetrieval;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.accessory.impl.DynamicModelAccessory;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.cortex.priming.TfEnvCsaPriming;
import com.braintribe.model.processing.dataio.FileBasedGmPathValueStore;
import com.braintribe.model.processing.deployment.api.DeployedComponent;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.TrackingErrorHandler;
import com.braintribe.provider.Hub;
import com.braintribe.utils.paths.PathCollectors;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.api.resource.ResourcesBuilder;
import tribefire.platform.base.ModuleSourceBinaryRetrieval;
import tribefire.platform.impl.deployment.PostCortexSupplier;
import tribefire.platform.impl.deployment.PreCortexDeployer;
import tribefire.platform.impl.resource.ResourcesBuilderBasedPersistence;
import tribefire.platform.wire.space.MasterResourcesSpace;
import tribefire.platform.wire.space.bindings.BindingsSpace;
import tribefire.platform.wire.space.common.MarshallingSpace;
import tribefire.platform.wire.space.common.ResourceProcessingSpace;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.cortex.PreCortexSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.cortex.services.AccessServiceSpace;
import tribefire.platform.wire.space.module.ModuleInitializationSpace;
import tribefire.platform.wire.space.streaming.ResourceAccessSpace;

@Managed
public class SystemAccessCommonsSpace implements WireSpace {

	private static final String ACCESS_ID_SETUP = "setup";

	@Import
	private GmSessionsSpace gmSessions;

	@Import
	private MasterResourcesSpace resources;

	@Import
	private MarshallingSpace marshalling;

	@Import
	private ModuleInitializationSpace moduleInitialization;

	@Import
	private ResourceAccessSpace resourceAccess;

	@Import
	private DeploymentSpace deployment;

	@Import
	private BindingsSpace bindings;

	@Import
	private PreCortexSpace preCortex;

	@Import
	private ResourceProcessingSpace resourceProcessing;

	@Import
	private AccessServiceSpace accessService;

	@Managed
	public CollaborativeSmoodAccess collaborativeSmoodAccess(String accessId) {
		CollaborativeSmoodAccess bean = newRegularOrDistributedCsa(accessId);
		bean.setReadWriteLock(new ReentrantReadWriteLock());
		bean.setManipulationPersistence(manipulationPersistence(bean, accessId));
		bean.setModelAccessory(dynamicModelAccessory(accessId));
		bean.setCollaborativeRequestProcessor(collaborativeAccessManager(accessId));

		return bean;
	}

	private BasicManipulationPersistence manipulationPersistence(CollaborativeSmoodAccess csa, String accessId) {
		BasicManipulationPersistence bean = new BasicManipulationPersistence();
		bean.setStaticPostInitializers(TfEnvCsaPriming.getEnvironmentInitializersFor(accessId));
		bean.setStorageBase(storageBase(accessId));
		bean.setCsaStatePersistence(csaStatePersistence(accessId));
		bean.setGmmlErrorHandler(gmmlErrorHandler(accessId));
		bean.setCustomInitializerResolver(moduleInitialization.moduleBoundInitializerResolverFor(accessId));

		if (TribefireRuntime.getPlatformSetupSupport() && !accessId.equalsIgnoreCase(ACCESS_ID_SETUP))
			bean.setAppendedManipulationListener(appendedManListenerFor(csa));

		return bean;
	}

	private Consumer<Manipulation> appendedManListenerFor(CollaborativeSmoodAccess csa) {
		return m -> deployment.platformSetupManager().notifyAppendedManipulation(csa, m);
	}

	@Managed
	private CollaborativeAccessManager collaborativeAccessManager(String accessId) {
		CollaborativeAccessManager bean = new CollaborativeAccessManager();
		bean.setAccess(collaborativeSmoodAccess(accessId));
		bean.setCsaStatePersistence(csaStatePersistence(accessId));
		bean.setSourcePathResolver(resourceAccess.accessPathResolver().pathResolverForDomain(accessId));
		bean.setGmmlErrorHandler(gmmlErrorHandler(accessId));
		bean.setResourceBuilder(resourceProcessing.transientResourceBuilder());

		return bean;
	}

	@Managed
	public CsaStatePersistence csaStatePersistence(String accessId) {
		CsaStatePersistenceImpl result = new CsaStatePersistenceImpl();
		result.setPathValueStore(fileBasedKeyValueStore(accessId));

		return result;
	}

	private FileBasedGmPathValueStore fileBasedKeyValueStore(String accessId) {
		FileBasedGmPathValueStore bean = new FileBasedGmPathValueStore();
		bean.setDescriptor("CsaStatePersistence (" + accessId + ")");
		bean.setRootDir(storageBase(accessId));
		bean.setSerializationOptions(GmSerializationOptions.defaultOptions.derive().outputPrettiness(OutputPrettiness.high).build());
		bean.setMarshaller(marshalling.jsonMarshaller());

		return bean;
	}

	@Managed
	public GmmlManipulatorErrorHandler gmmlErrorHandler(String accessId) {
		return new TrackingErrorHandler(accessId);
	}

	@Managed
	public File storageBase(String accessId) {
		String configFilePath = PathCollectors.filePath.join(accessId, "data");
		return resources.database(configFilePath).asFile();
	}

	@Managed
	public DynamicModelAccessory dynamicModelAccessory(String accessId) {
		DynamicModelAccessory bean = new DynamicModelAccessory(() -> gmSessions.systemModelAccessoryFactory().getForAccess(accessId));
		return bean;
	}

	// #######################################################
	// ## . . . . . . . . Distributed setup . . . . . . . . ##
	// #######################################################

	// DCSA

	// This must not be @Managed!!!
	public CollaborativeSmoodAccess newRegularOrDistributedCsa(String accessId) {
		if (!isDistributedSetup()) {
			CollaborativeSmoodAccess csa = new CollaborativeSmoodAccess();
			csa.setAccessId(accessId);

			return csa;

		} else {
			DistributedCollaborativeSmoodAccess dcsa = new DistributedCollaborativeSmoodAccess();
			dcsa.setAccessId(accessId);
			dcsa.setSharedStorage(sharedStorage());
			dcsa.setCsaStatePersistence(csaStatePersistence(accessId));
			dcsa.setBinaryPersistenceEventSource(resourceAccess.fileSystemBinaryProcessor());

			return dcsa;
		}
	}

	@Managed
	public Supplier<DcsaSharedStorage> sharedStorageSupplier() {
		PostCortexSupplier<DcsaSharedStorage> bean = new PostCortexSupplier<>();
		bean.setSuppliedCommponentName("DCSA Shared Storage");
		bean.setDelegate((Supplier<DcsaSharedStorage>) this::sharedStorageOrNull);
		bean.setDeployRegistry(deployment.registry());

		return bean;
	}

	private DcsaSharedStorage sharedStorageOrNull() {
		return isDistributedSetup() ? sharedStorage() : null;
	}

	/**
	 * WARNING: accessing this bean should only be done once cortex is initialized, because doing it before might interfere with the actual
	 * initialization!!!
	 * <p>
	 * To be sure, use {@link #sharedStorageSupplier()} and do not resolve the actual storage from the supplier eagerly, cause you'll end with a
	 * {@link IllegalStateException}.
	 */
	@Managed
	private DcsaSharedStorage sharedStorage() {
		com.braintribe.model.dcsadeployment.DcsaSharedStorage dcsaSharedStorage = preCortex.dcsaStorage();

		PreCortexDeployer.doPreCortexDeploy(deployment.parallelDeploymentService(), preCortex.session(), preCortex.dcsaStorageRelatedDeployables());

		return deployment.registry() //
				.resolve(dcsaSharedStorage) //
				.getComponent(com.braintribe.model.dcsadeployment.DcsaSharedStorage.T);
	}

	@Managed
	private Hub<String> markerPersistence(String accessId) {
		ResourcesBuilderBasedPersistence<String> bean = new ResourcesBuilderBasedPersistence<>();
		bean.setDescriptor("collaborative marker (" + accessId + ")");
		bean.setStorageResourcesBuilderFactory(() -> collaborativeSmoodMarkerResource(accessId));
		bean.setMarshaller(marshalling.jsonMarshaller());

		return bean;
	}

	private ResourcesBuilder collaborativeSmoodMarkerResource(String accessId) {
		String configFilePath = PathCollectors.filePath.join(accessId, "data", "marker.txt");
		return resources.database(configFilePath);
	}

	public boolean isDistributedSetup() {
		return preCortex.dcsaStorage() != null;
	}

	// #######################################################
	// ## . . . . . ModuleSource Binary Retrieval . . . . . ##
	// #######################################################

	@Managed
	public ModuleSourceBinaryRetrieval moduleSourceBinaryRetrieval() {
		ModuleSourceBinaryRetrieval bean = new ModuleSourceBinaryRetrieval();
		bean.setModuleResourcesContractResolver(moduleName -> moduleInitialization.moduleLoader().resolveModuleResourcesContract(moduleName));

		return bean;
	}

	@Managed
	public HardwiredBinaryRetrieval moduleSourceBinaryRetrievalDeployable() {
		HardwiredBinaryRetrieval bean = HardwiredBinaryRetrieval.T.create();
		bean.setName("ModuleSource Binary Retrieval");
		bean.setExternalId("binaryRetrieval.moduleSource");
		bean.setGlobalId("hardwired:resource/" + bean.getExternalId());

		return bean;
	}

	// #######################################################
	// ## . . . . . . . CSA Binary Persistence. . . . . . . ##
	// #######################################################

	@Managed
	public CsaBinaryRetrieval csaBinaryRetrieval() {
		CsaBinaryRetrieval bean = new CsaBinaryRetrieval();
		bean.setRetrievalDelegate(resourceAccess.fileSystemBinaryProcessor());
		bean.setAccessIdToResourcesBase(resourceAccess.accessIdToResourcesBaseFile());
		bean.setDeployRegistry(this::resolveCsa);

		return bean;
	}

	private CollaborativeSmoodAccess resolveCsa(String accessId) {
		DeployedUnit du = deployment.registry().resolve(accessId);
		if (du == null)
			throw new IllegalStateException("Cannot resolve CSA as no access is deployed with externalId: " + accessId);

		DeployedComponent dc = du.getDeployedComponent(CollaborativeAccess.T);

		// We rely on the fact that there are no other CollaborativeAccess implementations, only CSA
		return (CollaborativeSmoodAccess) dc.suppliedImplementation();
	}

	@Managed
	public HardwiredBinaryRetrieval csaBinaryRetrievalDeployable() {
		HardwiredBinaryRetrieval bean = HardwiredBinaryRetrieval.T.create();
		bean.setName("CSA Binary Retrieval");
		bean.setExternalId("binaryRetrieval.csa");
		bean.setGlobalId("hardwired:resource/" + bean.getExternalId());

		return bean;
	}

}
