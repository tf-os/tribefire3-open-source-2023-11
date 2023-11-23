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
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.filter.Conjunction;
import com.braintribe.model.queryplan.filter.Disjunction;
import com.braintribe.model.queryplan.filter.Equality;
import com.braintribe.model.queryplan.index.RepositoryIndex;
import com.braintribe.model.queryplan.index.RepositoryMetricIndex;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.IndexSubSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.StaticSet;

public class DisjunctionTests extends AbstractQueryPlannerTests {

	@Test
	public void simpleDisjunctionOnSoureByDirectReference() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.disjunction()
						.entity("p").eq().entity(instance(Person.T, 66, "partA"))
						.entity("p").eq().entity(instance(Person.T, 77, "partA"))
					.close()
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
	public void simpleDisjuncitonOnIndexedProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.disjunction()
						.property("p", "indexedName").eq("Name1")							
						.property("p", "indexedName").eq("Name2")							
						.property("p", "indexedName").eq("Name3")							
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexSubSet.T)
				.whereProperty("typeSignature").is_(Person.class.getName())
				.whereProperty("propertyName").is_("indexedName")
				.whereProperty("keys")
					.isStaticSet_("Name1", "Name2", "Name3")
				.whereProperty("lookupIndex").hasType(RepositoryMetricIndex.T).close()
		;
		// @formatter:on
	}

	@Test
	public void simpleDisjuncitonOnIndexedProperty_In() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.disjunction()
						.property("p", "indexedName").eq("Name1")							
						.property("p", "indexedName").in(asSet("Name2","Name3"))							
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexSubSet.T)
				.whereProperty("typeSignature").is_(Person.class.getName())
				.whereProperty("propertyName").is_("indexedName")
				.whereProperty("keys")
					.isStaticSet_("Name1", "Name2", "Name3")
				.whereProperty("lookupIndex").hasType(RepositoryMetricIndex.T).close()
		;
		// @formatter:on
	}

	@Test
	public void simpleDisjuncitonOnIndexedPropertyChain() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.disjunction()
						.property("p", "indexedCompany.indexedName").eq("Name1")							
						.property("p", "indexedCompany.indexedName").eq("Name2")							
						.property("p", "indexedCompany.indexedName").eq("Name3")							
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// TODO INDEX-CHAIN + DISJUNCTION

		// @formatter:off
//		assertQueryPlan()
//			.hasType(Projection.T).whereOperand()
//				.hasType(IndexSubSet.T)
//				.whereProperty("typeSignature").is_(Person.class.getName())
//				.whereProperty("propertyName").is_("indexedName")
//				.whereProperty("keys")
//					.hasType(StaticValue.T).whereValue().containsOnly("Name1", "Name2", "Name3").close()
//				.close()
//				.whereProperty("lookupIndex").hasType(RepositoryMetricIndex.T).close()
//		;
		// @formatter:on
	}

	@Test
	public void disjuncitonOnIndexedPropertyWithOtherFiltersOnTopLevel() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.conjunction()
						.property("p", "name").eq("name")							
						.disjunction()
							.property("p", "indexedName").eq("Name1")							
							.property("p", "indexedName").eq("Name2")							
							.property("p", "indexedName").eq("Name3")							
						.close()
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.hasType(IndexSubSet.T)
					.whereProperty("typeSignature").is_(Person.class.getName())
					.whereProperty("propertyName").is_("indexedName")
					.whereProperty("keys")
						.isStaticSet_("Name1", "Name2", "Name3")
					.whereProperty("lookupIndex").hasType(RepositoryMetricIndex.T).close()
				.close()
				.whereProperty("filter").hasType(Equality.T)
		;
		// @formatter:on
	}

	@Test
	public void disjuncitonOnIndexedPropertyWithOtherFiltersAsPartOfDisjunction() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.disjunction()
						.conjunction()
							.property("p", "indexedName").eq("Name1")
							.property("p", "name").eq("name")
						.close()
						.conjunction()
							.property("p", "indexedName").eq("Name2")							
							.property("p", "age").eq(42)
						.close()
						.conjunction()
							.property("p", "indexedName").eq("Name3")							
							.property("p", "phoneNumber").eq("555-DUCK")							
						.close()
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.hasType(IndexSubSet.T)
					.whereProperty("typeSignature").is_(Person.class.getName())
					.whereProperty("propertyName").is_("indexedName")
					.whereProperty("keys")
						.isStaticSet_("Name1", "Name2", "Name3")
					.whereProperty("lookupIndex").hasType(RepositoryMetricIndex.T).close()
				.close()
				.whereProperty("filter")
					.hasType(Disjunction.T)
					.whereProperty("operands")
						.whereElementAt(0).hasType(Conjunction.T).close()
						.whereElementAt(1).hasType(Conjunction.T).close()
						.whereElementAt(2).hasType(Conjunction.T).close()
		;
		// @formatter:on
	}

	@Test
	public void disjuncitonOnTwoIndexedPropertiesOfTwoEntities() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "company")
				.where()
					.disjunction()
						.conjunction()
							.property("p", "indexedInteger").eq(1)
							.property("company", "indexedName").eq("Company1")
						.close()
						.conjunction()
							.property("p", "indexedInteger").eq(2)							
							.property("company", "indexedName").eq("Company2")
						.close()
						.conjunction()
							.property("p", "indexedInteger").eq(3)							
							.property("company", "indexedName").eq("Company3")
						.close()
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.hasType(CartesianProduct.T)
					.whereProperty("operands")
						.whenOrderedBy("typeSignature")
						.whereElementAt(0)
							.hasType(IndexSubSet.T)
							.whereProperty("typeSignature").is_(Company.class.getName())
							.whereProperty("propertyName").is_("indexedName")
							.whereProperty("keys")
								.isStaticSet_("Company1", "Company2", "Company3")
							.whereProperty("lookupIndex").hasType(RepositoryIndex.T).close()
						.close()
						.whereElementAt(1)
							.hasType(IndexSubSet.T)
							.whereProperty("typeSignature").is_(Person.class.getName())
							.whereProperty("propertyName").is_("indexedInteger")
							.whereProperty("keys")
								.isStaticSet_(1, 2, 3)
							.whereProperty("lookupIndex").hasType(RepositoryMetricIndex.T).close()
						.close()
					.close()
				.close()
				.whereProperty("filter").hasType(Disjunction.T)//TODO all other condition
		;
		// @formatter:on
	}

}
