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
package tribefire.cortex.module.loading;

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static java.util.Objects.requireNonNull;
import static tribefire.cortex.module.loading.ModuleLoaderHelper.createWiringLoader;
import static tribefire.cortex.module.loading.ModuleLoaderHelper.getModuleBaseDir;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.cfg.ScopeContext;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.csa.CustomInitializer;
import com.braintribe.model.csa.DynamicInitializer;
import com.braintribe.model.csa.ModuleInitializer;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;
import com.braintribe.model.processing.session.api.collaboration.DataInitializer;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.provider.ManagedValue;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.ScopeContextHolders;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.space.ContractSpaceResolver;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.module.loading.api.PlatformContractsRegistry;
import tribefire.descriptor.model.ModuleDescriptor;
import tribefire.descriptor.model.ModulePackagingInfo;
import tribefire.descriptor.model.PlatformDescriptor;
import tribefire.module.wire.contract.ModuleResourcesContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.platform.impl.deployment.DenotationBindingsRegistry;

/**
 * This class is responsible for loading all the tribefire modules.
 * <p>
 * Loading a module means resolving it's {@link TribefireModuleContract} and on that calling all it's bind methods. The actual module loading is done
 * in the following steps:
 * <ol>
 * <li>Prepare a {@link ModuleClassLoader}. If the module lies on the main classpath, this class-loader will only be used to read the
 * {@link ModulePackagingInfo} of the module (for now only the {@link ModulePackagingInfo#getWireModule() wireModule} property is relevant), otherwise
 * this class-loader will be used to also load the WireModule itself.</li>
 * 
 * <li>Load module's WireModule class-name with the class loader from previous step. This WireModule can either be a class/enum with a static field
 * called <tt>INSTANCE</tt>, or a class with a no-argument constructor.</li>
 * 
 * <li>Load module's WireModule with the appropriate class-loader (see previous steps).</li>
 * 
 * <li>Prepare a {@link ModuleLoadingWireModule} which extends the WireModule from previous step, containing also all the contracts available for
 * currently loaded module, courtesy of {@link ModuleContractsRegistry}.</li>
 * 
 * <li>Using the {@linkplain ModuleLoadingWireModule} from previous step, we load the module's {@link TribefireModuleContract} implementation.</li>
 * 
 * <li>We register the module internally for later, when binding {@link Deployable}s.</li>
 * 
 * <li>We create a {@link ModuleInitializersRegistry registry} for all {@link DataInitializer data initializers} of given module and register the
 * default initializers, that add it's own {@link Module denotation type}, as well as it's {@link HardwiredDeployable}s.</li>
 * 
 * <li>We bind the module's {@link HardwiredDeployable hardwired deployables}, {@link WireSpace wire contracts} and {@link DataInitializer
 * initializers} via the corresponding {@link TribefireModuleContract}'s bind methods.</li>
 * 
 * <li>We bind the module's regular (i.e. not hardwired) {@link Deployable deployables} via the
 * {@link ModuleLoading#bindModuleDeployables(String, ModuleEntry)} method..</li>
 * </ol>
 * 
 * <p>
 * When it comes to {@link DataInitializer}s, every {@link CollaborativeAccess} must have a {@link ModuleInitializer} entry in it's persistence for
 * every module that initializes it. When such an entry is being resolved, {@link ModuleInitializersRegistry} is resolved for given module, and then
 * the actual {@link PersistenceInitializer} via {@link ModuleInitializersRegistry#getInitializer(String)}, based on the access id. See this last
 * method for more details.
 * 
 * @author peter.gazdik
 */
public class ModuleLoader implements DestructionAware {

	private static final Logger log = Logger.getLogger(ModuleLoader.class);

	private File modulesFolder;
	private File modulesLibFolder;
	private DenotationBindingsRegistry bindingRegistry;
	private PlatformHardwiredDeployablesRegistry hardwiredRegistry;
	private ContractSpaceResolver propertyContractResolver;
	private Function<ScopeContext, Map<ScopeContext, ScopeContextHolders>> shareScopeContextsExpert;

	/* package */ final ModulesCortexInitializer cortexInitializer = new ModulesCortexInitializer();
	/* package */ Function<String, File> accessStorageResolver;

	private final Map<String, ModuleEntry> moduleEntries = newLinkedMap();
	private final ModuleContractsRegistry contractsRegistry = new ModuleContractsRegistry(cortexInitializer);
	private final ModuleDenotationBindingRegistry denotationBindingRegistry = new ModuleDenotationBindingRegistry();
	private final DeployableModuleAssigning deployableModuleAssigning = new DeployableModuleAssigning();

	private final Map<String, WireContext<TribefireModuleContract>> moduleWireContexts = newLinkedMap();
	private LoadingState state = LoadingState.off;

