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
package com.braintribe.model.processing.query.tools;

import static com.braintribe.utils.lcd.CollectionTools.getList;
import static com.braintribe.utils.lcd.CollectionTools.getSet;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.query.support.QueryAdaptingTools;
import com.braintribe.model.processing.query.test.builder.DataBuilder;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.MetaModelProvider;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.utils.junit.assertions.BtAssertions;
import com.braintribe.utils.lcd.MapTools;

/**
 * This was copied from SmoodTests (EntityOrPropertyQueryTests), just slightly adjusted so that the evaluation does not
 * access smood directly, but uses {@link QueryAdaptingTools}.
 */
public class QueryAdaptingToolsTests {

	private List<GenericEntity> entities;

	protected static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected DataBuilder b;
	protected Smood smood;

	@Before
	public void setup() {
		smood = new Smood(EmptyReadWriteLock.INSTANCE);
		smood.setMetaModel(MetaModelProvider.provideEnrichedModel());

		b = new DataBuilder(smood);
	}

	@Test
	public void queryEntities() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		EntityQuery query = EntityQueryBuilder.from(Person.T).done();
		queryEntities(query);

		BtAssertions.assertThat(entities).containsOnly(p1, p2);
	}

	@SuppressWarnings("unused")
	@Test
	public void queryEntitiesWithCondition() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		EntityQuery query = EntityQueryBuilder.from(Person.T).where().property("name").eq("Jack").done();
		queryEntities(query);

		BtAssertions.assertThat(entities).containsOnly(p1);
	}

	// ######################################
	// ## . . . . Simple property . . . . .##
	// ######################################

	@Test
	public void querySimpleProperty() throws Exception {
		Company c = b.company("c").create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "name").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat(property).isEqualTo("c");
	}

	@Test
	public void querySimpleProperty_NotPolymorphic() throws Exception {
		Person p = b.owner("Jack").name("p").create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Person.T, p.getId(), "name").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat(property).isEqualTo(null);
	}

	// ######################################
	// ## . . . . . List property . . . . .##
	// ######################################

	@Test
	public void queryListProperty_Simple() throws Exception {
		Company c = b.company("c").personNames("p1", "p2").create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personNameList").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat(property).isEqualTo(getList("p1", "p2"));
	}

	@Test
	public void queryListProperty_SimpleEmpty() throws Exception {
		Company c = b.company("c").personNames().create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personNameList").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat(property).isEqualTo(getList());
	}

	@Test
	public void queryListProperty_SimpleNull() throws Exception {
		Company c = b.company("c").create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personNameList").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat((List<?>) property).isEmpty();
	}

	@Test
	public void queryListProperty_Entities() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		Company c = b.company("c").persons(p1, p2).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "persons").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat(property).isEqualTo(getList(p1, p2));
	}

	// ######################################
	// ## . . . . . Set property . . . . . ##
	// ######################################

	@Test
	public void querySetProperty_Simple() throws Exception {
		Company c = b.company("c").personNames("p1", "p2").create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personNameSet").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat(property).isEqualTo(getSet("p1", "p2"));
	}

	@Test
	public void querySetProperty_SimpleEmpty() throws Exception {
		Company c = b.company("c").personNames().create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personNameSet").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat(property).isEqualTo(getSet());
	}

	@Test
	public void querySetProperty_SimpleNull() throws Exception {
		Company c = b.company("c").create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personNameSet").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat((Set<?>) property).isEmpty();
	}

	@Test
	public void querySetProperty_Entities() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		Company c = b.company("c").persons(p1, p2).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personSet").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat(property).isEqualTo(getSet(p1, p2));
	}

	// ######################################
	// ## . . . . . Map property . . . . . ##
	// ######################################

	@Test
	public void queryMapProperty_Simple() throws Exception {
		Person p = b.owner("Jack").addToCompanyTypeMap("c1", "llc").addToCompanyTypeMap("c2", "non-profit").create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Owner.T, p.getId(), "companyTypeMap").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat(property).isEqualTo(MapTools.getMap("c1", "llc", "c2", "non-profit"));
	}

	@Test
	public void queryMapProperty_SimpleEmpty() throws Exception {
		Owner p = b.owner("Jack").create();
		p.setCompanyTypeMap(new HashMap<String, String>());

		PropertyQuery query = PropertyQueryBuilder.forProperty(Owner.T, p.getId(), "companyTypeMap").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat(property).isEqualTo(MapTools.getMap());
	}

	@Test
	public void queryMapProperty_SimpleNull() throws Exception {
		Owner p = b.owner("Jack").create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Owner.T, p.getId(), "companyTypeMap").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat((Map<?, ?>) property).isEmpty();
	}

	@Test
	public void queryMapProperty_Entities() throws Exception {
		Company c1 = b.company("c1").create();
		Company c2 = b.company("c2").create();

		Person p = b.owner("Jack").addToCompanyMap("c1", c1).addToCompanyMap("c2", c2).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Owner.T, p.getId(), "companyMap").done();
		Object property = queryProperty(query);

		BtAssertions.assertThat(property).isEqualTo(MapTools.getMap("c1", c1, "c2", c2));
	}

	// ######################################
	// ## . Properties with restrictions . ##
	// ######################################

	@Test
	public void queryPropertyWithCondition() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		Company c = b.company("c").persons(p1, p2).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personSet").where().property("name").eq("Jack")
				.done();
		Object property = queryProperty(query);

		BtAssertions.assertThat(property).isEqualTo(asSet(p1));
	}

	@Test
	public void queryPropertyWithOrdering() throws Exception {
		Person p1 = b.person("p1").create();
		Person p2 = b.person("p2").create();
		Person p3 = b.person("p3").create();
		Person p4 = b.person("p4").create();

		Company c = b.company("c").persons(p1, p2, p3, p4).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personSet").orderBy("name").done();
		Object property = queryProperty(query);

		ArrayList<Object> list = new ArrayList<Object>((Collection<?>) property);
		BtAssertions.assertThat(list).containsSequence(p1, p2, p3, p4);
	}

	private Object queryProperty(PropertyQuery query) throws ModelAccessException {
		return QueryAdaptingTools.queryProperties(query, smood).getPropertyValue();
	}

	private void queryEntities(EntityQuery query) throws ModelAccessException {
		entities = QueryAdaptingTools.queryEntities(query, smood).getEntities();
	}

}
