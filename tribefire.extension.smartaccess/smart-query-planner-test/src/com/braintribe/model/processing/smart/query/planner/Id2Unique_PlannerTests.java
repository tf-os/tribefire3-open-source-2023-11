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
package com.braintribe.model.processing.smart.query.planner;

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdA;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.Id2UniqueEntityA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.processing.smart.query.planner.base.TestAccess;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;

/**
 * 
 * Special set of tests for cases when id of a smart entity is mapped to a non-id property in delegate.
 */
public class Id2Unique_PlannerTests extends AbstractSmartQueryPlannerTests {

	@Before
	public void prepareUniqueToIdMapping() {
		TestAccess.idMapping = Arrays.<Object> asList("99", 99L);
	}

	@Test
	public void propertyEntityCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.property("p", "id2UniqueEntityA").eq().entity(id2UniqueEntity("99"))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("nameA")
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.equal)
							.whereProperty("leftOperand").isSourceOnlyPropertyOperand().whereSource().isJoin("id2UniqueEntityA").close(2)
							.whereProperty("rightOperand").isReference_(Id2UniqueEntityA.T, 99L, accessIdA)
				.endQuery()
			;
		// @formatter:on
	}

	@Test
	public void collectionEntityCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.property("p", "id2UniqueEntitySetA").contains().entity(id2UniqueEntity("99"))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("nameA")
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.contains)
							.whereProperty("leftOperand").isPropertyOperand("id2UniqueEntitySetA").close()
							.whereProperty("rightOperand").isReference_(Id2UniqueEntityA.T, 99L, accessIdA) 
				.endQuery()
			;
		// @formatter:on
	}

}
