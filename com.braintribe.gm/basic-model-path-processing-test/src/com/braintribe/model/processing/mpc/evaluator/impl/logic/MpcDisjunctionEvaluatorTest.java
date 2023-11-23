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
package com.braintribe.model.processing.mpc.evaluator.impl.logic;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.mpc.logic.MpcDisjunction;
import com.braintribe.model.mpc.logic.MpcJunctionCapture;
import com.braintribe.model.path.GmModelPathElementType;
import com.braintribe.model.processing.MpGenerator;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;

/**
 * Provides tests for {@link MpcDisjunctionEvaluator}.
 * 
 */
public class MpcDisjunctionEvaluatorTest extends MpcJunctionEvaluatorTest {

	/**
	 * Validate that a {@link MpcDisjunction} condition of two operands will MATCH against a
	 * {@link IModelPathElement} which consists of "Root.favouriteColours[1]" ( {@link MpGenerator#getTernaryPath()}).
	 */
	@Test
	public void testMatchingMultipleOperands() throws Exception {

		IModelPathElement path = MpGenerator.getTernaryPath();
		MpcDisjunction condition = $.disjunction(MpcJunctionCapture.first, 
															$.elementTypeCondition(GmModelPathElementType.ListItem),
															$.negation($.elementTypeCondition(GmModelPathElementType.ListItem)));

		int junctionCaptureLength = MpcJunctionCapture.values().length;

		for (int i = 0; i < junctionCaptureLength; i++) {
			MpcJunctionCapture currentJC = getJunctionCapture(i);
			condition.setJunctionCapture(currentJC);

			MpcMatch evaluationResult = multipleOperandSuccessTest(condition, path);
			
			//validate output
			assertThat(evaluationResult).isNotNull();
			switch (currentJC) {
				case first:
				case shortest:
				case longest:
				case last:
					assertThat(evaluationResult.getPath().getDepth()).isEqualTo(1);
					break;
				case none:
					assertThat(evaluationResult.getPath().getDepth()).isEqualTo(2);
					break;
				// unknown junction capture
				default:
					fail("Unsupported Junction Capture: " + currentJC);
			}
		}
	}

	/**
	 * Validate that a {@link MpcDisjunction} condition of multiple operands will NOT MATCH against a
	 * {@link IModelPathElement} which consists of "Root.name" ( {@link MpGenerator#getSimplePath()}).
	 */
	@Test
	public void testNotMatchingMultipleOperands() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();
		MpcDisjunction condition = $.disjunction(MpcJunctionCapture.first,
															$.elementTypeCondition(GmModelPathElementType.ListItem),
															$.negation($.elementTypeCondition(GmModelPathElementType.Property)));

		int junctionCaptureLength = MpcJunctionCapture.values().length;

		for (int i = 0; i < junctionCaptureLength; i++) {
			MpcJunctionCapture currentJC = getJunctionCapture(i);
			condition.setJunctionCapture(currentJC);

			MpcMatch evaluationResult = multipleOperandFailureTest(condition, path);

			// validate output
			switch (currentJC) {
				case shortest:
				case longest:
				case last:
				case first:
				case none:
					assertThat(evaluationResult).isNull();
					break;
				// unknown junction capture
				default:
					fail("Unsupported Junction Capture: " + currentJC);
			}
		}
	}


	/**
	 * Validate that a {@link MpcDisjunction} condition of single operand will NOT MATCH against a
	 * {@link IModelPathElement} which consists of "Root.favouriteColours[1]" ( {@link MpGenerator#getTernaryPath()}).
	 */	
	@Test
	public void testNotMatchingSingleOperand() throws Exception {

		MpcDisjunction condition = $.singleOperandDisjunction();

		IModelPathElement path = MpGenerator.getTernaryPath();

		int junctionCaptureLength = MpcJunctionCapture.values().length;

		for (int i = 0; i < junctionCaptureLength; i++) {
			MpcJunctionCapture currentJC = getJunctionCapture(i);
			condition.setJunctionCapture(currentJC);

			MpcMatch evaluationResult = singleOperandFailureTest(condition, path);

			switch (currentJC) {
				case shortest:
				case longest:
				case last:
				case first:
				case none:
					// validate that the result is one item shorter
					assertThat(evaluationResult).isNull();

					break;
				// unknown junction capture
				default:
					fail("Unsupported Junction Capture: " + currentJC);
			}
		}
	}

	/**
	 * Validate that a {@link MpcDisjunction} condition of single operand will  MATCH against a
	 * {@link IModelPathElement} which consists of "Root.name" ( {@link MpGenerator#getSimplePath()}).
	 */	
	@Test
	public void testMatchingSingleOperand() throws Exception {

		MpcDisjunction condition = $.singleOperandDisjunction();

		IModelPathElement path = MpGenerator.getSimplePath();

		int junctionCaptureLength = MpcJunctionCapture.values().length;

		for (int i = 0; i < junctionCaptureLength; i++) {
			MpcJunctionCapture currentJC = getJunctionCapture(i);
			condition.setJunctionCapture(currentJC);

			MpcMatch evaluationResult = singleOperandSuccessTest(condition, path);

			switch (currentJC) {
				case shortest:
				case longest:
				case last:
				case first:

					assertThat(evaluationResult).isNotNull();
					// validate that the result is one item shorter
					assertThat(evaluationResult.getPath().getDepth()).isEqualTo(0);
					assertEquals(evaluationResult.getPath().getDepth(), 0);
					// validate that the current element is indeed the root
					assertThat(evaluationResult.getPath().getElementType()).isEqualTo(
							ModelPathElementType.Root);
					break;

				case none:

					// validate that the result is same size
					assertEquals(evaluationResult.getPath().getDepth(), 1);
					// validate that the current element is indeed the property
					assertEquals(evaluationResult.getPath().getElementType(), ModelPathElementType.Property);
					// validate that the previous element is indeed the root
					assertEquals(evaluationResult.getPath().getPrevious().getElementType(), ModelPathElementType.Root);

					break;
				// unknown junction capture
				default:
					fail("Unsupported Junction Capture: " + currentJC);
			}
		}
	}
}
