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
package tribefire.extension.graphux.graph_ux_initializer;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.graphux.graph_ux_initializer.wire.GraphUxInitializerWireModule;
import tribefire.extension.graphux.graph_ux_initializer.wire.contract.GraphUxInitializerContract;
import tribefire.extension.graphux.graph_ux_initializer.wire.contract.GraphUxInitializerMainContract;
import tribefire.extension.graphux.graph_ux_initializer.wire.contract.GraphUxInitializerModelsContract;
import tribefire.extension.graphux.model.service.GraphUxServiceRequest;


public class GraphUxInitializer extends AbstractInitializer<GraphUxInitializerMainContract> {

	@Override
	public WireTerminalModule<GraphUxInitializerMainContract> getInitializerWireModule() {
		return GraphUxInitializerWireModule.INSTANCE;
	}
	
	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<GraphUxInitializerMainContract> initializerContext,
			GraphUxInitializerMainContract initializerMainContract) {

		GraphUxInitializerModelsContract models = initializerMainContract.models();
		CoreInstancesContract coreInstances = initializerMainContract.coreInstances();
		
		GmMetaModel cortexModel = coreInstances.cortexModel();
		GmMetaModel cortexServiceModel = coreInstances.cortexServiceModel();
		
		cortexModel.getDependencies().add(models.configuredGraphUxDeploymentModel());

		cortexServiceModel.getDependencies().add(models.configuredGraphUxServiceModel());
		
		GraphUxInitializerContract initializer = initializerMainContract.initializer();
		
		initializer.graphUxProcessor();	
		
		BasicModelMetaDataEditor editor = new BasicModelMetaDataEditor(models.configuredGraphUxServiceModel());		
		editor.onEntityType(GraphUxServiceRequest.T).addMetaData(initializer.processWithGraphUxProcessor());
		
		GmMetaModel simpleModels = initializerMainContract.existingInstances().simpleServiceModel();
		
		simpleModels.getDependencies().add(models.configuredGraphUxServiceModel());
		simpleModels.getDependencies().add(initializerMainContract.simpleExistingInstances().simpleDataModel());
		
		// Fix ID type problem - remove after P.G. fix it :)
		TypeSpecification md = TypeSpecification.T.create();
		md.setType(context.getSession().query().findEntity("type:string"));
		md.setConflictPriority(1d);
		md.setImportant(true);
		
		BasicModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(initializerMainContract.existingInstances().simpleDataModel());
		mdEditor.onEntityType(GenericEntity.T).addPropertyMetaData("id", md);
		
	}
}
