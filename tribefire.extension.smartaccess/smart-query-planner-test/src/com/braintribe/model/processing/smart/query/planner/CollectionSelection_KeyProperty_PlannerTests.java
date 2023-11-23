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
import static com.braintribe.model.generic.GenericEntity.id;
import static com.braintribe.model.generic.GenericEntity.partition;
import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdA;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.testing.category.KnownIssue;

/**
 * 
 */
public class CollectionSelection_KeyProperty_PlannerTests extends AbstractSmartQueryPlannerTests {

	@Test
	@Category(KnownIssue.class)
	public void EXPECTED_TO_FAIL_simpleSetQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "keyCompanySetA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					/* TODO add correct query assertion - current is doing: where (PersonA1.companyNameSetA = CompanyA0.nameA), which is wrong */
					.whereSelection(5)
						.whereElementAt(0).isPropertyOperand(globalId).whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(1).isPropertyOperand(id).whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(2).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(3).isPropertyOperand(partition).whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(4).isSourceOnlyPropertyOperand().whereSource().isJoin("companyNameSetA").whereSource().isFrom(PersonA.T)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(Company.T)
		;
		// @formatter:on

		Assert.fail("Fix the plan to handle the join with a simple collection correctly");
	}

	@Test
	public void simpleSetQuery_ExternalDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "keyCompanySetExternalDqj")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isSourceOnlyPropertyOperand().whereSource().isJoin("companyNameSetA").whereSource().isFrom(PersonA.T)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(4)
							.whereElementAt(0).isPropertyOperand(globalId).whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(1).isPropertyOperand(id).whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(2).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(3).isPropertyOperand(partition).whereSource().isFrom(CompanyA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(Company.T)
		;
		// @formatter:on
	}

	/**
	 * Delegating the collection condition does not work when doing internal DQJ. All these EXPECTED_TO_FAIL tests share
	 * that.
	 */
	@Category(KnownIssue.class)
	@Test
	public void EXPECTED_TO_FAIL_queryWithDelegatableSetCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.entity(company(99L)).in().property("p", "keyCompanySetA")
				.done();
		// @formatter:on

		runTest(selectQuery);

