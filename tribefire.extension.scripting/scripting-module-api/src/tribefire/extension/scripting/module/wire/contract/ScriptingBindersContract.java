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
package tribefire.extension.scripting.module.wire.contract;

import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.scripting.model.deployment.ScriptingEngine;

/**
 * Scripting module contract for connecting a {@link ScriptingEngine} denotation type to a an 
 * actual (API) expert {@link tribefire.extension.scripting.api.ScriptingEngine}. 
 * 
 * @author Dirk Scheffler
 *
 */
public interface ScriptingBindersContract extends WireSpace {

	ComponentBinder<ScriptingEngine, tribefire.extension.scripting.api.ScriptingEngine<?>> scriptingEngine();
}
