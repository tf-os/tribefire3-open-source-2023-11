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
package tribefire.extension.hydrux.model.deployment.prototyping;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.hydrux.model.deployment.HxApplication;
import tribefire.extension.hydrux.model.deployment.HxView;

/**
 * The main {@link HxView view} of a prototype application.
 * <p>
 * When a prototype application resolves its {@link HxApplication}, the Hydrux servlet always returns an application whose
 * {@link HxApplication#getView() view} is an {@link HxMainView}.
 * <p>
 * This view is not actually configured on the HxApplication, but is created dynamically, because it's {@link HxView#getModule()} needs to be created
 * dynamically, based on URL parameters, because that's the whole point of hydrux-prototyping - no need to configure anything on the server side.
 * <p>
 * The client prototype application then simply binds this view:
 * 
 * <pre>
 * export const contract: hx.IHxModuleContract = {
 * 	    bind(context: hx.IHxModuleBindingContext): void {
 * 			context.componentBinder().bindView(HxMainView, mainView); 
 * 		}
 * }

 * async function mainView(denotation: HxMainView, context: hx.IHxComponentCreationContext): Promise<hx.IHxView> {
 *    ...
 * }
 * </pre>
 * 
 * @see HxView
 */
public interface HxMainView extends HxView {

	EntityType<HxMainView> T = EntityTypes.T(HxMainView.class);

	String PROTOTYPING_DOMAIN_ID = "hydrux-prototyping";
	
}
