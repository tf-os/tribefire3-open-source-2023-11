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
package com.braintribe.model.manipulation.parser.impl.manipulator;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.manipulation.parser.impl.model.Joat;
import com.braintribe.model.manipulation.parser.impl.model.JoatSub;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlManipulatorParserConfiguration;

/**
 * Tests for lenient handling of errors detected by the GMML manipulator itself.
 * 
 * @see AbstractModifiedGmmlManipulatorTest
 * @see LenientEntityManagerErrorTest
 * 
 * @author peter.gazdik
 */
public class LenientManipulatorTest extends AbstractModifiedGmmlManipulatorTest {

	@Test
	public void createUnknownType() throws Exception {
		gmmlModifier = s -> s.replaceAll("Joat", "NonExistentJoat");

		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat1");
		});

		Set<GenericEntity> entities = smood.getEntitiesPerType(GenericEntity.T);
		assertThat(entities).isEmpty();
	}

	@Test
	public void createAndDeleteUnknownType() throws Exception {
		gmmlModifier = s -> s.replaceAll("Joat", "NonExistentJoat");

		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat1");
			session.deleteEntity(joat);
		});

		Set<GenericEntity> entities = smood.getEntitiesPerType(GenericEntity.T);
		assertThat(entities).isEmpty();
	}

	@Test
	public void setMissingOnPropertyIsNoOp() throws Exception {
		gmmlModifier = s -> s.replaceAll("JoatSub", "NonExistentJoat");

		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat");

			JoatSub missingJoat = session.create(JoatSub.T);
			missingJoat.setGlobalId("joatSub");

			joat.setEntityValue(joat);
			joat.setEntityValue(missingJoat);
		});

		Set<GenericEntity> entities = smood.getEntitiesPerType(GenericEntity.T);

		assertThat(entities).hasSize(1);
		Joat joat = first(entities);
		assertThat(joat.getGlobalId()).isEqualTo("joat");
		assertThat(joat.getEntityValue()).isSameAs(joat);
	}

	@Test
	public void addMissingToCollection() throws Exception {
		gmmlModifier = s -> s.replaceAll("JoatSub", "NonExistentJoat");

		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat");

			JoatSub missingJoat = session.create(JoatSub.T);
			missingJoat.setGlobalId("joatSub");

			joat.getObjectList().add("one");
			joat.getObjectList().add(missingJoat);
			joat.getObjectList().add("two");

			joat.getObjectSet().add("one");
			joat.getObjectSet().add(missingJoat);
			joat.getObjectSet().add("two");

			joat.getStringObjectMap().put("one", joat);
			joat.getStringObjectMap().put("one", missingJoat);
		});

		Set<GenericEntity> entities = smood.getEntitiesPerType(GenericEntity.T);

		assertThat(entities).hasSize(1);
		Joat joat = first(entities);
		assertThat(joat.getGlobalId()).isEqualTo("joat");

		/* Why null? ----> See GmmlManipulatorParserListener.addToList or putToMap. This replaces the value with null, thus we then treat it as if the
		 * null was the correct value. */
		// We'll probably change this to not put null into collection
		assertThat(joat.getObjectList()).containsExactly("one", null/* see comment above */, "two");
		assertThat(joat.getObjectSet()).containsOnly("one", "two");
		assertThat(joat.getStringObjectMap()).isEqualTo(asMap("one", joat));
	}

	@Test
	public void addElementToWrongPositionInList() throws Exception {
		gmmlModifier = s -> s.replace("1:", "5:");

		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat");

			joat.getStringList().add("one");
			joat.getStringList().add("two");
			joat.getStringList().add("three");
		});

		Set<GenericEntity> entities = smood.getEntitiesPerType(GenericEntity.T);

		assertThat(entities).hasSize(1);
		Joat joat = first(entities);
		assertThat(joat.getGlobalId()).isEqualTo("joat");
		assertThat(joat.getStringList()).containsExactly("one", "two", "three");
	}

	@Test
	public void changeOwnerType_PreservedPropertiesWork() throws Exception {
		gmmlModifier = s -> s.replaceAll("JoatSub", "Joat");

		recordStringifyAndApply(session -> {
			JoatSub joat = session.create(JoatSub.T);
			joat.setGlobalId("joatSub");
			joat.setExtraStringValue("someValue");
		});

		Set<GenericEntity> entities = smood.getEntitiesPerType(GenericEntity.T);

		assertThat(entities).hasSize(1);
		assertThat(first(entities).getGlobalId()).isEqualTo("joatSub");
	}

	@Test
	public void changePropertyType_Ignored() throws Exception {
		gmmlModifier = s -> s.replaceAll("'someValue'", "1L");

		recordStringifyAndApply(session -> {
			JoatSub joat = session.create(JoatSub.T);
			joat.setGlobalId("joatSub");
			joat.setExtraStringValue("someValue");
		});

		Set<GenericEntity> entities = smood.getEntitiesPerType(GenericEntity.T);

		assertThat(entities).hasSize(1);

		JoatSub e = first(entities);
		assertThat(e.getGlobalId()).isEqualTo("joatSub");
		assertThat(e.getExtraStringValue()).isNull();
	}

	@Test
	public void mapLiteralValidForSetProperty() throws Exception {
		gmmlModifier = s -> s.replaceAll("\\('one','two'\\)", "{'one':'one','two':'two'}");

		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat");
			joat.getStringSet().addAll(asSet("one", "two"));
		});

		Set<GenericEntity> entities = smood.getEntitiesPerType(GenericEntity.T);

		assertThat(entities).hasSize(1);
		Joat joat = first(entities);
		assertThat(joat.getGlobalId()).isEqualTo("joat");
		assertThat(joat.getStringSet()).containsOnly("one", "two");
	}

	@Test
	public void changeProblematicEntity() throws Exception {
		Joat problematicEntity = session.createRaw(Joat.T);
		problematicEntity.setGlobalId("entity.problematic");

		gmmlModifier = s -> s.replaceAll("\\(\\)", "('entity.problematic')");

		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T); // will be replaced with lookup for problematic entity
			joat.getStringSet().addAll(asSet("one", "two"));
		});

		assertThat(problematicEntity.getStringSet()).containsOnly("one", "two");
	}

	@Override
	protected MutableGmmlManipulatorParserConfiguration parserConfig() {
		MutableGmmlManipulatorParserConfiguration result = super.parserConfig();
		result.setProblematicEntitiesRegistry(new StaticProblematicEntitiesRegistry(asSet("entity.problematic")));
		return result;
	}

}
