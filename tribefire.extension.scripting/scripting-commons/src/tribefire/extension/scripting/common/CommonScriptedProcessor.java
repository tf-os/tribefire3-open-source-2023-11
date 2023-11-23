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
package tribefire.extension.scripting.common;

import java.util.Map;

import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.utils.lcd.LazyInitialized;

import tribefire.extension.scripting.api.CompiledScript;
import tribefire.extension.scripting.api.ScriptingEngineResolver;
import tribefire.extension.scripting.model.deployment.Script;

/**
 * Common base class for scripted service processors like {@link ScriptedServiceProcessor}. The common class hold the {@link ScriptEngineResolver},
 * the {@link Script} and a singleton version of the {@link CompiledScript}. Most importantly it offers the central common processReasonedScripted
 * method.
 * 
 *
 */
public class CommonScriptedProcessor extends AbstractScriptedProcessor {

	private Script script;
	private LazyInitialized<ProcessableScript> processableScript = new LazyInitialized<AbstractScriptedProcessor.ProcessableScript>(
			() -> getProcessableScript(script));

	@Required
	public void setScript(Script script) {
		this.script = script;
	}

	public Script getScript() {
		return this.script;
	}

	/**
	 * The central common algorithm for scripted service processing. The input data passed to the script are based on "bindings", and are internally
	 * further enhanced with more common data. Scripts are pre-compiled once before they are evaluated (multiple times).
	 * 
	 * @param <R>
	 *            Return object.
	 * @param bindings
	 *            Must be mutable and will be further modified internally.
	 * @return A reasoned return object.
	 */
	protected <R> Maybe<R> processReasonedScripted(Map<String, Object> bindings) {

		return processableScript.get().processReasoned(bindings);
	}

}
