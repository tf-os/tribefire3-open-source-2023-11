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
package tribefire.extension.js.model.asset.natures;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A {@link JsLibrary} which also fulfills the tribefire UX module contract. This means that the <code>js.zip</code> part of this asset contains an
 * <code>index.js</code> file which can be loaded by the UX framework (e.g. Hydrux).
 * <p>
 * Additionally, every JsUxModule is reflected in cortex with a UxMoule instance (<code>tribefire.extension.js:js-deployment-model</code>).
 */
public interface JsUxModule extends JsLibrary {

	EntityType<JsUxModule> T = EntityTypes.T(JsUxModule.class);

}
