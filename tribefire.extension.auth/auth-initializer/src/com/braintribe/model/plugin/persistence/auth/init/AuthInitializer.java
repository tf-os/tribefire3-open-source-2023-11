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
package com.braintribe.model.plugin.persistence.auth.init;

import com.braintribe.gm.persistence.initializer.support.api.SimplifiedWireSupportedInitializer;
import com.braintribe.gm.persistence.initializer.support.api.WiredInitializerContext;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.plugin.persistence.auth.init.wire.AuthInitializerWireModule;
import com.braintribe.model.plugin.persistence.auth.init.wire.contract.AuthMainContract;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

public class AuthInitializer implements SimplifiedWireSupportedInitializer<AuthMainContract> {

	@Override
	public WireTerminalModule<AuthMainContract> getWireModule() {
		return AuthInitializerWireModule.INSTANCE;
	}

	@Override
	public void initializeModels(PersistenceInitializationContext context, WiredInitializerContext<AuthMainContract> initializerContext,
			AuthMainContract initializerContract) {

		//GmMetaModel cortexModel = initializerContract.coreInstances().cortexModel();

		//cortexModel.getDependencies().add(initializerContract.existingInstances().deploymentModel());

	}

	@Override
	public void initializeData(PersistenceInitializationContext context, WiredInitializerContext<AuthMainContract> initializerContext,
			AuthMainContract initializerContract) {

		initializerContract.metaData().metaData();

	}
}
