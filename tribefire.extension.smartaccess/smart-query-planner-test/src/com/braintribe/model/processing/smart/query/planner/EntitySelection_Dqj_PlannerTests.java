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

import static com.braintribe.model.generic.GenericEntity.globalId;
import static com.braintribe.model.generic.GenericEntity.partition;
import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdA;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonB;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.testing.category.KnownIssue;

/**
 * 
 */
public class EntitySelection_Dqj_PlannerTests extends AbstractSmartQueryPlannerTests {

	/**
	 * This is a typical case for a query to load multiple properties at once, as done by GME.
	 */
	@Test
	public void selectNormalAndKpaProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("p", "nameA")
				.select("p", "keyCompanyA")
				.from(SmartPersonA.T, "p")
				.where()
					.entity("p").eq().entityReference(reference(SmartPersonA.class, accessIdA, 99L))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(5)
						.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
					.whereCondition()
						.isConjunction(2)
							.whereElementAt(0).isValueComparison(Operator.equal)
								.whereLeftOperand().isSourceOnlyPropertyOperand().close()
								.whereRightOperand().isReference_(PersonA.T, 99L, accessIdA)
							.close()
							.whereElementAt(1).isValueComparison(Operator.equal)
								.whereLeftOperand().isPropertyOperand(PersonA.companyNameA).whereSource().isFrom(PersonA.T).close(2)
								.whereRightOperand().isPropertyOperand(CompanyA.nameA).whereSource().isFrom(CompanyA.T).close(2)
						.close()
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(4)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(Company.T)
		;
		// @formatter:on
	}

	/**
	 * This is a typical case for a query to load multiple properties at once, as done by GME.
	 */
	@Test
	public void selectNormalAndKpaExternalDqjProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.select("p", "keyCompanyExternalDqj")
				.where()
					.entity("p").eq().entityReference(reference(SmartPersonA.class, accessIdA, 99L))
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
						.whereSelection(2)
							.whereElementAt(0).isPropertyOperand("companyNameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(1).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
						.whereCondition()
							.isConjunction(1).whereElementAt(0)
								.isValueComparison(Operator.equal)
									.whereLeftOperand().isSourceOnlyPropertyOperand().close()
									.whereRightOperand().isReference_(PersonA.T, 99L, accessIdA)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(4)
							.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(CompanyA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(1)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(Company.T)
		;
		// @formatter:on
	}

	@Test
	public void selectNormalAndKpa_ExtraConditionOnBothSources() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "keyCompanyA", "c")
				.select("p", "nameA")
				.select("c")
				.where()
					.property("p", "nameA").eq().property("c", "nameA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(5)
						.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
					.whereCondition()
						.isConjunction(2)
							.whereElementAt(0).isValueComparison(Operator.equal)
								.whereLeftOperand().isPropertyOperand(PersonA.nameA).whereSource().isFrom(PersonA.T).close(2)
								.whereRightOperand().isPropertyOperand(CompanyA.nameA).whereSource().isFrom(CompanyA.T).close(2)
							.close()
							.whereElementAt(1).isValueComparison(Operator.equal)
								.whereLeftOperand().isPropertyOperand(PersonA.companyNameA).whereSource().isFrom(PersonA.T).close(2)
								.whereRightOperand().isPropertyOperand(CompanyA.nameA).whereSource().isFrom(CompanyA.T).close(2)
						.close()
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(4)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(Company.T)
		;
		// @formatter:on
	}

	/** There is a bug where the planner grouped both sources together and forgot about the DQJ. Will be fixed one day, right?  */
	@Test
	@Category(KnownIssue.class)
	public void TODO_FIX_selectNormalAndKpa_ExtraConditionOnBothSources_ExternalDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "keyCompanyExternalDqj", "c")
				.select("p", "nameA")
				.select("c")
				.where()
					.property("p", "nameA").eq().property("c", "nameA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		Assert.fail("TODO THIS MUST BE A DQJ!!!");
	}

	@Test
	public void simpleInverseKeyPropertyJoin() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "inverseKeyItem")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isPropertyOperand("nickNameX").whereSource().isFrom(PersonA.T).close(2)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(6)
							.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).castedToString().isPropertyOperand("itemType").whereSource().isFrom(ItemB.T).close(3)
							.whereElementAt(++i).isPropertyOperand("nameB").whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("singleOwnerName").whereSource().isFrom(ItemB.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T)
		;
		// @formatter:on
	}

	/**
	 * There was a bug that we did not join these type of structure correctly...
	 */
	@Test
	public void simpleInverseKeyPropertyJoin_FollowedByNormalJoin() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "inverseKeyItem.ownerB")
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
							.whereElementAt(0).isPropertyOperand("nickNameX").whereSource().isFrom(PersonA.T).close(2)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0)
							.isFrom(ItemB.T).whereProperty("joins").isSetWithSize(1).whereFirstElement().isJoin("ownerB")
						.whereSelection(8)
							.whereElementAt(i=0).isPropertyOperand("singleOwnerName").whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("ageB").whereSource().isJoin("ownerB").close(2)
							.whereElementAt(++i).isPropertyOperand("birthDate").whereSource().isJoin("ownerB").close(2)
							.whereElementAt(++i).isPropertyOperand("companyNameB").whereSource().isJoin("ownerB").close(2)
							.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isJoin("ownerB").close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isJoin("ownerB").close(2)
							.whereElementAt(++i).isPropertyOperand("nameB").whereSource().isJoin("ownerB").close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isJoin("ownerB").close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonB.T)
		;
		// @formatter:on
	}

}
