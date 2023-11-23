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
package com.braintribe.model.processing.accessory.test.cortex;

import static com.braintribe.model.processing.accessory.test.cortex.MaTestConstants.CUSTOM_MODEL_NAME;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.Collection;

import com.braintribe.model.access.collaboration.persistence.ModelsPersistenceInitializer;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessory.test.custom.model.CustomEntity;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * @author peter.gazdik
 */
public class MaInit_1B_CustomModel extends ModelsPersistenceInitializer {

	private final NewMetaModelGeneration mmg = new NewMetaModelGeneration(asList(GenericEntity.T.getModel()));

	public MaInit_1B_CustomModel() {
		setCheckIfModelsAreAlreadyThere(true);
	}

	@Override
	protected Collection<GmMetaModel> getModels() {
		return asList(customDataModel());
	}

	private GmMetaModel customDataModel() {
		return mmg.buildMetaModel(CUSTOM_MODEL_NAME, asList(CustomEntity.T));
	}

}
