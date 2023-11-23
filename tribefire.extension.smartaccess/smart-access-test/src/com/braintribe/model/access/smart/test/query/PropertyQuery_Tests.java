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

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdA;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 * @author peter.gazdik
 */
public class PropertyQuery_Tests extends AbstractSmartQueryTests {

	Object propertyQueryResult;

	@Test
	public void simpleListProperty() {
		PersonA p = bA.personA("p1").nickNamesA("n1", "n2", "n3", "n4", "n5").create();

		evaluate(PropertyQueryBuilder.forProperty(SmartPersonA.T, p.getId(), accessIdA, "nickNamesListA").done());

		BtAssertions.assertThat((List<?>) propertyQueryResult).containsSequence("n1", "n2", "n3", "n4", "n5");
	}

	@Test
	public void explicitEntityListProperty() {
		// This is shuffled on purpose, so that we know the order is not arbitrary in our case
		CompanyA c4 = bA.company("c4").create();
		CompanyA c3 = bA.company("c3").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c1 = bA.company("c1").create();
		CompanyA c5 = bA.company("c5").create();

		PersonA p = bA.personA("p1").companies(c1, c2, c3, c4, c5).create();

		evaluate(PropertyQueryBuilder.forProperty(SmartPersonA.T, p.getId(), accessIdA, "companyListA").done());

		BtAssertions.assertThat((List<?>) propertyQueryResult).containsSequence(smartCompany(c1), smartCompany(c2), smartCompany(c3),
				smartCompany(c4), smartCompany(c5));
	}

	@Test
	@Category(KnownIssue.class)
	public void EXPECTED_TO_FAIL_kpaEntityListProperty() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c3 = bA.company("c3").create();
		CompanyA c4 = bA.company("c4").create();
		CompanyA c5 = bA.company("c5").create();

		PersonA p = bA.personA("p1").companyNames("c1", "c2", "c3", "c4", "c5").create();

		evaluate(PropertyQueryBuilder.forProperty(SmartPersonA.T, p.getId(), accessIdA, "keyCompanyListA").done());

		BtAssertions.assertThat((List<?>) propertyQueryResult).containsSequence(smartCompany(c1), smartCompany(c2), smartCompany(c3),
				smartCompany(c4), smartCompany(c5));
	}

	@Test
	public void kpaEntityListProperty_ExternalDqj() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c3 = bA.company("c3").create();
		CompanyA c4 = bA.company("c4").create();
		CompanyA c5 = bA.company("c5").create();

		PersonA p = bA.personA("p1").companyNames("c1", "c2", "c3", "c4", "c5").create();

		evaluate(PropertyQueryBuilder.forProperty(SmartPersonA.T, p.getId(), accessIdA, "keyCompanyListExternalDqj").done());

		BtAssertions.assertThat((List<?>) propertyQueryResult).containsSequence(smartCompany(c1), smartCompany(c2), smartCompany(c3),
				smartCompany(c4), smartCompany(c5));
	}
	@Test
	public void lpaEntityListProperty() {
		ItemB i2 = bB.item("i2").create();
		ItemB i5 = bB.item("i5").create();
		ItemB i1 = bB.item("i1").create();
		ItemB i3 = bB.item("i3").create();
		ItemB i4 = bB.item("i4").create();

		PersonA p = bA.personA("p1").create();

		bB.personItemOrderedLink(p, i1, 0);
		bB.personItemOrderedLink(p, i2, 1);
		bB.personItemOrderedLink(p, i3, 2);
		bB.personItemOrderedLink(p, i4, 3);
		bB.personItemOrderedLink(p, i5, 4);

		evaluate(PropertyQueryBuilder.forProperty(SmartPersonA.T, p.getId(), accessIdA, "orderedLinkItems").done());

		BtAssertions.assertThat((List<?>) propertyQueryResult).containsSequence(smartItem(i1), smartItem(i2), smartItem(i3), smartItem(i4),
				smartItem(i5));
	}

	protected void evaluate(PropertyQuery query) {
		PropertyQueryResult queryResult = executeQuery(query);
		propertyQueryResult = mergeQueryResultToLocalSession(queryResult.getPropertyValue());
	}

	protected PropertyQueryResult executeQuery(PropertyQuery query) {
		try {
			return smartAccess.queryProperty(query);

		} catch (ModelAccessException e) {
			throw new RuntimeException("SelectQuery could not be evaluated.", e);
		}
	}

}
