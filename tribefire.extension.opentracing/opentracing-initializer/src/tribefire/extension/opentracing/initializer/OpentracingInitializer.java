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
package tribefire.extension.opentracing.initializer;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.opentracing.initializer.wire.OpentracingInitializerWireModule;
import tribefire.extension.opentracing.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.opentracing.initializer.wire.contract.OpentracingInitializerMainContract;

public class OpentracingInitializer extends AbstractInitializer<OpentracingInitializerMainContract> {

	@Override
	public WireTerminalModule<OpentracingInitializerMainContract> getInitializerWireModule() {
		return OpentracingInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<OpentracingInitializerMainContract> initializerContext,
			OpentracingInitializerMainContract initializerMainContract) {

		// TODO: add service models to cortex

		GmMetaModel cortexModel = initializerMainContract.coreInstances().cortexModel();

		ExistingInstancesContract existingInstances = initializerMainContract.existingInstances();

		GmMetaModel deploymentModel = existingInstances.deploymentModel();
		GmMetaModel serviceModel = existingInstances.serviceModel();
		cortexModel.getDependencies().add(deploymentModel);
		cortexModel.getDependencies().add(serviceModel);

		initializerMainContract.initializer().setupDefaultConfiguration();
	}
}
