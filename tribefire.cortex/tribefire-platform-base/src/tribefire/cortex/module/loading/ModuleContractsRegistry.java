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

import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.newIdentityMap;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Collections.emptySet;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.module.loading.api.PlatformContractsRegistry;
import tribefire.descriptor.model.ComponentDescriptor;
import tribefire.descriptor.model.ModuleDescriptor;
import tribefire.descriptor.model.PlatformDescriptor;
import tribefire.module.api.WireContractBindingBuilder;
import tribefire.module.wire.contract.ModuleReflectionContract;
import tribefire.module.wire.contract.ModuleResourcesContract;
import tribefire.module.wire.contract.TribefireModuleContract;

/**
 * This is a registry of all the contracts available for a module that is being loaded.
 * <p>
 * It contains all the core contracts from the platform (see {@link #bindPlatform}), as well as all the contracts that already-loaded modules
 * announced in their {@link TribefireModuleContract#bindWireContracts} method. Note that this class' instance is actually passed as an argument to
 * that method, so modules actually directly call the {@link #bindContracts} method here. When a module has called that method, the bound contracts
 * are registered internally in this class, and then bound to the next modules' wire context via {@link #bindContracts} method.
 * 
 * @see #bindPlatform
 * @see #bind
 * @see #bindContracts
 * 
 * @author peter.gazdik
 */
/* package */ class ModuleContractsRegistry implements WireContractBindingBuilder, PlatformContractsRegistry {

	private final ModulesCortexInitializer cortexInitializer;

	private ComponentDescriptor currentComponent;
	private File currentModuleFolder;
	private ClassLoader currentModuleClassLoader;

	private final Map<Class<? extends WireSpace>, WireSpace> spaces = newIdentityMap();
	private final Map<Class<? extends WireSpace>, ComponentDescriptor> spaceOrigins = newIdentityMap();

	public ModuleContractsRegistry(ModulesCortexInitializer cortexInitializer) {
		this.cortexInitializer = cortexInitializer;
	}

	/**
	 * For normalization, and to be sure modules don't overwrite these spaces, we register the platform spaces here as well, rather than binding them
	 * in {@link ModuleLoadingWireModule#configureContext(WireContextBuilder)}. The binding is done vie configured consumer of
	 * {@link PlatformContractsRegistry}.
	 */
	public void bindPlatform(PlatformDescriptor platformDescriptor, Consumer<PlatformContractsRegistry> platformBinder) {
		currentComponent = platformDescriptor;

		platformBinder.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void bindAllContractsOf(WireSpace space) {
		for (Class<WireSpace> contractClass : contractsOf(space.getClass()))
			bind(contractClass, space);
	}

	private final Map<Class<? extends WireSpace>, Set<Class<WireSpace>>> wireSpaceToAllImplementingContracts = newMap();

	private Set<Class<WireSpace>> contractsOf(Class<? extends WireSpace> wireSpaceClass) {
		if (wireSpaceClass == WireSpace.class)
			return emptySet();

		Set<Class<WireSpace>> result = acquireSet(wireSpaceToAllImplementingContracts, wireSpaceClass);
		if (!result.isEmpty())
			return result;

		if (wireSpaceClass.isInterface())
			result.add((Class<WireSpace>) wireSpaceClass);

		Stream.of(wireSpaceClass.getInterfaces()) //
				.filter(WireSpace.class::isAssignableFrom) //
				.map(cl -> (Class<WireSpace>) cl) //
				.map(this::contractsOf) //
				.forEach(result::addAll);

		return result;
	}

	public void setCurrentModule(ModuleDescriptor currentModule, File currentModuleFolder) {
		this.currentComponent = currentModule;
		this.currentModuleFolder = currentModuleFolder;
	}

	public void setCurrentModuleClassLoader(ClassLoader currentModuleClassLoader) {
		this.currentModuleClassLoader = currentModuleClassLoader;
	}

	/** {@inheritDoc} */
	@Override
	public <T extends WireSpace> void bind(Class<T> contractClass, T space) {
		verifyContractLoadedByPlatform(contractClass);

		WireSpace otherSpace = spaces.put(contractClass, space);
		if (otherSpace != null)
			throwDuplicitContractBindingException(contractClass);

		spaceOrigins.put(contractClass, currentComponent);
		cortexInitializer.onBindWireContract(currentComponent);
	}

	/**
	 * Checks that the contract we are currently binding was in fact loaded by the platform ClassLoader, and not a module ClassLoader. This is
	 * important, because only then can it be used in another module.
	 */
	private void verifyContractLoadedByPlatform(Class<?> contractClass) {
		ClassLoader cl = contractClass.getClassLoader();
		if (cl instanceof ModuleClassLoader) {
			String moduleName = ((ModuleClassLoader) cl).getModuleName();
			throw new IllegalStateException("Cannot bind Wire contract '" + contractClass.getName()
					+ "' because the contract class was loaded by a module-specific ClassLoader, and thus wouldn't be visible outside of that module."
					+ " The wire contract should be placed in a 'GM-API' artifact, to assure it is present on the main (platform) classpath."
					+ " This contract was most likely put directly in the module artifact or maybe other non-'GM-API' artifact. "
					+ " Module which loaded this contract: " + moduleName);
		}
	}

	private void throwDuplicitContractBindingException(Class<? extends WireSpace> contractClass) {
		ComponentDescriptor otherComponent = spaceOrigins.get(contractClass);

		throw new IllegalArgumentException("Fatal error. Both modules '" + currentComponent.getArtifactId() + "' and '"
				+ otherComponent.getArtifactId() + "' are trying to bind a wire space for contract: " + contractClass.getName());
	}

	/**
	 * Binds all registered contracts for given wire context. This method is called exactly once for every module, and binds all the contracts
	 * available for this module (i.e. both the core ones of the platform as well as all those made available from all already-processed modules).
	 */
	public void bindContracts(WireContextBuilder<?> contextBuilder) {
		contextBuilder.bindContract(ModuleReflectionContract.class, newModuleReflectionSpace());
		contextBuilder.bindContract(ModuleResourcesContract.class, newModuleResourcesSpace());

		for (Entry<Class<? extends WireSpace>, WireSpace> entry : spaces.entrySet())
			contextBuilder.bindContract((Class<WireSpace>) (Class<?>) entry.getKey(), entry.getValue());
	}

	private ModuleReflectionSpace newModuleReflectionSpace() {
		return new ModuleReflectionSpace((ModuleDescriptor) currentComponent, currentModuleClassLoader);
	}

	private final Map<String, ModuleResourcesSpace> moduleNameToResourcesSpace = newMap();

	private ModuleResourcesSpace newModuleResourcesSpace() {
		ModuleDescriptor currentModuleDescriptor = (ModuleDescriptor) currentComponent;
		ModuleResourcesSpace result = new ModuleResourcesSpace(currentModuleDescriptor, currentModuleFolder, currentModuleClassLoader);

		moduleNameToResourcesSpace.put(currentModuleDescriptor.name(), result);

		return result;
	}

	public ModuleResourcesContract resolveModuleResourcesContract(String moduleName) {
		ModuleResourcesSpace result = moduleNameToResourcesSpace.get(moduleName);
		if (result == null)
			throw new IllegalArgumentException("Cannto resolve resources contract for module name: " + moduleName + ". Known module names: "
					+ moduleNameToResourcesSpace.keySet().stream().collect(Collectors.joining("\n\t", "[\n\t", "]")));

		return result;
	}

}
