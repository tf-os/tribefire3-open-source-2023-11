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
package com.braintribe.model.processing.smart.query.planner2.unmapped;

import static com.braintribe.model.generic.GenericEntity.globalId;
import static com.braintribe.model.generic.GenericEntity.id;
import static com.braintribe.model.generic.GenericEntity.partition;
import static com.braintribe.model.processing.query.smart.test2.unmapped.model.accessA.SimpleUnmappedA.mappedName;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.processing.query.smart.test2._common.SmartModelTestSetup;
import com.braintribe.model.processing.query.smart.test2.unmapped.UnmappedSmartSetup;
import com.braintribe.model.processing.query.smart.test2.unmapped.model.accessA.SimpleUnmappedA;
import com.braintribe.model.processing.query.smart.test2.unmapped.model.smart.UnmappedPropertySmart;
import com.braintribe.model.processing.query.smart.test2.unmapped.model.smart.UnmappedSubTypeSmart;
import com.braintribe.model.processing.query.smart.test2.unmapped.model.smart.UnmappedTopLevelTypeSmart;
import com.braintribe.model.processing.smart.query.planner.SingleSource_PrimitiveSelection_PlannerTests;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner2._base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.testing.category.KnownIssue;

/**
 * @author peter.gazdik
 */
public class Unmapped_PlannerTest extends AbstractSmartQueryPlannerTests {

	@Override
	protected SmartModelTestSetup getSmartModelTestSetup() {
		return UnmappedSmartSetup.UNMAPPED_SETUP;
	}

	@Test
	public void selectUnmappedTypeProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("e", mappedName)
				.from(SimpleUnmappedA.T, "e")
				.done();
		// @formatter:on

		runTest(selectQuery);

		assertQueryPlan(1).isDelegateQueryAsIs("accessA");
	}

	@Test
	public void selectMappedAndUnmappedProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("e", UnmappedPropertySmart.mappedSubName)
				.select("e", UnmappedPropertySmart.unmappedSubName)
				.from(UnmappedPropertySmart.T, "e")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1)
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand(mappedName).whereSource().isFrom(SimpleUnmappedA.T).close(2)
				.endQuery()				
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(0)
				.whereElementAt(1).isStaticValue_(null)
		;
		// @formatter:on
	}

	@Test
	public void selectMappedTypeWithUnmappedSubType() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("e")
				.from(UnmappedPropertySmart.T, "e")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(4)
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(4)
						.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(SimpleUnmappedA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(id).whereSource().isFrom(SimpleUnmappedA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(mappedName).whereSource().isFrom(SimpleUnmappedA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(SimpleUnmappedA.T).close(2)
				.endQuery()				
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isQueryFunctionValueAndQf()
					.isAssembleEntity(UnmappedPropertySmart.T)
		;
		// @formatter:on
	}

	@Test(expected = SmartQueryPlannerException.class)
	public void selectPropertyOfUnmappedType() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("e")
				.from(UnmappedSubTypeSmart.T, "e")
				.done();
		// @formatter:on

		runTest(selectQuery);

	}

	/** Similar to {@link #leftJoinWithUnmappedProperty()}, but an inner join which causes the entire query plan to be an empty one. */
	@Category(KnownIssue.class)
	@Test
	public void EXPECTED_TO_FAIL_joinWithUnmappedProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("e")
				.select("ue")
				.from(UnmappedPropertySmart.T, "e")
					.join("e", UnmappedPropertySmart.unmappedEntity, "ue")
				.done();
		// @formatter:on

		runTest(selectQuery);

		assertQueryPlan(0);
	}

	/**
	 * There is a bug that this doesn't work as the planner goes on to create an {@link EntitySourceNode} for the joined property and at that moment
	 * it fails because the property type - {@link UnmappedTopLevelTypeSmart} - isn't mapped. This must be fixed by pre-processing the query to
	 * eliminate unmapped joins and adjust selects/conditions/orderings accordingly.
	 * 
	 * This differs from {@link SingleSource_PrimitiveSelection_PlannerTests#selectUnmappedProperty_Entity()} in that the property type itself is not
	 * mapped. Also, the join we are doing here is an inner join.
	 */
	@Test
	public void leftJoinWithUnmappedProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("e")
				.select("ue")
				.from(UnmappedPropertySmart.T, "e")
					.leftJoin("e", UnmappedPropertySmart.unmappedEntity, "ue")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(4)
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(4)
						.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(SimpleUnmappedA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(id).whereSource().isFrom(SimpleUnmappedA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(mappedName).whereSource().isFrom(SimpleUnmappedA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(SimpleUnmappedA.T).close(2)
				.endQuery()				
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0)
					.isQueryFunctionValueAndQf()
						.isAssembleEntity(UnmappedPropertySmart.T)
					.close(2)
				.whereElementAt(1).isStaticValue_(null)
		;
		// @formatter:on
	}
}
