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
import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdB;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.Date;

import org.junit.Test;

import com.braintribe.model.accessdeployment.smart.meta.conversion.DateToString;
import com.braintribe.model.accessdeployment.smart.meta.conversion.LongToString;
import com.braintribe.model.generic.builder.vd.VdBuilder;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.StandardIdEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartStringIdEntity;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.value.AsString;
import com.braintribe.model.queryplan.filter.GreaterThan;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;
import com.braintribe.model.smartqueryplan.set.OperandRestriction;
import com.braintribe.model.smartqueryplan.value.ConvertedValue;

/**
 * 
 */
public class PropertyConversion_PlannerTests extends AbstractSmartQueryPlannerTests {

	// #########################################
	// ## . . . . . Simple Property . . . . . ##
	// #########################################

	@Test
	public void selectConvertedProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.T, "p")
				.select("p", "convertedBirthDate")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessB").close()
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("birthDate")
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(ConvertedValue.T)
					.whereProperty("operand").isTupleComponent_(0)
					.whereProperty("conversion").hasType(DateToString.T)
		;
		// @formatter:on
	}

	@Test
	public void filterByConvertedProperty_Equals() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.T, "p")
				.select("p")
				.where()
					.property("p", "convertedBirthDate").eq(new Date(0))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessB").close()
				.whereProperty("scalarMappings").isListWithSize(7).close()
				.whereDelegateQuery()
					.whereSelection(7)
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.equal)
							.whereProperty("leftOperand").isPropertyOperand("birthDate").close()
							.whereProperty("rightOperand").hasType(String.class)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonB.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void filterByConvertedProperty_Inequality_NonDelegatable() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.T, "p")
				.select("p")
				.where()
					.property("p", "convertedBirthDate").gt(new Date(0))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(FilteredSet.T)
				.whereProperty("operand")
					.hasType(DelegateQuerySet.T)
					.whereProperty("delegateAccess").isDelegateAccess("accessB").close()
					.whereProperty("scalarMappings").isListWithSize(7).close()
					.whereDelegateQuery()
						.whereSelection(7).close()
						.withNoRestriction() // Our condition is on the smart level
					.endQuery()
				.close()
				.whereProperty("filter")
					.hasType(GreaterThan.T)
					.whereProperty("leftOperand")
						.hasType(ConvertedValue.T)
						.whereProperty("operand").isTupleComponent_(1)
						.whereProperty("conversion").hasType(DateToString.T).close()
					.close()
					.whereProperty("rightOperand").isStaticValueAndValue().hasType(Date.class).close(2)
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonB.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void filterByConvertedProperty_In() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.T, "p")
				.select("p")
				.where()
					.property("p", "convertedBirthDate").in(asSet(new Date(0), new Date(1000)))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessB").close()
				.whereProperty("scalarMappings").isListWithSize(7).close()
				.whereDelegateQuery()
					.whereSelection(7)
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.in)
							.whereProperty("leftOperand").isPropertyOperand("birthDate").close()
							.whereProperty("rightOperand").isSetWithSize(2).setToList()
								.whereElementAt(0).hasType(String.class).close()
								.whereElementAt(1).hasType(String.class).close()
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonB.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void delegateWhenConvertedToSmartString_Like() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartStringIdEntity.T, "c")
				.select("c")
				.where()
					.property("c", "id").like("*8")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQuerySet("accessB")
				.whereDelegateQuery()
					.whereFroms(1).whereElementAt(0).isFrom(StandardIdEntity.T)
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.like)
							.whereLeftOperand().hasType(AsString.T)
								.whereOperand().isPropertyOperand("id")
				.endQuery()
			.close()
		;
		// @formatter:on
	}

	// #########################################
	// ## . . . . . . KPA Property . . . . . .##
	// #########################################

	/** @see #selectConvertedProperty_Kpa_Left() */
	@Test
	public void selectConvertedProperty_Kpa() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.T, "p")
					.join("p", "convertedSmartParentA", "cp", JoinType.inner)
				.select("p", "nameB")
				.select("cp")
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
						.whereProperty("scalarMappings").isListWithSize(7)
							.whereElementAt(0).isScalarMappingAndValue(0).isTupleComponent_(0).close()
							.whereElementAt(1).isScalarMappingAndValue(1).isTupleComponent_(1).close()
							.whereElementAt(2).isScalarMappingAndValue(2).isTupleComponent_(2).close()
							.whereElementAt(3).isScalarMappingAndValue(3).isTupleComponent_(2).close()
							.whereElementAt(4).isScalarMappingAndValue(4).isTupleComponent_(3).close()
							.whereElementAt(5).isScalarMappingAndValue(5).isTupleComponent_(4).close()
							.whereElementAt(6).isScalarMappingAndValue(6).isTupleComponent_(5).close()
						.close()
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0)
								.isFrom(PersonA.T)
							.whereSelection(6)
								.whereElementAt(i=0).isPropertyOperand("companyNameA").close()
								.whereElementAt(++i).isPropertyOperand(globalId).close()
								.whereElementAt(++i).isPropertyOperand("id").close()
								.whereElementAt(++i).isPropertyOperand("nameA").close()
								.whereElementAt(++i).isPropertyOperand("nickNameX").close()
								.whereElementAt(++i).isPropertyOperand(partition).close()
							.withNoRestriction()
						.endQuery()
					.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
						.whereProperty("scalarMappings").isListWithSize(2)
							.whereElementAt(0).isScalarMappingAndValue(7).isTupleComponent_(0).close()
							.whereElementAt(1).isScalarMappingAndValue(3)
								.hasType(ConvertedValue.T)
									.whereProperty("conversion").hasType(LongToString.T).close()
									.whereProperty("operand").isTupleComponent_(1)
							.close(2)
						.close()
						.whereDelegateQuery()
							.whereSelection(2)
								.whereElementAt(0).isPropertyOperand("nameB").close()
								.whereElementAt(1).isPropertyOperand("parentA").close()
						.endQuery()
				.close()
				.whereProperty("joinRestrictions").isListWithSize(1)
					.whereElementAt(0).hasType(OperandRestriction.T)
						.whereProperty("materializedCorrelationValue").hasType(ConvertedValue.T)
							.whereProperty("inverse").isTrue_()
							.whereProperty("conversion").hasType(LongToString.T).close()
							.whereProperty("operand").isTupleComponent_(3).close()
					.close()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(7)
				.whereElementAt(1).hasType(QueryFunctionValue.T)
					.whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T)
		;
		// @formatter:on
	}

	/**
	 * So, what is the difference between this and the version with an inner join ({@link #selectConvertedProperty_Kpa()}):
	 * 
	 * We do our mappings of convertedSmartParentA like this:<tt>PersonB.parentA.strToLong() = PersonA.id</tt> So the difference is the
	 * order in which we retrieve the sources.
	 * 
	 * In the inner join case, we do PersonA first, then PersonB (cause alphabetically). Thus when we do the query part of the DQJ, we have
	 * to convert the id we have retrieved from PersonA to string, for correlation condition (see the "materializedCorrelationValue" of the
	 * "joinRestrictions" above).
	 * 
	 * In the left join case we do PersonB first (owner), then PersonA (property). Here we convert the parentA property to a long when we
	 * put it into the tuple, so no conversion is then needed during the actual execution of the DQJ.
	 */
	@Test
	public void selectConvertedProperty_Kpa_Left() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.T, "p")
				.select("p", "nameB")
				.select("p", "convertedSmartParentA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessB")
						.whereProperty("scalarMappings").isListWithSize(2)
							.whereElementAt(0).isScalarMappingAndValue(0).isTupleComponent_(0).close()
							.whereElementAt(1).isScalarMappingAndValue(1)
								.hasType(ConvertedValue.T)
									.whereProperty("conversion").hasType(LongToString.T).close()
									.whereProperty("operand").isTupleComponent_(1)
							.close(2)
						.close()
						.whereDelegateQuery()
							.whereSelection(2)
								.whereElementAt(0).isPropertyOperand("nameB").close()
								.whereElementAt(1).isPropertyOperand("parentA").close()
							.withNoRestriction()
						.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
						.whereProperty("scalarMappings").isListWithSize(7)
							.whereElementAt(0).isScalarMappingAndValue(2).isTupleComponent_(0).close()
							.whereElementAt(1).isScalarMappingAndValue(3).isTupleComponent_(1).close()
							.whereElementAt(2).isScalarMappingAndValue(4).isTupleComponent_(2).close()
							.whereElementAt(3).isScalarMappingAndValue(1).isTupleComponent_(2).close()
							.whereElementAt(4).isScalarMappingAndValue(5).isTupleComponent_(3).close()
							.whereElementAt(5).isScalarMappingAndValue(6).isTupleComponent_(4).close()
							.whereElementAt(6).isScalarMappingAndValue(7).isTupleComponent_(5).close()
						.close()
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0)
								.isFrom(PersonA.T)
							.whereSelection(6)
								.whereElementAt(i=0).isPropertyOperand("companyNameA").close()
								.whereElementAt(++i).isPropertyOperand(globalId).close()
								.whereElementAt(++i).isPropertyOperand("id").close()
								.whereElementAt(++i).isPropertyOperand("nameA").close()
								.whereElementAt(++i).isPropertyOperand("nickNameX").close()
								.whereElementAt(++i).isPropertyOperand(partition).close()
						.endQuery()
					.close()
				.whereProperty("joinRestrictions").isListWithSize(1)
					.whereElementAt(0).hasType(OperandRestriction.T)
						.whereProperty("materializedCorrelationValue").isTupleComponent_(1).close()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(0)
				.whereElementAt(1).hasType(QueryFunctionValue.T)
					.whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T)
		;
		// @formatter:on
	}
	
	@Test
	public void conditionOnConvertedKpa() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("p")
				.from(SmartPersonB.T, "p")
				.where()
					.property("p", "convertedSmartParentA").eq().entityReference(reference(SmartPersonA.class, accessIdA,  99L))
				.done();
		// @formatter:on

		runTest(selectQuery);
		
		// TODO add asserts
	}

	// #########################################
	// ## . . . . Collection Property . . . . ##
	// #########################################

	@Test
	public void selectConvertedCollectionProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.T, "p")
				.select("p", "convertedDates")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessB").close()
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isSourceOnlyPropertyOperand().whereSource().isJoin("dates")
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(ConvertedValue.T)
					.whereProperty("operand").isTupleComponent_(0)
					.whereProperty("conversion").hasType(DateToString.T)
		;
		// @formatter:on
	}

	@Test
	public void filterByConvertedCollectionProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.T, "p")
				.select("p")
				.where()
					.value(new Date(0)).in().property("p", "convertedDates")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessB").close()
				.whereProperty("scalarMappings").isListWithSize(7).close()
				.whereDelegateQuery()
					.whereSelection(7)
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.in)
							.whereProperty("leftOperand").hasType(String.class).close()
							.whereProperty("rightOperand").isPropertyOperand("dates")
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonB.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void filterByConvertedCollectionProperty_Inequality_NonDelegatable() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.T, "p")
					.join("p", "convertedDates", "d")
				.select("p")
				.where()
					.entity("d").gt(new Date(0))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(FilteredSet.T)
				.whereProperty("operand")
					.hasType(DelegateQuerySet.T)
					.whereProperty("delegateAccess").isDelegateAccess("accessB").close()
					.whereProperty("scalarMappings").isListWithSize(8).close()
					.whereDelegateQuery()
						.whereSelection(8).close()
						.withNoRestriction() // Our condition is on the smart level
					.endQuery()
				.close()
				.whereProperty("filter")
					.hasType(GreaterThan.T)
					.whereProperty("leftOperand")
						.hasType(ConvertedValue.T)
						.whereProperty("operand").isTupleComponent_(7)
						.whereProperty("conversion").hasType(DateToString.T).close()
					.close()
					.whereProperty("rightOperand").isStaticValueAndValue().hasType(Date.class).close(2)
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonB.T).close(2)
		;
		// @formatter:on
	}

	// #########################################
	// ## . . . . IdProperty Property . . . . ##
	// #########################################

	@Test
	public void entityPropertyComparison() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartStringIdEntity.T, "c")
				.select("c")
				.where()
					.property("c", "parent").eq(stringIdReference("1"))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessB").close()
				.whereProperty("scalarMappings").isListWithSize(4).close()
				.whereDelegateQuery()
					.whereSelection(4).close()
					.whereCondition().isSingleOperandConjunctionAndOperand()
						.isValueComparison(Operator.equal)
						.whereProperty("leftOperand").isSourceOnlyPropertyOperand().whereSource().isJoin("parent").close(2)
						.whereProperty("rightOperand").isReference_(StandardIdEntity.T, 1L, accessIdB)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartStringIdEntity.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void entityCollection_InCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartStringIdEntity.T, "c")
				.select("c")
				.where()
					.property("c", "children").contains().entityReference(stringIdReference("1"))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessB").close()
				.whereProperty("scalarMappings").isListWithSize(4).close()
				.whereDelegateQuery()
					.whereSelection(4).close()
					.whereCondition().isSingleOperandConjunctionAndOperand()
						.isValueComparison(Operator.contains)
						.whereProperty("leftOperand").isPropertyOperand("children").close()
						.whereProperty("rightOperand").isReference_(StandardIdEntity.T, 1L, accessIdB)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartStringIdEntity.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void idConversion_KpaPropertyCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartStringIdEntity.T, "c")
				.select("c")
				.where()
					.property("c", "kpaParent").eq(stringIdReference("1"))
				.done();
		// @formatter:on

		runTest(selectQuery);

//		// @formatter:off
//		assertQueryPlan()
//			.hasType(Projection.T)
//			.whereProperty("operand")
//				.hasType(DelegateQuerySet.T)
//				.whereProperty("delegateAccess").isDelegateAccess("accessB").close()
//				.whereProperty("scalarMappings").isListWithSize(2).close()
//				.whereDelegateQuery()
//					.whereSelection(2).close()
//					.whereCondition().isSingleOperandConjunctionAndOperand()
//						.isValueComparison(Operator.equal)
//						.whereProperty("leftOperand").isSourceOnlyPropertyOperand().whereSource().isJoin("parent").close(2)
//						.whereProperty("rightOperand")
//							.isReference(StandardIdEntity.T.getName(), 1L)
//						.close()
//				.endQuery()
//			.close()
//			.whereProperty("values").isListWithSize(1)
//				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartStringIdEntity.T).close(2)
//		;
//		// @formatter:on
	}

	protected static PersistentEntityReference stringIdReference(String id) {
		return VdBuilder.persistentReference(SmartStringIdEntity.class.getName(), id, SmartMappingSetup.accessIdB);
	}

}
