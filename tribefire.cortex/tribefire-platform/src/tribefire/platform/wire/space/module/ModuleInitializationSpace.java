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
package tribefire.platform.wire.space.module;

import java.io.File;
import java.util.function.Function;

import com.braintribe.model.csa.CustomInitializer;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.provider.ManagedValue;
import com.braintribe.utils.paths.PathCollectors;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.ContractSpaceResolver;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.module.loading.ModuleLoader;
import tribefire.cortex.module.loading.PropertyContractResolver;
import tribefire.cortex.module.loading.api.PlatformContractsRegistry;
import tribefire.module.wire.contract.CryptoContract;
import tribefire.module.wire.contract.HttpContract;
import tribefire.module.wire.contract.MasterUserAuthContextContract;
import tribefire.module.wire.contract.MessagingContract;
import tribefire.module.wire.contract.MessagingDestinationsContract;
import tribefire.module.wire.contract.ModelApiContract;
import tribefire.module.wire.contract.RequestProcessingContract;
import tribefire.module.wire.contract.RequestUserRelatedContract;
import tribefire.module.wire.contract.ResourceProcessingContract;
import tribefire.module.wire.contract.SecurityContract;
import tribefire.module.wire.contract.ServletsContract;
import tribefire.module.wire.contract.SystemToolsContract;
import tribefire.module.wire.contract.SystemUserRelatedContract;
import tribefire.module.wire.contract.ThreadingContract;
import tribefire.module.wire.contract.TopologyContract;
import tribefire.module.wire.contract.TribefireConnectionsContract;
import tribefire.module.wire.contract.WorkerContract;
import tribefire.platform.impl.initializer.UxModulesCortexInitializer;
import tribefire.platform.wire.TribefirePlatformWireModule;
import tribefire.platform.wire.space.MasterResourcesSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.DeployablesSpace;

/**
 * @author peter.gazdik
 */
@Managed
public class ModuleInitializationSpace implements WireSpace {

	// @formatter:off
	@Import private DeployablesSpace deployables;
	@Import private MasterResourcesSpace resources;
	@Import private TribefireWebPlatformSpace tfPlatform;
	// @formatter:on

	public void loadModules() {
		moduleLoader().loadModules();
	}

	@Managed
	public ModuleLoader moduleLoader() {
		ModuleLoader bean = new ModuleLoader();
		bean.setPlatform("tribefire.cortex.services", "tribefire-web-platform", this::bindPlatform);
		bean.setModulesFolder(modulesFolder());
		bean.setHardwiredRegistry(tfPlatform.hardwiredDeployables());
		bean.setHardwiredExperts(tfPlatform.hardwiredExperts());
		bean.setPropertyContractResolver(propertyContractResolver());
		bean.setShareScopeContextsExpert(TribefirePlatformWireModule.INSTANCE.getShareScopeContextsExpert());
		bean.setDenotationBindingsRegistry(deployables.bindings());
		bean.setAccessStorageResolver(this::resolveAcessDataFolder);

		return bean;
	}

	/** @see ModuleLoader#getImplicitCortexInitializer */
	public PersistenceInitializer implicitCortexInitialier() {
		return moduleLoader().getImplicitCortexInitializer();
	}

	@Managed
	public Function<CustomInitializer, ManagedValue<PersistenceInitializer>> moduleBoundInitializerResolverFor(String accessId) {
		return mi -> moduleLoader().resolveModuleBoundInitializer(accessId, mi);
	}

	public File modulesFolder() {
		String modulesDir = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_MODULES_DIR);
		return resources.resource(modulesDir).asFile();
	}

	private ContractSpaceResolver propertyContractResolver() {
		PropertyContractResolver bean = new PropertyContractResolver();
		bean.setSuppressDecryption(Boolean.TRUE.toString().equals(TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_SECURED_ENVIRONMENT)));

		return bean;
	}

	private void bindPlatform(PlatformContractsRegistry registry) {
		// Contracts that extend other contracts have to be bound for all levels (e.g. TfWebPlatform, TfPlatform)
		registry.bindAllContractsOf(tfPlatform);
		registry.bindAllContractsOf(tfPlatform.binders());
		registry.bindAllContractsOf(tfPlatform.hardwiredDeployables());
		registry.bindAllContractsOf(tfPlatform.hardwiredExperts());
		registry.bindAllContractsOf(tfPlatform.marshalling());
		registry.bindAllContractsOf(tfPlatform.platformReflection());
		registry.bindAllContractsOf(tfPlatform.resources());

		registry.bind(ModelApiContract.class, tfPlatform.modelApi());

		registry.bind(RequestProcessingContract.class, tfPlatform.requestProcessing());
		registry.bind(ThreadingContract.class, tfPlatform.threading());
		registry.bind(RequestUserRelatedContract.class, tfPlatform.requestUserRelated());
		registry.bind(SystemUserRelatedContract.class, tfPlatform.systemUserRelated());

		registry.bind(MessagingContract.class, tfPlatform.messaging());
		registry.bind(MessagingDestinationsContract.class, tfPlatform.messaging().destinations());

		registry.bind(HttpContract.class, tfPlatform.http());
		registry.bind(CryptoContract.class, tfPlatform.crypto());
		registry.bind(MasterUserAuthContextContract.class, tfPlatform.masterUserAuthContext());
		registry.bind(SecurityContract.class, tfPlatform.security());
		registry.bind(ServletsContract.class, tfPlatform.servlets());
		registry.bind(SystemToolsContract.class, tfPlatform.systemTools());
		registry.bind(TopologyContract.class, tfPlatform.topology());
		registry.bind(TribefireConnectionsContract.class, tfPlatform.tribefireConnections());
		registry.bind(WorkerContract.class, tfPlatform.worker());

		registry.bind(ResourceProcessingContract.class, tfPlatform.resourceProcessing());
	}

	private File resolveAcessDataFolder(String accessId) {
		String configFilePath = PathCollectors.filePath.join(accessId, "data");
		return resources.database(configFilePath).asFile();

	}

	@Managed
	public UxModulesCortexInitializer uxModulesCortexInitializer() {
		UxModulesCortexInitializer bean = new UxModulesCortexInitializer();

		bean.setModulesFolder(modulesFolder());

		return bean;
	}
}
