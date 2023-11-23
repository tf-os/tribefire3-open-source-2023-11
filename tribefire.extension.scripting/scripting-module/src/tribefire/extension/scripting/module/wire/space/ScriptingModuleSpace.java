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
package tribefire.extension.scripting.module.wire.space;

import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.ComponentBinders;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.scripting.api.ScriptingEngine;
import tribefire.extension.scripting.model.deployment.Script;
import tribefire.extension.scripting.module.impl.MetaDataMappedScriptingEngineResolver;
import tribefire.extension.scripting.module.wire.contract.ScriptingBindersContract;
import tribefire.extension.scripting.module.wire.contract.ScriptingContract;
import tribefire.module.api.WireContractBindingBuilder;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * The scripting extension provides access to {@link Script}s linked to {@link ScriptingEngine}s 
 * based on their type. Both, script resources as well as script engine experts must be deployed 
 * separately in cortex.   
 * The {@link MetaDataMappedScriptingEngineResolver} provides the option to obtain a concrete
 * {@link tribefire.extension.scripting.api.ScriptingEngine} expert given a specific script type. 
 * 
 * @author Dirk Scheffler
 */
@Managed
public class ScriptingModuleSpace implements TribefireModuleContract, ScriptingBindersContract, ScriptingContract {

	@Import
	private TribefireWebPlatformContract tribefireWebPlatform;

	//
	// WireContracts
	//

	@Override
	public void bindWireContracts(WireContractBindingBuilder bindings) {
		bindings.bind(ScriptingBindersContract.class, this);
		bindings.bind(ScriptingContract.class, this);
	}

	@Override
	public ComponentBinder<tribefire.extension.scripting.model.deployment.ScriptingEngine, ScriptingEngine<?>> scriptingEngine() {
		return ComponentBinders.binder(tribefire.extension.scripting.model.deployment.ScriptingEngine.T,
				ScriptingEngine.class);
	}

	@Managed
	@Override
	public MetaDataMappedScriptingEngineResolver scriptingEngineResolver() {
		MetaDataMappedScriptingEngineResolver bean = new MetaDataMappedScriptingEngineResolver();
		//bean.setDeployedComponentResolver(tribefireWebPlatform.deployment().);
		bean.setDeployRegistry(tribefireWebPlatform.deployment().deployRegistry());
		bean.setCortexModelAccessory(tribefireWebPlatform.systemUserRelated().cortexModelAccessorySupplier().get());
		return bean;
	}
}
