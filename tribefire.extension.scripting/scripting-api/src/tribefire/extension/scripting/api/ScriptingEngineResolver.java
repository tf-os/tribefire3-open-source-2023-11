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
import com.braintribe.model.generic.reflection.EntityType;

import tribefire.extension.scripting.model.deployment.Script;

/**
 * Can resolve a suited {@linke ScriptingEngine} given a {@link Script} denotation type. 
 * Furthermore, also direct 
 * calls to evaluate, or compile with internal ScriptingEngine resolution are supported.
 *
 * @author Dirk Scheffler
 */

public interface ScriptingEngineResolver {

	/**
	 * Resolve {@link ScriptingEngine} given the entity type of a {@link Script}.
	 * 
	 * @param <S> Is the denotation type of the {@link Script} to be processed. 
	 * @param scriptType The object holding the script data.
	 * 
	 * @return Reason containing a {@link ScriptingEngine} for {@link Script} entity type.
	 */
	<S extends Script> Maybe<ScriptingEngine<S>> resolveEngine(EntityType<S> scriptType);

	/**
	 * To evaluate a script object given the input parameter bindings, using internal resolution of the 
	 * scripting engine. 
	 * 
	 * @param <S>
	 *            Denotes the script denotation type.
	 * @param <T>
	 *            Denotes the return type of the script.
	 * @param script
	 *            Is the script object holding the script data.
	 * @param bindings
	 *            Are the script input parameter bindings, which are passed to the script as input parameters.
	 *            
	 * @return Reason containing the script return object.
	 */
	default <S extends Script, T> Maybe<T> evaluate(S script, Map<String, Object> bindings) {
		return resolveEngine(script.entityType()).flatMap(engine -> engine.evaluate(script, bindings));
	}

	/**
	 * To compile a script object into a {@link CompiledScript} object, using internal scripting engine resolution.
	 * 
	 * @param <S>
	 *            Denotes the script type.
	 * @param <T>
	 *            Denotes the return type of the script.
	 * @param script
	 *            Is the script object.
	 * @return Reason containing the {@link CompiledScript}.
	 */
	default <S extends Script, T> Maybe<CompiledScript> compile(S script) {
		return resolveEngine(script.entityType()).flatMap(engine -> engine.compile(script));
	}
}
