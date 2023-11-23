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

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.manipulation.parser.impl.model.Joat;
import com.braintribe.model.manipulation.parser.impl.model.SomeEnum;
import com.braintribe.model.manipulation.parser.impl.model.keyword.offset.Keyworder;

/**
 * @author peter.gazdik
 */
public class ManipulatorTest extends AbstractManipulatorTest {

	@Test
	public void create() throws Exception {
		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat1");
		});

		Set<GenericEntity> entities = smood.getEntitiesPerType(GenericEntity.T);

		assertThat(entities).hasSize(1);
		assertThat(first(entities).getGlobalId()).isEqualTo("joat1");
	}

	@Test
	public void createAndDelete() throws Exception {
		recordStringifyAndApply(session -> {
			Joat delJoat = session.create(Joat.T);
			delJoat.setGlobalId("delJoat");
			session.deleteEntity(delJoat);
		});

		Set<GenericEntity> entities = smood.getEntitiesPerType(GenericEntity.T);

		assertThat(entities).isEmpty();
	}

	@Test
	public void createAndDeleteAndDoMore() throws Exception {
		recordStringifyAndApply(session -> {
			Joat delJoat = session.create(Joat.T, "delJoat");
			Joat regularJoat = session.create(Joat.T, "regularJoat");
			session.deleteEntity(delJoat);
			regularJoat.setStringValue("hello");
		});

		Set<GenericEntity> entities = smood.getEntitiesPerType(GenericEntity.T);

		assertThat(entities).hasSize(1);

		Joat joat = first(entities);
		assertThat(joat.getStringValue()).isEqualTo("hello");
	}

	@Test
	public void simpleProperties() throws Exception {
		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat1");

			joat.setEnumValue(SomeEnum.foo);

			joat.setStringList(asList("oneL", "twoL"));
			joat.getStringList().clear();
			joat.setStringList(asList("threeL", "fourL"));

			joat.setStringSet(asSet("oneS", "twoS"));
			joat.getStringSet().clear();
			joat.setStringSet(asSet("threeS", "fourS"));

			joat.setStringObjectMap(asMap("one", 1, "two", 2));
			joat.getStringObjectMap().clear();
			joat.setStringObjectMap(asMap("three", 3, "four", 4));
			joat.getStringObjectMap().putAll(asMap("three", 3, "four", 4));
		});

		Set<Joat> entities = smood.getEntitiesPerType(Joat.T);
		assertThat(entities).hasSize(1);

		Joat joat = first(entities);

		assertThat(joat.getEnumValue()).isSameAs(SomeEnum.foo);

		assertThat(joat.getStringList()).containsExactly("threeL", "fourL");
		assertThat(joat.getStringSet()).containsOnly("threeS", "fourS");
		assertThat(joat.getStringObjectMap()).isEqualTo(asMap("three", 3, "four", 4));
	}

	@Test
	public void collectionAdds() throws Exception {
		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat1");

			joat.getStringList().addAll(asList("oneL", "twoL"));
			joat.getStringSet().addAll(asSet("oneS", "twoS"));
			joat.getStringObjectMap().putAll(asMap("one", 1, "two", 2));
		});

		Set<Joat> entities = smood.getEntitiesPerType(Joat.T);
		assertThat(entities).hasSize(1);

		Joat joat = first(entities);

		assertThat(joat.getStringList()).containsExactly("oneL", "twoL");
		assertThat(joat.getStringSet()).containsOnly("oneS", "twoS");
		assertThat(joat.getStringObjectMap()).isEqualTo(asMap("one", 1, "two", 2));
	}

	@Test
	public void collectionRemoves() throws Exception {
		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat1");

			joat.getStringList().addAll(asList("oneL", "twoL", "threeL", "fourL"));
			joat.getStringList().removeAll(asList("threeL", "fourL"));

			joat.getStringSet().addAll(asSet("oneS", "twoS", "threeS", "fourS"));
			joat.getStringSet().removeAll(asSet("threeS", "fourS"));

			joat.getStringObjectMap().putAll(asMap("one", 1, "two", 2, "three", 3));
			joat.getStringObjectMap().remove("three");
		});

		Set<Joat> entities = smood.getEntitiesPerType(Joat.T);
		assertThat(entities).hasSize(1);

		Joat joat = first(entities);

		assertThat(joat.getStringList()).containsExactly("oneL", "twoL");
		assertThat(joat.getStringSet()).containsOnly("oneS", "twoS");
		assertThat(joat.getStringObjectMap()).isEqualTo(asMap("one", 1, "two", 2));
	}

	@Test
	public void nullProperty() throws Exception {
		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat1");

			joat.setEnumValue(null);
		});

		Set<Joat> entities = smood.getEntitiesPerType(Joat.T);
		assertThat(entities).hasSize(1);

		Joat joat = first(entities);

		assertThat(joat.getEnumValue()).isNull();
	}

	/** DEVCX-627:There was a bug where the '\' was not un-escaped: */
	@Test
	public void regexString() throws Exception {
		final String REGEX = "A\t B\\t C\u0001 D\n E\r F\'";

		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat1");

			joat.setStringValue(REGEX);
		});

		Set<Joat> entities = smood.getEntitiesPerType(Joat.T);
		assertThat(entities).hasSize(1);

		Joat joat = first(entities);
		assertThat(joat.getStringValue()).isEqualTo(REGEX);
	}

	@Test
	public void entityProperties() throws Exception {
		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat1");

			Joat other = session.create(Joat.T);
			other.setGlobalId("other");

			joat.setEntityValue(other);
			joat.getObjectList().addAll(asList("first", other));
		});

		Set<Joat> entities = smood.getEntitiesPerType(Joat.T);
		assertThat(entities).hasSize(2);

		Joat joat = smood.findEntityByGlobalId("joat1");

		assertThat(joat.getEntityValue().getGlobalId()).isEqualTo("other");

		List<Object> list = joat.getObjectList();
		assertThat(list.get(0)).isEqualTo("first");
		assertThat(((Joat) list.get(1)).getGlobalId()).isEqualTo("other");
	}

	@Test
	public void propertyValueIsNaN() throws Exception {
		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat1");
			joat.setFloatValue(Float.NaN);
			joat.setDoubleValue(Double.NaN);
		});

		Set<Joat> entities = smood.getEntitiesPerType(Joat.T);
		assertThat(entities).hasSize(1);

		Joat joat = smood.findEntityByGlobalId("joat1");

		assertThat(joat.getFloatValue()).isNaN();
		assertThat(joat.getDoubleValue()).isNaN();
	}

	@Test
	public void propertyValueIsPositiveInfinity() throws Exception {
		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat1");
			joat.setFloatValue(Float.POSITIVE_INFINITY);
			joat.setDoubleValue(Double.POSITIVE_INFINITY);
		});

		Set<Joat> entities = smood.getEntitiesPerType(Joat.T);
		assertThat(entities).hasSize(1);

		Joat joat = smood.findEntityByGlobalId("joat1");

		assertThat(joat.getFloatValue()).isPositive().isInfinite();
		assertThat(joat.getDoubleValue()).isPositive().isInfinite();
	}

	@Test
	public void propertyValueIsNegativeInfinity() throws Exception {
		recordStringifyAndApply(session -> {
			Joat joat = session.create(Joat.T);
			joat.setGlobalId("joat1");
			joat.setFloatValue(Float.NEGATIVE_INFINITY);
			joat.setDoubleValue(Double.NEGATIVE_INFINITY);
		});

		Set<Joat> entities = smood.getEntitiesPerType(Joat.T);
		assertThat(entities).hasSize(1);

		Joat joat = smood.findEntityByGlobalId("joat1");

		assertThat(joat.getFloatValue()).isNegative().isInfinite();
		assertThat(joat.getDoubleValue()).isNegative().isInfinite();
	}

	@Test
	public void keywordInPackage() throws Exception {
		recordStringifyAndApply(session -> {
			Keyworder keyworder = session.create(Keyworder.T);
			keyworder.setPartition("Partition");
		});

		Set<Keyworder> entities = smood.getEntitiesPerType(Keyworder.T);
		assertThat(entities).hasSize(1);

		Keyworder keyworder = first(entities);

		assertThat(keyworder.getPartition()).isEqualTo("Partition");
	}

	@Test
	public void keywordProperty() throws Exception {
		recordStringifyAndApply(session -> {
			Keyworder keyworder = session.create(Keyworder.T);
			keyworder.setOffset(10);
		});

		Set<Keyworder> entities = smood.getEntitiesPerType(Keyworder.T);
		assertThat(entities).hasSize(1);

		Keyworder keyworder = first(entities);

		assertThat(keyworder.getOffset()).isEqualTo(10);
	}

}
