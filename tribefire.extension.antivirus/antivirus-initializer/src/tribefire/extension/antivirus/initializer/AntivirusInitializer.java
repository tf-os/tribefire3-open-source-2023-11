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
package tribefire.extension.antivirus.initializer;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.antivirus.initializer.wire.AntivirusInitializerWireModule;
import tribefire.extension.antivirus.initializer.wire.contract.AntivirusInitializerMainContract;
import tribefire.extension.antivirus.initializer.wire.contract.DefaultAntivirusProvider;
import tribefire.extension.antivirus.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.antivirus.initializer.wire.contract.RuntimePropertiesContract;

public class AntivirusInitializer extends AbstractInitializer<AntivirusInitializerMainContract> {

	@Override
	public WireTerminalModule<AntivirusInitializerMainContract> getInitializerWireModule() {
		return AntivirusInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<AntivirusInitializerMainContract> initializerContext,
			AntivirusInitializerMainContract initializerMainContract) {

		@SuppressWarnings("unused")
		GmMetaModel cortexModel = initializerMainContract.coreInstances().cortexModel();

		ExistingInstancesContract existingInstances = initializerMainContract.existingInstances();

		@SuppressWarnings("unused")
		GmMetaModel deploymentModel = existingInstances.deploymentModel();
		GmMetaModel serviceModel = existingInstances.serviceModel();
		GmMetaModel cortexServiceModel = initializerMainContract.coreInstances().cortexServiceModel();
		cortexServiceModel.getDependencies().add(serviceModel);

		initializerMainContract.initializer().healthCheckProcessor();
		initializerMainContract.initializer().functionalCheckBundle();

		RuntimePropertiesContract properties = initializerMainContract.properties();
		DefaultAntivirusProvider defaultProvider = properties.ANTIVIRUS_DEFAULT_PROVIDER();

		// only initialize in case of default config
		if (defaultProvider != null) {
			initializerMainContract.initializer().setupDefaultConfiguration(defaultProvider);
			cortexServiceModel.getDependencies().add(existingInstances.defaultServiceModel());
		}
	}
}
