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
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Test;

import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.query.smart.test.model.accessA.Address;
import com.braintribe.model.processing.query.smart.test.model.accessA.CarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.FlyingCarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessA.VehicleA;
import com.braintribe.model.processing.query.smart.test.model.smart.Car;
import com.braintribe.model.processing.query.smart.test.model.smart.FlyingCar;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartAddress;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.Vehicle;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.smartqueryplan.functions.PropertyMappingNode;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;

/**
 * 
 */
public class EntitySelection_PlannerTests extends AbstractSmartQueryPlannerTests {

	@Test
	public void selectSimpleEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartAddress.T, "a")
				.select("a")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereFroms(1).whereElementAt(0).isFrom(Address.T)
					.whereSelection(7)
						.whereElementAt(i=0).isPropertyOperand("country").close()
						.whereElementAt(++i).isPropertyOperand(globalId).close()
						.whereElementAt(++i).isPropertyOperand("id").close()
						.whereElementAt(++i).isPropertyOperand("number").close()
						.whereElementAt(++i).isPropertyOperand(partition).close()
						.whereElementAt(++i).isPropertyOperand("street").close()
						.whereElementAt(++i).isPropertyOperand("zipCode").close()
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(7).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isQueryFunctionValueAndQf()
					.isAssembleEntity(SmartAddress.T)
						.whereProperty("signatureToPropertyMappingNode").isMapWithSize(1)
							.whereValueFor(SmartAddress.class.getName()).hasType(PropertyMappingNode.T)
								.whereProperty("propertyMappings").isMapWithSize(7)
									.whereValueFor("country").isTupleComponent_(i=0)
									.whereValueFor(globalId).isTupleComponent_(++i)
									.whereValueFor("id").isTupleComponent_(++i)
									.whereValueFor("number").isTupleComponent_(++i)
									.whereValueFor(partition).isTupleComponent_(++i)
									.whereValueFor("street").isTupleComponent_(++i)
									.whereValueFor("zipCode").isTupleComponent_(++i)
		;
		// @formatter:on
	}

	@Test
	public void selectPolymorphicEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(Car.T, "c")
				.select("c")
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
						.whereFroms(1).whereElementAt(0).isFrom(CarA.T)
						.whereSelection(7)
							.whereElementAt(i=0).isPropertyOperand(globalId).close()
							.whereElementAt(++i).isPropertyOperand("id").close()
							.whereElementAt(++i).isPropertyOperand("maxSpeed").close()
							.whereElementAt(++i).isPropertyOperand("model").close()
							.whereElementAt(++i).isPropertyOperand(partition).close()
							.whereElementAt(++i).isPropertyOperand("serialNumber").close()
							.whereElementAt(++i).hasType(EntitySignature.T)
					.endQuery()
					.whereProperty("scalarMappings").isListWithSize(7).close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(FlyingCarA.T)
						.whereSelection(2)
							.whereElementAt(0).isPropertyOperand("id").close()
							.whereElementAt(1).isPropertyOperand("maxFlyingSpeed").close()
					.endQuery()
					.whereProperty("scalarMappings").isListWithSize(2)
						.whereElementAt(0).isScalarMappingAndValue(1).isTupleComponent_(0).close()
						.whereElementAt(1).isScalarMappingAndValue(7).isTupleComponent_(1).close()
					.close()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isQueryFunctionValueAndQf()
					.isAssembleEntity(Car.T)
						.whereProperty("signatureToPropertyMappingNode").isMapWithSize(2)
							.whereValueFor(Car.class.getName()).hasType(PropertyMappingNode.T)
								.whereProperty("propertyMappings").isMapWithSize(6)
									.whereValueFor(globalId).isTupleComponent_(i=0)
									.whereValueFor("id").isTupleComponent_(++i)
									.whereValueFor("maxSpeed").isTupleComponent_(++i)
									.whereValueFor("model").isTupleComponent_(++i)
									.whereValueFor(partition).isTupleComponent_(++i)
									.whereValueFor("serialNumber").isTupleComponent_(++i).close()
							.close()
							.whereValueFor(FlyingCar.class.getName()).hasType(PropertyMappingNode.T)
								.whereProperty("propertyMappings").isMapWithSize(1)
									.whereValueFor("maxFlyingSpeed").isTupleComponent_(7)
		;
		// @formatter:on
	}

	@Test
	public void selectPolymorphicEntity_MoreAbstract() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(Vehicle.T, "v")
				.select("v")
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
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(VehicleA.T)
						.endQuery()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(CarA.T)
						.endQuery()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(FlyingCarA.T)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isQueryFunctionValueAndQf()
					.isAssembleEntity(Vehicle.T)
		;
		// @formatter:on
	}

	@Test
	public void directEntityCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")				
				.where()
					.entity("p").eq(reference(SmartPersonA.class, accessIdA, 1l))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("nameA").close()
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.equal)
							.whereProperty("leftOperand").isSourceOnlyPropertyOperand().whereSource().isFrom(PersonA.T).close(2)
							.whereProperty("rightOperand").isReference_(PersonA.T, 1l, accessIdA)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

	@Test
	public void directEntityCondition_AnyPartition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")				
				.where()
					.entity("p").eq(reference(SmartPersonA.class, EntityReference.ANY_PARTITION, 1l))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("nameA").close()
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.equal)
							.whereProperty("leftOperand").isSourceOnlyPropertyOperand().whereSource().isFrom(PersonA.T).close(2)
							.whereProperty("rightOperand").isReference_(PersonA.T, 1l, EntityReference.ANY_PARTITION)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

	@Test
	public void directEntityCondition_selectIkpaProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("p", "inverseIdKeyCompanyA")
				.from(SmartPersonA.T, "p")
				.where()
					.entity("p").eq().entityReference(reference(SmartPersonA.class, accessIdA, 99L))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isStaticTupleWithScalarMappings(1)
						.whereElementAt(0).isScalarMappingAndValue(0).isStaticValue_(99L).close(2)
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(5)
							.whereElementAt(i=0).isPropertyOperand("globalId").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("ownerIdA").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("partition").whereSource().isFrom(CompanyA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
		;
		// @formatter:on
	}

	@Test
	public void directEntityCondition_selectIkpaProperty_disjunction() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("p", "inverseIdKeyCompanyA")
				.from(SmartPersonA.T, "p")
				.where()
					.disjunction()
						.entity("p").eq().entityReference(reference(SmartPersonA.class, accessIdA, 77L))
						.entity("p").eq().entityReference(reference(SmartPersonA.class, accessIdA, 88L))
						.entity("p").eq().entityReference(reference(SmartPersonA.class, accessIdA, 99L))
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isStaticTuples(3)
						.whereElementAt(0).isStaticTupleWithScalarMappings(1)
							.whereElementAt(0).isScalarMappingAndValue(0).isStaticValue_(77L).close(3)
						.whereElementAt(1).isStaticTupleWithScalarMappings(1)
							.whereElementAt(0).isScalarMappingAndValue(0).isStaticValue_(88L).close(3)
						.whereElementAt(2).isStaticTupleWithScalarMappings(1)
							.whereElementAt(0).isScalarMappingAndValue(0).isStaticValue_(99L).close(3)
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(5)
							.whereElementAt(i=0).isPropertyOperand("globalId").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("ownerIdA").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("partition").whereSource().isFrom(CompanyA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
		;
		// @formatter:on
	}

	@Test
	public void keyPropertyCondition_selectIkpaProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("p", "inverseIdKeyCompanyA")
				.from(SmartPersonA.T, "p")
				.where()
					.property("p", "id").eq(99L)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isStaticTupleWithScalarMappings(1)
						.whereElementAt(0).isScalarMappingAndValue(0).isStaticValue_(99L).close(2)
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(5)
							.whereElementAt(i=0).isPropertyOperand("globalId").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("ownerIdA").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("partition").whereSource().isFrom(CompanyA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
		;
		// @formatter:on
	}

	@Test
	public void keyPropertyCondition_selectIkpaProperty_disjunction() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("p", "inverseIdKeyCompanyA")
				.from(SmartPersonA.T, "p")
				.where()
					.disjunction()
						.property("p", "id").eq(77L)
						.property("p", "id").eq(88L)
						.property("p", "id").eq(99L)
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isStaticTuples(3)
						.whereElementAt(0).isStaticTupleWithScalarMappings(1)
							.whereElementAt(0).isScalarMappingAndValue(0).isStaticValue_(77L).close(3)
						.whereElementAt(1).isStaticTupleWithScalarMappings(1)
							.whereElementAt(0).isScalarMappingAndValue(0).isStaticValue_(88L).close(3)
						.whereElementAt(2).isStaticTupleWithScalarMappings(1)
							.whereElementAt(0).isScalarMappingAndValue(0).isStaticValue_(99L).close(3)
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(5)
							.whereElementAt(i=0).isPropertyOperand("globalId").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("ownerIdA").whereSource().isFrom(CompanyA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("partition").whereSource().isFrom(CompanyA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
		;
		// @formatter:on
	}

	@Test
	public void propertyEntityCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.property("p", "companyA").eq().entity(company(99L))
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
							.whereProperty("leftOperand").isSourceOnlyPropertyOperand().whereSource().isJoin("companyA").close(2)
							.whereProperty("rightOperand").isReference_(CompanyA.T, 99L, accessIdA)
				.endQuery()
			;
		// @formatter:on
	}

	@Test
	public void propertyEntityCondition_In() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.property("p", "companyA").inEntities(asSet(company(77L), company(99L))) 
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
						.whereElementAt(0).isValueComparison(Operator.in)
							.whereProperty("leftOperand").isSourceOnlyPropertyOperand().whereSource().isJoin("companyA").close(2)
							.whereProperty("rightOperand").isSetWithSize(2).setToList().whenOrderedBy("refId")
								.whereElementAt(0).isReference_(CompanyA.T, 77L, accessIdA)
								.whereElementAt(1).isReference_(CompanyA.T, 99L, accessIdA)
				.endQuery()
			;
		// @formatter:on
	}

	@Test
	public void keyPropertyEntityCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.property("p", "keyCompanyA").eq().entity(company(1L))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
					.whereFroms(2)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
		;
		// @formatter:on
	}

	@Test
	public void keyPropertyEntityCondition_ExternalDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.property("p", "keyCompanyExternalDqj").eq().entity(company(1L))
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
							.whereElementAt(0).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(2)
							.whereElementAt(0).isPropertyOperand("companyNameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(1).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
		;
		// @formatter:on
	}
	
	@Test
	public void entityConditionUsingId() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")		
				.join("p", "companyA", "c")
				.where()
					.property("c", "id").eq(1L)
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
				.endQuery()
			;
		// @formatter:on
	}

	@Test
	public void entityConditionUsingIdPath() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")				
				.where()
					.property("p", "companyA.id").eq(1L)
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
							.whereProperty("leftOperand").isPropertyOperand("id").whereSource().isJoin("companyA").close(2)
							.whereProperty("rightOperand").is_(1l)
				.endQuery()
			;
		// @formatter:on
	}

	@Test
	public void fulltextConditionSelectingPropertyOnly() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")				
				.where()
					.fullText("p", "p*")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereDelegateQuery()
					.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("nameA")
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isFulltextAndSource("p*").isFrom(PersonA.T)
				.endQuery()
			;
		// @formatter:on
	}

	@Test
	public void fulltextConditionSelectingEntireEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p")				
				.where()
					.fullText("p", "p*")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// TODO FIX there is no need to select the nickNameX property!!! (This was not there in 1.1)
		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereDelegateQuery()
					.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
					.whereSelection(6)
						.whereElementAt(i=0).isPropertyOperand("companyNameA").close()
						.whereElementAt(++i).isPropertyOperand(globalId).close()
						.whereElementAt(++i).isPropertyOperand("id").close()
						.whereElementAt(++i).isPropertyOperand("nameA").close()
						.whereElementAt(++i).isPropertyOperand("nickNameX").close()
						.whereElementAt(++i).isPropertyOperand(partition).close()
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isFulltextAndSource("p*").isFrom(PersonA.T)
				.endQuery()
			;
		// @formatter:on
	}

}
