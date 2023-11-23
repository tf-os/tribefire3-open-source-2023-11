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
package tribefire.extension.hydrux.prototyping_initializer;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.cortex.initializer.support.impl.CortexInitializerTools;
import tribefire.extension.hydrux.model.deployment.prototyping.HxMainView;
import tribefire.extension.hydrux.prototyping_initializer.wire.HydruxPrototypingInitializerWireModule;
import tribefire.extension.hydrux.prototyping_initializer.wire.contract.HydruxPrototypingInitializerContract;

/**
 * We have to:
 * <ul>
 * <li>Add hydrux-prototyping-deployment-model to the cortex-model.
 * <li>Configure the HX prototyping access HxDemoAcceess via {@link HydruxPrototypingInitializerContract#access()}.
 * </ul>
 */
public class HydruxPrototypingInitializer extends AbstractInitializer<HydruxPrototypingInitializerContract> {

	@Override
	public WireTerminalModule<HydruxPrototypingInitializerContract> getInitializerWireModule() {
		return HydruxPrototypingInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<HydruxPrototypingInitializerContract> initializerContext,
			HydruxPrototypingInitializerContract hydruxPrototypingInitializer) {

		CortexInitializerTools.addToCortexModel(context.getSession(), HxMainView.T.getModel());

		hydruxPrototypingInitializer.access();
	}
}
