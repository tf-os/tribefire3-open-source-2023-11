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
package com.braintribe.model.processing.mpc.evaluator.impl.generic;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.mpc.logic.MpcConjunction;
import com.braintribe.model.mpc.logic.MpcJunctionCapture;
import com.braintribe.model.mpc.quantifier.MpcQuantifier;
import com.braintribe.model.mpc.quantifier.MpcQuantifierStrategy;
import com.braintribe.model.mpc.structure.MpcElementType;
import com.braintribe.model.mpc.structure.MpcPropertyName;
import com.braintribe.model.mpc.structure.MpcSequence;
import com.braintribe.model.processing.MpGenerator;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.AbstractMpcTest;


/**
 * Provides tests for {@link MpcGenericVde}.
 * 
 */
public class MpcGenericVdeTest  extends AbstractMpcTest{


	/**
	 * Tests the abstract injection of MPC into VDE
	 * @throws Exception
	 */
	@Test
	public void testMPCInjectionWithEvaluateTrue() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();
		
		MpcConjunction condition = $.conjunction(MpcJunctionCapture.first, $.trueLiteral(false),$.evaluate($.trueLiteral(false)));
		
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);
		
		assertThat(evaluationResult).isNotNull();
		assertThat(evaluationResult.getPath()).isNotNull();
		assertThat(evaluationResult.getPath().getDepth()).isEqualTo(0);
	
	}

	@Test
	public void testMPCInjectionWithEvaluateQuantifier() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.asteriskQuantifier(MpcQuantifierStrategy.greedy, nameCondition);
		
		MpcConjunction condition = $.conjunction(MpcJunctionCapture.first, $.evaluate(quantifiedCondition),$.trueLiteral(false));
		
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, nameCondition , nameCondition , nameCondition );
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath().getDepth()).isEqualTo(0);
		// validate that the evaluation Result is the same as the expected result
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testtMPCInjectionWithQuantifierOfEvaluateQuantifier() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier firstQuantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, nameCondition, 0, null);
		
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, firstQuantifiedCondition);
		
		MpcQuantifier secondQuantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, $.evaluate(seqConditionWithQuantifier), 1, 1);		
		
		MpcMatch evaluationResult = MPC.mpcMatches(secondQuantifiedCondition, path);

		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	
}
