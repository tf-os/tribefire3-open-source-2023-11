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
package com.braintribe.model.processing;

import com.braintribe.model.bvd.predicate.Assignable;
import com.braintribe.model.bvd.predicate.Equal;
import com.braintribe.model.bvd.predicate.Greater;
import com.braintribe.model.bvd.predicate.GreaterOrEqual;
import com.braintribe.model.bvd.predicate.Ilike;
import com.braintribe.model.bvd.predicate.In;
import com.braintribe.model.bvd.predicate.InstanceOf;
import com.braintribe.model.bvd.predicate.Less;
import com.braintribe.model.bvd.predicate.LessOrEqual;
import com.braintribe.model.bvd.predicate.Like;
import com.braintribe.model.bvd.predicate.NotEqual;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.value.Evaluate;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.mpc.ModelPathCondition;
import com.braintribe.model.mpc.atomic.MpcFalse;
import com.braintribe.model.mpc.atomic.MpcTrue;
import com.braintribe.model.mpc.logic.MpcConjunction;
import com.braintribe.model.mpc.logic.MpcDisjunction;
import com.braintribe.model.mpc.logic.MpcJunctionCapture;
import com.braintribe.model.mpc.logic.MpcNegation;
import com.braintribe.model.mpc.quantifier.MpcQuantifier;
import com.braintribe.model.mpc.quantifier.MpcQuantifierStrategy;
import com.braintribe.model.mpc.structure.MpcElementType;
import com.braintribe.model.mpc.structure.MpcPropertyName;
import com.braintribe.model.mpc.structure.MpcSequence;
import com.braintribe.model.mpc.value.MpcElementAxis;
import com.braintribe.model.mpc.value.MpcElementValue;
import com.braintribe.model.mpc.value.MpcMatchesType;
import com.braintribe.model.path.GmModelPathElementType;
import com.braintribe.model.processing.misc.model.Person;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.builder.api.MpcBuilder;

/**
 * Generator of all the conditions used in the testing of the MPC
 * 
 */
public class MpcGenerator {

	final static MpcBuilder $ = MPC.builder();

	/**
	 * @return a {@link MpcMatchesType} with the provided {@link MpcElementAxis}
	 *         and {@link TypeCondition}
	 */
	public MpcMatchesType matchesType(MpcElementAxis axis, TypeCondition typeCondition) {

		MpcMatchesType condition = $.matchesType(axis, typeCondition);
		return condition;
	}

	/**
	 * @return a {@link MpcSequence} with the provided nonCapture flag and list
	 *         of {@link ModelPathCondition}
	 */
	public MpcSequence sequence(boolean nonCapture, Object... mpc) {

		MpcSequence condition = null;
		if(nonCapture){
			condition = $.sequence(mpc);
		}
		else{
			condition = $.sequenceWithoutCapture(mpc);
		}
		
		return condition;
	}

	/**
	 * @return a {@link InstanceOf} with the provided {@link MpcElementAxis},
	 *         typeSignature
	 */
	public InstanceOf instanceOf(MpcElementAxis elementValue, String typeSignature) {

		InstanceOf condition = $.instanceOf(elementValue, typeSignature);
		return condition;
	}

	/**
	 * @return a {@link InstanceOf} with the provided operands
	 */
	public InstanceOf instanceOf(Object leftOperand, Object rightOperand) {

		InstanceOf condition = $.instanceOf(leftOperand,rightOperand);
		return condition;
	}
	
	/**
	 * @return a {@link Assignable} with the provided operands
	 */
	public Assignable assignable(Object leftOperand, Object rightOperand) {

		Assignable condition = $.assignable(leftOperand,rightOperand);
		return condition;
	}
	
	/**
	 * @return a {@link Assignable} with the provided {@link MpcElementAxis},
	 *         typeSignature
	 */
	public Assignable assignableFrom(MpcElementAxis elementValue, String typeSignature) {

		Assignable condition = $.assignableFrom(elementValue, typeSignature);
		return condition;
	}

	public Equal equal(Object leftOperand, Object rightOperand) {
		return $.equal(leftOperand, rightOperand);
	}

	public Greater greater(Object leftOperand, Object rightOperand) {
		return $.greater(leftOperand, rightOperand);
	}

	public GreaterOrEqual greaterOrEqual(Object leftOperand, Object rightOperand) {
		return $.greaterOrEqual(leftOperand, rightOperand);
	}

	public Ilike ilike(Object leftOperand, Object rightOperand) {
		return $.ilike(leftOperand, rightOperand);
	}

	public In in(Object leftOperand, Object rightOperand) {
		return $.in(leftOperand, rightOperand);
	}

	public Less less(Object leftOperand, Object rightOperand) {
		return $.less(leftOperand, rightOperand);
	}
	
	public MpcElementValue elementValue(MpcElementAxis elementAxis) {
		return $.elementValue(elementAxis);
	}

	public LessOrEqual lessOrEqual(Object leftOperand, Object rightOperand) {
		return $.lessOrEqual(leftOperand, rightOperand);
	}

	public Like like(Object leftOperand, Object rightOperand) {
		return $.like(leftOperand, rightOperand);
	}

	public NotEqual notEqual(Object leftOperand, Object rightOperand) {
		return $.notEqual(leftOperand, rightOperand);
	}

