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
package tribefire.extension.hydrux.model.deployment;

import java.util.List;

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.ModelMetaData;

import tribefire.extension.js.model.deployment.UxModule;

/**
 * This model meta-data specifies which Hydrux-based client application should be used to view it.
 * <p>
 * <h2>What is Hydrux (Hx)</h2>
 * 
 * Hydrux is a TypeScript based framework for writing modular client (UX) applications based on tribefire.js.
 * <p>
 * Hydrux client is configured by meta-data on an access data-model or a service domain. The top-level application is defined by
 * {@link HxApplication}, and components for handling individual entity types are defined using {@link HxViewWith}.
 * 
 * @see HxComponent
 * 
 * @author peter.gazdik
 */
public interface HxApplication extends ModelMetaData {

	EntityType<HxApplication> T = EntityTypes.T(HxApplication.class);

	/** Value that is set as the HTML document title. */
	String getTitle();
	void setTitle(String title);

	/**
	 * Technical identifier of your Hydrux application that is sent to the server. This plays a role e.g. when the server is pushing notifications to
	 * the registered clients, where the recipients may be filtered by instanceId.
	 */
	String getApplicationId();
	void setApplicationId(String clientId);

	/** Main view of the application. The corresponding HTMLElement is set as the document body's single child element by the Hydrux platform. */
	HxView getView();
	void setView(HxView view);

	/**
	 * The top-level scope that is always on the scope stack.
	 * <p>
	 * If none of your {@link HxComponent components} has it's {@link HxComponent#getScope() scope} set, this will be the only scope in your
	 * application, covering all components.
	 * <p>
	 * The rootScope always exists in runtime. We make this property mandatory so that it also always has a denotation instance. We cannot use
	 * <tt>null</tt> to denote rootScope, as {@link HxComponent#getScope() component's scope} being <tt>null</tt> has a special meaning (the scope of
	 * such component X is inherited from the scope of the component that is resolving X).
	 * <p>
	 * <b>If you don't know nor wish to know anything about scopes</b>, you probably don't need them. Just create an {@link HxScope} instance, set
	 * it's name to say "root", assign it here and forget about it.
	 * 
	 * @see HxComponent#getScope()
	 */
	@Mandatory
	HxScope getRootScope();
	void setRootScope(HxScope rootScope);

	/**
	 * List of modules to be loaded automatically on application load. This is relevant for avoiding delays once the app is running, as well as
	 * loading modules which would otherwise not be reachable (were there are no HxComponets referencing them). Modules that simply bind Dialog
	 * Processors are prime candidates.
	 * 
	 * @see HxRequestDialog
	 */
	List<UxModule> getPrefetchModules();
	void setPrefetchModules(List<UxModule> prefetchModules);

}
