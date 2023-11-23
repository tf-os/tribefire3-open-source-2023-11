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
package com.braintribe.model.processing.editor;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import org.junit.Test;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.oracle.model.ModelNames;
import com.braintribe.model.processing.oracle.model.ModelOracleModelProvider;
import com.braintribe.model.processing.oracle.model.basic.fish.Fish;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * @author peter.gazdik
 */
public class BasicMdEditor_Lenient_Test implements ModelNames {

	private static final Visible VISIBLE_MD = Visible.T.create();

	private final ModelMetaDataEditor mammalEditor = newLenientMammalEditor();

	private ModelMetaDataEditor newLenientMammalEditor() {
		NewMetaModelGeneration mmg = new NewMetaModelGeneration().withValidation();
		GmMetaModel animalModel = mmg.buildMetaModel(ANIMAL_MODEL, ModelOracleModelProvider.animalTypes, asList(mmg.rootMetaModel()));
		GmMetaModel mammalModel = mmg.buildMetaModel(MAMMAL_MODEL, ModelOracleModelProvider.mammalTypes, asList(animalModel));

		return BasicModelMetaDataEditor.create(mammalModel).typeLenient(true).done();
	}

	@Test
	public void mdOnUnknownTypIsOk() {
		mammalEditor.onEntityType(Fish.T).addMetaData(VISIBLE_MD);
		mammalEditor.onEnumType("bat.virus.corona.UnknownEnumType").addMetaData(VISIBLE_MD);
	}

}
