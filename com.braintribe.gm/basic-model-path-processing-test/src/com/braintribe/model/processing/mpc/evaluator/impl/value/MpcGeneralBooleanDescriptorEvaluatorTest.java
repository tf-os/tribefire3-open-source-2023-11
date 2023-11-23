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
package com.braintribe.model.processing.mpc.evaluator.impl.value;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.bvd.predicate.Assignable;
import com.braintribe.model.bvd.predicate.BinaryPredicate;
import com.braintribe.model.bvd.predicate.InstanceOf;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.mpc.value.MpcElementAxis;
import com.braintribe.model.mpc.value.MpcElementValue;
import com.braintribe.model.processing.MpGenerator;
import com.braintribe.model.processing.misc.model.Person;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.AbstractMpcTest;


/**
 * Provides tests for {@link MpcComparisonEvaluator}.
 * 
 */
public class MpcGeneralBooleanDescriptorEvaluatorTest  extends AbstractMpcTest{

	public enum PredicateOperator {
		in, equal, greater, greaterOrEqual, ilike,  less, lessOrEqual, like, notEqual , assignable, instance
	}
	
	@Test
	public void testComparisonOfMpcElementValueAgainstString() throws Exception {

		MpcElementValue firstOperand = $.elementValue(MpcElementAxis.value);

		// This is the same value that should be part of the path (Root.name)
		Object secondOperand = "My Name";

		IModelPathElement path = MpGenerator.getSimplePath();

		int comparisonOperatorsLength = PredicateOperator.values().length;

		for (int i = 0; i < comparisonOperatorsLength; i++) {
			
			PredicateOperator comparisonOperator = getComparisonOperator(i);
			
			// following operators are not valid for comparison against object
			if (comparisonOperator == PredicateOperator.in || comparisonOperator == PredicateOperator.instance || comparisonOperator == PredicateOperator.assignable)
				continue;
			BinaryPredicate condition = getPredicate(comparisonOperator, firstOperand, secondOperand);

			// run the matching method
			MpcMatch evaluationResult = MPC.mpcMatches(condition, path);

			// validate output
			switch (comparisonOperator) {
				case equal:
				case greaterOrEqual:
				case lessOrEqual:
			//	case instance:
				case like:
				case ilike:
			//	case assignable:
					// validate that the path has been consumed
					assertThat(evaluationResult).isNotNull();
					assertThat(evaluationResult.getPath().getDepth()).isEqualTo(0);
					break;
				case notEqual:
				case greater:
				case less:
					assertThat(evaluationResult).isNull(); // no match
					break;
//				case isOfType:
//					break;
				default:
					break;
			}
		}
	}

	@Test
	public void testAssignabilityMpcElementValueAgainstString() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath().getPrevious();
		
		Assignable condition = $.assignableFrom(MpcElementAxis.value, Person.class.getName());
		
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);
		
		assertThat(evaluationResult).isNotNull();
		assertThat(evaluationResult.getPath()).isNull();
	
	}
	
	@Test
	public void testInstanceOfMpcElementValueAgainstStringNoMatch() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();
		
		InstanceOf condition = $.instanceOf(MpcElementAxis.value, Person.class.getName());
		
		Boolean evaluationResult = MPC.matches(condition, path);
		
		assertThat(evaluationResult).isFalse();
	
	}
	
	/**
	 * Validate that a {@link MpcInstanceOf} condition which is preset with a {@link MpcElementAxis} of "value",
	 * typeSignature of {@link Person} and not assignable will NOT MATCH against a {@link IModelPathElement} which consists
	 * of "Root.name" ({@link MpGenerator#getSimplePath()})
	 */
	@Test
	public void testNoMatch() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();

		// validate input
		// validate it is not root path
		assertThat(path.getDepth()).isEqualTo(1);

		InstanceOf condition = $.instanceOf(MpcElementAxis.value, Person.class.getName());
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);

		// validate output
		// validate no consumption has been made
		assertThat(evaluationResult).isNull();
	}
	
	
	
	
	
	
	/**
	 * @param position
	 *            index in array
	 * @return The {@link MpcComparisonOperator} at the indicated position
	 */
	private static PredicateOperator getComparisonOperator(int position) {
		return PredicateOperator.values()[position];
	}
	
	private static BinaryPredicate getPredicate(PredicateOperator operator, Object leftOperand, Object rightOperand){
		
		switch (operator) {
			case equal:
				return $.equal(leftOperand, rightOperand);
			case greater:
				return $.greater(leftOperand, rightOperand);
			case greaterOrEqual:
				return $.greaterOrEqual(leftOperand, rightOperand);
			case ilike:
				return $.ilike(leftOperand, rightOperand);
			case in:
				return $.in(leftOperand, rightOperand);
			case less:
				return $.less(leftOperand, rightOperand);
			case lessOrEqual:
				return $.lessOrEqual(leftOperand, rightOperand);
			case like:
				return $.like(leftOperand, rightOperand);
			case notEqual:
				return $.notEqual(leftOperand, rightOperand);
			case assignable:
				return $.assignable(leftOperand, rightOperand);
			case instance:
				return $.instanceOf(leftOperand, rightOperand);
			default:
				break;			
			}
		
		return null;
		
	}

}
