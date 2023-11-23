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
package com.braintribe.model.processing.traversing.engine.impl.clone;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.IsTypeKind;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.mpc.ModelPathCondition;
import com.braintribe.model.mpc.logic.MpcJunctionCapture;
import com.braintribe.model.mpc.value.MpcElementAxis;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.builder.api.MpcBuilder;
import com.braintribe.model.processing.traversing.api.GmTraversingSkippingCriteria;
import com.braintribe.model.processing.traversing.engine.GMT;
import com.braintribe.model.processing.traversing.engine.api.usecase.AbsentifySkipUseCase;
import com.braintribe.model.processing.traversing.engine.impl.misc.model.ComplexTraversingObject;
import com.braintribe.model.processing.traversing.engine.impl.misc.model.SimpleTraversingObject;
import com.braintribe.model.processing.traversing.engine.impl.skip.conditional.MpcConfigurableSkipper;
import com.braintribe.model.processing.traversing.engine.impl.walk.BasicModelWalkerCustomization;
import com.braintribe.model.processing.traversing.engine.impl.walk.ModelWalker;

/**
 * Tests for {@link Cloner}
 * 
 */
public class ClonerTest extends AbstractClonerTest {

	static final IsTypeKind isSimpleType = TypeConditions.isKind(TypeKind.simpleType);
	static final IsTypeKind isCollectionType = TypeConditions.isKind(TypeKind.collectionType);

	private static final MpcBuilder $ = MPC.builder();

	@Test
	public void testCloneRootWithSimpleTypeAndList() throws Exception {
		Object assembly = getAssemblyComplexObjectWithList();

		// @formatter:off
		TraversingCriterion tc = TC.create().negation()
												.disjunction()
													.root()
													.typeCondition(isSimpleType)
													.pattern()
														.root()
														.entity()
														.property("listComplex")
													.close()
													.listElement()
												.close()
											.done();
		// @formatter:on

		MpcConfigurableSkipper skipper = new MpcConfigurableSkipper();

		// @formatter:off
		ModelPathCondition condition = $.negation(
												$.disjunction(	MpcJunctionCapture.none, 
														$.root(),
														$.matchesType(MpcElementAxis.value, isSimpleType), 
														$.sequence( $.root(),
																	$.property("listComplex")),
														$.listItem()
													)
										);
		// @formatter:on

		skipper.setCondition(condition);
		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);

