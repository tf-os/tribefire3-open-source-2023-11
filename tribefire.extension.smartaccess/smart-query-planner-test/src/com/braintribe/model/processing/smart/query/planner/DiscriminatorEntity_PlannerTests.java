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
import static com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType1.DISC_TYPE1;
import static com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType2.DISC_TYPE2;

import org.junit.Test;

import com.braintribe.model.accessdeployment.smart.meta.PolymorphicBaseEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PolymorphicDerivateEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedEntityAssignment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorBase;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType1;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType2;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.smartqueryplan.functions.PropertyMappingNode;
import com.braintribe.model.smartqueryplan.value.SimpleDiscriminatorBasedSignature;

/**
 * 
 */
public class DiscriminatorEntity_PlannerTests extends AbstractSmartQueryPlannerTests {

	/**
	 * For this the basic {@link QualifiedEntityAssignment} functionality must work with {@link PolymorphicBaseEntityAssignment} level..
	 * This MUST already delegate condition on discriminator, cause that one could theoretically also have other values than ones defined by
	 * mappings for this hierarchy.
	 */
	@Test
	public void selectPropert_HierarchyBase() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorBase.T, "e")
				.select("e", "name")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("name")
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.in)
							.whereLeftOperand().isPropertyOperand("discriminator").close()
							.whereRightOperand().isSet(DISC_TYPE1, DISC_TYPE2)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(TupleComponent.T)
		;
		// @formatter:on
	}

	/**
	 * For this the basic {@link QualifiedEntityAssignment} functionality must work with {@link PolymorphicDerivateEntityAssignment}. This
	 * is the exact same query as the one above, but only asks for instances where discriminator has value
	 * {@link SmartDiscriminatorType1#DISC_TYPE1}.
	 */
	@Test
	public void selectPropert_HierarchyLeaf() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorType1.T, "e")
				.select("e", "type1Name")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("type1Name")
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.in)
							.whereLeftOperand().isPropertyOperand("discriminator").close()
							.whereRightOperand().isSet(DISC_TYPE1)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(TupleComponent.T)
		;
		// @formatter:on
	}

	/**
	 * This is the next level for discriminator property - make sure it is selected and the condition is also delegated properly.
	 * 
	 * NOTE that this can be optimized to not retrieve the discriminator value in this case, because there is no sub-type with different
	 * value. But for now let's just focus on the general use-case when we want delegate the condition and retrieve the value.
	 */
	@Test
	public void selectTheDiscriminatorProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorType2.T, "e")
				.select("e", "discriminator")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("discriminator")
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.in)
							.whereLeftOperand().isPropertyOperand("discriminator").close()
							.whereRightOperand().isSet(DISC_TYPE2)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(TupleComponent.T)
		;
		// @formatter:on
	}

	/**
	 * Make sure to load discriminator and have correct rules for entity type. This is the easier case than selecting the abstract
	 * super-type, as we don't have to deal with properties which are only mapped from sub-types (in our case
	 * {@link SmartDiscriminatorType1#getType1Name()}).
	 */
	@Test
	public void selectEntity_HierarchyLeaf() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorType1.T, "e")
				.select("e")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(6)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(6)
					.whereElementAt(i=0).isPropertyOperand("discriminator").close()
					.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).close()
					.whereElementAt(++i).isPropertyOperand("id").close()
					.whereElementAt(++i).isPropertyOperand("name").close()
					.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).close()
					.whereElementAt(++i).isPropertyOperand("type1Name").close()
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.in)
							.whereLeftOperand().isPropertyOperand("discriminator").close()
							.whereRightOperand().isSet(DISC_TYPE1)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(6).close()
			.close()
			.whereProperty("values").isListWithSize(1)
			.whereElementAt(0).hasType(QueryFunctionValue.T)
				.whereProperty("queryFunction").isAssembleEntity(SmartDiscriminatorType1.T)
					.whereProperty("signatureToPropertyMappingNode").isMapWithSize(1)
						.whereValueFor(SmartDiscriminatorType1.class.getName()).hasType(PropertyMappingNode.T)
							.whereProperty("propertyMappings").isMapWithSize(5)
								.whereValueFor(globalId).isTupleComponent_(1)
								.whereValueFor("id").isTupleComponent_(2)
								.whereValueFor("name").isTupleComponent_(3)
								.whereValueFor(partition).isTupleComponent_(4)
								.whereValueFor("type1Name").isTupleComponent_(5).close()
						.close()
					.close()
					.whereProperty("smartEntitySignature")
						.isStaticValue_(SmartDiscriminatorType1.class.getName())
		;
		// @formatter:on
	}

	/** Make sure to load discriminator and have correct rules for the hierarchy. */
	@Test
	public void selectEntity_HierarchyBase() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorBase.T, "e")
				.select("e")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(7)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(7)
					.whereElementAt(i=0).isPropertyOperand("discriminator").close()
					.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).close()
					.whereElementAt(++i).isPropertyOperand("id").close()
					.whereElementAt(++i).isPropertyOperand("name").close()
					.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).close()
					.whereElementAt(++i).isPropertyOperand("type1Name").close()
					.whereElementAt(++i).isPropertyOperand("type2Name").close()
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.in)
							.whereLeftOperand().isPropertyOperand("discriminator").close()
							.whereRightOperand().isSet(DISC_TYPE1, DISC_TYPE2)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(7).close()
			.close()
			.whereProperty("values").isListWithSize(1)
			.whereElementAt(0).hasType(QueryFunctionValue.T)
				.whereProperty("queryFunction").isAssembleEntity(SmartDiscriminatorBase.T)
					.whereProperty("signatureToPropertyMappingNode").isMapWithSize(3)
						.whereValueFor(SmartDiscriminatorBase.class.getName()).hasType(PropertyMappingNode.T)
							.whereProperty("propertyMappings").isMapWithSize(4)
								.whereValueFor(globalId).isTupleComponent_(1)
								.whereValueFor("id").isTupleComponent_(2)
								.whereValueFor("name").isTupleComponent_(3)
								.whereValueFor(partition).isTupleComponent_(4).close()
						.close()
						.whereValueFor(SmartDiscriminatorType1.class.getName()).hasType(PropertyMappingNode.T)
							.whereProperty("propertyMappings").isMapWithSize(1)
								.whereValueFor("type1Name").isTupleComponent_(5).close()
						.close()
						.whereValueFor(SmartDiscriminatorType2.class.getName()).hasType(PropertyMappingNode.T)
							.whereProperty("propertyMappings").isMapWithSize(2)
								.whereValueFor("discriminator").isTupleComponent_(0)
								.whereValueFor("type2Name").isTupleComponent_(6).close()
						.close()
					.close()
					.whereProperty("smartEntitySignature").hasType(SimpleDiscriminatorBasedSignature.T)
						.whereProperty("tuplePosition").is_(0)
						.whereProperty("signatureMapping").isMapWithSize(2)
							.whereValueFor("type1").is_(SmartDiscriminatorType1.class.getName())
							.whereValueFor("type2").is_(SmartDiscriminatorType2.class.getName())
		;
		// @formatter:on
	}

	@Test
	public void selectTypeSignature() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorBase.T, "e")
				.select().entitySignature().entity("e")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("discriminator").close()
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.in)
							.whereLeftOperand().isPropertyOperand("discriminator").close()
							.whereRightOperand().isSet(DISC_TYPE1, DISC_TYPE2)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
			.whereElementAt(0).hasType(QueryFunctionValue.T)
				.whereProperty("queryFunction").hasType(EntitySignature.T).close()
				.whereProperty("operandMappings").isMapWithSize(1)
				.whereFirstValue().hasType(SimpleDiscriminatorBasedSignature.T)
					.whereProperty("tuplePosition").is_(0)
					.whereProperty("signatureMapping").isMapWithSize(2)
						.whereValueFor("type1").is_(SmartDiscriminatorType1.class.getName())
						.whereValueFor("type2").is_(SmartDiscriminatorType2.class.getName())
					.close()
		;
		// @formatter:on
	}

	/** This one will be fun. */
	@Test
	public void selectEntity_ConditionOnType() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorBase.T, "e")
				.select("e")
				.where()
					.entitySignature("e").eq(SmartDiscriminatorType1.class.getName())
				.done();
		// @formatter:on

		runTest(selectQuery);

		// TODO implement and add assertions
	}

}
