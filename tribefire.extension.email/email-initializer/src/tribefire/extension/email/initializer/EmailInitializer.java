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
package tribefire.extension.email.initializer;

import java.util.Set;

import com.braintribe.gm.model.reason.essential.CommunicationError;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.email.service.EmailServiceRequest;
import com.braintribe.model.email.service.reason.ConfigurationMissing;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.email.initializer.wire.EmailInitializerWireModule;
import tribefire.extension.email.initializer.wire.contract.EmailInitializerContract;
import tribefire.extension.email.initializer.wire.contract.EmailInitializerMainContract;
import tribefire.extension.email.initializer.wire.contract.ExistingInstancesContract;

public class EmailInitializer extends AbstractInitializer<EmailInitializerMainContract> {

	@Override
	public WireTerminalModule<EmailInitializerMainContract> getInitializerWireModule() {
		return EmailInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<EmailInitializerMainContract> initializerContext,
			EmailInitializerMainContract initializerMainContract) {
		initializerMainContract.coreInstances().cortexModel().getDependencies().add(initializerMainContract.existingInstances().deploymentModel());
		initializerMainContract.coreInstances().cortexServiceModel().getDependencies()
				.add(initializerMainContract.existingInstances().serviceModel());

		EmailInitializerContract initializer = initializerMainContract.initializer();
		initializer.emailServiceProcessor();
		initializer.connectivityCheckBundle();
		initializer.apiServiceDomain();

		ExistingInstancesContract existingInstances = initializerMainContract.existingInstances();
		Set<DdraMapping> ddraMappings = existingInstances.ddraConfiguration().getMappings();
		ddraMappings.addAll(initializer.ddraMappings());

		configureMetaData(initializerMainContract);

	}

	private void configureMetaData(EmailInitializerMainContract initializerMainContract) {
		EmailInitializerContract initializer = initializerMainContract.initializer();

		BasicModelMetaDataEditor editor = new BasicModelMetaDataEditor(initializerMainContract.models().configuredEmailApiModel());
		editor.onEntityType(EmailServiceRequest.T).addMetaData(initializerMainContract.initializer().processWithEmailServiceProcessor());

		editor.onEntityType(CommunicationError.T).addMetaData(initializer.httpStatus502Md(), initializer.logReasonTrace());
		editor.onEntityType(NotFound.T).addMetaData(initializer.httpStatus404Md(), initializer.logReasonTrace());
		editor.onEntityType(InternalError.T).addMetaData(initializer.httpStatus500Md(), initializer.logReasonTrace());
		editor.onEntityType(ConfigurationMissing.T).addMetaData(initializer.httpStatus501Md(), initializer.logReasonTrace());
	}
}
