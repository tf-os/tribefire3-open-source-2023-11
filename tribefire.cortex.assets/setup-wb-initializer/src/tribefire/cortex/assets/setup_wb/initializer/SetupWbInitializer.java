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
package tribefire.cortex.assets.setup_wb.initializer;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;

import tribefire.cortex.assets.setup_wb.initializer.wire.SetupWbInitializerWireModule;
import tribefire.cortex.assets.setup_wb.initializer.wire.contract.SetupWbInitializerContract;

public class SetupWbInitializer extends AbstractInitializer<SetupWbInitializerContract> {

	@Override
	public WireTerminalModule<SetupWbInitializerContract> getInitializerWireModule() {
		return SetupWbInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<SetupWbInitializerContract> initializerContext,
			SetupWbInitializerContract initializerContract) {

		initializerContract.initialize();

	}

}