	private static enum LoadingState {
		off,
		loading,
		ready;
	}

	/**
	 * Registers tribefire module contracts necessary for given platform via ModuleContractsRegistry#bindPlatform and also registers cortex
	 * initializer which prepares a {@link Module} denotation for the platform itself.
	 */
	@Required
	public void setPlatform(String paltformGroupId, String platformArtifactId, Consumer<PlatformContractsRegistry> platformContractsBinder) {
		PlatformDescriptor platformDescriptor = ModuleLoaderHelper.platformDescriptor(paltformGroupId, platformArtifactId);
		contractsRegistry.bindPlatform(platformDescriptor, platformContractsBinder);

		cortexInitializer.bind(ctx -> ModuleLoaderHelper.createPlatformModuleAndAssingToAllDeployables(ctx, paltformGroupId, platformArtifactId));
	}

	@Required
	public void setHardwiredRegistry(PlatformHardwiredDeployablesRegistry hardwiredRegistry) {
		this.hardwiredRegistry = hardwiredRegistry;
		this.hardwiredRegistry.cortexInitializer = cortexInitializer;
	}

	@Required
	public void setHardwiredExperts(PlatformHardwiredExpertsRegistry hardwiredExperts) {
		hardwiredExperts.cortexInitializer = cortexInitializer;
	}

	@Required
	public void setModulesFolder(File modulesFolder) {
		this.modulesFolder = modulesFolder;
		this.modulesLibFolder = new File(modulesFolder, "lib");
	}

	@Required
	public void setDenotationBindingsRegistry(DenotationBindingsRegistry bindingRegistry) {
		this.bindingRegistry = bindingRegistry;
		this.deployableModuleAssigning.bindingRegistry = bindingRegistry;
	}

	@Required
	public void setPropertyContractResolver(ContractSpaceResolver propertyContractResolver) {
		this.propertyContractResolver = propertyContractResolver;
	}

	@Required
	public void setShareScopeContextsExpert(Function<ScopeContext, Map<ScopeContext, ScopeContextHolders>> shareScopeContextsExpert) {
		this.shareScopeContextsExpert = shareScopeContextsExpert;
	}

	/** This function resolves an access id into the access' data folder inside the storage folder. */
	@Required
	public void setAccessStorageResolver(Function<String, File> accessStorageResolver) {
		this.accessStorageResolver = accessStorageResolver;
	}

	public ModuleResourcesContract resolveModuleResourcesContract(String moduleName) {
		return contractsRegistry.resolveModuleResourcesContract(moduleName);
	}

	public void loadModules() {
		checkConfiguredProperly();

		state = LoadingState.loading;

		new ModuleLoading().loadModules();

		state = LoadingState.ready;
	}

	private void checkConfiguredProperly() {
		if (!modulesFolder.exists())
			throw new GenericModelException("Cannot initialize modules. Modules folder does not exist: " + modulesFolder.getAbsolutePath());
	}

	@Override
	public void preDestroy() {
		if (state != LoadingState.ready)
			return;

		for (Entry<String, ? extends WireContext<?>> e : moduleWireContexts.entrySet()) {
			log.debug(() -> "Shutting down Wire-Context of module: " + e.getKey());

			try {
				e.getValue().shutdown();
			} catch (Exception ex) {
				log.error("Error while shutting down Wire-Context of module: " + e.getKey(), ex);
			}
		}
	}

	private class ModuleLoading {

		private ModuleDescriptor currentModule;
		private File currentModuleDir;
		private StopWatch currentStopWatch;

		private final Map<ModuleDescriptor, StopWatch> stopWatches = newLinkedMap();

		private void loadModules() {
			List<ModuleDescriptor> moduleDescriptors = ModuleLoaderHelper.readModuleDescriptors(modulesFolder);

			logFoundModules(moduleDescriptors);

			for (ModuleDescriptor moduleDescriptor : moduleDescriptors)
				wireModule(moduleDescriptor);

			for (ModuleEntry me : moduleEntries.values())
				bindModule(me);

			for (Entry<String, ModuleEntry> e : moduleEntries.entrySet())
				bindModuleDeployables(e.getKey(), e.getValue());

			logModuleLoadingTimes();
		}

		private void logFoundModules(List<ModuleDescriptor> moduleDescriptors) {
			String moduleNames = moduleDescriptors.stream() //
					.map(ModuleDescriptor::name) //
					.sorted() //
					.collect(Collectors.joining("\n    "));

			log.info("Starting Tribefire with modules:\n    " + moduleNames);

		}

		private void logModuleLoadingTimes() {
			String moduleTimeEntries = stopWatches.entrySet().stream() //
					.map(e -> e.getKey().getArtifactId() + "  -  " + e.getValue().getElapsedTime() + " ms") //
					.collect(Collectors.joining("\n    "));

			log.info("Started Tribefire with modules:\n    " + moduleTimeEntries);
		}

