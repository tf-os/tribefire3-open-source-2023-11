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
package com.braintribe.model.csa;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * {@link SmoodInitializer} which represents a script file based persistence initializer.
 * <p>
 * The implementation loads and runs given script files. The actual files are always called the same, for groovy scripts
 * (the first ones to support) the names are "model.groovy" and "data.groovy" and are found in a sub-folder with the
 * name of this initializer.
 * 
 * @author peter.gazdik
 */
public interface ScriptInitializer extends SmoodInitializer {

	EntityType<ScriptInitializer> T = EntityTypes.T(ScriptInitializer.class);

}