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
package tribefire.extension.model_browser.module.initializer;

import com.braintribe.model.processing.session.api.collaboration.DataInitializer;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

import tribefire.extension.modelbrowser.model.deployment.ModelBrowser;

/**
 * Creation of a ModelBrowser. 
 * 
 * @author Dirk Scheffler
 *
 */

public class ModelBrowserInitializer implements DataInitializer {
	@Override
	public void initialize(PersistenceInitializationContext context) {
		ManagedGmSession session = context.getSession();
		
		ModelBrowser modelBrowser = session.create(ModelBrowser.T);
		modelBrowser.setExternalId("web-terminal.model-browser");
		modelBrowser.setGlobalId("199234f7-f4d3-477e-bacb-85e82a6dc8c7");
		modelBrowser.setPathIdentifier("model-browser");
		modelBrowser.setName("Model Browser");
	}
}