		// #################################################
		// ## . . . . . . . . Wire Module . . . . . . . . ##
		// #################################################

		private void wireModule(ModuleDescriptor moduleDescriptor) {
			log.debug(() -> "Wiring module: " + moduleDescriptor.name());

			setCurrentModule(moduleDescriptor, new StopWatch());

			TribefireModuleContract tfModuleContract = loadCurrentModuleContract();

			tfModuleContract.bindWireContracts(contractsRegistry);

			prepareModuleEntry(tfModuleContract);

			currentStopWatch.pause();
		}

		private TribefireModuleContract loadCurrentModuleContract() {
			WiringLoader wiringLoader = createWiringLoader(currentModule, modulesLibFolder, currentModuleDir);

			contractsRegistry.setCurrentModuleClassLoader(wiringLoader.getWiringClassLoader());

			WireModule moduleWiring = wiringLoader.loadModuleWiring();

			ModuleLoadingWireModule moduleInitializationWiring = new ModuleLoadingWireModule( //
					contractsRegistry, moduleWiring, propertyContractResolver, shareScopeContextsExpert);

			return getContractOf(moduleInitializationWiring);
		}

		private TribefireModuleContract getContractOf(ModuleLoadingWireModule moduleInitializationWiring) {
			try {
				WireContext<TribefireModuleContract> wireContext = Wire.context(moduleInitializationWiring);
				moduleWireContexts.put(currentModule.name(), wireContext);

				return wireContext.contract();

			} catch (Throwable t) {
				Exceptions.contextualize(t, "Error while loading module: " + currentModule.name());
				throw t;
			}
		}

		private void prepareModuleEntry(TribefireModuleContract tfModuleContract) {
			ModuleInitializersRegistry miRegistry = new ModuleInitializersRegistry(currentModule, ModuleLoader.this);
			ModuleEntry moduleEntry = new ModuleEntry(currentModule, tfModuleContract, miRegistry);

			registerEntry(currentModule.getModuleGlobalId(), moduleEntry);
		}

		private void registerEntry(String moduleGlobalId, ModuleEntry moduleEntry) {
			ModuleEntry other = moduleEntries.put(moduleGlobalId, moduleEntry);
			if (other != null)
				throw new IllegalStateException("Problem with setup. Multiple modules found with globalId '" + moduleGlobalId
						+ "'. Each module has a module.yml file containing this globalId. If the file is missing, the globalId is derived from the module folder name.");
		}

		// #################################################
		// ## . . . . . . . . Bind Module . . . . . . . . ##
		// #################################################

		private void bindModule(ModuleEntry moduleEntry) {
			log.debug(() -> "Binding module: " + moduleEntry.descriptor.name());

			setCurrentModule(moduleEntry.descriptor, null);

			moduleEntry.wireContract.onBeforeBinding();

			primeCortexInitialization(moduleEntry);
			performModuleBinding(moduleEntry);
			primeDeployableModuleAssignerIfRelevant(moduleEntry);

			moduleEntry.wireContract.onAfterBinding();

			currentStopWatch.pause();
		}

		/**
		 * We configure the default cortex priming which creates the {@link Module} denotation type and also puts all hardwired Deployables into
		 * cortex with an information about their origin module.
		 */
		private void primeCortexInitialization(ModuleEntry me) {
			cortexInitializer.bind(ctx -> {
				Module moduleDenotation = cortexInitializer.createModuleDenotation(ctx.getSession(), me.descriptor);
				hardwiredRegistry.putHarwiredDeployablesIntoCortex(ctx, me.descriptor, moduleDenotation);
			});
		}

		private void performModuleBinding(ModuleEntry entry) {
			TribefireModuleContract mc = entry.wireContract;

			mc.bindHardwired();

			mc.bindInitializers(entry.initializersRegistry);

			// This just collects deployables information, so that cortex can be extended automatically.
			// Actual binding happens later, via bindDeployables method below (this is controlled by the platform)
			/* This is now probably not needed, as Deployables are bound before any deployment (i.e. also before cortex), but just in case, let's keep
			 * it here for now. */
			mc.bindDeployables(cortexExtendingBindingBuilder(entry));
		}

		private DenotationBindingBuilder cortexExtendingBindingBuilder(ModuleEntry entry) {
			return denotationBindingRegistry.newCortexExtendingDenotationBindingBuilder(entry, cortexInitializer);
		}

		private void primeDeployableModuleAssignerIfRelevant(ModuleEntry me) {
			if (me.initializersRegistry.bindsCortex())
				me.initializersRegistry.bind("cortex", deployableModuleAssigning.newModuleAssingingInitializer(me));
		}

