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
package com.braintribe.model.processing.query.test.stringifier;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.PropertyQuery;

public class PropertyQueryTests extends AbstractSelectQueryTests {

	@Test
	public void querySimpleProperty() {
		Company c = getCompany();
		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "name").done();

		String queryString = stringify(query);
		assertThat(queryString).isEqualToIgnoringCase("property name of reference(com.braintribe.model.processing.query.test.model.Company, 1l)");
	}

	@Test
	public void querySimpleProperty_EntityWithPartition() {
		Company c = getCompany();
		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "cortex", "name").done();

		String queryString = stringify(query);
		assertThat(queryString)
				.isEqualToIgnoringCase("property name of reference(com.braintribe.model.processing.query.test.model.Company, 1l, 'cortex')");
	}

	@Test
	public void querySimpleProperty_Polymorphic() {
		Person p = getPerson();
		PropertyQuery query = PropertyQueryBuilder.forProperty(Person.T, p.getId(), "name").done();

		String queryString = stringify(query);
		assertThat(queryString).isEqualToIgnoringCase("property name of reference(com.braintribe.model.processing.query.test.model.Person, 1l)");
	}

	@Test
	public void querySetProperty() {
		Company c = getTwoPersonCompany();
		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personSet").done();

		String queryString = stringify(query);
		assertThat(queryString)
				.isEqualToIgnoringCase("property personSet of reference(com.braintribe.model.processing.query.test.model.Company, 1l)");
	}

	@Test
	public void querySetPropertyWithCondition() {
		Company c = getTwoPersonCompany();
		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personSet").where().property("name").eq("Jack").done();

		String queryString = stringify(query);
		assertThat(queryString).isEqualToIgnoringCase(
				"property personSet of reference(com.braintribe.model.processing.query.test.model.Company, 1l) where name = 'Jack'");
	}

	@Test
	public void querySetPropertyWithFulltextCondition() {
		Company c = getTwoPersonCompany();
		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personSet").where().fullText(null, "").done();

		String queryString = stringify(query);
		assertThat(queryString).isEqualToIgnoringCase(
				"property personSet of reference(com.braintribe.model.processing.query.test.model.Company, 1l) where fulltext( '')");
	}

	@Test
	public void queryMapProperty() {
		Owner o = getOwner();
		PropertyQuery query = PropertyQueryBuilder.forProperty(Owner.T, o.getId(), "companyMap").done();

		String queryString = stringify(query);
		assertThat(queryString).isEqualToIgnoringCase("property companyMap of reference(com.braintribe.model.processing.query.test.model.Owner, 1l)");
	}

	@Test
	public void queryPropertyWithOrdering() {
		Company c = getFourPersonCompany();
		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "personSet").orderBy("name").done();

		String queryString = stringify(query);
		assertThat(queryString).isEqualToIgnoringCase(
				"property personSet of reference(com.braintribe.model.processing.query.test.model.Company, 1l) order by name asc");
	}

	@Test
	public void querySimpleSetPropertyWithLimit() {
		Company c = getFourPersonCompany();
		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "persons").limit(10).done();

		String queryString = stringify(query);
		assertThat(queryString).isEqualToIgnoringCase(
				"property persons of reference(com.braintribe.model.processing.query.test.model.Company, 1l) limit 10 offset 0");
	}

	@Test
	public void querySimpleSetPropertyWithLimit2() {
		Company c = getFourPersonCompany();
		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, c.getId(), "persons").limit(25).done();

		String queryString = stringify(query);
		assertThat(queryString).isEqualToIgnoringCase(
				"property persons of reference(com.braintribe.model.processing.query.test.model.Company, 1l) limit 25 offset 0");
	}

	@Test
	public void querySimpleSetPropertyWithPaging() {
		Person p = getNicknamePerson();
		PropertyQuery query = PropertyQueryBuilder.forProperty(Person.T, p.getId(), "nicknames").paging(1, 3).done();

		String queryString = stringify(query);
		assertThat(queryString).isEqualToIgnoringCase(
				"property nicknames of reference(com.braintribe.model.processing.query.test.model.Person, 1l) limit 1 offset 3");
	}

	@Test
	public void querySimpleSetPropertyWithPaging_2() {
		Person p = getNicknamePerson();
		PropertyQuery query = PropertyQueryBuilder.forProperty(Person.T, p.getId(), "nicknames").paging(100, 200).done();

		String queryString = stringify(query);
		assertThat(queryString).isEqualToIgnoringCase(
				"property nicknames of reference(com.braintribe.model.processing.query.test.model.Person, 1l) limit 100 offset 200");
	}
}
