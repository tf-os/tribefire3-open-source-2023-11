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

import java.util.List;

import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.filter.Conjunction;
import com.braintribe.model.queryplan.filter.Disjunction;
import com.braintribe.model.queryplan.filter.Equality;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.join.EntityJoin;
import com.braintribe.model.queryplan.set.join.JoinKind;
import com.braintribe.model.queryplan.set.join.ListJoin;
import com.braintribe.model.queryplan.set.join.MapJoin;
import com.braintribe.model.queryplan.set.join.SetJoin;
import com.braintribe.model.queryplan.value.ValueProperty;

public class BasicUseCaseTests extends AbstractQueryPlannerTests {

	@Test
	public void singleSourceNoCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.isSourceSet_(Person.T)
		;
		// @formatter:on
	}

	@Test
	public void sameSourceTwiceNoCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p1")
				.from(Person.T, "p2")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(CartesianProduct.T)
				.whereProperty("operands")
					.hasType(List.class)
					.whereElementAt(0).isSourceSet_(Person.T)
					.whereElementAt(1).isSourceSet_(Person.T)
		;
		// @formatter:on
	}

	@Test
	public void singleSourceNonIndexCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "name").eq("John")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand().isSourceSet_(Person.T)
				.whereProperty("filter")
					.hasType(Equality.T)
		;
		// @formatter:on
	}

	@Test
	public void singleSourceNonIndexConditionOnEntityProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "company").eq().entity(instance(Company.T, 99L))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand().isSourceSet_(Person.T)
				.whereProperty("filter")
					.hasType(Equality.T)
					.whereProperty("leftOperand").isValueProperty_("company")
					.whereProperty("rightOperand").isStaticValue_(Company.T, 99L, null)
		;
		// @formatter:on
	}

	@Test
	public void sourceAndJoinSelection() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "p")
					.join("p", "company", "company", JoinType.left)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(EntityJoin.T)
				.whereOperand().isSourceSet_(Owner.T)
				.whereProperty("joinKind").is_(JoinKind.left)
		;
		// @formatter:on
	}

	/** We had a bug with REST call when this was causing an NPE */
	@Test
	public void sourceAndJoinSelection_JoinKindNotSpecified() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "p")
					.join("p", "company", "company", null)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(EntityJoin.T)
				.whereOperand().isSourceSet_(Owner.T)
				.whereProperty("joinKind").is_(JoinKind.inner)
		;
		// @formatter:on
	}

	@Test
	public void singleSourceNonIndexConditionOnJoined() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "p")
					.join("p", "company", "company")
				.where()
					.property("company", "name").eq("HP")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.hasType(EntityJoin.T)
					.whereOperand().isSourceSet_(Owner.T)
				.close()
				.whereProperty("filter")
					.hasType(Equality.T)
		;
		// @formatter:on
	}

	@Test
	public void singleSourceNonIndexConditionOnFrom() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "p")
					.join("p", "company", "company")
				.where()
					.property("p", "name").eq("HP")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(EntityJoin.T)
				.whereOperand()
					.hasType(FilteredSet.T)
					.whereOperand().isSourceSet_(Owner.T)
					.whereProperty("filter")
						.hasType(Equality.T)
					.close()
		;
		// @formatter:on
	}

	/* There was a bug, that longer join chains were not handled properly, causing NPE, and another one that cause a
	 * join to be applied multiple times. */
	@Test
	public void singleSourceDoulbeJoin() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
					.join("p", "company", "c")
						.join("c", "address", "a")
				.where()
					.property("a", "street").eq("Elm Street")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(FilteredSet.T)
					.whereProperty("filter").hasType(Equality.T).close()
					.whereOperand()
						.hasType(EntityJoin.T)
						.whereProperty("valueProperty").isValueProperty_("address")
						.whereOperand()
							.hasType(EntityJoin.T)
							.whereProperty("valueProperty").isValueProperty_("company")
							.whereOperand().isSourceSet_(Person.T)
		;
		// @formatter:on
	}

	@Test
	public void singleSourceNullTestConditionOnJoined() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.class, "p")
				.leftJoin("p", "company", "company")
				.where()
					.entity("company").eq(null)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.hasType(EntityJoin.T)
					.whereOperand().isSourceSet_(Owner.T)
				.close()
				.whereProperty("filter")
					.hasType(Equality.T)
					.whereProperty("leftOperand").isTupleComponent_(1)
					.whereProperty("rightOperand").isStaticValue_(null);
		// @formatter:on
	}

	@Test
	public void singleSourceRightJoinConditionOnFrom() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.class, "p")
				.rightJoin("p", "company", "company")
				.where()
					.entity("p").eq(null)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.hasType(EntityJoin.T)
					.whereProperty("joinKind").is_(JoinKind.right)
					.whereOperand().isSourceSet_(Owner.T)
				.close()
				.whereProperty("filter")
					.hasType(Equality.T)
		;
	// @formatter:on
	}

	@Test
	public void singleSourceNonIndexConjunctionOfConditions() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.conjunction()
						.property("p", "name").eq("John")
						.property("p", "companyName").ilike("S%")
						.property("p", "phoneNumber").like("555-%")
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand().isSourceSet_(Person.T)
				.whereProperty("filter")
					.hasType(Conjunction.T)
		;
		// @formatter:on
	}

	/* TODO <LOW PRIO> this currently does the join first, then applies the entire conjunction. Split the
	 * conjunction...? */
	@Test
	public void singleSourceNonIndexConjunctionOfConditionsWithJoin() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.class, "p")
				.join("p", "company", "company")
				.where()
					.conjunction()
						.property("p", "name").eq("John")
						.property("p", "phoneNumber").like("555-%")
						.property("company", "name").eq("HP")
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);
	}

	@Test
	public void singleSourceNonIndexDisjunctionOfConditions() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
				.disjunction()
						.property("p", "name").eq("John")
						.property("p", "companyName").ilike("S%")
						.property("p", "phoneNumber").like("555-%")
				.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand().isSourceSet_(Person.T)
				.whereProperty("filter")
					.hasType(Disjunction.T)
		;
		// @formatter:on
	}

	@Test
	public void singleSourceNonIndexDisjunctionGivenAsNegationOfConjunction() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.negation()
						.conjunction()
							.property("p", "name").eq("John")
							.property("p", "companyName").like("s%")
							.property("p", "phoneNumber").ilike("555-%")
						.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand().isSourceSet_(Person.T)
				.whereProperty("filter")
					.hasType(Disjunction.T)
		;
		// @formatter:on
	}

	// ####################################
	// ## . . . . collection joins . . . ##
	// ####################################

	@Test
	public void joinWithSet() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.class, "p")
					.join("p", "companySet", "cs")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(SetJoin.T)
				.whereOperand().isSourceSet_(Owner.T)
				.whereProperty("valueProperty")
					.hasType(ValueProperty.T)
					.whereProperty("propertyPath").is_("companySet");
		// @formatter:on
	}

	@Test
	public void joinWithListAndCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.class, "p")
				.join("p", "companyList", "cs")
				.where()
					.listIndex("cs").le(1)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.hasType(ListJoin.T)
					.whereOperand().isSourceSet_(Owner.T)
					.whereProperty("valueProperty")
						.isValueProperty_("companyList")
		;
		// @formatter:on
	}

	@Test
	public void joinWithMapAndCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.class, "p")
				.join("p", "companyMap", "cs")
				.where()
					.mapKey("cs").eq("C2")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.hasType(MapJoin.T)
					.whereOperand().isSourceSet_(Owner.T)
					.whereProperty("valueProperty")
						.isValueProperty_("companyMap")
		;
		// @formatter:on
	}

}
