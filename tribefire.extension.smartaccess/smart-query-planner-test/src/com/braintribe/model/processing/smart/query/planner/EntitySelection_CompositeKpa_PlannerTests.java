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

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeKpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeKpaEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;

/**
 * 
 */
public class EntitySelection_CompositeKpa_PlannerTests extends AbstractSmartQueryPlannerTests {

	// TODO FIX corresponding evaluation tests
	@Test
	public void selectCompositeKpaEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.select("p", "compositeKpaEntity")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(8)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA") 
				.whereDelegateQuery()
					.whereFroms(2)
						.whereElementAt(0).isFrom(CompositeKpaEntityA.T).close()
						.whereElementAt(1).isFrom(PersonA.T).close()
					.whereSelection(8)
						.whereElementAt(i=0).isPropertyOperand("description").close()
						.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).close()
						.whereElementAt(++i).isPropertyOperand("id").close()
						.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).close()
						.whereElementAt(++i).isPropertyOperand("personCompanyName").close()
						.whereElementAt(++i).isPropertyOperand("personId").close()
						.whereElementAt(++i).isPropertyOperand("personName").close()
						.whereElementAt(++i).isPropertyOperand("nameA").close()
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(8).close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(7)
				.whereElementAt(1).hasType(QueryFunctionValue.T)
					.whereProperty("queryFunction").isAssembleEntity(CompositeKpaEntity.T)
		;
		// @formatter:on
	}

	@Test
	public void selectCompositeKpaEntity_ExternalDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.select("p", "compositeKpaEntityExternalDqj")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(8)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
						.whereSelection(4)
							.whereElementAt(0).isPropertyOperand("compositeCompanyName").close()
							.whereElementAt(1).isPropertyOperand("compositeId").close()
							.whereElementAt(2).isPropertyOperand("compositeName").close()
							.whereElementAt(3).isPropertyOperand("nameA").close()
					.endQuery()
					.whereProperty("scalarMappings").isListWithSize(4)
						.whereElementAt(0).isScalarMappingAndValue(0).isTupleComponent_(0).close()
						.whereElementAt(1).isScalarMappingAndValue(1).isTupleComponent_(1).close()
						.whereElementAt(2).isScalarMappingAndValue(2).isTupleComponent_(2).close()
						.whereElementAt(3).isScalarMappingAndValue(3).isTupleComponent_(3).close()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(CompositeKpaEntityA.T)
						.whereSelection(7)
							.whereElementAt(i=0).isPropertyOperand("description").close()
							.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).close()
							.whereElementAt(++i).isPropertyOperand("id").close()
							.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).close()
							.whereElementAt(++i).isPropertyOperand("personCompanyName").close()
							.whereElementAt(++i).isPropertyOperand("personId").close()
							.whereElementAt(++i).isPropertyOperand("personName").close()
					.endQuery()
					.whereProperty("scalarMappings").isListWithSize(7)
						.whereElementAt(0).isScalarMappingAndValue(4).isTupleComponent_(0).close()
						.whereElementAt(1).isScalarMappingAndValue(5).isTupleComponent_(1).close()
						.whereElementAt(2).isScalarMappingAndValue(6).isTupleComponent_(2).close()
						.whereElementAt(3).isScalarMappingAndValue(7).isTupleComponent_(3).close()
						.whereElementAt(4).isScalarMappingAndValue(0).isTupleComponent_(4).close()
						.whereElementAt(5).isScalarMappingAndValue(1).isTupleComponent_(5).close()
						.whereElementAt(6).isScalarMappingAndValue(2).isTupleComponent_(6).close()
					.close()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(3)
				.whereElementAt(1).hasType(QueryFunctionValue.T)
					.whereProperty("queryFunction").isAssembleEntity(CompositeKpaEntity.T)
		;
		// @formatter:on
	}	

	@Test
	public void conditionOnCompositeKpaEntity_ExternalDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.property("p", "compositeKpaEntityExternalDqj").eq().entityReference(reference(CompositeKpaEntity.class, accessIdA, 99L))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(4)
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(CompositeKpaEntityA.T)
						.whereSelection(3)
							.whereElementAt(0).isPropertyOperand("personCompanyName").close()
							.whereElementAt(1).isPropertyOperand("personId").close()
							.whereElementAt(2).isPropertyOperand("personName").close()
						.whereCondition()
							.isConjunction(1).whereElementAt(0)
								.isValueComparison(Operator.equal)
								.whereProperty("leftOperand").isSourceOnlyPropertyOperand().whereSource().isFrom(CompositeKpaEntityA.T).close(2)
								.whereProperty("rightOperand").isReference_(CompositeKpaEntityA.T, 99L, accessIdA)
					.endQuery()
					.whereProperty("scalarMappings").isListWithSize(3).close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
						.whereSelection(4)
							.whereElementAt(0).isPropertyOperand("compositeCompanyName").close()
							.whereElementAt(1).isPropertyOperand("compositeId").close()
							.whereElementAt(2).isPropertyOperand("compositeName").close()
							.whereElementAt(3).isPropertyOperand("nameA").close()
					.endQuery()
					.whereProperty("scalarMappings").isListWithSize(4)
						.whereElementAt(0).isScalarMappingAndValue(0).isTupleComponent_(0).close()
						.whereElementAt(1).isScalarMappingAndValue(1).isTupleComponent_(1).close()
						.whereElementAt(2).isScalarMappingAndValue(2).isTupleComponent_(2).close()
						.whereElementAt(3).isScalarMappingAndValue(3).isTupleComponent_(3).close()
					.close()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(3)
		;
		// @formatter:on
	}
}
