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
package tribefire.extension.tutorial.initializer;

import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.tutorial.initializer.wire.TutorialInitializerWireModule;
import tribefire.extension.tutorial.initializer.wire.contract.TutorialInitializerContract;
import tribefire.extension.tutorial.initializer.wire.contract.TutorialInitializerMainContract;
import tribefire.extension.tutorial.initializer.wire.contract.TutorialInitializerModelsContract;
import tribefire.extension.tutorial.model.api.request.LetterCaseTransformRequest;

public class TutorialInitializer extends AbstractInitializer<TutorialInitializerMainContract> {


	@Override
	public WireTerminalModule<TutorialInitializerMainContract> getInitializerWireModule() {
		return TutorialInitializerWireModule.INSTANCE;
	}
	
	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<TutorialInitializerMainContract> initializerContext,
			TutorialInitializerMainContract initializerMainContract) {

		CoreInstancesContract coreInstances = initializerMainContract.coreInstances();
		TutorialInitializerModelsContract models = initializerMainContract.models();
		coreInstances.cortexServiceModel().getDependencies().add(models.configuredTutorialApiModel());
		addMetaDataToModels(initializerMainContract);
		
	}

	private void addMetaDataToModels(TutorialInitializerMainContract initializerMainContract) {
		TutorialInitializerModelsContract models = initializerMainContract.models();
		TutorialInitializerContract initializer = initializerMainContract.initializer();
		
		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(models.configuredTutorialApiModel());
		
		editor.onEntityType(LetterCaseTransformRequest.T).addMetaData(initializer.processWithLetterCaseTransform());
		
	}
}
