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

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.cfg.ScopeContext;
import com.braintribe.wire.api.context.ScopeContextHolders;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.space.ContractSpaceResolver;

import tribefire.module.wire.contract.TribefireModuleContract;

/**
 * This {@link WireModule} is used to load the wire configuration of a tribefire module.
 * 
 * @see ModuleLoader
 * 
 * @author peter.gazdik
 */
public class ModuleLoadingWireModule implements WireTerminalModule<TribefireModuleContract> {

	private final WireModule tfModuleWireModule;
	private final ModuleContractsRegistry contractsRegistry;
	private final ContractSpaceResolver propertyContractResolver;
	private final Function<ScopeContext, Map<ScopeContext, ScopeContextHolders>> shareScopeContextsExpert;

	public ModuleLoadingWireModule(ModuleContractsRegistry contractsRegistry, WireModule tfModuleWireModule,
			ContractSpaceResolver propertyContractResolver, Function<ScopeContext, Map<ScopeContext, ScopeContextHolders>> shareScopeContextsExpert) {

		this.tfModuleWireModule = tfModuleWireModule;
		this.contractsRegistry = contractsRegistry;
		this.propertyContractResolver = propertyContractResolver;
		this.shareScopeContextsExpert = shareScopeContextsExpert;
	}

	@Override
	public Class<TribefireModuleContract> contract() {
		return TribefireModuleContract.class;
	}

	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		contextBuilder.loadSpacesFrom(tfModuleWireModule.getClass().getClassLoader());
		contextBuilder.bindContracts(propertyContractResolver);
		contextBuilder.shareScopeContexts(shareScopeContextsExpert);

		contractsRegistry.bindContracts(contextBuilder);
	}

	@Override
	public List<WireModule> dependencies() {
		return asList(tfModuleWireModule);
	}

}
