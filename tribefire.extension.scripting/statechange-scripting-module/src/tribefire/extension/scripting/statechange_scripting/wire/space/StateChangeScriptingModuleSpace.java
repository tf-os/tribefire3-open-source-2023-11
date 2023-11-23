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
package tribefire.extension.scripting.statechange_scripting.wire.space;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.scripting.module.wire.contract.ScriptingContract;
import tribefire.extension.scripting.statechange.processing.ScriptedStateChangeProcessor;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.api.WireContractBindingBuilder;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefirePlatformContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class StateChangeScriptingModuleSpace implements TribefireModuleContract {

	// @Import
	// private TribefirePlatformContract tfPlatform;

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ScriptingContract scripting;

	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(tribefire.extension.scripting.statechange.model.deployment.ScriptedStateChangeProcessor.T).component(tfPlatform.binders().stateChangeProcessor())
				.expertFactory(this::scriptedStateChangeProcessor);
	}

	@Managed
	private ScriptedStateChangeProcessor scriptedStateChangeProcessor(
			ExpertContext<tribefire.extension.scripting.statechange.model.deployment.ScriptedStateChangeProcessor> context) {
		ScriptedStateChangeProcessor bean = new ScriptedStateChangeProcessor();
		tribefire.extension.scripting.statechange.model.deployment.ScriptedStateChangeProcessor deployable = context.getDeployable();
		bean.setDeployable(deployable);
		bean.setAfterScript(deployable.getAfterScript());
		bean.setBeforeScript(deployable.getBeforeScript());
		bean.setProcessScript(deployable.getProcessScript());
		bean.setEngineResolver(scripting.scriptingEngineResolver());
		bean.setSystemSessionFactory(tfPlatform.systemUserRelated().sessionFactory()); // TODO missing in TribefirePlatformContract
		bean.setRequestSessionFactory(tfPlatform.requestUserRelated().sessionFactory()); // TODO missing in TribefirePlatformContract
		bean.setPropertyLookup(tfPlatform.platformReflection()::getProperty);
		return bean;
	}

}
