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
package tribefire.extension.hydrux.demo.initializer;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.cortex.initializer.support.impl.CortexInitializerTools;
import tribefire.extension.hydrux.demo.initializer.wire.HxDemoInitializerWireModule;
import tribefire.extension.hydrux.demo.initializer.wire.contract.HxDemoInitializerContract;
import tribefire.extension.hydrux.demo.model.ux.deployment.HxDemoStaticPageView;

/**
 * We have to:
 * <ul>
 * <li>Add hx-demo-hx-deployment-model to the cortex-model.
 * <li>Configure the HX demo Access via {@link HxDemoInitializerContract#access()}.
 * </ul>
 */
public class HxDemoInitializer extends AbstractInitializer<HxDemoInitializerContract> {

	@Override
	public WireTerminalModule<HxDemoInitializerContract> getInitializerWireModule() {
		return HxDemoInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize( //
			PersistenceInitializationContext context, //
			WiredInitializerContext<HxDemoInitializerContract> initializerContext, //
			HxDemoInitializerContract demoInitializer) {

		CortexInitializerTools.addToCortexModel(context.getSession(), HxDemoStaticPageView.T.getModel());

		demoInitializer.access();
	}

}
