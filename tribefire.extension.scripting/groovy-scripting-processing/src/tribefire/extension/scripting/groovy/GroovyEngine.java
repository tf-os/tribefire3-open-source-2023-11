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
package tribefire.extension.scripting.groovy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.function.Supplier;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;

import tribefire.extension.scripting.api.CompiledScript;
import tribefire.extension.scripting.api.ScriptingEngine;
import tribefire.extension.scripting.deployment.model.GroovyScript;
import tribefire.extension.scripting.model.ScriptCompileError;
import tribefire.extension.scripting.model.ScriptRuntimeError;

/**
 * The actual Groovy {@link ScriptingEngine} expert implementation.
 * 
 * @author Dirk Scheffler
 *
 */
public class GroovyEngine implements ScriptingEngine<GroovyScript> {

	private static final Compilable groovyEngine = (Compilable) new ScriptEngineManager().getEngineByName("Groovy");

	private Supplier<PersistenceGmSession> cortexSessionSupplier;

	@Configurable
	@Required
	public void setCortexSession(Supplier<PersistenceGmSession> cortexSession) {
		this.cortexSessionSupplier = cortexSession;
	}

	/**
	 * Compile {@link GroovyScript} script into a {@link CompiledScript}. May fail with {@link ScriptCompileError} reason.
	 * 
	 * @param script
	 *            A GroovyScript to be compiled.
	 * 
	 * @return Maybe<CompiledScript> Which may return the compiled script, or returns a reason for not doing so.
	 */
	@Override
	public Maybe<CompiledScript> compile(GroovyScript script) {

		return getResource(script).flatMap(r -> compileResource(r, script));
	}

	private InputStream openStream(Resource resource) throws IOException {
		if (resource.isStreamable())
			return resource.openStream();
		else
			return cortexSessionSupplier.get().resources().openStream(resource);
	}

	private Maybe<CompiledScript> compileResource(Resource resource, GroovyScript script) {
		try (Reader reader = new BufferedReader(new InputStreamReader(openStream(resource), "UTF-8"))) {

			return Maybe.complete(new GroovyCompiledScript(script, groovyEngine.compile(reader)));

		} catch (ScriptException e) {
			return Reasons.build(ScriptCompileError.T) //
					.text("Error while compiling Groovy Script: " + script) //
					.cause(InternalError.from(e)) //
					.toMaybe();

		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
	}

	private Maybe<Resource> getResource(GroovyScript script) {
		if (script == null)
			return Reasons.build(InvalidArgument.T).text("Missing script").toMaybe();

		Resource source = script.getSource();
		if (source == null)
			return Reasons.build(InvalidArgument.T).text("Missing source for script : " + script).toMaybe();
		return Maybe.complete(source);
	}

	// ############################################
	// ## . . . . . . CompiledScript . . . . . . ##
	// ############################################

	private static class GroovyCompiledScript implements CompiledScript {
		private final GroovyScript script;
		private final javax.script.CompiledScript compiledScript;

		public GroovyCompiledScript(GroovyScript script, javax.script.CompiledScript compiledScript) {
			this.script = script;
			this.compiledScript = compiledScript;
		}

		/**
		 * Evaluated groovy {@link CompiledScript} given parameter bindings in context. May fail with {@link ScriptRuntimeError} reason.
		 * 
		 * @param context
		 *            The parameter map passed on to the script.
		 * @return Maybe<T> Where the return type T depends on the script to be evaluated.
		 */

		@Override
		@SuppressWarnings("unchecked")
		public <T> Maybe<T> evaluate(Map<String, Object> context) {

			Bindings bindings = new SimpleBindings();
			bindings.putAll(context);

			try {
				return Maybe.complete((T) compiledScript.eval(bindings));
			} catch (ScriptException e) {
				Throwable cause = e.getCause();
				if (cause instanceof UnsatisfiedMaybeTunneling) {
					return ((UnsatisfiedMaybeTunneling) cause).getMaybe();
				}

				return Reasons.build(ScriptRuntimeError.T) //
						.text("Error while evaluating Groovy Script: " + script) //
						.cause(InternalError.from(e)) //
						.toMaybe();

			}
		}
	}
}