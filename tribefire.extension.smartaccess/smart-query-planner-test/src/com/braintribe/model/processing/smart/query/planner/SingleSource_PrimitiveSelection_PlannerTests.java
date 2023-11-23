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

import org.junit.Test;

import com.braintribe.model.accessdeployment.smart.meta.conversion.EnumToSimpleValue;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.processing.smart.query.planner2.unmapped.Unmapped_PlannerTest;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.queryplan.filter.Equality;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.smartqueryplan.queryfunctions.ResolveId;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.model.smartqueryplan.value.ConvertedValue;

/**
 * 
 */
public class SingleSource_PrimitiveSelection_PlannerTests extends AbstractSmartQueryPlannerTests {

	@Test
	public void simpleQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(TupleComponent.T)
		;
		// @formatter:on
	}

	@Test
	public void simpleQuery_Enum() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartItem.T, "i")
				.select("i", "itemType")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessB")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).castedToString().isPropertyOperand("itemType")
				.endQuery()
				.whereProperty("delegateAccess").isDelegateAccess("accessB").close()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(ConvertedValue.T)
					.whereProperty("conversion")
					.hasType(EnumToSimpleValue.T)
					.whereProperty("valueMappings").isNotNull()
		;
		// @formatter:on
	}

	@Test
	public void simpleDqjQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.select("p", "smartParentB.companyNameB")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereProperty("scalarMappings").isListWithSize(2).close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
					.whereProperty("scalarMappings").isListWithSize(2).close() // nameB, companyNameB
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
		;
		// @formatter:on
	}

	@Test
	public void simpleQueryWithCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nickName")
				.where()
					.property("p", "nameA").eq("nameA1")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereProperty("selections")
						.whereElementAt(0).isPropertyOperand("nickNameX")
					.whereCondition()
						.hasType(Conjunction.T)
						.whereProperty("operands")
							.whereElementAt(0).hasType(ValueComparison.T)
								.whereProperty("leftOperand").isPropertyOperand("nameA")
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(TupleComponent.T)
		;
		// @formatter:on
	}

	@Test
	public void simpleQueryWithConditionNonSelectedProps() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.property("p", "companyNameA").eq("companyNameA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereProperty("selections")
						.whereElementAt(0).isPropertyOperand("nameA").close()
					.whereCondition()
						.hasType(Conjunction.T)
						.whereProperty("operands")
							.whereElementAt(0).hasType(ValueComparison.T)
								.whereProperty("leftOperand").isPropertyOperand("companyNameA")
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(TupleComponent.T)
		;
		// @formatter:on
	}

	@Test
	public void selectUnmappedProperty_Simple() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.select("p", "unmappedString")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereProperty("selections")
						.whereElementAt(0).isPropertyOperand("nameA").close()
					.withNoRestriction()
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(TupleComponent.T).close()
				.whereElementAt(1).isStaticValue_(null)
		;
		// @formatter:on
	}

	/** @see Unmapped_PlannerTest#leftJoinWithUnmappedProperty() */
	@Test
	public void selectUnmappedProperty_Entity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("p", "nameA")
				.select("p", "unmappedParent")
				.from(SmartPersonA.T, "p")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereProperty("selections")
						.whereElementAt(0).isPropertyOperand("nameA").close()
					.withNoRestriction()
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(TupleComponent.T).close()
				.whereElementAt(1).isStaticValue_(null)
		;
		// @formatter:on
	}

	/** This should throw an Exception as conditions are not supported on unmapped properties. */
	@Test(expected = SmartQueryPlannerException.class)
	public void conditionOnUnmappedProperty_Entity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.property("p", "unmappedParent").ne(null)
				.done();
		// @formatter:on

		runTest(selectQuery);
	}

	@Test
	public void simpleQueryWithNonDelegatableCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.select("p", "smartParentB.nameB")
				.where()
					.property("p", "nameA").eq().property("p", "smartParentB.nameB")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(FilteredSet.T)
				.whereProperty("operand")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet")
						.isDelegateQuerySet("accessA")
						.whereProperty("scalarMappings").isListWithSize(2).close() // nameA, parentB
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessB")
						.whereProperty("scalarMappings").isListWithSize(1).close() // nameB
					.close()
				.close()
				.whereProperty("filter").hasType(Equality.T).close()
			.close()
			.whereProperty("values").isListWithSize(2)
		;
		// @formatter:on
	}

	/**
	 * Almost the same as previous one, but we are selecting just one property.
	 */
	@Test
	public void simpleQueryWithNonDelegatableConditionNonSelectedProps() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.property("p", "nameA").eq().property("p", "smartParentB.nameB")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(FilteredSet.T)
				.whereProperty("operand")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet")
						.isDelegateQuerySet("accessA")
						.whereProperty("scalarMappings").isListWithSize(2).close() // nameA, parentB
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessB")
						.whereProperty("scalarMappings").isListWithSize(1).close() // nameB
					.close()
				.close()
				.whereProperty("filter").hasType(Equality.T).close()
			.close()
			.whereProperty("values").isListWithSize(1)
		;
		// @formatter:on
	}

	@Test
	public void queryingEntityIdWhenMapped() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "id")
				.select("p", "nameA")
				.where()
					.entity("p").eq().entity(person(1L))
				.done();
		// @formatter:on

		Source source = selectQuery.getFroms().get(0);
		selectQuery.getSelections().add(entityId(source));

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(2).close() // nameA
			.close()
			.whereProperty("values")
				.isListWithSize(3)
					.whereElementAt(0).isTupleComponent_(0) // id
					.whereElementAt(1).isTupleComponent_(1) // nameA
					.whereElementAt(0).isTupleComponent_(0) // id
		;
		// @formatter:on
	}

	private ResolveId entityId(Source source) {
		ResolveId result = ResolveId.T.createPlain();
		result.setSource(source);

		return result;
	}

}
