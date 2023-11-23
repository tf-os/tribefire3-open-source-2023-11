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
package tribefire.extension.scripting.statechange.processing;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.BeforeStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessors;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;
import com.braintribe.utils.lcd.LazyInitialized;

import tribefire.extension.scripting.common.AbstractScriptedProcessor;
import tribefire.extension.scripting.model.deployment.Script;

public class ScriptedStateChangeProcessor extends AbstractScriptedProcessor implements StateChangeProcessor<GenericEntity, GenericEntity> {

	private StateChangeProcessorCapabilities capabilities;

	private LazyInitialized<ProcessableScript> processableBeforeScript;
	private LazyInitialized<ProcessableScript> processableAfterScript;
	private LazyInitialized<ProcessableScript> processableProcessScript;

	@Configurable
	public void setBeforeScript(Script beforeScript) {
		if (beforeScript == null) {
			this.processableBeforeScript = null;
			return;
		}
		this.processableBeforeScript = new LazyInitialized<>(() -> getProcessableScript(beforeScript));
	}

	@Configurable
	public void setAfterScript(Script afterScript) {
		if (afterScript == null) {
			this.processableAfterScript = null;
			return;
		}
		this.processableAfterScript = new LazyInitialized<>(() -> getProcessableScript(afterScript));
	}

	@Configurable
	public void setProcessScript(Script processScript) {
		if (processScript == null) {
			this.processableProcessScript = null;
			return;
		}
		this.processableProcessScript = new LazyInitialized<>(() -> getProcessableScript(processScript));
	}

	@Override
	public void onAfterStateChange(AfterStateChangeContext<GenericEntity> context, GenericEntity customContext) throws StateChangeProcessorException {
		callScript(context, customContext, processableAfterScript);
	}

	@Override
	public GenericEntity onBeforeStateChange(BeforeStateChangeContext<GenericEntity> context) throws StateChangeProcessorException {
		Maybe<GenericEntity> reasonedReturn = callScript(context, null, processableBeforeScript);
		return reasonedReturn.get();
	}

	@Override
	public void processStateChange(com.braintribe.model.processing.sp.api.ProcessStateChangeContext<GenericEntity> context,
			GenericEntity customContext) throws StateChangeProcessorException {

		callScript(context, customContext, processableProcessScript);
	}

	private <T> Maybe<T> callScript(StateChangeContext<GenericEntity> context, GenericEntity customContext,
			LazyInitialized<ProcessableScript> processableScript) throws StateChangeProcessorException {

		if (processableScript == null)
			return Maybe.complete(null);

		Map<String, Object> bindings = createBindings(context, customContext);
		return processableScript.get().processReasoned(bindings);
	}

	private Map<String, Object> createBindings(StateChangeContext<GenericEntity> context, GenericEntity customContext) {

		StateChangeScriptContext scriptContext = new StateChangeScriptContext(context);
		scriptContext.setEntity(context.getProcessEntity());
		scriptContext.setStateChangeCustomContext(customContext);
		if (context.getProcessEntity() != null) {
			scriptContext.setAffectedProperty(context.getProcessEntity().entityType().getProperty(context.getEntityProperty().getPropertyName()));
		}

		Map<String, Object> bindings = new HashMap<String, Object>();
		bindings.put("$context", scriptContext);
		bindings.put("$session", context.getSession());
		bindings.put("$systemSession", context.getSystemSession());

		return bindings;
	}

	@Override
	public StateChangeProcessorCapabilities getCapabilities() {
		if (capabilities == null) {
			capabilities = StateChangeProcessors.capabilities(processableBeforeScript != null, processableAfterScript != null,
					processableProcessScript != null);
		}
		return capabilities;
	}

}