		private void setCurrentModule(ModuleDescriptor _currentModule, StopWatch sw) {
			currentModule = requireNonNull(_currentModule);

			if (sw == null) {
				currentStopWatch = stopWatches.get(_currentModule);
				currentStopWatch.resume();
			} else {
				validateCurrentModuleDescriptor();
				currentStopWatch = sw;
				stopWatches.put(_currentModule, sw);
			}

			currentModuleDir = getModuleBaseDir(modulesFolder, currentModule);

			contractsRegistry.setCurrentModule(currentModule, currentModuleDir);
			hardwiredRegistry.setCurrentModule(currentModule);
			cortexInitializer.setCurrentModule(currentModule);
		}

		private void validateCurrentModuleDescriptor() {
			requireNonNull(currentModule.getJarPath(), "jarPath is null for module: " + currentModule.name());
		}

		// #################################################
		// ## . . . . . . . Bind Deployables . . . . . . .##
		// #################################################

		private void bindModuleDeployables(String moduleGlobalId, ModuleEntry moduleEntry) {
			DenotationBindingBuilder bindingBuilder = bindingRegistry.bindForModule(moduleGlobalId);

			denotationBindingRegistry.applyActual(moduleEntry, bindingBuilder);
		}
	}

	// #################################################
	// ## . . . . . . CSA Initialization . . . . . . .##
	// #################################################

	/**
	 * Returns a cortex initializer which adds certain types of information to cortex automatically. This initializer:
	 * <ul>
	 * <li>Adds models of bound deployables to the cortex model (tribefire-cortex-model).</li>
	 * <li>Creates {@link Module} instance for every module and the platform itself.</li>
	 * <li>Creates {@link HardwiredDeployable} instance for every bound hardwired deployable.</li>
	 * </ul>
	 */
	public PersistenceInitializer getImplicitCortexInitializer() {
		return cortexInitializer;
	}

	public ManagedValue<PersistenceInitializer> resolveModuleBoundInitializer(String accessId, CustomInitializer ci) {
		checkModulesLoaded("resolve module-bound initializer");

		if (ci instanceof ModuleInitializer)
			return resolvePersistenceInitializer(accessId, (ModuleInitializer) ci);

		if (ci instanceof DynamicInitializer)
			return resolveDynamicInitializer(accessId, (DynamicInitializer) ci);

		throw new IllegalArgumentException("Unsupported custom initializer of type " + ci.entityType().getTypeSignature());
	}

	private ManagedValue<PersistenceInitializer> resolvePersistenceInitializer(String accessId, ModuleInitializer mi) {
		String moduleGlobalId = mi.getModuleId();

		String lookupAccessId = Optional.ofNullable(mi.getRedirectedAccessId()).orElse(accessId);

		PersistenceInitializer initializer = getModuleInitializerRegistry(moduleGlobalId).getInitializer(lookupAccessId);
		return ManagedValue.of(initializer);
	}

	private ManagedValue<PersistenceInitializer> resolveDynamicInitializer(String accessId, DynamicInitializer di) {
		String moduleGlobalId = Module.moduleGlobalId(di.getModuleName());

		PersistenceInitializer initializer = getModuleInitializerRegistry(moduleGlobalId).resolveDynamicInitializer(accessId, di);
		return ManagedValue.of(initializer);
	}

	private void checkModulesLoaded(String action) {
		switch (state) {
			case off:
				throw new IllegalStateException("Cannot " + action + ", module-loading has not even started yet! Did you change your platform wiring?"
						+ " This usually happens if something tries to access 'cortex' too early. Examine your stacktrace to see if that was the case.");
			case loading:
				throw new IllegalStateException("Cannot " + action + ", module-loading is still in progress! Not sure how this could even happen.");
			default:
				return;
		}
	}

	private ModuleInitializersRegistry getModuleInitializerRegistry(String moduleGlobalId) {
		return getModuleEntry(moduleGlobalId).initializersRegistry;
	}

	// #################################################
	// ## . . . . . . . . Internal . . . . . . . . . .##
	// #################################################

	private ModuleEntry getModuleEntry(String moduleGlobalId) {
		ModuleEntry result = moduleEntries.get(moduleGlobalId);
		if (result == null)
			throw new IllegalArgumentException(
					"No module found with globalId '" + moduleGlobalId + "'. Available modules: " + moduleEntries.keySet());

		return result;
	}

	static class ModuleEntry {
		final ModuleDescriptor descriptor;
		final TribefireModuleContract wireContract;
		final ModuleInitializersRegistry initializersRegistry;

		public ModuleEntry(ModuleDescriptor descriptor, TribefireModuleContract wireContract, ModuleInitializersRegistry initializersRegistry) {
			this.descriptor = descriptor;
			this.wireContract = wireContract;
			this.initializersRegistry = initializersRegistry;
		}
	}

}
