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
package tribefire.cortex.initializer.support.wire;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.space.ContractResolution;
import com.braintribe.wire.api.space.ContractSpaceResolver;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.impl.properties.PropertyLookups;

import tribefire.cortex.initializer.support.impl.InitializerSupportSpace;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;
import tribefire.cortex.initializer.support.impl.lookup.ManagedInstancesLookups;
import tribefire.cortex.initializer.support.wire.contract.InitializerSupportContract;
import tribefire.cortex.initializer.support.wire.contract.PropertyLookupContract;
import tribefire.module.wire.contract.ModuleReflectionContract;

/**
 * This wire module provides wire context configuration needed for wiring of initializer support and initializer wire spaces.
 * 
 */
public class InitializerSupportWireModule<S extends WireSpace> implements WireTerminalModule<S> {

	private final WireTerminalModule<S> initializerModule;
	private final ModuleReflectionContract moduleReflection;
	private final Supplier<String> initializerIdSupplier;
	private final PersistenceInitializationContext context;
	private final ManagedGmSession session;
	private final WireContext<?> parentContext;

	public InitializerSupportWireModule(WireTerminalModule<S> initializerModule, Supplier<String> initializerIdSupplier, PersistenceInitializationContext context,
			WireContext<?> parentContext) {
		this.initializerModule = initializerModule;
		this.moduleReflection = parentContext.contract(ModuleReflectionContract.class);
		this.initializerIdSupplier = initializerIdSupplier;
		this.context = context;
		this.session = context.getSession();
		this.parentContext = parentContext;
	}
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);

		InitializerSupportSpace initializerSpace = new InitializerSupportSpace(context, moduleReflection, initializerIdSupplier);

		contextBuilder
				.loadSpacesFrom(contract().getClassLoader())
				.bindContract(InitializerSupportContract.class, initializerSpace);
		
		contextBuilder.bindContracts(new LookupSpaceResolver());
		contextBuilder.bindContracts(new PropertyContractResolution());
		
		contextBuilder.parent(parentContext);
	}
	
	@Override
	public List<WireModule> dependencies() {
		return Collections.singletonList(initializerModule);
	}

	@Override
	public Class<S> contract() {
		return initializerModule.contract();
	}
	
	private class LookupSpaceResolver implements ContractSpaceResolver {

		@Override
			public ContractResolution resolveContractSpace(Class<? extends WireSpace> contractSpaceClass) {
				throw new UnsupportedOperationException();
			}
		
		@Override
		public ContractResolution resolveContractSpace(WireContext<?> wireContext, Class<? extends WireSpace> contractSpaceClass) {
			WireModule ownerModule = wireContext.findModuleFor(contractSpaceClass);
			InstanceLookup instanceLookup = contractSpaceClass.getAnnotation(InstanceLookup.class);
			if (instanceLookup != null && (instanceLookup.lookupOnly() || ownerModule.getClass() != initializerModule.getClass())) {
				return f -> ManagedInstancesLookups.create(contractSpaceClass, instanceLookup, ownerModule.getClass()::getSimpleName, session::findEntityByGlobalId);
			}
			else {
				return null;
			}
				
		}
		
	}
	
	private static class PropertyContractResolution implements ContractSpaceResolver {
		private final boolean suppressDecryption = Boolean.TRUE.toString().equals(TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_SECURED_ENVIRONMENT));
		@Override
		public ContractResolution resolveContractSpace(Class<? extends WireSpace> contractSpaceClass) {
			if (PropertyLookupContract.class.isAssignableFrom(contractSpaceClass))
				return f -> PropertyLookups.create(contractSpaceClass, TribefireRuntime::getProperty, suppressDecryption);
			else
				return null;
		}
	}


}
