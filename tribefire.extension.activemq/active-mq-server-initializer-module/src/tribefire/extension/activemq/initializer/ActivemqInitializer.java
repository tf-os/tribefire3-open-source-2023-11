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
package tribefire.extension.activemq.initializer;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.activemq.initializer.wire.ActivemqInitializerModuleWireModule;
import tribefire.extension.activemq.initializer.wire.contract.ActivemqInitializerModuleContract;
import tribefire.extension.activemq.initializer.wire.contract.ActivemqInitializerModuleMainContract;

public class ActivemqInitializer extends AbstractInitializer<ActivemqInitializerModuleMainContract> {

	@Override
	public WireTerminalModule<ActivemqInitializerModuleMainContract> getInitializerWireModule() {
		return ActivemqInitializerModuleWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context,
			WiredInitializerContext<ActivemqInitializerModuleMainContract> initializerContext,
			ActivemqInitializerModuleMainContract initializerMainContract) {

		GmMetaModel cortexModel = initializerMainContract.coreInstances().cortexModel();
		cortexModel.getDependencies().add(initializerMainContract.existingInstances().deploymentModel());

		ActivemqInitializerModuleContract initializer = initializerMainContract.initializer();

		if (initializerMainContract.properties().AMQ_DEPLOYMENT_START()) {
			initializer.worker();
		}
		initializer.functionalCheckBundle();
		initializer.healthCheckProcessor();
	}
}
