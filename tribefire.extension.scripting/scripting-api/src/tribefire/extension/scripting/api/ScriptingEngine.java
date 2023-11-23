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
package tribefire.extension.scripting.api;

import java.util.Map;

import com.braintribe.gm.model.reason.Maybe;

import tribefire.extension.scripting.model.deployment.Script;

/**
 * 
 * Interface for script engines. A ScriptEngine provides an evaluate method to directly execute a Script, and a compile method to first compile a
 * Script.
 * 
 * @author Dirk Scheffler
 *
 * @param <S>
 *            Denotes the concrete script type derived from {@link Script}.
 */
public interface ScriptingEngine<S extends Script> {

	/**
	 * To evaluate a script given the parameters in bindings. 
	 * The method may return a {@linke ScriptingRuntimeError}.
	 * 
	 * @param <T>
	 *            Arbitrary return type, depending on the actual script to be executed. 
	 * @param script
	 *            Script data.
	 * @param bindings
	 *            Parameter bindings as map, which are passed as inputs to the script. 
	 *            
	 * @return A Reasoned return object, wiht a type that depends on the actual script object. 
	 */
	default <T> Maybe<T> evaluate(S script, Map<String, Object> bindings) {
		return compile(script).flatMap(compiledScript -> compiledScript.evaluate(bindings));
	}

	/**
	 * To compile a script. The method may return a {@link ScriptCompileError}.
	 * 
	 * @param script
	 *            Script data.
	 *            
	 * @return A reasoned {@link CompiledScript} object. 
	 */
	Maybe<CompiledScript> compile(S script);
}
