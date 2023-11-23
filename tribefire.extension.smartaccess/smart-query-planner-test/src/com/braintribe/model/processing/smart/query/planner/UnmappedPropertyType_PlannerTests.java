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

import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ReaderA;
import com.braintribe.model.processing.query.smart.test.model.accessB.special.BookB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartManualA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartPublicationB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartReaderA;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;

/**
 * 
 */
public class UnmappedPropertyType_PlannerTests extends AbstractSmartQueryPlannerTests {

	// ########################################################
	// ## . . . . . IKPA defined on unmapped types . . . . . ##
	// ########################################################

	@Test
	public void selectUnmappedTypeProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "favoritePublication")
				.from(SmartReaderA.T, "r")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.isDelegateQueryJoin_Left()
					.whereProperty("materializedSet")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isPropertyOperand("favoritePublicationTitle").whereSource().isFrom(ReaderA.T).close(2)
						.endQuery()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessS")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(SmartPublicationB.T)
							.whereSelection(6)
								.whereElementAt(i=0).isPropertyOperand("author").whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand("title").whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).hasType(EntitySignature.T)
									.whereProperty("operand").isSourceOnlyPropertyOperand().whereSource().isFrom(SmartPublicationB.T).close(2)
						.endQuery()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessS")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SmartBookB.T)
						.whereSelection(3)
								.whereElementAt(0).isPropertyOperand("id").whereSource().isFrom(SmartBookB.T).close(2)
								.whereElementAt(1).isPropertyOperand("isbn").whereSource().isFrom(SmartBookB.T).close(2)
								.whereElementAt(2).isPropertyOperand("numberOfPages").whereSource().isFrom(SmartBookB.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPublicationB.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void selectUnmappedTypeProperty_Set() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "favoritePublications")
				.from(SmartReaderA.T, "r")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.isDelegateQueryJoin()
					.whereProperty("materializedSet")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isSourceOnlyPropertyOperand().whereSource().isJoin("favoritePublicationTitles").whereSource().isFrom(ReaderA.T)
						.endQuery()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessS")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(SmartPublicationB.T)
							.whereSelection(6)
								.whereElementAt(i=0).isPropertyOperand("author").whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand("title").whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).hasType(EntitySignature.T)
									.whereProperty("operand").isSourceOnlyPropertyOperand().whereSource().isFrom(SmartPublicationB.T).close(2)
						.endQuery()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessS")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SmartBookB.T)
						.whereSelection(3)
								.whereElementAt(0).isPropertyOperand("id").whereSource().isFrom(SmartBookB.T).close(2)
								.whereElementAt(1).isPropertyOperand("isbn").whereSource().isFrom(SmartBookB.T).close(2)
								.whereElementAt(2).isPropertyOperand("numberOfPages").whereSource().isFrom(SmartBookB.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPublicationB.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void selectUnmappedTypePropertyWithSignatureCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "favoritePublication")
				.from(SmartReaderA.T, "r")
				.where()
					.entitySignature("r", "favoritePublication").eq(SmartBookA.class.getName())
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isPropertyOperand("favoritePublicationTitle").whereSource().isFrom(ReaderA.T).close(2)
						.endQuery()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessS")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(SmartPublicationB.T)
							.whereSelection(6)
								.whereElementAt(i=0).isPropertyOperand("author").whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand("title").whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).hasType(EntitySignature.T)
									.whereProperty("operand").isSourceOnlyPropertyOperand().whereSource().isFrom(SmartPublicationB.T).close(2)
							.whereCondition().isConjunction(2)
								.whereElementAt(0).isValueComparison(Operator.equal)
									.whereProperty("leftOperand").close()
									.whereProperty("rightOperand").is_(SmartBookA.class.getName())
								.close()
								.whereElementAt(1).isDisjunction(1)
									.whereElementAt(0).isValueComparison(Operator.equal)
										.whereProperty("leftOperand").isPropertyOperand("title").close()
						.endQuery()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessS")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SmartBookB.T)
						.whereSelection(3)
								.whereElementAt(0).isPropertyOperand("id").whereSource().isFrom(SmartBookB.T).close(2)
								.whereElementAt(1).isPropertyOperand("isbn").whereSource().isFrom(SmartBookB.T).close(2)
								.whereElementAt(2).isPropertyOperand("numberOfPages").whereSource().isFrom(SmartBookB.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPublicationB.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void selectUnmappedTypePropertyWithEntityCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "favoritePublication")
				.from(SmartReaderA.T, "r")
				.where()
					.property("r", "favoritePublication").eq().entityReference(reference(SmartBookA.class, SmartMappingSetup.accessIdA, 99L))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.isDelegateQueryJoin_Inner()
					.whereProperty("materializedSet")
						.isDelegateQuerySet("accessS")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(SmartPublicationB.T)
							.whereSelection(6)
								.whereElementAt(i=0).isPropertyOperand("author").whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).isPropertyOperand("title").whereSource().isFrom(SmartPublicationB.T).close(2)
								.whereElementAt(++i).hasType(EntitySignature.T)
									.whereProperty("operand").isSourceOnlyPropertyOperand().whereSource().isFrom(SmartPublicationB.T).close(2)
							.whereCondition().isConjunction(1)
								.whereElementAt(0).isValueComparison(Operator.equal)
									.whereProperty("leftOperand").close()
									.whereProperty("rightOperand").isReference_(SmartBookA.T, 99L, accessIdA)
						.endQuery()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isPropertyOperand("favoritePublicationTitle").whereSource().isFrom(ReaderA.T).close(2)
						.endQuery()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessS")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SmartBookB.T)
						.whereSelection(3)
								.whereElementAt(0).isPropertyOperand("id").whereSource().isFrom(SmartBookB.T).close(2)
								.whereElementAt(1).isPropertyOperand("isbn").whereSource().isFrom(SmartBookB.T).close(2)
								.whereElementAt(2).isPropertyOperand("numberOfPages").whereSource().isFrom(SmartBookB.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPublicationB.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void selectAsIsMappedPropertyAndNotSmartReferenceUseCase() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "favoriteManual")
				.from(SmartReaderA.T, "r")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.whereDelegateQuery()
					.whereFroms(1).whereElementAt(0).isFrom(ReaderA.T)
					.whereSelection(8)
						.whereElementAt(i=0).isPropertyOperand("author").whereSource().isJoin("favoriteManual").close(2)
						.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).whereSource().isJoin("favoriteManual").close(2)
						.whereElementAt(++i).isPropertyOperand("id").whereSource().isJoin("favoriteManual").close(2)
						.whereElementAt(++i).isPropertyOperand("isbn").whereSource().isJoin("favoriteManual").close(2)
						.whereElementAt(++i).isPropertyOperand("manualString").whereSource().isJoin("favoriteManual").close(2)
						.whereElementAt(++i).isPropertyOperand("numberOfPages").whereSource().isJoin("favoriteManual").close(2)
						.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isJoin("favoriteManual").close(2)
						.whereElementAt(++i).isPropertyOperand("title").whereSource().isJoin("favoriteManual").close(2)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartManualA.T)
		;
		// @formatter:on
	}

	// ########################################################
	// ## . . . . . IKPA defined on unmapped types . . . . . ##
	// ########################################################

	/**
	 * The thing is, the {@link InverseKeyPropertyAssignment} is not defined on {@link SmartBookB}, but on it's super-type
	 * {@link SmartPublicationB}, which is a non-mapped type. So there we use {@link SmartPublicationB#getTitle()} as the keyProperty, which
	 * is not a delegate property, thus this has to be handled in a special way (to resolve the actual {@link BookB#setTitleB(String)}
	 * property).
	 */
	@Test
	public void ikpaJoinConfiguredOnUnmappedType_RecognizeActualTypeIsMapped() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartBookB.T, "b")
				.select("b", "favoriteReader")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isPropertyOperand("titleB").whereSource().isFrom(BookB.T).close(2)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(ReaderA.T)
						.whereSelection(5)
							.whereElementAt(i=0).isPropertyOperand(GenericEntity.globalId).whereSource().isFrom(ReaderA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(ReaderA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("ikpaPublicationTitle").whereSource().isFrom(ReaderA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("name").whereSource().isFrom(ReaderA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isFrom(ReaderA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartReaderA.T)
		;
		// @formatter:on
	}

	/**
	 * Unlike in {@link #ikpaJoinConfiguredOnUnmappedType_RecognizeActualTypeIsMapped()} test, here we take the
	 * favoriteReader property from {@link SmartPublicationB} (and not {@link SmartBookB}). This means we see the IKPA
	 * really from {@link SmartPublicationB} point of view, which means the smart access delegates to itself.
	 */
	@Test
	public void ikpaJoinConfiguredOnUnmappedType_DoQueryOnSelf() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartReaderA.T, "r")
				.select("r", "favoritePublication.favoriteReader")
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
							.whereElementAt(0).isPropertyOperand("favoritePublicationTitle").whereSource().isFrom(ReaderA.T).close(2)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessS")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SmartPublicationB.T).whereProperty("joins").isSetWithSize(1).whereFirstElement().isJoin("favoriteReader")
						.whereSelection(5)
							.whereElementAt(i=0).isPropertyOperand("title").whereSource().isFrom(SmartPublicationB.T).close(2)
							.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).whereSource().isJoin("favoriteReader").close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isJoin("favoriteReader").close(2)
							.whereElementAt(++i).isPropertyOperand("name").whereSource().isJoin("favoriteReader").close(2)
							.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isJoin("favoriteReader").close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartReaderA.T)
		;
		// @formatter:on
	}

}
