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

import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.join.EntityJoin;
import com.braintribe.model.queryplan.set.join.MapJoin;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.queryplan.value.TupleComponent;

/**
 * 
 */
public class ProjectionTests extends AbstractQueryPlannerTests {

	@Test
	public void selectingEntityAndProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p")
				.select("p", "name")
				.from(Person.T, "p")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand().isSourceSet_(Person.T)
			.hasValues(2)
				.whereElementAt(0).isTupleComponent_(0)
				.whereElementAt(1).isValueProperty_("name");
		// @formatter:on
	}

	@Test
	public void selectingCompoundProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p")
				.select("p", "company.name")
				.from(Owner.class, "p")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereOperand().hasType(EntityJoin.T).close()
			.hasValues(2)
				.whereElementAt(0).hasType(TupleComponent.T).close()
				.whereElementAt(1).isValueProperty_("name");
		// @formatter:on
	}

	@Test
	public void selectingConstants() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p")
				.select().value(99L)
				.select().value("constantString")
				.from(Person.T, "p")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand().isSourceSet_(Person.T)
			.hasValues(3)
				.whereElementAt(0).isTupleComponent_(0)
				.whereElementAt(1).isStaticValue_(99L)
				.whereElementAt(2).isStaticValue_("constantString")
		;
		// @formatter:on
	}

	@Test
	public void selectingLocalizedValue() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.select().localize("pt").property("p", "localizedString")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand().isSourceSet_(Person.T)
			.hasValues(1)
				.whereElementAt(0)
					.hasType(QueryFunctionValue.T)
					.whereProperty("queryFunction")
						.hasType(Localize.T)
						.whereProperty("localizedStringOperand").isPropertyOperand("localizedString").close()
						.whereProperty("locale").is_("pt");
		// @formatter:on
	}

	@Test
	public void selectingMapKey() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.class, "p")
				.join("p", "companyMap", "cs")
				.select().mapKey("cs")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand().hasType(MapJoin.T).close()
			.hasValues(1)
				.whereElementAt(0).isTupleComponent_(1);
		// @formatter:on
	}

}
