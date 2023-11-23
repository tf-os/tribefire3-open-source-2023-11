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

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.mpc.logic.MpcJunction;
import com.braintribe.model.mpc.logic.MpcJunctionCapture;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.AbstractMpcTest;

/**
 * Abstract class that contains common functions for  {@link MpcConjunctionEvaluatorTest} and {@link MpcDisjunctionEvaluatorTest}  
 *
 */
public abstract class MpcJunctionEvaluatorTest extends AbstractMpcTest {
	/**
	 * @param position
	 *            index in array
	 * @return The {@link MpcComparisonOperator} at the indicated position
	 */
	protected  MpcJunctionCapture getJunctionCapture(int position) {
		return MpcJunctionCapture.values()[position];
	}

	/**
	 * Helper function for {@link MpcConjunctionEvaluatorTest#testMatchingSingleOperand()} and {@link MpcDisjunctionEvaluatorTest#testMatchingSingleOperand()}  
	 */	
	protected <C extends MpcJunction> MpcMatch singleOperandSuccessTest(C condition, IModelPathElement path) throws MpcEvaluatorRuntimeException {

		// check the path consists of root and another element
		assertThat(path.getDepth()).isEqualTo(1);
		// check the condition has one operand only
		assertThat(condition.getOperands().size()).isEqualTo(1);
		// validate that the current element is indeed a property
		assertThat(path.getElementType()).isEqualTo(ModelPathElementType.Property);
		// validate that the previous element is indeed the root
		assertThat(path.getPrevious().getElementType()).isEqualTo(ModelPathElementType.Root);

		// evaluate the condition against the path
		MpcMatch evalResult = MPC.mpcMatches(condition, path);

		return evalResult;
	}

	/**
	 * Helper function for {@link MpcConjunctionEvaluatorTest#testNotMatchingMultipleOperands()} and {@link MpcDisjunctionEvaluatorTest#testNotMatchingMultipleOperands()}  
	 */	
	protected <C extends MpcJunction> MpcMatch multipleOperandFailureTest(C condition, IModelPathElement path) throws MpcEvaluatorRuntimeException {

		// check the path consists of root and another element
		assertThat(path.getDepth()).isEqualTo(1);
		// check the condition has one operand only
		assertThat(condition.getOperands().size()).isEqualTo(2);
		// validate that the current element is indeed a property
		assertThat(path.getElementType()).isEqualTo(ModelPathElementType.Property);
		// validate that the previous element is indeed the root
		assertThat(path.getPrevious().getElementType()).isEqualTo(ModelPathElementType.Root);
		// evaluate the condition against the path
		MpcMatch evalResult = MPC.mpcMatches(condition, path);

		return evalResult;
	}

	/**
	 * Helper function for {@link MpcConjunctionEvaluatorTest#testMatchingMultipleOperands()} and {@link MpcDisjunctionEvaluatorTest#testMatchingMultipleOperands()}  
	 */	
	protected <C extends MpcJunction> MpcMatch multipleOperandSuccessTest(C condition, IModelPathElement path) throws MpcEvaluatorRuntimeException {

		// check the path consists of root and another element
		assertThat(path.getDepth()).isEqualTo(2);
		// check the condition has one operand only
		assertThat(condition.getOperands().size()).isEqualTo(2);
		// validate that the current element is indeed a listItem
		assertThat(path.getElementType()).isEqualTo(ModelPathElementType.ListItem);
		// validate that the previous element is indeed a property
		assertThat(path.getPrevious().getElementType()).isEqualTo(ModelPathElementType.Property);
		// validate that the first element in the path is the root
		assertThat(path.getPrevious().getPrevious().getElementType()).isEqualTo(ModelPathElementType.Root);
		// evaluate the condition against the path
		MpcMatch evalResult = MPC.mpcMatches(condition, path);

		return evalResult;
	}

	/**
	 * Helper function for {@link MpcConjunctionEvaluatorTest#testNotMatchingSingleOperand()} and {@link MpcDisjunctionEvaluatorTest#testNotMatchingSingleOperand()}  
	 */	
	protected <C extends MpcJunction> MpcMatch singleOperandFailureTest(C condition, IModelPathElement path) throws MpcEvaluatorRuntimeException {

		// check the path consists of root and other two element
		assertThat(path.getDepth()).isEqualTo(2);
		// check the condition has one operand only
		assertThat(condition.getOperands().size()).isEqualTo(1);
		// validate that the current element is indeed a listItem
		assertThat(path.getElementType()).isEqualTo(ModelPathElementType.ListItem);
		// validate that the previous element is indeed a property
		assertThat(path.getPrevious().getElementType()).isEqualTo(ModelPathElementType.Property);
		// validate that the first element in the path is the root
		assertThat(path.getPrevious().getPrevious().getElementType()).isEqualTo(ModelPathElementType.Root);

		// evaluate the condition against the path
		MpcMatch evalResult = MPC.mpcMatches(condition, path);

		return evalResult;
	}

}
