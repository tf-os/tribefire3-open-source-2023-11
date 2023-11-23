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
package com.braintribe.model.processing.mpc.evaluator.impl.quantifier;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.mpc.atomic.MpcTrue;
import com.braintribe.model.mpc.quantifier.MpcQuantifier;
import com.braintribe.model.mpc.quantifier.MpcQuantifierStrategy;
import com.braintribe.model.mpc.structure.MpcElementType;
import com.braintribe.model.mpc.structure.MpcPropertyName;
import com.braintribe.model.mpc.structure.MpcSequence;
import com.braintribe.model.processing.MpGenerator;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.AbstractMpcTest;

/**
 * Provides tests for {@link MpcQuantifierEvaluator} as well as indirect testing for the backtracking mechanism of MPC.matches
 * 
 */
public class MpcQuantifierEvaluatorTest extends AbstractMpcTest{

	
	@Test
	public void testGreedyAsteriskQuantifierStandAloneMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.asteriskQuantifier(MpcQuantifierStrategy.greedy, nameCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(quantifiedCondition, path);
		
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
	public void testReluctantAsteriskQuantifierStandAloneMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.asteriskQuantifier(MpcQuantifierStrategy.reluctant, nameCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(quantifiedCondition, path);
		
		assertThat(evaluationResult).isNotNull();
		assertThat(evaluationResult.getPath()).isNotNull();
		// the result of the evaluation should be a match with the full path as it is, i.e. like No consumption of path and matching nothing as minRep is 0
		assertThat(evaluationResult.getPath().getDepth()).isEqualTo(path.getDepth());
	}
	
	
	@Test
	public void testPossessiveAsteriskQuantifierStandAloneNoMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.asteriskQuantifier(MpcQuantifierStrategy.possessive, nameCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(quantifiedCondition, path);

		// all path is consumed, so the last element failed, hence null
		assertThat(evaluationResult).isNull();
	}
	
	@Test
	public void testGreedyPlusQuantifierStandAloneMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.plusQuantifier(MpcQuantifierStrategy.greedy, nameCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(quantifiedCondition, path);
		
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
	public void testReluctantPlusQuantifierStandAloneMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.plusQuantifier(MpcQuantifierStrategy.reluctant, nameCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(quantifiedCondition, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, nameCondition  );
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);
				
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath().getDepth()).isEqualTo(2);
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testPossessivePlusQuantifierStandAloneNoMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.plusQuantifier(MpcQuantifierStrategy.possessive, nameCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(quantifiedCondition, path);
		
		// all path is consumed, so the last element failed, hence null
		assertThat(evaluationResult).isNull();
	}
	
	
	@Test
	public void testGreedyQuestionQuantifierStandAloneMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.questionQuantifier(MpcQuantifierStrategy.greedy, nameCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(quantifiedCondition, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, nameCondition );
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath().getDepth()).isEqualTo(2);
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
		
	}
	
	
	@Test
	public void testReluctantQuestionQuantifierStandAloneMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.questionQuantifier(MpcQuantifierStrategy.reluctant, nameCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(quantifiedCondition, path);
		
		assertThat(evaluationResult).isNotNull();
		assertThat(evaluationResult.getPath()).isNotNull();
		// the result of the evaluation should be a match with the full path as it is, i.e. like No consumption of path and matching nothing as minRep is 0
		assertThat(evaluationResult.getPath().getDepth()).isEqualTo(path.getDepth());
	}
	
