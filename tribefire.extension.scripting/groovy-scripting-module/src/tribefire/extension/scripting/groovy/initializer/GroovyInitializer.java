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
package tribefire.extension.scripting.groovy.initializer;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.scripting.deployment.model.GroovyScript;
import tribefire.extension.scripting.deployment.model.GroovyScriptingEngine;
import tribefire.extension.scripting.groovy.initializer.wire.GroovyInitializerWireModule;
import tribefire.extension.scripting.groovy.initializer.wire.contract.GroovyInitializerContract;
import tribefire.extension.scripting.groovy.initializer.wire.space.GroovyInitializerSpace;
import tribefire.extension.scripting.model.deployment.meta.EvaluateScriptWith;
import tribefire.module.wire.contract.ModelApiContract;

/**
 * The GroovyInitializer adds the {@link EvaluateScriptWith} type to the meta data of {@link GroovyScript}. 
 * The scripting engine {@link GroovyScriptingEngine} (wrapped into the {@link EvaluateScriptWith}) 
 * is obtained via {@link GroovyInitializerSpace}. 
 * 
 * @author Dirk Scheffler
 */
public class GroovyInitializer extends AbstractInitializer<GroovyInitializerContract> {

	private final ClassLoader classLoader;
	private final ModelApiContract modelApi;

	public GroovyInitializer(ClassLoader classLoader, ModelApiContract modelApi) {
		this.classLoader = classLoader;
		this.modelApi = modelApi;
	}

	@Override
	protected WireTerminalModule<GroovyInitializerContract> getInitializerWireModule() {
		return new GroovyInitializerWireModule(classLoader);
	}

	@Override
	protected void initialize(PersistenceInitializationContext context, WiredInitializerContext<GroovyInitializerContract> initializerContext,
			GroovyInitializerContract initializerContract) {

		GmMetaModel cortexConfigurationModel = initializerContract.cortexConfigurationModel();
		GmMetaModel groovyScriptingModel = initializerContract.groovyScriptingModel();
		cortexConfigurationModel.getDependencies().add(groovyScriptingModel);

		ModelMetaDataEditor modelEditor = modelApi.newMetaDataEditor(cortexConfigurationModel).done();

		modelEditor.onEntityType(GroovyScript.T).addMetaData(initializerContract.evaluateScriptWith());
	}
}