	public Evaluate evaluate(ValueDescriptor mpc) {
		return $.evaluate(mpc);
	}
	
	/**
	 * @return a {@link MpcElementType} with the provided
	 *         {@link GmModelPathElementType}
	 */
	public MpcElementType elementTypeCondition(GmModelPathElementType elementType) {

		MpcElementType condition = $.elementType(elementType);
		return condition;
	}

	/**
	 * @return a {@link MpcNegation} for a {@link MpcPropertyName} with value
	 *         "name"
	 */
	public MpcNegation negationCondition() {

		MpcNegation condition = $.negation($.property("name"));
		return condition;
	}

	/**
	 * @return a {@link MpcNegation} with the provided
	 *         {@link ModelPathCondition}
	 */
	public MpcNegation negation(ModelPathCondition con) {

		MpcNegation condition = $.negation(con);
		return condition;
	}

	/**
	 * @return a {@link MpcPropertyName} with the provided name
	 */
	public MpcPropertyName propertyNameCondition(String name) {

		MpcPropertyName condition = $.property(name);
		return condition;
	}

	/**
	 * @return a {@link MpcConjunction} with the provided
	 *         {@link MpcJunctionCapture} and list of {@link ModelPathCondition}
	 */
	public MpcConjunction conjunction(MpcJunctionCapture capture, Object... mpc) {

		MpcConjunction con = $.conjunction(capture, mpc);
		return con;
	}

	/**
	 * @return a {@link MpcDisjunction} with the provided
	 *         {@link MpcJunctionCapture} and list of {@link ModelPathCondition}
	 */
	public MpcDisjunction disjunction(MpcJunctionCapture capture, Object... mpc) {

		MpcDisjunction con = $.disjunction(capture, mpc);
		return con;
	}

	/**
	 * @return a {@link MpcConjunction} that is the result of the invocation of
	 *         {@link #singleOperandConjunction(MpcJunctionCapture)} with
	 *         {@link MpcJunctionCapture} set to first
	 */
	public MpcConjunction singleOperandConjunction() {

		return singleOperandConjunction(MpcJunctionCapture.first);
	}

	/**
	 * @param capture
	 *            The required {@link MpcJunctionCapture} for the
	 *            {@link MpcConjunction}
	 * @return a {@link MpcConjunction} that has only one operand. It is
	 *         compatible with {@link Person}. The operand is
	 *         {@link MpcPropertyName} with value "name".
	 */
	public MpcConjunction singleOperandConjunction(MpcJunctionCapture capture) {

		return conjunction(capture, propertyNameCondition("name"));
	}

	/**
	 * @return a {@link MpcDisjunction} that is the result of the invocation of
	 *         {@link #singleOperandDisjunction(MpcJunctionCapture)} with
	 *         {@link MpcJunctionCapture} set to first
	 */
	public MpcDisjunction singleOperandDisjunction() {

		return singleOperandDisjunction(MpcJunctionCapture.first);
	}

	/**
	 * @param capture
	 *            The required {@link MpcJunctionCapture} for the
	 *            {@link MpcDisjunction}
	 * @return a {@link MpcDisjunction} that has only one operand. It is
	 *         compatible with {@link Person}. The operand is
	 *         {@link MpcPropertyName} with value "name".
	 */
	public MpcDisjunction singleOperandDisjunction(MpcJunctionCapture capture) {

		return disjunction(capture, propertyNameCondition("name"));
	}

	/**
	 * @return a {@link MpcTrue}
	 */
	public MpcTrue trueLiteral(boolean nonCapture) {

		MpcTrue condition = $.trueLiteral(nonCapture);
		return condition;
	}

	/**
	 * @return a {@link MpcFalse}
	 */
	public MpcFalse falseLiteral() {

		MpcFalse condition = $.falseLiteral();
		return condition;
	}

	/**
	 * @return a {@link MpcQuantifier} that is an Asterisk operator
	 */
	public MpcQuantifier asteriskQuantifier(MpcQuantifierStrategy strategy, Object condition) {

		MpcQuantifier quantifiedCondition = $.asteriskQuantifier(strategy, condition);
		return quantifiedCondition;
	}

	/**
	 * @return a {@link MpcQuantifier} that is a Plus operator
	 */
	public MpcQuantifier plusQuantifier(MpcQuantifierStrategy strategy, Object condition) {

		MpcQuantifier quantifiedCondition = $.plusQuantifier(strategy, condition);
		return quantifiedCondition;
	}

	/**
	 * @return a {@link MpcQuantifier} that is a Question operator
	 */
	public MpcQuantifier questionQuantifier(MpcQuantifierStrategy strategy, Object condition) {

		MpcQuantifier quantifiedCondition = $.questionQuantifier(strategy, condition);
		return quantifiedCondition;
	}

	/**
	 * @return a {@link MpcQuantifier}
	 */
	public MpcQuantifier quantifier(MpcQuantifierStrategy strategy, Object condition, Integer miniumRepitions, Integer maximumRepitions) {

		MpcQuantifier quantifiedCondition = $.quantifier(strategy, condition, miniumRepitions, maximumRepitions);
		return quantifiedCondition;
	}

	/**
	 * @return a {@link MpcElementType} as root
	 */
	public MpcElementType root() {

		MpcElementType condition = $.root();
		return condition;
	}

}