//		// @formatter:off
//		assertQueryPlan()
//		.hasType(Projection.T)
//			.whereProperty("operand")
//				.isDelegateQuerySet("accessA")
//					.whereProperty("scalarMappings").isListWithSize(1).close()
//					.whereDelegateQuery()
//						.whereSelection(1)
//							.whereElementAt(0).isPropertyOperand("nameA").close()
//						.whereFroms(2)
//							.whereElementAt(0).isFrom(CompanyA.T).close()
//							.whereElementAt(1).isFrom(PersonA.T).close()
//						.whereCondition().isConjunction(2)
//							.whereElementAt(0).isValueComparison(Operator.equal)
//								.whereProperty("leftOperand").isReference(CompanyA.T.getName(), 99L, accessIdA) .close()
//								.whereProperty("rightOperand").isSourceOnlyPropertyOperand().close(2)
//							.whereElementAt(1).isValueComparison(Operator.equal)
//								.whereProperty("leftOperand").isPropertyOperand(PersonA.companyNameSetA).close()
//								.whereProperty("rightOperand").isPropertyOperand(CompanyA.nameA).close()
//					.endQuery()
//			.close()
//			.whereProperty("values").isListWithSize(1).whereElementAt(0).hasType(TupleComponent.T)
//		;
//		// @formatter:on

		Assert.fail("This case does not work at all!!!");
	}

	@Test
	public void queryWithDelegatableSetCondition_ExternalDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.entity(company(99L)).in().property("p", "keyCompanySetExternalDqj")
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
						.whereProperty("scalarMappings").isListWithSize(1).close()
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isPropertyOperand("nameA").close()
							.whereCondition().isConjunction(1)
								.whereElementAt(0).isValueComparison(Operator.equal)
									.whereProperty("leftOperand").isReference_(CompanyA.T, 99L, accessIdA)
									.whereProperty("rightOperand").isSourceOnlyPropertyOperand().close(2)
						.endQuery()
					.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
						.whereProperty("scalarMappings").isListWithSize(2).close()
						.whereDelegateQuery()
							.whereSelection(2)
								.whereElementAt(0).isPropertyOperand("nameA").close()
								.whereElementAt(1).isSourceOnlyPropertyOperand().whereSource().isJoin("companyNameSetA")
							.whereCondition()
								.isConjunction(1)
									.whereElementAt(0).isDisjunction(1)
										.whereElementAt(0).isValueComparison(Operator.equal)
											.whereProperty("leftOperand").isSourceOnlyPropertyOperand().whereSource().isJoin("companyNameSetA")
						.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1).whereElementAt(0).hasType(TupleComponent.T)
		;
		// @formatter:on
	}

	/** @see #EXPECTED_TO_FAIL_queryWithDelegatableSetCondition() */
	@Category(KnownIssue.class)
	@Test
	public void EXPECTED_TO_FAIL_setQueryWithDelegatableSetCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.select("p", "keyCompanySetA")
				.where()
					.entity(company(99L)).in().property("p", "keyCompanySetA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		Assert.fail("This case does not work at all!!!");

//		// @formatter:off
//		assertQueryPlan()
//		.hasType(Projection.T)
//			.whereProperty("operand")
//			.isDelegateQuerySet("accessA")
//				.whereProperty("scalarMappings").isListWithSize(6).close()
//				.whereDelegateQuery()
//					.whereSelection(6)
//						.whereElementAt(0).isPropertyOperand(globalId).whereSource().isFrom(CompanyA.T).close(2)
//						.whereElementAt(1).isPropertyOperand(id).whereSource().isFrom(CompanyA.T).close(2)
//						.whereElementAt(2).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
//						.whereElementAt(3).isPropertyOperand(partition).whereSource().isFrom(CompanyA.T).close(2)
//						.whereElementAt(4).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
//						.whereElementAt(5).isSourceOnlyPropertyOperand().whereSource().isJoin("companyNameSetA")
//					.whereCondition().isConjunction(3)
//						.whereElementAt(0).isValueComparison(Operator.equal)
//							.whereProperty("leftOperand").isReference(CompanyA.T.getName(), 99L, accessIdA) .close()
//							.whereProperty("rightOperand").isSourceOnlyPropertyOperand().whereSource().isFrom(CompanyA.T).close(3)
//						.whereElementAt(1).isValueComparison(Operator.equal)
//							// This is just terribly wrong
//							.whereProperty("leftOperand").isPropertyOperand(PersonA.companyNameSetA).whereSource().isFrom(PersonA.T).close(2)
//							.whereProperty("rightOperand").isPropertyOperand(CompanyA.nameA).whereSource().isFrom(Company.T).close(2)
//						.whereElementAt(2).isValueComparison(Operator.equal)
//							.whereProperty("leftOperand").isPropertyOperand(PersonA.nameA).whereSource().isFrom(PersonA.T).close(2)
//							.whereProperty("rightOperand").isPropertyOperand(CompanyA.nameA).whereSource().isFrom(Company.T).close(2)
//				.endQuery()
//			.close()
//			.whereProperty("values").isListWithSize(2)
//				.whereElementAt(0).hasType(TupleComponent.T).close()
//				.whereElementAt(1).hasType(QueryFunctionValue.T)
//					.whereProperty("queryFunction").isAssembleEntity(Company.T)
//		;
//		// @formatter:on
	}

	@Test
	public void setQueryWithDelegatableSetCondition_ExternalDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.select("p", "keyCompanySetExternalDqj")
				.where()
					.entity(company(99L)).in().property("p", "keyCompanySetExternalDqj")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet")
						.isDelegateQuerySet("accessA")
							.whereProperty("scalarMappings").isListWithSize(1).close()
							.whereDelegateQuery()
								.whereSelection(1)
									.whereElementAt(0).isPropertyOperand("nameA").close()
								.whereCondition().isConjunction(1)
									.whereElementAt(0).isValueComparison(Operator.equal)
										.whereProperty("leftOperand").isReference_(CompanyA.T, 99L, accessIdA)
										.whereProperty("rightOperand").isSourceOnlyPropertyOperand().close(2)
							.endQuery()
						.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessA")
							.whereProperty("scalarMappings").isListWithSize(3).close()
							.whereDelegateQuery()
								.whereSelection(3)
									.whereElementAt(0).isPropertyOperand("nameA").close()
									.whereElementAt(1).isSourceOnlyPropertyOperand().whereSource().isJoin("companyNameSetA").close(2)
									.whereElementAt(2).isSourceOnlyPropertyOperand().whereSource().isJoin("companyNameSetA")
							.endQuery()
						.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
						.whereProperty("scalarMappings").isListWithSize(4).close()
						.whereDelegateQuery()
							.whereSelection(4)
								.whereElementAt(0).isPropertyOperand(globalId).close()
								.whereElementAt(1).isPropertyOperand(id).close()
								.whereElementAt(2).isPropertyOperand("nameA").close()
								.whereElementAt(3).isPropertyOperand(partition).close()
							.whereCondition()
								.isConjunction(1)
									.whereElementAt(0).isDisjunction(1)
										.whereElementAt(0).isValueComparison(Operator.equal).whereProperty("leftOperand").isPropertyOperand("nameA").close(3)
						.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(TupleComponent.T).close()
				.whereElementAt(1).hasType(QueryFunctionValue.T)
					.whereProperty("queryFunction").isAssembleEntity(Company.T)
		;
		// @formatter:on
	}

	/** @see #EXPECTED_TO_FAIL_queryWithDelegatableSetCondition() */
	@Category(KnownIssue.class)
	@Test
	public void EXPECTED_TO_FAIL_simpleListQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "keyCompanyListA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		Assert.fail("This case does not work at all!!!");
	}

	@Test
	public void simpleListQuery_ExternalDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "keyCompanyListExternalDqj")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isSourceOnlyPropertyOperand().whereSource().isJoin("companyNameListA").whereSource().isFrom(PersonA.T)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(4)
							.whereElementAt(0).isPropertyOperand(globalId).whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(1).isPropertyOperand(id).whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(2).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(3).isPropertyOperand(partition).whereSource().isFrom(CompanyA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(Company.T)
		;
		// @formatter:on
	}

	/** @see #EXPECTED_TO_FAIL_queryWithDelegatableSetCondition() */
	@Category(KnownIssue.class)
	@Test
	public void EXPECTED_TO_FAIL_listQueryWithListIndex() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "keyCompanyListA", "e")
				.select().listIndex("e")
				.select("e")
				.done();
		// @formatter:on

		runTest(selectQuery);

		Assert.fail("This case does not work at all!!!");
	}

	@Test
	public void listQueryWithListIndex_ExternalDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "keyCompanyListExternalDqj", "e")
				.select().listIndex("e")
				.select("e")
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
						.whereProperty("scalarMappings").isListWithSize(4).close()
						.whereDelegateQuery()
							.whereSelection(4)
								.whereElementAt(0).isPropertyOperand(globalId).close()
								.whereElementAt(1).isPropertyOperand(id).close()
								.whereElementAt(2).isPropertyOperand("nameA").close()
								.whereElementAt(3).isPropertyOperand(partition).close()
						.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
						.whereProperty("scalarMappings").isListWithSize(2).close()
						.whereDelegateQuery()
							.whereSelection(2)
								.whereElementAt(0).isSourceOnlyPropertyOperand().whereSource().isJoin("companyNameListA").close(2)
								.whereElementAt(1).isListIndexOnJoin("companyNameListA")
							.whereCondition()
								.isSingleOperandConjunctionAndOperand()
									.isDisjunction(1)
										.whereElementAt(0).isValueComparison(Operator.equal)
											.whereProperty("leftOperand").isSourceOnlyPropertyOperand().whereSource().isJoin("companyNameListA")
						.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(TupleComponent.T).close()
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(Company.T)
		;
		// @formatter:on
	}

	/** @see #EXPECTED_TO_FAIL_queryWithDelegatableSetCondition() */
	@Category(KnownIssue.class)
	@Test
	public void EXPECTED_TO_FAIL_simpleMapQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "keyFriendEmployerA", "e")
				.select().mapKey("e")
				.select("e")
				.done();
		// @formatter:on

		runTest(selectQuery);

		Assert.fail("This case does not work at all!!!");
	}

	@Test
	public void simpleMapQuery_ExternalDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "keyFriendEmployerExternalDqj", "e")
				.select().mapKey("e")
				.select("e")
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
						.whereProperty("scalarMappings").isListWithSize(4).close()
						.whereDelegateQuery()
							.whereSelection(4)
								.whereElementAt(0).isPropertyOperand(globalId).close()
								.whereElementAt(1).isPropertyOperand(id).close()
								.whereElementAt(2).isPropertyOperand("nameA").close()
								.whereElementAt(3).isPropertyOperand(partition).close()
						.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
						.whereProperty("scalarMappings").isListWithSize(2).close()
						.whereDelegateQuery()
							.whereSelection(2)
								.whereElementAt(0).isSourceOnlyPropertyOperand().whereSource().isJoin("keyFriendEmployerNameA").close(2)
								.whereElementAt(1).isMapKeyOnJoin("keyFriendEmployerNameA")
							.whereCondition()
								.isSingleOperandConjunctionAndOperand()
									.isDisjunction(1)
										.whereElementAt(0).isValueComparison(Operator.equal)
											.whereProperty("leftOperand").isSourceOnlyPropertyOperand().whereSource().isJoin("keyFriendEmployerNameA")
						.endQuery()
					.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(TupleComponent.T).close()
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(Company.T)
		;
		// @formatter:on
	}
}
