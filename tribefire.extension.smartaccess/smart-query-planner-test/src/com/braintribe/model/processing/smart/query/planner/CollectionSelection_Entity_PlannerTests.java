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

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.CarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.FlyingCarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.Car;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.MapKey;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;

/**
 * 
 */
public class CollectionSelection_Entity_PlannerTests extends AbstractSmartQueryPlannerTests {

	@Test
	public void simpleSetQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "companySetA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessA").close()
				.whereProperty("scalarMappings").isListWithSize(4).close()
				.whereDelegateQuery()
					.whereSelection(4)
						.whereElementAt(i=0).isPropertyOperand(globalId).close()
						.whereElementAt(++i).isPropertyOperand("id").close()
						.whereElementAt(++i).isPropertyOperand("nameA").close()
						.whereElementAt(++i).isPropertyOperand(partition).close()
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isQueryFunctionValueAndQf().isAssembleEntity(Company.T)
		;
		// @formatter:on
	}

	@Test
	public void queryWithDelegatableSetCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.entity(company(99L)).in().property("p", "companySetA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessA").close()
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).hasType(PropertyOperand.T).whereProperty("propertyName").is_("nameA")
					.whereCondition().isSingleOperandConjunctionAndOperand()
						.isValueComparison(Operator.in)
						.whereProperty("leftOperand").isReference_(CompanyA.T, 99L, SmartMappingSetup.accessIdA)
						.whereProperty("rightOperand").isPropertyOperand("companySetA")
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(TupleComponent.T)
		;
		// @formatter:on
	}

	@Test
	public void setQueryWithDelegatableSetCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.select("p", "companySetA")
				.where()
					.entity(company(99L)).in().property("p", "companySetA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand").isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(5).close()
				.whereDelegateQuery()
					.whereSelection(5)
						.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isJoin("companySetA").close(2)
						.whereElementAt(++i).isPropertyOperand("id").whereSource().isJoin("companySetA").close(2)
						.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isJoin("companySetA").close(2)
						.whereElementAt(++i).isPropertyOperand(partition).whereSource().isJoin("companySetA").close(2)
						.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T)
					.whereCondition().isSingleOperandConjunctionAndOperand()
						.isValueComparison(Operator.in)
						.whereProperty("leftOperand").isReference_(CompanyA.T, 99L, SmartMappingSetup.accessIdA)
						.whereProperty("rightOperand").isPropertyOperand("companySetA")
				.endQuery()
				
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(TupleComponent.T).close()
				.whereElementAt(1).hasType(QueryFunctionValue.T)
		;
		// @formatter:on
	}

	@Test
	public void simpleMapQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "companyOwnerA", "o")
				.select().mapKey("o")
				.select("o")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand").isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(10).close()
				.whereDelegateQuery()
					.whereSelection(10)
						.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(CompanyA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("companyNameA").whereSource().isJoin("companyOwnerA").close(2)
						.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isJoin("companyOwnerA").close(2)
						.whereElementAt(++i).isPropertyOperand("id").whereSource().isJoin("companyOwnerA") .close(2)
						.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isJoin("companyOwnerA").close(2)
						.whereElementAt(++i).isPropertyOperand("nickNameX").whereSource().isJoin("companyOwnerA").close(2)
						.whereElementAt(++i).isPropertyOperand(partition).whereSource().isJoin("companyOwnerA").close(2)
					.whereCondition().isSingleOperandConjunctionAndOperand()
						.isValueComparison(Operator.equal)
						.whereProperty("leftOperand").hasType(MapKey.T).whereProperty("join").isJoin("companyOwnerA") .close(2)
						.whereProperty("rightOperand").isSourceOnlyPropertyOperand().whereSource().isFrom(CompanyA.T)
				.endQuery()
				
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isQueryFunctionValueAndQf().isAssembleEntity(Company.T).close(2)
				.whereElementAt(1).isQueryFunctionValueAndQf().isAssembleEntity(SmartPersonA.T)
		;
		// @formatter:on
	}

	@Test
	public void simpleValueMapWithPolymorphicKeyQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "carAliasA", "o")
				.select().mapKey("o")
				.select("o")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
			.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet").isDelegateQuerySet("accessA")
					.whereProperty("scalarMappings").isListWithSize(8).close()
					.whereDelegateQuery()
						/* Expecting: select string, CarA.id, CarA.maxSpeed, CarA.model, CarA.serialNumber, entitySignature(CarA) 
						 * from PersonA as PersonA, CarA as CarA left join PersonA.carAliasA as string 
						 * where (mapKey(string) = CarA) */
						.whereSelection(8)
							.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(CarA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(CarA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("maxSpeed").whereSource().isFrom(CarA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("model").whereSource().isFrom(CarA.T) .close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(CarA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("serialNumber").whereSource().isFrom(CarA.T).close(2)
							.whereElementAt(++i).hasType(EntitySignature.T).whereProperty("operand").close(2)
							.whereElementAt(++i).isSourceOnlyPropertyOperand().whereSource().isJoin("carAliasA").close(2)
						.whereCondition().isSingleOperandConjunctionAndOperand()
							.isValueComparison(Operator.equal)
							.whereProperty("leftOperand").hasType(MapKey.T).whereProperty("join").isJoin("carAliasA").close(2)
							.whereProperty("rightOperand").isSourceOnlyPropertyOperand().whereSource().isFrom(CarA.T)
					.endQuery()
				.close()
				.whereProperty("querySet").isDelegateQuerySet("accessA")
					.whereProperty("scalarMappings").isListWithSize(2).close()
					.whereDelegateQuery()
						.whereSelection(2)
							.whereElementAt(0).isPropertyOperand("id").whereSource().isFrom(FlyingCarA.T).close(2)
							.whereElementAt(1).isPropertyOperand("maxFlyingSpeed").whereSource().isFrom(FlyingCarA.T).close(2)
						.whereCondition().isSingleOperandConjunctionAndOperand()
							.isSingleOperandDisjunctionAndOperand()
								.isValueComparison(Operator.equal)
								.whereProperty("leftOperand").isPropertyOperand("id").close(1)
								.whereProperty("rightOperand").isNull()
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(Car.T).close(2)
				.whereElementAt(1).isTupleComponent_(7)
		;
		// @formatter:on
	}

	@Test
	public void entityValueMapWithPolymorphicKeyQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "carLendToA", "o")
				.select().mapKey("o")
				.select("o")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
			.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet").isDelegateQuerySet("accessA")
					.whereProperty("scalarMappings").isListWithSize(13).close()
					.whereDelegateQuery()
						/* Expecting: select string, CarA.id, CarA.maxSpeed, CarA.model, CarA.serialNumber, entitySignature(CarA) 
						 * from PersonA as PersonA, CarA as CarA left join PersonA.carAliasA as string 
						 * where (mapKey(string) = CarA) */
						.whereSelection(13)
							.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(CarA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(CarA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("maxSpeed").whereSource().isFrom(CarA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("model").whereSource().isFrom(CarA.T) .close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(CarA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("serialNumber").whereSource().isFrom(CarA.T).close(2)
							.whereElementAt(++i).hasType(EntitySignature.T).whereProperty("operand").isSourceOnlyPropertyOperand().whereSource().isFrom(CarA.T).close(3)
							.whereElementAt(++i).isPropertyOperand("companyNameA").whereSource().isJoin("carLendToA").close(2)
							.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isJoin("carLendToA").close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isJoin("carLendToA").close(2)
							.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isJoin("carLendToA").close(2)
							.whereElementAt(++i).isPropertyOperand("nickNameX").whereSource().isJoin("carLendToA").close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isJoin("carLendToA").close(2)
						.whereCondition().isSingleOperandConjunctionAndOperand()
							.isValueComparison(Operator.equal)
							.whereProperty("leftOperand").hasType(MapKey.T).whereProperty("join").isJoin("carLendToA").close(2)
							.whereProperty("rightOperand").isSourceOnlyPropertyOperand().whereSource().isFrom(CarA.T)
					.endQuery()
				.close()
				.whereProperty("querySet").isDelegateQuerySet("accessA")
					.whereProperty("scalarMappings").isListWithSize(2).close()
					.whereDelegateQuery()
						.whereSelection(2)
							.whereElementAt(0).isPropertyOperand("id").whereSource().isFrom(FlyingCarA.T).close(2)
							.whereElementAt(1).isPropertyOperand("maxFlyingSpeed").whereSource().isFrom(FlyingCarA.T).close(2)
						.whereCondition().isSingleOperandConjunctionAndOperand()
							.isSingleOperandDisjunctionAndOperand()
								.isValueComparison(Operator.equal)
								.whereProperty("leftOperand").isPropertyOperand("id").close(1)
								.whereProperty("rightOperand").isNull()
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(Car.T).close(2)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T)
		;
		// @formatter:on
	}
}
