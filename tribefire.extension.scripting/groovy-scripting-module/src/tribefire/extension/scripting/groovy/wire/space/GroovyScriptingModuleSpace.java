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
package tribefire.extension.scripting.groovy.wire.space;

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContext;

import tribefire.extension.scripting.deployment.model.GroovyScriptingEngine;
import tribefire.extension.scripting.groovy.GroovyEngine;
import tribefire.extension.scripting.groovy.initializer.GroovyInitializer;
import tribefire.extension.scripting.module.wire.contract.ScriptingBindersContract;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.wire.contract.HardwiredDeployablesContract;
import tribefire.module.wire.contract.ModelApiContract;
import tribefire.module.wire.contract.ModuleReflectionContract;
import tribefire.module.wire.contract.SystemUserRelatedContract;
import tribefire.module.wire.contract.TribefireModuleContract;

/**
 * Wiring of the Groovy scripting engine. Provides the static instance of {@link GroovyEngine} deployable and binds it to the
 * {@link GroovyScriptingEngine} denotation type.
 * 
 * @author Dirk Scheffler
 */
@Managed
public class GroovyScriptingModuleSpace implements TribefireModuleContract {

	@Import
	private ModuleReflectionContract moduleReflection;

	@Import
	protected ScriptingBindersContract scriptingBinding;

	@Import
	private HardwiredDeployablesContract hardwiredDeployables;

	@Import
	private SystemUserRelatedContract systemUserRelated;

	@Import
	private ModelApiContract modelApi;

	@Import
	private WireContext<?> wireContext;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(GroovyScriptingEngine.T).component(scriptingBinding.scriptingEngine()).expertSupplier(this::groovy);
	}

	/** @return Static instance of a {@link GroovyEngine}. */
	@Managed
	private GroovyEngine groovy() {
		GroovyEngine bean = new GroovyEngine();
		bean.setCortexSession(systemUserRelated.cortexSessionSupplier());
		return bean;
	}

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		bindings.bind(this::initializeCortex);
	}

	private void initializeCortex(PersistenceInitializationContext context) {
		new GroovyInitializer(moduleReflection.moduleClassLoader(), modelApi).initialize(context, wireContext);
	}

}