		compareClonersWithThreeSkipUseCase(skipper, assembly, tc, ComplexTraversingObject.T);
	}

	@Test
	public void testCloneRootWithSimpleTypeAndSet() throws Exception {
		Object assembly = getAssemblyComplexObjectWithSet();

		// @formatter:off
		TraversingCriterion tc = TC.create().negation()
												.disjunction()
													.root()
													.typeCondition(isSimpleType)
													.pattern()
														.root()
														.entity()
														.property("setComplex")
													.close()
													.setElement()
												.close()
											.done();
		// @formatter:on

		MpcConfigurableSkipper skipper = new MpcConfigurableSkipper();

		// @formatter:off
		ModelPathCondition condition = $.negation(
												$.disjunction(	MpcJunctionCapture.none, 
													$.root(),
													$.matchesType(MpcElementAxis.value, isSimpleType), 
													$.sequence( $.root(),
																$.property("setComplex")),
													$.setItem()
												)
										);
		// @formatter:on

		skipper.setCondition(condition);
		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);

		compareClonersWithThreeSkipUseCase(skipper, assembly, tc, ComplexTraversingObject.T);
	}

	@Test
	public void testCloneRootWithSimpleTypeAndObject() throws Exception {
		Object assembly = getAssemblyComplexObjectWithObjectAndList();

		// @formatter:off
		TraversingCriterion tc = TC.create().negation()
												.disjunction()
													.root()
													.typeCondition(isSimpleType)
													.pattern()
														.root()
														.entity()
														.property("simpleObject")
													.close()
												.close()
											.done();
		// @formatter:on

		MpcConfigurableSkipper skipper = new MpcConfigurableSkipper();

		// @formatter:off
		ModelPathCondition condition = $.negation(
												$.disjunction(	MpcJunctionCapture.none, 
													$.root(),
													$.matchesType(MpcElementAxis.value, isSimpleType), 
													$.sequence($.root(),
															$.property("simpleObject")
													)
												)
										);
		// @formatter:on

		skipper.setCondition(condition);
		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);

		compareClonersWithThreeSkipUseCase(skipper, assembly, tc, ComplexTraversingObject.T);
	}

	@Test
	public void testCloneRootWithSimpleTypes() throws Exception {
		Object assembly = getAssemblySimpleObject();

		// @formatter:off
		TraversingCriterion tc = TC.create().negation()
												.disjunction()
													.root()
													.typeCondition(isSimpleType)
												.close()
											.done();
		// @formatter:on

		MpcConfigurableSkipper skipper = new MpcConfigurableSkipper();

		// @formatter:off
		ModelPathCondition condition = $.negation(
												$.disjunction(	MpcJunctionCapture.none, 
													$.root(),
													$.matchesType(MpcElementAxis.value, isSimpleType)
												)
										);
		// @formatter:on

		skipper.setCondition(condition);
		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);

		compareClonersWithThreeSkipUseCase(skipper, assembly, tc, SimpleTraversingObject.T);
	}

	@Test
	public void testCloneRootWithoutSimpleTypes() throws Exception {

		Object assembly = getAssemblySimpleObject();

		TraversingCriterion tc = TC.create().typeCondition(isSimpleType).done();

		MpcConfigurableSkipper skipper = new MpcConfigurableSkipper();

		ModelPathCondition condition = $.matchesType(MpcElementAxis.value, isSimpleType);

		skipper.setCondition(condition);
		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);

		// only test Skip and Absentify as the Reference case does not exist in the old cloner
		compareClonersWithThreeSkipUseCase(skipper, assembly, tc, SimpleTraversingObject.T, new boolean[] { true, true, false });

	}

	@Test
	public void testCloneRootWithComplexTypes() throws Exception {
		Object assembly = getAssemblyComplexObjectWithCollections();

		// @formatter:off
		TraversingCriterion tc = TC.create().negation()
												.disjunction()
													.root()
													.listElement()
													.setElement()
													.mapKey()
													.mapValue()
													.typeCondition(isCollectionType)
												.close()
											.done();
		// @formatter:on

		MpcConfigurableSkipper skipper = new MpcConfigurableSkipper();

		// @formatter:off
		ModelPathCondition condition = $.negation(
												$.disjunction(	MpcJunctionCapture.none, 
													$.root(),
													$.listItem(),
													$.setItem(),
													$.mapKey(),
													$.mapValue(),
													$.matchesType(MpcElementAxis.value, isCollectionType)
												)
										);
		// @formatter:on

		skipper.setCondition(condition);
		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);

		compareClonersWithThreeSkipUseCase(skipper, assembly, tc, ComplexTraversingObject.T);
	}

	@Test
	public void testCloneRootWithoutComplexTypes() throws Exception {
		Object assembly = getAssemblyComplexObjectWithCollections();

		TraversingCriterion tc = TC.create().typeCondition(isCollectionType).done();

		MpcConfigurableSkipper skipper = new MpcConfigurableSkipper();

		ModelPathCondition condition = $.matchesType(MpcElementAxis.value, isCollectionType);

		skipper.setCondition(condition);
		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);

		compareClonersWithThreeSkipUseCase(skipper, assembly, tc, ComplexTraversingObject.T);
	}

	@Test
	public void testCloneRootWithListOfNull() throws Exception {
		Object assembly = getAssemblyComplexObjectWithCollections();

		// @formatter:off
		TraversingCriterion tc = TC.create().pattern()
												.root()
												.entity()
												.property("listComplex")
												.listElement()
												.close()
											.done();
		// @formatter:on

		MpcConfigurableSkipper skipper = new MpcConfigurableSkipper();

		// @formatter:off
		ModelPathCondition condition = $.sequence(	$.root(),
													$.property("listComplex"),
													$.listItem()
												);
		// @formatter:on

		skipper.setCondition(condition);
		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);

		// only test Skip and Absentify as the Reference case does not exist in the old cloner
		compareClonersWithThreeSkipUseCase(skipper, assembly, tc, ComplexTraversingObject.T, new boolean[] { true, true, false });
	}

	@Ignore
	@Test
	public void testHugeBreadth() throws Exception {
		Object assembly = getAssemblyStressComplexObjectWithCollections(30000, 1);

		// @formatter:off
		TraversingCriterion tc = TC.create().negation()
												.disjunction()
													.root()
													.listElement()
													.setElement()
													.mapKey()
													.mapValue()
													.typeCondition(isCollectionType)
												.close()
											.done();
		// @formatter:on

		MpcConfigurableSkipper skipper = new MpcConfigurableSkipper();

		// @formatter:off
		ModelPathCondition condition = $.negation(
												$.disjunction(	MpcJunctionCapture.none, 
													$.root(),
													$.listItem(),
													$.setItem(),
													$.mapKey(),
													$.mapValue(),
													$.matchesType(MpcElementAxis.value, isCollectionType)
												)
										);
		// @formatter:on

		skipper.setCondition(condition);
		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);

		// comparison Will not compare results as there will be discrepancy due to ordering of map and set elements
		compareClonersWithThreeSkipUseCase(skipper, assembly, tc, ComplexTraversingObject.T);

	}

	@Ignore
	@Test
	public void testHugeDepth() throws Exception {
		Object assembly = getAssemblyStressComplexObjectWithCollections(1, 3);

		// @formatter:off
		TraversingCriterion tc = TC.create().negation()
												.disjunction()
													.root()
													.listElement()
													.setElement()
													.mapKey()
													.mapValue()
													.typeCondition(isCollectionType)
												.close()
											.done();
		// @formatter:on

		MpcConfigurableSkipper skipper = new MpcConfigurableSkipper();

		// @formatter:off
		ModelPathCondition condition = $.negation(
												$.disjunction(	MpcJunctionCapture.none, 
													$.root(),
													$.listItem(),
													$.setItem(),
													$.mapKey(),
													$.mapValue(),
													$.matchesType(MpcElementAxis.value, isCollectionType)
												)
										);
		// @formatter:on

		skipper.setCondition(condition);
		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);

		// comparison Will not compare results as there will be discrepancy due to ordering of map and set elements
		compareClonersWithThreeSkipUseCase(skipper, assembly, tc, ComplexTraversingObject.T);
	}

	@Test
	public void testCloneRootWithAbscenceInformation() throws Exception {

		Object assembly = getAssemblyComplexObjectWithList();

		MpcConfigurableSkipper skipper = new MpcConfigurableSkipper();

		// @formatter:off
		ModelPathCondition condition = $.negation(
												$.disjunction(	MpcJunctionCapture.none, 
														$.root(),
														$.matchesType(MpcElementAxis.value, isCollectionType), 
														$.sequence( $.root(),
																	$.property("listComplex")),
														$.listItem()
													)
										);
		// @formatter:on

		skipper.setCondition(condition);
		skipper.setSkipUseCase(AbsentifySkipUseCase.INSTANCE);
		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);

		Object clonedAssembly = cloneWithOneSkipper(skipper, assembly);

		skipper.setSkipUseCase(AbsentifySkipUseCase.INSTANCE);
		// TODO check this case
		System.out.println(printOriginalEntity((GenericEntity) clonedAssembly, ComplexTraversingObject.T));
		// AI is ignored by walker, so they are not present to the cloner in the first place
		Object clonedObject = cloneWithOneSkipper(skipper, clonedAssembly);

		ModelWalker modelWalker = new ModelWalker();
		BasicModelWalkerCustomization basicCustomisation = new BasicModelWalkerCustomization();
		basicCustomisation.setAbsenceTraversable(true);
		modelWalker.setWalkerCustomization(basicCustomisation);
		modelWalker.setBreadthFirst(true);

		Cloner cloner = new Cloner();
		GMT.traverse().customWalk(modelWalker).visitor(skipper).visitor(cloner).doFor(assembly);

		// this can be tested via the new changes to the modelWalker customization

		System.out.println(printEntity((GenericEntity) clonedObject, ComplexTraversingObject.T));

	}

	@Test
	public void testCloneFull() throws Exception {
		Object assembly = getAssemblyComplexObjectWithCollections();
		Object clonedAssembly = cloneWithNoSkipper(assembly);

		compareCloningResults(assembly, clonedAssembly, ComplexTraversingObject.T);

	}

}
