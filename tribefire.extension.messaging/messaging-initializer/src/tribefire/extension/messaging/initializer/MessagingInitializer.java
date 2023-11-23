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
package tribefire.extension.messaging.initializer;

import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.messaging.initializer.wire.MessagingInitializerWireModule;
import tribefire.extension.messaging.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.messaging.initializer.wire.contract.MessagingInitializerMainContract;

public class MessagingInitializer extends AbstractInitializer<MessagingInitializerMainContract> {

	private static final Logger logger = Logger.getLogger(MessagingInitializer.class);

	@Override
	public WireTerminalModule<MessagingInitializerMainContract> getInitializerWireModule() {
		return MessagingInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<MessagingInitializerMainContract> initializerContext,
			MessagingInitializerMainContract initializerMainContract) {

		@SuppressWarnings("unused")
		GmMetaModel cortexModel = initializerMainContract.coreInstances().cortexModel();

		ExistingInstancesContract existingInstances = initializerMainContract.existingInstances();

		@SuppressWarnings("unused")
		GmMetaModel deploymentModel = existingInstances.deploymentModel();
		GmMetaModel serviceModel = existingInstances.serviceModel();
		GmMetaModel cortexServiceModel = initializerMainContract.coreInstances().cortexServiceModel();

		initializerMainContract.initializer().healthCheckProcessor();
		initializerMainContract.initializer().functionalCheckBundle();

		if (initializerMainContract.properties().MESSAGING_CREATE_DEFAULT_SETUP()) {
			logger.info(() -> "Creating a default setup configuration");
			initializerMainContract.defaults().setupDefaultConfiguration();
			cortexServiceModel.getDependencies().add(existingInstances.defaultConfiguredServiceModel());
		} else {
			logger.debug(() -> "Not creating a default setup configuration");
		}

		// TODO INITIALIZER FOR TEST PURPOSE COMMENT OUT BEFORE COMMIT !!!
		// initializerMainContract.initializer().setupDefaultConfiguration();
		// WARNING: this line should be after setupDefaultConfiguration is done
		// cortexServiceModel.getDependencies().add(existingInstances.messagingProducerServiceModel());
		// cortexServiceModel.getDependencies().add(existingInstances.messagingConsumerServiceModel());
	}
}
