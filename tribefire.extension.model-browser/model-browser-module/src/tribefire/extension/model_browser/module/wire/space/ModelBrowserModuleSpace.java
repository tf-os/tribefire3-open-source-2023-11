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
package tribefire.extension.model_browser.module.wire.space;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.model_browser.module.initializer.ModelBrowserInitializer;
import tribefire.extension.modelbrowser.servelet.ModelBrowser;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.wire.contract.ModuleResourcesContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * The ModelBrowser module creates the ModelBrowser denotation types as well as the corresponding expert.
 * 
 * @author Dirk Scheffler
 */
@Managed
public class ModelBrowserModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ModuleResourcesContract moduleResources;

	//
	// Initializers
	//

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		bindings.bind(modelBrowserInitializer());
	}

	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(tribefire.extension.modelbrowser.model.deployment.ModelBrowser.T).component(tfPlatform.binders().webTerminal())
				.expertFactory(this::modelBrowser);
	}

	//
	// Experts
	//

	@Managed
	private ModelBrowserInitializer modelBrowserInitializer() {
		return new ModelBrowserInitializer();
	}

	@Managed
	private ModelBrowser modelBrowser(ExpertContext<tribefire.extension.modelbrowser.model.deployment.ModelBrowser> context) {
		tribefire.extension.modelbrowser.model.deployment.ModelBrowser deployable = context.getDeployable();
		ModelBrowser bean = new ModelBrowser();
		bean.setCortexSessionProvider(tfPlatform.systemUserRelated().cortexSessionSupplier());
		bean.setResourceBaseUrl(moduleResources.resource(".").asUrl());
		bean.setModelAccessoryFactory(tfPlatform.systemUserRelated().modelAccessoryFactory());
		bean.setRequiredRoles(deployable.getRequiredRoles());
		bean.setUserSessionStack(tfPlatform.requestUserRelated().userSessionStack());
		return bean;
	}
}
