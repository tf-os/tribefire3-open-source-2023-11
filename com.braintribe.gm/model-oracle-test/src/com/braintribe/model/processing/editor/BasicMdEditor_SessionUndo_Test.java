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

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.oracle.model.ModelNames;
import com.braintribe.model.processing.oracle.model.ModelOracleModelProvider;
import com.braintribe.model.processing.oracle.model.basic.animal.Animal;
import com.braintribe.model.processing.oracle.model.basic.animal.Gender;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.testing.tools.gm.GmTestTools;

/**
 * @author peter.gazdik
 */
public class BasicMdEditor_SessionUndo_Test implements ModelNames {

	private PersistenceGmSession session;
	private ModelMetaDataEditor mammalEditor;

	@Before
	public void initialize() {
		session = GmTestTools.newSessionWithSmoodAccessMemoryOnly();
		mammalEditor = buildMammalModelEditor();
	}

	private BasicModelMetaDataEditor buildMammalModelEditor() {
		NewMetaModelGeneration mmg = new NewMetaModelGeneration(session::create).withValidation();
		GmMetaModel animalModel = mmg.buildMetaModel(ANIMAL_MODEL, ModelOracleModelProvider.animalTypes, asList(mmg.rootMetaModel()));
		GmMetaModel mammalModel = mmg.buildMetaModel(MAMMAL_MODEL, ModelOracleModelProvider.mammalTypes, asList(animalModel));

		session.commit();

		return BasicModelMetaDataEditor.create(mammalModel).withSession(session).done();
	}

	@Test
	public void canUndoEntityMd() throws Exception {
		testUndoIsFine(this::addEntityMd);
	}

	private void addEntityMd() {
		mammalEditor.onEntityType(Animal.T).addMetaData(visible());
	}

	@Test
	public void canUndoPropertyMd() throws Exception {
		testUndoIsFine(this::addPropertyMd);
	}

	private void addPropertyMd() {
		mammalEditor.onEntityType(Animal.T).addPropertyMetaData(GENDER, visible());
	}

	@Test
	public void canUndoEnumMd() throws Exception {
		testUndoIsFine(this::addEnumMd);
	}

	private void addEnumMd() {
		mammalEditor.onEnumType(Gender.class).addMetaData(visible());
	}

	@Test
	public void canUndoConstantMd() throws Exception {
		testUndoIsFine(this::addConstantMd);
	}

	private void testUndoIsFine(Runnable mdAdding) {
		NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
		mdAdding.run();
		nestedTransaction.rollback();

		mdAdding.run();
		session.commit();
	}

	private void addConstantMd() {
		mammalEditor.addConstantMetaData(Gender.M, visible());
	}

	private Visible visible() {
		return session.create(Visible.T);
	}

}
