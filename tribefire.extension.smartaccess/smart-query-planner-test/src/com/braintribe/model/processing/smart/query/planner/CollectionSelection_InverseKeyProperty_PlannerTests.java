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
import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdB;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeIkpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeIkpaEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
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
public class CollectionSelection_InverseKeyProperty_PlannerTests extends AbstractSmartQueryPlannerTests {

	// ##########################################################################
	// ## . . . . . Delegate -> Simple ; Smart -> Set<GenericEntity> . . . . . ##
	// ##########################################################################

	/* This means we have many2one in the delegate, but on the smart level, it is presented as one2many */
	@Test
	public void simpleSetQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "inverseKeyItemSet")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(6)
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isPropertyOperand("nameA")
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(6)
							.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).castedToString().isPropertyOperand("itemType").whereSource().isFrom(ItemB.T).close(3)
							.whereElementAt(++i).isPropertyOperand("multiOwnerName").whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameB").whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(ItemB.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T)
		;
		// @formatter:on
	}

	@Test
	public void simpleSetQuery_Composite() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "compositeIkpaEntitySet")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(4)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereFroms(2)
						.whereElementAt(0).isFrom(CompositeIkpaEntityA.T).close()
						.whereElementAt(1).isFrom(PersonA.T).close()					
					.whereSelection(4)
						.whereElementAt(i=0).isPropertyOperand("description").close()
						.whereElementAt(++i).isPropertyOperand(globalId).close()
						.whereElementAt(++i).isPropertyOperand(id).close()
						.whereElementAt(++i).isPropertyOperand(partition).close()
					.whereCondition().isConjunction(2)
						.whereElementAt(0)
							.isValueComparison(Operator.equal)
							.whereLeftOperand().isPropertyOperand("id").close()
							.whereRightOperand().isPropertyOperand("personId_Set").close(2)
						.whereElementAt(1)
							.isValueComparison(Operator.equal)
							.whereLeftOperand().isPropertyOperand("nameA").close()
							.whereRightOperand().isPropertyOperand("personName_Set").close(2)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(CompositeIkpaEntity.T)
		;
		// @formatter:on
	}

	@Test
	public void simpleSetQuery_Composite_ExternalDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "compositeIkpaEntitySetExternalDqj")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(6)
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
						.whereSelection(2)
							.whereElementAt(0).isPropertyOperand("id").close()
							.whereElementAt(1).isPropertyOperand("nameA").close()
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(CompositeIkpaEntityA.T)
						.whereSelection(6)
							.whereElementAt(i=0).isPropertyOperand("description").close()
							.whereElementAt(++i).isPropertyOperand(globalId).close()
							.whereElementAt(++i).isPropertyOperand("id").close()
							.whereElementAt(++i).isPropertyOperand(partition).close()
							.whereElementAt(++i).isPropertyOperand("personId_Set").close()
							.whereElementAt(++i).isPropertyOperand("personName_Set").close()
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(CompositeIkpaEntity.T)
		;
		// @formatter:on
	}

	@Test
	public void queryWithSetCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p")
				.where()
					.entity(item(99L)).in().property("p", "inverseKeyItemSet")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isPropertyOperand("multiOwnerName").whereSource().isFrom(ItemB.T)
						.whereCondition().isConjunction(1)
							.whereElementAt(0).isValueComparison(Operator.equal)
								.whereProperty("leftOperand").isReference_(ItemB.T, 99L, accessIdB)
								.whereProperty("rightOperand").isSourceOnlyPropertyOperand()
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(6)
							.whereElementAt(i=0).isPropertyOperand("companyNameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").close()
							.whereElementAt(++i).isPropertyOperand("nameA").close()
							.whereElementAt(++i).isPropertyOperand("nickNameX").close()
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(PersonA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T)
		;
		// @formatter:on
	}

	@Test
	public void queryWithSetCondition_Composite() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p")
				.where()
					.entityReference(reference(CompositeIkpaEntity.class, accessIdA, 99L)).in().property("p", "compositeIkpaEntitySet")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(6)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereFroms(2)
						.whereElementAt(0).isFrom(CompositeIkpaEntityA.T).close()
						.whereElementAt(1).isFrom(PersonA.T).close()					
					.whereSelection(6)
						.whereElementAt(i=0).isPropertyOperand("companyNameA").close()
						.whereElementAt(++i).isPropertyOperand(globalId).close()
						.whereElementAt(++i).isPropertyOperand("id").close()
						.whereElementAt(++i).isPropertyOperand("nameA").close()
						.whereElementAt(++i).isPropertyOperand("nickNameX").close()
						.whereElementAt(++i).isPropertyOperand(partition).close()
					.whereCondition().isConjunction(3)
						.whereElementAt(0).isValueComparison(Operator.equal)
							.whereProperty("leftOperand").isReference_(CompositeIkpaEntityA.T, 99L, accessIdA)
							.whereProperty("rightOperand").isSourceOnlyPropertyOperand().close(2)
						.whereElementAt(1)
							.isValueComparison(Operator.equal)
							.whereLeftOperand().isPropertyOperand("id").close()
							.whereRightOperand().isPropertyOperand("personId_Set").close(2)
						.whereElementAt(2)
							.isValueComparison(Operator.equal)
							.whereLeftOperand().isPropertyOperand("nameA").close()
							.whereRightOperand().isPropertyOperand("personName_Set").close(2)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T)
		;
		// @formatter:on
	}

	@Test
	public void queryWithSetCondition_Composite_ExternalDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p")
				.where()
					.entityReference(reference(CompositeIkpaEntity.class, accessIdA, 99L)).in().property("p", "compositeIkpaEntitySetExternalDqj")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(6)
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(CompositeIkpaEntityA.T)
						.whereSelection(2)
							.whereElementAt(0).isPropertyOperand("personId_Set").close()
							.whereElementAt(1).isPropertyOperand("personName_Set").close()
						.whereCondition().isConjunction(1)
							.whereElementAt(0).isValueComparison(Operator.equal)
								.whereProperty("leftOperand").isReference_(CompositeIkpaEntityA.T, 99L, accessIdA)
								.whereProperty("rightOperand").isSourceOnlyPropertyOperand()
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
						.whereSelection(6)
							.whereElementAt(i=0).isPropertyOperand("companyNameA").close()
							.whereElementAt(++i).isPropertyOperand(globalId).close()
							.whereElementAt(++i).isPropertyOperand("id").close()
							.whereElementAt(++i).isPropertyOperand("nameA").close()
							.whereElementAt(++i).isPropertyOperand("nickNameX").close()
							.whereElementAt(++i).isPropertyOperand(partition).close()
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T)
		;
		// @formatter:on
	}

	@Test
	public void setQueryWithSetCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p")
				.select("p", "inverseKeyItemSet")
				.where()
					.entity(item(99L)).in().property("p", "inverseKeyItemSet")
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
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isPropertyOperand("multiOwnerName").whereSource().isFrom(ItemB.T).close(2)
						.endQuery()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereSelection(6)
								.whereElementAt(i=0).isPropertyOperand("companyNameA").whereSource().isFrom(PersonA.T).close(2)
								.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isFrom(PersonA.T).close(2)
								.whereElementAt(++i).isPropertyOperand("id").close()
								.whereElementAt(++i).isPropertyOperand("nameA").close()
								.whereElementAt(++i).isPropertyOperand("nickNameX").close()
								.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(PersonA.T).close(2)
							.whereCondition().isConjunction(1)
								.whereElementAt(0).isDisjunction(1)
									.whereElementAt(0).isValueComparison(Operator.equal)
										.whereProperty("leftOperand").isPropertyOperand("nameA")
						.endQuery()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(6)
							.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).castedToString().isPropertyOperand("itemType").whereSource().isFrom(ItemB.T).close(3)
							.whereElementAt(++i).isPropertyOperand("multiOwnerName").whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameB").whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(ItemB.T).close(2)
						.whereCondition().isConjunction(1)
							.whereElementAt(0).isDisjunction(1)
								.whereElementAt(0).isValueComparison(Operator.equal)
									.whereProperty("leftOperand").isPropertyOperand("multiOwnerName")
					.endQuery()
				.close()
				
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T).close(2)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T)
		;
		// @formatter:on
	}

	// ##########################################################################
	// ## . . . . . Delegate -> Set<Simple> ; Smart -> GenericEntity . . . . . ##
	// ##########################################################################

	/* This means we have one2many in the delegate, but on the smart level, it is presented as many2one */
	@Test
	public void simpleEntityQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "inverseKeySharedItem")
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
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isPropertyOperand("nameA")
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
							.whereElementAt(++i).isSourceOnlyPropertyOperand().whereSource().isJoin("sharedOwnerNames").close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T)
		;
		// @formatter:on
	}

	/* This means we have one2many in the delegate, but on the smart level, it is presented as many2one */
	@Test
	public void queryWithEntityCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p")
				.where()
					.property("p", "inverseKeySharedItem").eq().entity(item(99L))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isSourceOnlyPropertyOperand().whereSource().isJoin("sharedOwnerNames").close(2)
						.whereCondition().isConjunction(1)
							.whereElementAt(0).isValueComparison(Operator.equal)
								.whereProperty("leftOperand").isSourceOnlyPropertyOperand().close()
								.whereProperty("rightOperand").isReference_(ItemB.T, 99L, accessIdB)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(6)
							.whereElementAt(i=0).isPropertyOperand("companyNameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nickNameX").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(PersonA.T).close(2)
						.whereCondition().isConjunction(1)
							.whereElementAt(0).isDisjunction(1)
								.whereElementAt(0).isValueComparison(Operator.equal)
									.whereProperty("leftOperand").isPropertyOperand("nameA")
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T)
		;
		// @formatter:on
	}

	/* This means we have one2many in the delegate, but on the smart level, it is presented as many2one */
	@Test
	public void queryWithEntityCondition_AndSelectingInverseKeyProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p")
				.select("p", "inverseKeySharedItem")
				.where()
					.property("p", "inverseKeySharedItem").eq().entity(item(99L))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(6)
							.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).castedToString().isPropertyOperand("itemType").whereSource().isFrom(ItemB.T).close(3)
							.whereElementAt(++i).isPropertyOperand("nameB").whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(ItemB.T).close(2)
							.whereElementAt(++i).isSourceOnlyPropertyOperand().whereSource().isJoin("sharedOwnerNames").close(2)
						.whereCondition().isConjunction(1)
							.whereElementAt(0).isValueComparison(Operator.equal)
								.whereProperty("leftOperand").isSourceOnlyPropertyOperand().close()
								.whereProperty("rightOperand").isReference_(ItemB.T, 99L, accessIdB)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(6)
							.whereElementAt(i=0).isPropertyOperand("companyNameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nickNameX").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(PersonA.T).close(2)
						.whereCondition().isConjunction(1)
							.whereElementAt(0).isDisjunction(1)
								.whereElementAt(0).isValueComparison(Operator.equal)
									.whereProperty("leftOperand").isPropertyOperand("nameA")
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T).close(2)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T).close(2)
		;
		// @formatter:on
	}

	// ##########################################################################
	// ## . . . . Delegate -> Set<Simple> ; Smart -> Set<GenericEntity> . . . .##
	// ##########################################################################

	/* This means we have many2one in the delegate, but on the smart level, it is presented as one2many */
	@Test
	public void simpleInverseSetQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "inverseKeyMultiSharedItemSet")
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
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isPropertyOperand("nameA")
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
							.whereElementAt(++i).isSourceOnlyPropertyOperand().whereSource().isJoin("multiSharedOwnerNames").close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T)
		;
		// @formatter:on
	}

	@Test
	public void queryWithInverseSetCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p")
				.where()
					.entity(item(99L)).in().property("p", "inverseKeyMultiSharedItemSet")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isSourceOnlyPropertyOperand().whereSource().isJoin("multiSharedOwnerNames").close(2)
						.whereCondition().isConjunction(1)
							.whereElementAt(0).isValueComparison(Operator.equal)
								.whereProperty("leftOperand").isReference_(ItemB.T, 99L, accessIdB)
								.whereProperty("rightOperand").isSourceOnlyPropertyOperand().close()
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(6)
							.whereElementAt(i=0).isPropertyOperand("companyNameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nickNameX").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(PersonA.T).close(2)
						.whereCondition().isConjunction(1)
							.whereElementAt(0).isDisjunction(1)
								.whereElementAt(0).isValueComparison(Operator.equal)
									.whereProperty("leftOperand").isPropertyOperand("nameA")
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T)
		;
		// @formatter:on
	}

	@Test
	public void inverseSetQueryWithInverseSetCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p")
				.select("p", "inverseKeyMultiSharedItemSet")
				.where()
					.entity(item(99L)).in().property("p", "inverseKeyMultiSharedItemSet")
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
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isSourceOnlyPropertyOperand().whereSource().isJoin("multiSharedOwnerNames").close(2)
							.whereCondition().isConjunction(1)
								.whereElementAt(0).isValueComparison(Operator.equal)
									.whereProperty("leftOperand").isReference_(ItemB.T, 99L, accessIdB)
									.whereProperty("rightOperand").isSourceOnlyPropertyOperand().close()
						.endQuery()					
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereSelection(6)
								.whereElementAt(i=0).isPropertyOperand("companyNameA").whereSource().isFrom(PersonA.T).close(2)
								.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isFrom(PersonA.T).close(2)
								.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(PersonA.T).close(2)
								.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
								.whereElementAt(++i).isPropertyOperand("nickNameX").whereSource().isFrom(PersonA.T).close(2)
								.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(PersonA.T).close(2)
						.endQuery()
					.close()
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
							.whereElementAt(++i).isSourceOnlyPropertyOperand().whereSource().isJoin("multiSharedOwnerNames").close(3)
						.whereCondition().isConjunction(1)
							.whereElementAt(0).isDisjunction(1)
								.whereElementAt(0).isValueComparison(Operator.equal)
									.whereProperty("leftOperand").isSourceOnlyPropertyOperand().whereSource().isJoin("multiSharedOwnerNames")
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T).close(2)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T).close(2)
		;
		// @formatter:on
	}
}
