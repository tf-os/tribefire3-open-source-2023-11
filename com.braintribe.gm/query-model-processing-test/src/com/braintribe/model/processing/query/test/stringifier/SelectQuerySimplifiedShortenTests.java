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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.processing.query.shortening.Simplified;
import com.braintribe.model.processing.query.stringifier.experts.basic.DateStringifier;
import com.braintribe.model.processing.query.test.model.Color;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

public class SelectQuerySimplifiedShortenTests extends AbstractSelectQueryTests {
	@Test
	public void singleSourceNoCondition() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from Person _Person");
	}

	@Test
	public void sourceType_() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Owner.class, "_Owner")
				.select().entitySignature().entity("_Owner")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select typeSignature(_Owner) from Owner _Owner");
	}

	@Test
	public void sourceType_Hierarchy() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select().entitySignature().entity("_Person")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select typeSignature(_Person) from Person _Person");
	}

	@Test
	public void conditionOnSourceType() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select().entity("_Person")
				.where()
				.entitySignature("_Person").eq(Owner.class.getName())
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase(
				"select _Person from Person _Person where typeSignature(_Person) = 'com.braintribe.model.processing.query.test.model.Owner'");
	}

	@Test
	public void sameSourceTwiceNoCondition() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.from(Person.class, "_Person2")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from Person _Person, Person _Person2");
	}

	@Test
	public void singleSourceNonIndexCondition() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.property("_Person", "name").eq("John")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from Person _Person where _Person.name = 'John'");
	}

	@Test
	public void singleSourceConditionOnEntityProperty() {
		Company c = getCompany();

		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.property("_Person", "company").eq().entity(c)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from Person _Person where _Person.company = reference(Company, 1l)");
	}

	@Test
	public void singleSourceConditionOnEntityProperty2() {
		Company c = getCompany();

		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.property("_Person", "company").eq().value(c)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from Person _Person where _Person.company = reference(Company, 1l)");
	}

	@Test
	public void singleSourceInConditionOnEntityProperty() {
		Company c = getCompany();

		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.property("_Person", "company").in(asSet(c))
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from Person _Person where _Person.company in (reference(Company, 1l))");
	}

	@Test
	public void singleSourceNonIndexConditionOnJoined() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Owner.class, "_Owner")
				.join("_Owner", "company", "_Company")
				.where()
				.property("_Company", "name").eq("HP")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select * from Owner _Owner join _Owner.company _Company where _Company.name = 'HP'");
	}

	@Test
	public void singleSourceNonIndexConditionOnFrom() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Owner.class, "_Owner")
				.join("_Owner", "company", "_Company")
				.where()
				.property("_Owner", "name").eq("Bill Hewlett")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select * from Owner _Owner join _Owner.company _Company where _Owner.name = 'Bill Hewlett'");
	}

	@Test
	public void singleSourceNonIndexConjunctionOfConditions() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.conjunction()
				.property("_Person", "name").eq("John")
				.property("_Person", "companyName").ilike("S*")
				.property("_Person", "phoneNumber").like("555-*")
				.close()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase(
				"select * from Person _Person where (_Person.name = 'John' and _Person.companyName ilike 'S*' and _Person.phoneNumber like '555-*')");
	}

	@Test
	public void singleSourceNonIndexConjunctionOfConditionsWithJoin() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Owner.class, "_Owner")
				.join("_Owner", "company", "_Company")
				.where()
				.conjunction()
				.property("_Owner", "name").eq("John")
				.property("_Owner", "phoneNumber").like("555-*")
				.property("_Company", "name").eq("HP")
				.close()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase(
				"select * from Owner _Owner join _Owner.company _Company where (_Owner.name = 'John' and _Owner.phoneNumber like '555-*' and _Company.name = 'HP')");
	}

	@Test
	public void simpleNoWhereInnerJoin() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.join("_Person", "company", "_Company", JoinType.inner)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from Person _Person join _Person.company _Company");
	}

	@Test
	public void simpleNoWhereLeftJoin() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.join("_Person", "company", "_Company", JoinType.left)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from Person _Person left join _Person.company _Company");
	}

	@Test
	public void simpleNoWhereRightJoin() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.join("_Person", "company", "_Company", JoinType.right)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from Person _Person right join _Person.company _Company");
	}

	@Test
	public void simpleNoWhereFullJoin() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.join("_Person", "company", "_Company", JoinType.full)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from Person _Person full join _Person.company _Company");
	}

	@Test
	public void singleSourceNonIndexDisjunctionOfConditions() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.disjunction()
				.property("_Person", "name").eq("John")
				.property("_Person", "companyName").ilike("S*")
				.property("_Person", "phoneNumber").like("555-*")
				.close()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase(
				"select * from Person _Person where (_Person.name = 'John' or _Person.companyName ilike 'S*' or _Person.phoneNumber like '555-*')");
	}

	@Test
	public void dateSourceConditionOnEntityProperty() {
		// Create birthDate in GMT+1 TimeZone and clear value
		Calendar setBirthDate = Calendar.getInstance();
		setBirthDate.clear();

		// Set specific date as birthDate
		setBirthDate.set(1975, 2, 20, 12, 50, 33);
		setBirthDate.setTimeInMillis(setBirthDate.getTimeInMillis() + 582);

		// @formatter:off
		 Date birthDate = setBirthDate.getTime();
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.disjunction()
				.property("_Person", "birthDate").eq(birthDate)
				.close()
				.done();
		// @formatter:on

		String birthDateString = new DateStringifier().stringify(birthDate, null);

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from Person _Person where (_Person.birthDate = " + birthDateString + ")");
	}

	@Test
	public void mixedSourceConditionOnEntityProperty() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.disjunction()
				.property("_Person", "color").eq(Color.GREEN)
				.property("_Person", "indexedFriend").ne(this.getPerson())
				.conjunction()
				.property("_Person", "age").ge(40)
				.property("_Person", "age").le(60)
				.close()
				.property("_Person", "company").ne(this.getCompany())
				.close()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from Person _Person where (_Person.color = enum(Color, GREEN) or _Person.indexedFriend != reference(Person, 1l) or (_Person.age >= 40 and _Person.age <= 60) or _Person.company != reference(Company, 1l))"));
	}

	// ####################################
	// ## . . . . collection joins . . . ##
	// ####################################

	@Test
	public void joinWithSet() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Owner.class, "_Owner")
				.join("_Owner", "companySet", "_Company")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from Owner _Owner join _Owner.companySet _Company");
	}

	@Test
	public void joinWithListAndCondition() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Owner.class, "_Owner")
				.join("_Owner", "companyList", "_Company")
				.where()
				.listIndex("_Company").le(1)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select * from Owner _Owner join _Owner.companyList _Company where listIndex(_Company) <= 1");
	}

	@Test
	public void joinWithMapAndCondition() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Owner.class, "_Owner")
				.join("_Owner", "companyMap", "_Company")
				.where()
				.mapKey("_Company").eq("c2")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select * from Owner _Owner join _Owner.companyMap _Company where mapKey(_Company) = 'c2'");
	}

	@Test
	public void listIndexWithJoinAndCondition() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.join("_Person", "company", "_Company")
				.select().listIndex("_Company")
				.where()
				.listIndex("_Company").eq().value(5)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select listIndex(_Company) from Person _Person join _Person.company _Company where listIndex(_Company) = 5");
	}
}
