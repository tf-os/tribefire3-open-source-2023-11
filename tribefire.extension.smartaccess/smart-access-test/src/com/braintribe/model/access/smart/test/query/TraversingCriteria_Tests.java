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

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.basic.TypeKind.scalarType;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class TraversingCriteria_Tests extends AbstractSmartQueryTests {

	private PersonA p1, p2;
	private CompanyA c1, c2;

	private SmartPersonA sp1, sp2;
	private Company sc1, sc2;

	@Test
	public void loadPersonWithCompany() {
		generateDelegateEntities();
		runWithCriteria(personWithCompanyTc());
		loadSmartEntities();

		BtAssertions.assertThat(sp1.getCompanyA()).isSameAs(sc1);
		BtAssertions.assertThat(sp2.getCompanyA()).isSameAs(sc2);

		Property p = sc1.entityType().getProperty("ownerA");
		BtAssertions.assertThat(p.getAbsenceInformation(sc1)).isSameAs(GMF.absenceInformation());
		BtAssertions.assertThat(p.getAbsenceInformation(sc2)).isSameAs(GMF.absenceInformation());
	}

	/** We load all simple/enum properties and the property "companyA" on our top-level entity. */
	private TraversingCriterion personWithCompanyTc() {
		// @formatter:off
		return TC.create()
					.negation()
						.disjunction()
							.typeCondition(isKind(scalarType))
							.pattern()
								  .root()
								  .entity()
								  .property("companyA")
							 .close()
						.close()
				.done();
		// @formatter:on
	}

	@Test
	public void loadAll() {
		generateDelegateEntities();
		runWithCriteria(allTc());
		loadSmartEntities();

		BtAssertions.assertThat(sp1.getCompanyA()).isSameAs(sc1);
		BtAssertions.assertThat(sp2.getCompanyA()).isSameAs(sc2);

		BtAssertions.assertThat(sc1.getOwnerA()).isSameAs(sp1);
		BtAssertions.assertThat(sc2.getOwnerA()).isSameAs(sp2);
	}

	/** We load everything. (Almost. See comment below this method.) */
	private TraversingCriterion allTc() {
		// @formatter:off
		return TC.create()
				.negation()
					.disjunction()
						.typeCondition(isKind(scalarType)						)
						.property("companyA")
						.property("ownerA")
						// we test that loading unmapped properties doesn't cause problems 
						.property("unmappedParent")
						.property("unmappedEntity")
					.close()
			.done();
	// @formatter:on
	}

	/* We don'tuse this anymore as some internal-DQJ properties are currently not supported, hence we do not want to
	 * load them. If it's ever fixed, this test should switch back to this allTc method. */

	// private TraversingCriterion allTc() {
//		// @formatter:off
//		return TC.create()
//				.negation()				
//					.joker()
//			.done();
//		// @formatter:on
	// }

	private void generateDelegateEntities() {
		c1 = bA.company("company1").create();
		c2 = bA.company("company2").create();

		p1 = bA.personA("p1").companyA(c1).create();
		p2 = bA.personA("p2").companyA(c2).create();

		c1.setOwnerA(p1);
		c2.setOwnerA(p2);
	}

	private void runWithCriteria(TraversingCriterion tc) {
		smartAccess.setDefaultTraversingCriteria(asMap(GenericEntity.class, tc));

		SelectQuery selectQuery = query().from(SmartPersonA.class, "p").select("p").done();

		evaluate(selectQuery);
	}

	/**
	 * if we did this before query evaluation, we would lose the AbsenceInformation, as this would create an entity in
	 * the session
	 */
	private void loadSmartEntities() {
		sp1 = smartPerson(p1);
		sp2 = smartPerson(p2);

		assertResultContains(sp1);
		assertResultContains(sp2);
		assertNoMoreResults();

		sc1 = smartCompany(c1);
		sc2 = smartCompany(c2);
	}

}
