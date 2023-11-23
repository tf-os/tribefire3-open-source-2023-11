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
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ReaderA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartManualA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartReaderA;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;

/**
 * Tests for the "weak" property type. This means we have some unmapped type A as type of our property, but we only want to allow it's sub
 * types (SubA) as values. We therefore use SubA in the mappings, and end up with query plans which only consider SubA, and not the entire A
 * hierarchy.
 */
public class UnmappedPropertyType_Weak_PlannerTests extends AbstractSmartQueryPlannerTests {

	// ########################################################
	// ## . . . . . IKPA defined on unmapped types . . . . . ##
	// ########################################################

	@Test
	public void selectUnmappedTypeProperty_Weak() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "weakFavoriteManual")
				.from(SmartReaderA.T, "r")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isPropertyOperand("favoriteManualTitle").whereSource().isFrom(ReaderA.T).close(2)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessS")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SmartManualA.T)
						.whereSelection(8)
							.whereElementAt(i=0).isPropertyOperand("author").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("isbn").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("numberOfPages").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("smartManualString").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("title").whereSource().isFrom(SmartManualA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartManualA.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void selectUnmappedTypeProperty_Weak_Set() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "weakFavoriteManuals")
				.from(SmartReaderA.T, "r")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isSourceOnlyPropertyOperand().whereSource().isJoin("favoriteManualTitles").whereSource().isFrom(ReaderA.T)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessS")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SmartManualA.T)
						.whereSelection(8)
							.whereElementAt(i=0).isPropertyOperand("author").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("isbn").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("numberOfPages").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("smartManualString").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("title").whereSource().isFrom(SmartManualA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartManualA.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void selectUnmappedTypeProperty_WeakInverse_Set() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "weakInverseFavoriteManuals")
				.from(SmartReaderA.T, "r")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isPropertyOperand("name").whereSource().isFrom(ReaderA.T)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessS")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SmartManualA.T)
						.whereSelection(8)
							.whereElementAt(i=0).isPropertyOperand("author").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("isbn").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("numberOfPages").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("smartManualString").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("title").whereSource().isFrom(SmartManualA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartManualA.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void selectUnmappedTypePropertyWithEntityCondition_Weak() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "weakFavoriteManual")
				.from(SmartReaderA.T, "r")
				.where()
					.property("r", "weakFavoriteManual").eq().entityReference(reference(SmartManualA.class, SmartMappingSetup.accessIdA, 99L))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQueryJoin_Inner()
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessS")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SmartManualA.T)
						.whereSelection(8)
							.whereElementAt(i=0).isPropertyOperand("author").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("isbn").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("numberOfPages").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("smartManualString").whereSource().isFrom(SmartManualA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("title").whereSource().isFrom(SmartManualA.T).close(2)
						.whereCondition().isConjunction(1)
							.whereElementAt(0).isValueComparison(Operator.equal)
								.whereProperty("leftOperand").close()
								.whereProperty("rightOperand").isReference_(SmartManualA.T, 99L, accessIdA)
					.endQuery()
				.close()
				.whereProperty("querySet")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("favoriteManualTitle").whereSource().isFrom(ReaderA.T).close(2)
				.endQuery()
			.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartManualA.T).close(2)
		;
		// @formatter:on
	}

}
