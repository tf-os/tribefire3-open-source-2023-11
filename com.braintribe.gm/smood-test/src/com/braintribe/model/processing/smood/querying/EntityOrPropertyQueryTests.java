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
package com.braintribe.model.processing.smood.querying;

import static com.braintribe.model.processing.query.fluent.EntityQueryBuilder.DEFAULT_SOURCE;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static org.fest.assertions.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.query.support.QueryConverter;
import com.braintribe.model.processing.query.test.model.Address;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.smood.test.AbstractSmoodTests;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class EntityOrPropertyQueryTests extends AbstractSmoodTests {

	private List<GenericEntity> entities;

	@Test
	public void queryEntities() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		EntityQuery query = EntityQueryBuilder.from(Person.T).done();
		queryEntities(query);

		assertThat(entities).containsOnly(p1, p2);
	}

	@SuppressWarnings("unused")
	@Test
	public void queryEntitiesWithCondition() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		EntityQuery query = EntityQueryBuilder.from(Person.T).where().property("name").eq("Jack").done();
		queryEntities(query);

		assertThat(entities).containsOnly(p1);
	}

	@Test
	public void queryEntitiesWithSourceTypeCondition() throws Exception {
		Person p;
		p = b.owner("John").create();
		p = b.person("Jack").create();

		EntityQuery query = EntityQueryBuilder.from(Person.T).where().entitySignature(DEFAULT_SOURCE).eq(Person.T.getTypeSignature()).done();
		queryEntities(query);

		assertThat(entities).containsOnly(p);
	}

	@Test
	public void querySimpleProperty() throws Exception {
		Company c = b.company("c").create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "name").done();
		Object property = queryProperty(query);

		assertThat(property).isEqualTo("c");
	}

	@Test(expected = NotFoundException.class)
	public void querySimpleProperty_Polymorphic() throws Exception {
		Person p = b.owner("Jack").name("p").create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Person.T, p.getId(), "name").done();
		queryProperty(query);
	}

	@Test
	public void querySetProperty() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		Company c = b.company("c").persons(p1, p2).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personSet").done();
		Object property = queryProperty(query);

		assertThat(property).isEqualTo(asSet(p1, p2));
	}

	@Test
	public void queryListProperty() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();
		Person p3 = b.person("Jim").create();

		Company c = b.company("c").persons(p1, p2, p3).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "persons").done();
		Object property = queryProperty(query);

		assertThat(property).isEqualTo(asList(p1, p2, p3));
	}

	@Test
	public void querySetPropertyWithCondition_Entity() throws Exception {
		Person p1 = b.person("Jack").create();

		Company c = b.company("c").owner(p1).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "owner").where().property("name").eq("Jack").done();

		Object property = queryProperty(query);
		assertThat(property).isEqualTo(p1);
		
		PropertyQuery query2 = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "owner").where().property("name").eq("John").done();

		Object property2 = queryProperty(query2);
		assertThat(property2).isNull();
	}

	@Test
	public void querySetPropertyWithCondition_Set() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		Company c = b.company("c").persons(p1, p2).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personSet").where().property("name").eq("Jack").done();
		Object property = queryProperty(query);

		assertThat(property).isEqualTo(asSet(p1));
	}

	@Test
	public void querySetPropertyWithCondition_List() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		Company c = b.company("c").persons(p1, p2).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "persons").where().property("name").eq("Jack").done();
		Object property = queryProperty(query);

		assertThat(property).isEqualTo(asList(p1));
	}

	@Test
	public void querySetPropertyWithPaging() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();
		Person p3 = b.person("John2").create();
		Person p4 = b.person("John3").create();
		Person p5 = b.person("John4").create();

		Company c = b.company("c").persons(p1, p2, p3, p4, p5).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personSet").paging(3, 0).done();
		Set<Person> property = (Set<Person>) queryProperty(query);

		BtAssertions.assertThat(property.size()).isEqualTo(3);

		query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personSet").paging(3, 3).done();
		property = (Set<Person>) queryProperty(query);

		assertThat(property.size()).isEqualTo(2);

	}

	/**
	 * There was a bug that querying for a Set property on a super-type would return the value of the property of a
	 * sub-type in case they had the same id. This was because for query like this the {@link PropertyQuery} is
	 * converted into a {@link SelectQuery} and the {@link QueryConverter} was not also also restricting the type
	 * signature.
	 */
	@Test
	public void querySetPropertyWithPaging_Polymorphic() throws Exception {
		Person owner = b.owner("Owner").create();
		Person person = b.person("Person").create();

		BtAssertions.assertThat(owner.<Long> getId()).isNotNull().isEqualTo(person.getId());

		Address address = b.address("Owner's Address").create();

		owner.getAddressSet().add(address);

		PropertyQuery query = PropertyQueryBuilder.forProperty(Person.T, person.getId(), "addressSet").paging(10, 0).done();
		Set<?> value = queryProperty(query);

		assertThat(value).isEmpty();
	}

	@Test
	public void querySetPropertyWithFulltextCondition() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		Company c = b.company("c").persons(p1, p2).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personSet").where().fullText(null, "").done();
		Object property = queryProperty(query);

		assertThat((Set<?>) property).containsOnly(p1, p2);
	}

	@Test
	public void queryMapProperty() throws Exception {
		Company c1 = b.company("c1").create();
		Company c2 = b.company("c2").create();

		Person p = b.owner("Jack").addToCompanyMap("c1", c1).addToCompanyMap("c2", c2).create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Owner.T, p.getId(), "companyMap").done();
		Object property = queryProperty(query);

		assertThat(property).isEqualTo(asMap("c1", c1, "c2", c2));
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

		List<Object> list = newList((Collection<?>) property);
		assertThat(list).containsSequence(p1, p2, p3, p4);
	}

	@Test
	public void querySimpleSetPropertyWithPaging() throws Exception {
		Person p = b.person("p1").nicknames("n1", "n2", "n3", "n4").create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Person.T, p.getId(), "nicknames").paging(3, 1).done();
		Object property = queryProperty(query);

		assertThat((Set<?>) property).containsOnly("n2", "n3", "n4");
	}

	/** Same as {@link #querySimpleSetPropertyWithPaging()}, but different bounds. */
	@Test
	public void querySimpleSetPropertyWithPaging_2() throws Exception {
		Person p = b.person("p1").nicknames("n1", "n2", "n3", "n4").create();

		PropertyQuery query = PropertyQueryBuilder.forProperty(Person.T, p.getId(), "nicknames").paging(3, 2).done();
		Object property = queryProperty(query);

		assertThat((Set<?>) property).containsOnly("n3", "n4");
	}

	private <T> T queryProperty(PropertyQuery query) {
		return (T) smood.queryProperty(query).getPropertyValue();
	}

	private void queryEntities(EntityQuery query) throws ModelAccessException {
		entities = smood.queryEntities(query).getEntities();
	}

}