	@Test
	public void testPossessiveQuestionQuantifierStandAloneMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.questionQuantifier(MpcQuantifierStrategy.possessive, nameCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(quantifiedCondition, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, nameCondition );
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath().getDepth()).isEqualTo(2);
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
		
	}
	
	@Test
	public void testGreedyAsteriskQuantifierAtEndOfSequenceMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.asteriskQuantifier(MpcQuantifierStrategy.greedy, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testReluctantAsteriskQuantifierAtEndOfSequenceMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.asteriskQuantifier(MpcQuantifierStrategy.reluctant, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testPossessiveAsteriskQuantifierAtEndOfSequenceNoMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.asteriskQuantifier(MpcQuantifierStrategy.possessive, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		assertThat(evaluationResult).isNull();
	}
	
	@Test
	public void testGreedyPlusQuantifierAtEndOfSequenceMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.plusQuantifier(MpcQuantifierStrategy.greedy, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	
	@Test
	public void testReluctantPlusQuantifierAtEndOfSequenceMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.plusQuantifier(MpcQuantifierStrategy.reluctant, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	
	@Test
	public void testPossessivePlusQuantifierAtEndOfSequenceNoMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.plusQuantifier(MpcQuantifierStrategy.possessive, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		assertThat(evaluationResult).isNull();
	}
	
	
	@Test
	public void testGreedyQuestionQuantifierAtEndOfSequenceNoMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.questionQuantifier(MpcQuantifierStrategy.greedy, nameCondition);
		
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition );
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(expectedResult).isNull();
		assertThat(evaluationResult).isEqualTo(expectedResult);
	}
	
	@Test
	public void testReluctantQuestionQuantifierAtEndOfSequenceNoMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.questionQuantifier(MpcQuantifierStrategy.reluctant, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition );
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(expectedResult).isNull();
		assertThat(evaluationResult).isEqualTo(expectedResult);
	}
	
	
	@Test
	public void testPossessiveQuestionQuantifierAtEndOfSequenceMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.questionQuantifier(MpcQuantifierStrategy.possessive, nameCondition);
		// there is no sequence consisting of only one nameCondition then root, so it condition fails, but quantifier works
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		assertThat(evaluationResult).isNull();
	}
	
	
	@Test
	public void testGreedyAsteriskQuantifierInMiddleOfSequenceMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.asteriskQuantifier(MpcQuantifierStrategy.greedy, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, nameCondition , quantifiedCondition, nameCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testReluctantAsteriskQuantifierInMiddleOfSequenceMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.asteriskQuantifier(MpcQuantifierStrategy.reluctant, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, nameCondition , quantifiedCondition, nameCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testPossessiveAsteriskQuantifierInMiddleOfSequenceNoMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.asteriskQuantifier(MpcQuantifierStrategy.possessive, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, nameCondition , quantifiedCondition, nameCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		assertThat(evaluationResult).isNull();
	}
	
	@Test
	public void testGreedyPlusQuantifierInMiddleOfSequenceMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.plusQuantifier(MpcQuantifierStrategy.greedy, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, nameCondition , quantifiedCondition, nameCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testReluctantPlusQuantifierInMiddleOfSequenceMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.plusQuantifier(MpcQuantifierStrategy.reluctant, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, nameCondition , quantifiedCondition, nameCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testPossessivePlusQuantifierInMiddleOfSequenceNoMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.plusQuantifier(MpcQuantifierStrategy.possessive, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, nameCondition , quantifiedCondition, nameCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		assertThat(evaluationResult).isNull();
	}
	
	@Test
	public void testGreedyQuestionQuantifierInMiddleOfSequenceMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.questionQuantifier(MpcQuantifierStrategy.greedy, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, nameCondition , quantifiedCondition, nameCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testReluctantQuestionQuantifierInMiddleOfSequenceMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.questionQuantifier(MpcQuantifierStrategy.reluctant, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, nameCondition , quantifiedCondition, nameCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testPossessiveQuestionQuantifierInMiddleOfSequenceMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.questionQuantifier(MpcQuantifierStrategy.possessive, nameCondition);
		// all path will be consumed
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, nameCondition , quantifiedCondition, nameCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);
		
		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	
	@Test
	public void testPossessiveAsteriskQuantifierStandAloneWithTrueMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcTrue trueCondition = $.trueLiteral(false);
		
		MpcQuantifier quantifiedCondition = $.asteriskQuantifier(MpcQuantifierStrategy.possessive, trueCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(quantifiedCondition, path);

		assertThat(evaluationResult).isNotNull();
		assertThat(evaluationResult.getPath()).isNull();
	}
	
	@Test
	public void testGreedyMinZeroMaxZeroQuantifierMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, nameCondition, 0, 0);
		
		MpcMatch evaluationResult = MPC.mpcMatches(quantifiedCondition, path);

		assertThat(evaluationResult).isNotNull();
		assertThat(evaluationResult.getPath()).isNotNull();
		// the result of the evaluation should be a match with the full path as it is, i.e. like No consumption of path and matching nothing as minRep is 0
		assertThat(evaluationResult.getPath().getDepth()).isEqualTo(path.getDepth());
	}
	
	@Test(expected = MpcEvaluatorRuntimeException.class)
	public void testInvalidMinMaxQuantifierParameters() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, nameCondition, -1, -1);
		
		MPC.mpcMatches(quantifiedCondition, path);
	}
	
	@Test(expected = MpcEvaluatorRuntimeException.class)
	public void testInvalidMinMaxRangeQuantifierParameters() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		
		MpcQuantifier quantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, nameCondition, 4, 2);
		
		MPC.mpcMatches(quantifiedCondition, path);
	}
	
	@Test
	public void testMinTwoMaxTwoQuantifierMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, nameCondition, 2, 2);
		
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition, nameCondition );
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);

		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testGreedyMinTwoMaxThreeQuantifierMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, nameCondition, 2, 3);
		
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);

		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testReluctantMinTwoMaxThreeQuantifierMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.quantifier(MpcQuantifierStrategy.reluctant, nameCondition, 2, 3);
		
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);

		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testPossessiveMinTwoMaxThreeQuantifierMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier quantifiedCondition = $.quantifier(MpcQuantifierStrategy.possessive, nameCondition, 2, 3);
		
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, quantifiedCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);

		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	@Test
	public void testQuantifierOfQuantifierMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier firstQuantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, nameCondition, 0, null);
		
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, firstQuantifiedCondition);
		
		MpcQuantifier secondQuantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, seqConditionWithQuantifier, 1, 1);		
		
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
	
	
	@Test
	public void testQuantifierOfQuantifierNoMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		
		MpcQuantifier firstQuantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, nameCondition, 0, 3);
		
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, firstQuantifiedCondition);
		
		MpcQuantifier secondQuantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, seqConditionWithQuantifier, 2, 2);
			
		MpcMatch evaluationResult = MPC.mpcMatches(secondQuantifiedCondition, path);
		
		assertThat(evaluationResult).isNull();
	}
	
	@Test
	public void testTwoGreedyQuantifiersInSameScopeMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		MpcTrue trueLiteral = $.trueLiteral(false);
		
		MpcQuantifier firstQuantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, nameCondition, 0, null);
		
		MpcQuantifier secondQuantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, trueLiteral, 0, null);		
		
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, nameCondition, firstQuantifiedCondition, nameCondition, secondQuantifiedCondition);
		
		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);

		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	
	@Test
	public void testThreeGreedyQuantifiersInSameScopeMatch() throws Exception {
		
		IModelPathElement path = MpGenerator.getSimpleReptitivePath();
		MpcPropertyName nameCondition = $.propertyNameCondition("descendant");
		MpcElementType rootCondition = $.root();
		MpcTrue trueLiteral = $.trueLiteral(false);
		
		MpcQuantifier firstQuantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, nameCondition, 1, null);
		
		MpcQuantifier secondQuantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, trueLiteral, null, null);	
		
		MpcQuantifier thirdQuantifiedCondition = $.quantifier(MpcQuantifierStrategy.greedy, nameCondition, 1, null);
		
		MpcSequence seqConditionWithQuantifier = $.sequence(false, rootCondition, firstQuantifiedCondition, secondQuantifiedCondition, nameCondition, thirdQuantifiedCondition);

		MpcMatch evaluationResult = MPC.mpcMatches(seqConditionWithQuantifier, path);

		MpcSequence seqConditionWithoutQuantifier = $.sequence(false, rootCondition, nameCondition , nameCondition, nameCondition);
		
		MpcMatch expectedResult = MPC.mpcMatches(seqConditionWithoutQuantifier, path);		
		
		assertThat(evaluationResult).isNotNull();
		assertThat(expectedResult).isNotNull();
		// check that the expected result is actually correct
		assertThat(expectedResult.getPath()).isNull();
		// validate that the evaluation Result is the same as the expected result 
		assertThat(evaluationResult.getPath()).isEqualTo(expectedResult.getPath());
	}
	
	// maybe add test cases for
	// disj( seq (quant()), quant())
	
}
