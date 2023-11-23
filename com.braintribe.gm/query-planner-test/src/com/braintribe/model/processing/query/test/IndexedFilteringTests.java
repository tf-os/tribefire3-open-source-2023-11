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
package com.braintribe.model.processing.query.test;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.query.test.repository.IndexConfiguration;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.index.RepositoryIndex;
import com.braintribe.model.queryplan.index.RepositoryMetricIndex;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.IndexRange;
import com.braintribe.model.queryplan.set.IndexSubSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.StaticSet;
import com.braintribe.model.queryplan.value.IndexValue;
import com.braintribe.model.queryplan.value.StaticValue;
import com.braintribe.model.queryplan.value.range.SimpleRange;

/**
 * 
 */
public class IndexedFilteringTests extends AbstractQueryPlannerTests {

	// This is a special case, that is however the most important one of them all

	@Test
	public void singleSourceFindByDirectReferecne_Eq() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.entity("p").eq().entity(instance(Person.T, 77L, "partA"))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(StaticSet.T)
				.whereProperty("values")
					.isSetWithSize(1)
					.whereFirstElement()
						.isReference_(Person.T, 77L, "partA")
		;
		// @formatter:on
	}

	@Test
	public void singleSourceFindByDirectReferecne_In() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.entity("p").inEntities(asSet(
						instance(Person.T, 66, "partA"),
						instance(Person.T, 77, "partA")
					))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(StaticSet.T)
				.whereProperty("values")
					.isSetWithSize(2)
					.whenOrderedBy("refId")
					.whereElementAt(0).isReference_(Person.T, 66L, "partA")
					.whereElementAt(1).isReference_(Person.T, 77L, "partA")
		;
		// @formatter:on
	}

	@Test
	public void singleSourceFindForIndexedEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "indexedCompany").eq().entity(instance(Company.T, 99))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.isIndexSubSet(Person.T, "indexedCompany")
				.whereProperty("keys")
					.hasType(StaticValue.T)
					.whereValue()
						.setToList().isListWithSize(1).whereElementAt(0).isReference_(Company.T, 99L, null).close()
					.close()
				.close()
				.whereProperty("lookupIndex")
					.hasType(RepositoryIndex.T)
				.close()
		;
		// @formatter:on
	}

	@Test
	public void singleSourceFindForIndexInt() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "indexedInteger").eq(45)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.isIndexSubSet(Person.T, "indexedInteger")
				.whereProperty("keys")
					.isStaticSet_(45)
				.whereProperty("lookupIndex")
					.hasType(RepositoryMetricIndex.T)
				.close()
		;
		// @formatter:on
	}

	@Test
	public void singleSourceFindForIndexInt_Reverse() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
				 	.value(45).eq().property("p", "indexedInteger")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.isIndexSubSet(Person.T, "indexedInteger")
				.whereProperty("keys").isStaticSet_(45)
				.whereProperty("lookupIndex")
					.hasType(RepositoryMetricIndex.T)
				.close()
		;
		// @formatter:on
	}

	@Test
	public void singleSourceInOperatorWithIndexedInt() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "indexedInteger").in(asSet(1, 2, 3, 4))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.isIndexSubSet(Person.T, "indexedInteger")
				.whereProperty("keys")
					.isStaticSet_(1, 2, 3, 4)
				.whereProperty("lookupIndex")
					.hasType(RepositoryMetricIndex.T)
				.close()
		;
		// @formatter:on
	}

	@Test
	public void singleSourceFindRangeForIndexInt() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
				.conjunction()
					.property("p", "indexedInteger").gt(85)
					.property("p", "indexedInteger").le(95)
				.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexRange.T)
				.whereProperty("range")
					.hasType(SimpleRange.T)
					.whereProperty("lowerBound")
						.isStaticValue_(85)
					.whereProperty("lowerInclusive").isFalse_()
					.whereProperty("upperBound")
						.isStaticValue_(95)
					.whereProperty("upperInclusive").isTrue_()
		;
		// @formatter:on
	}

	@Test
	public void singleSourceSimpleIndexChain() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
				.property("p", "indexedCompany.indexedName").eq("companyName")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.isIndexSubSet(Person.T, "indexedCompany")
				.whereProperty("keys")
					.hasType(IndexValue.T)
					.whereProperty("indexId").is_(IndexConfiguration.indexId(Company.T, "indexedName"))
					.whereProperty("keys")
						.isStaticSet_("companyName")
		;
		// @formatter:on
	}

	/**
	 * There was a bug where if the left operand was an indexed property, the right hand side would not be checked that it's a static value, and query
	 * like this would create an {@link IndexSubSet} plan with {@link PropertyOperand} for the other property as the key for lookup.
	 */
	@Test
	public void singleSourceIndexedPropertyTrap() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "indexedName").eq().property("p", "indexedUniqueName")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.isSourceSet_(Person.T)
		;
		// @formatter:on
	}

}
