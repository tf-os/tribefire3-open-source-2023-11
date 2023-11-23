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
package tribefire.extension.simple.initializer;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.simple.initializer.wire.SimpleInitializerWireModule;
import tribefire.extension.simple.initializer.wire.contract.SimpleInitializerContract;
import tribefire.extension.simple.initializer.wire.contract.SimpleInitializerMainContract;
import tribefire.extension.simple.initializer.wire.contract.SimpleInitializerModelsContract;
import tribefire.extension.simple.model.data.Department;
import tribefire.extension.simple.model.data.Person;
import tribefire.extension.simple.model.service.SimpleEchoRequest;

public class SimpleInitializer extends AbstractInitializer<SimpleInitializerMainContract> {

	@Override
	public WireTerminalModule<SimpleInitializerMainContract> getInitializerWireModule() {
		return SimpleInitializerWireModule.INSTANCE;
	}
	
	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<SimpleInitializerMainContract> initializerContext,
			SimpleInitializerMainContract initializerMainContract) {

		SimpleInitializerModelsContract models = initializerMainContract.models();
		CoreInstancesContract coreInstances = initializerMainContract.coreInstances();
		
		GmMetaModel cortexModel = coreInstances.cortexModel();
		GmMetaModel cortexServiceModel = coreInstances.cortexServiceModel();
		
		cortexModel.getDependencies().add(models.configuredSimpleDeploymentModel());
		cortexServiceModel.getDependencies().add(models.configuredSimpleServiceModel());
		
		SimpleInitializerContract initializer = initializerMainContract.initializer();
		
		initializer.simpleInMemoryAccess();
		initializer.simpleEchoProcessor();
		initializer.simpleWebTerminal();		
		
		addMetadataToModels(initializerMainContract);
		
	}
	
	private void addMetadataToModels(SimpleInitializerMainContract initializerMainContract) {
		SimpleInitializerContract initializer = initializerMainContract.initializer();
		SimpleInitializerModelsContract models = initializerMainContract.models();
				
		// set some metadata, e.g. hide a property or make an entity type not instantiable
		// (you can e.g. verify this via the tribefire Explorer)
		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(models.configuredSimpleDataModel());
		editor.onEntityType(Person.T).addPropertyMetaData(Person.gender, initializer.hidden());
		editor.onEntityType(Department.T).addMetaData(initializer.nonInstantiable());

		editor = new BasicModelMetaDataEditor(models.configuredSimpleServiceModel());		
		editor.onEntityType(SimpleEchoRequest.T).addMetaData(initializer.processWithSimpleEchoProcessor());		
	}
}
