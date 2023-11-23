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
package tribefire.extension.scripting.groovy.initializer.wire.space;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.scripting.deployment.model.GroovyScriptingEngine;
import tribefire.extension.scripting.groovy.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.scripting.groovy.initializer.wire.contract.GroovyInitializerContract;
import tribefire.extension.scripting.model.deployment.meta.EvaluateScriptWith;

/**
 * Wiring of the {@link GroovyInitializerContract} to the {@link ExistingInstancesContract}. This
 * provides an instance of the {@link GroovyScriptingEngine} wrapped into an {@link EvaluateScriptWith} object.
 * 
 */
@Managed
public class GroovyInitializerSpace extends AbstractInitializerSpace implements GroovyInitializerContract {

	@Import
	private CoreInstancesContract coreInstances;
	
	@Import
	private ExistingInstancesContract existingInstances;

	@Override
	public GmMetaModel cortexConfigurationModel() {
		return coreInstances.cortexModel();
	}
	
	@Override
	public GmMetaModel groovyScriptingModel() {
		return existingInstances.groovyScriptingModel();
	}
	
	@Override
	@Managed
	public EvaluateScriptWith evaluateScriptWith() {
		EvaluateScriptWith bean = create(EvaluateScriptWith.T);
		bean.setEngine(groovyScriptingEngine());
		return bean;
	}

	@Managed
	private GroovyScriptingEngine groovyScriptingEngine() {
		GroovyScriptingEngine bean = create(GroovyScriptingEngine.T);
		bean.setExternalId("script-engine-groovy");
		return bean;
	}
}
