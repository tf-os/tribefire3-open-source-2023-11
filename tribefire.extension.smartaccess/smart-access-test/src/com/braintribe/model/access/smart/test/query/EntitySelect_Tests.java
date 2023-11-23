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
package com.braintribe.model.access.smart.test.query;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.processing.query.smart.test.model.accessA.Address;
import com.braintribe.model.processing.query.smart.test.model.accessA.CarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.FlyingCarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.BasicSmartEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.Car;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.FlyingCar;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartAddress;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartReaderA;
import com.braintribe.model.processing.smart.query.planner.EntitySelection_PlannerTests;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class EntitySelect_Tests extends AbstractSmartQueryTests {

	/** @see EntitySelection_PlannerTests#selectSimpleEntity() */
	@Test
	public void selectSimpleEntity() {
		Address a1 = bA.address("street1").create();
		Address a2 = bA.address("street2").create();

		SmartAddress sa1 = smartAddress(a1);
		SmartAddress sa2 = smartAddress(a2);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartAddress.class, "a")
				.select("a")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sa1);
		assertResultContains(sa2);
		assertNoMoreResults();
	}

	/** @see EntitySelection_PlannerTests#selectPolymorphicEntity() */
	@Test
	public void selectPolymorphicEntity() {
		CarA c1 = bA.carA("car-1").create();
		FlyingCarA c2 = bA.flyingCarA("flying-car-2").maxFlyingSpeed(1200).create();

		Car sc1 = smartCar(c1);
		FlyingCar sc2 = smartFlyingCar(c2);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(Car.class, "c")
				.select("c")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sc1);
		assertResultContains(sc2);
		assertNoMoreResults();

		BtAssertions.assertThat(sc1.getSerialNumber()).isEqualTo("car-1");
		BtAssertions.assertThat(sc2.getSerialNumber()).isEqualTo("flying-car-2");
		BtAssertions.assertThat(sc2.getMaxFlyingSpeed()).isEqualTo(1200);
	}

	@Test
	public void singleSourceNoConditionEvaluatingNonMappedEntity() {
		Address a1 = bA.address("street1").create();
		Address a2 = bA.address("street2").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(BasicSmartEntity.class, "a")
				.select("a")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartAddress(a1));
		assertResultContains(smartAddress(a2));
		assertNoMoreResults();
	}

	/** @see EntitySelection_PlannerTests#directEntityCondition_selectIkpaProperty() */
	@Test
	public void directEntityCondition_selectIkpaProperty() {
		PersonA p1 = bA.personA("p1").create();
		PersonA p2 = bA.personA("p2").create();

		CompanyA c;
		c = bA.company("c1").ownerIdA(p1.getId()).create();
		c = bA.company("c2").ownerIdA(p2.getId()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("p", "inverseIdKeyCompanyA")
				.from(SmartPersonA.class, "p")
				.where()
					.entity("p").eq().entity(smartPerson(p2))
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartCompany(c));
		assertNoMoreResults();
	}

	/** @see EntitySelection_PlannerTests#directEntityCondition_selectIkpaProperty_disjunction() */
	@SuppressWarnings("unused")
	@Test
	public void directEntityCondition_selectIkpaProperty_disjunction() {
		PersonA p1 = bA.personA("p1").create();
		PersonA p2 = bA.personA("p2").create();
		PersonA p3 = bA.personA("p3").create();

		CompanyA c1 = bA.company("c1").ownerIdA(p1.getId()).create();
		CompanyA c2 = bA.company("c2").ownerIdA(p2.getId()).create();
		CompanyA c3 = bA.company("c3").ownerIdA(p3.getId()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("p", "inverseIdKeyCompanyA")
				.from(SmartPersonA.class, "p")
				.where()
					.disjunction()
						.entity("p").eq().entity(smartPerson(p1))
						.entity("p").eq().entity(smartPerson(p2))
					.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartCompany(c1));
		assertResultContains(smartCompany(c2));
		assertNoMoreResults();
	}

	/** @see EntitySelection_PlannerTests#propertyEntityCondition_In() */
	@Test
	public void propertyEntityCondition_In() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c3 = bA.company("c3").create();

		bA.personA("p1").companyA(c1).create();
		bA.personA("p2").companyA(c2).create();
		bA.personA("p3").companyA(c3).create();

		Company sc1 = smartCompany(c1);
		Company sc2 = smartCompany(c2);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.where()
					.property("p", "companyA").inEntities(asSet(sc1, sc2)) 
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p1");
		assertResultContains("p2");
		assertNoMoreResults();
	}

	@Test
	@Category(KnownIssue.class)
	public void EXPECTED_TO_FAIL_selectKpaProperty_LeftJoin() {
		bA.personA("p1").companyNameA("c1").create();
		bA.personA("p2").companyNameA("c2").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("p", "keyCompanyA")
				.from(SmartPersonA.class, "p")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(null);
		assertNextResult(null);
		assertNoMoreResults();
	}

	@Test
	public void selectKpaProperty_LeftJoin_ExternalDqj() {
		bA.personA("p1").companyNameA("c1").create();
		bA.personA("p2").companyNameA("c2").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("p", "keyCompanyExternalDqj")
				.from(SmartPersonA.class, "p")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(null);
		assertNextResult(null);
		assertNoMoreResults();
	}
	
	
	/**
	 * There was a bug that when doing a DQJ which is a left join, and the property-type is polymorphic, the resolution of the signature
	 * would throw an exception.
	 */
	@Test
	public void selectKpaProperty_LeftJoin_Polymorphic() {
		bA.readerA("r").favoritePublicationTitle("Feet of clay").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "favoritePublication")
				.from(SmartReaderA.class, "r")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(null);
		assertNoMoreResults();
	}
}
