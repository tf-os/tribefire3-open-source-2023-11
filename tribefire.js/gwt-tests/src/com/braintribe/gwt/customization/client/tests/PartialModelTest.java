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
package com.braintribe.gwt.customization.client.tests;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import com.braintribe.gwt.customization.client.tests.model.initializer.InitializedEntity;
import com.braintribe.gwt.customization.client.tests.model.initializer.InitializedSubEntity;
import com.braintribe.gwt.customization.client.tests.model.partial.PartialEntity;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * Tests that a model can be woven even if all the dependencies / types from dependencies are shallow.
 * 
 * @author peter.gazdik
 */
public class PartialModelTest extends AbstractGwtTest {

	@Override
	protected void tryRun() throws GmfException {
		GmMetaModel metaModel = generateModel();

		makeSignaturesDynamic(metaModel);

		metaModel = shallowify(metaModel);
		ensureModelTypes(metaModel);

		EntityType<?> et = typeReflection.getEntityType(makeSignatureDynamic(PartialEntity.class.getName()));
		GenericEntity instance = et.create();

		if (instance == null) {
			logError("ITW FAILED");
		} else {
			log("Properly woven shallow entity type:" + et.getTypeSignature());
		}
	}

	private GmMetaModel shallowify(GmMetaModel metaModel) {
		metaModel.getDependencies().clear();

		return GmMetaModel.T.clone(new ShallowifyingCloningContext(), metaModel, null);
	}

	private static class ShallowifyingCloningContext extends StandardCloningContext {

		@Override
		public <T> T getAssociated(GenericEntity entity) {
			T associated = super.getAssociated(entity);
			if (associated != null || !(entity instanceof GmType))
				return associated;

			GmType gmType = (GmType) entity;

			if (gmType.getTypeSignature().contains("partial"))
				return null;

			GmType shallowCopy = (GmType) gmType.entityType().create();
			shallowCopy.setTypeSignature(gmType.getTypeSignature());

			registerAsVisited(entity, shallowCopy);

			return (T) shallowCopy;
		}

	}

	private GmMetaModel generateModel() {
		log("generating meta model");

		NewMetaModelGeneration mmg = new NewMetaModelGeneration();
		GmMetaModel initializerModel = mmg.buildMetaModel("test.gwt:initializer-model", asList(InitializedEntity.T, InitializedSubEntity.T));
		GmMetaModel partialModel = mmg.buildMetaModel("test.gwt:partial-model", asList(PartialEntity.T), asList(initializerModel));

		return partialModel;
	}

}
