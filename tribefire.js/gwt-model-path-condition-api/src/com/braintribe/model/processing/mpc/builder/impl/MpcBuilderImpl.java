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
package com.braintribe.model.processing.mpc.builder.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.braintribe.model.bvd.logic.Conjunction;
import com.braintribe.model.bvd.logic.Disjunction;
import com.braintribe.model.bvd.logic.Negation;
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
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.mpc.atomic.MpcAtomic;
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
import com.braintribe.model.processing.mpc.builder.api.MpcBuilder;
import com.braintribe.model.processing.vde.builder.impl.VdBuilderImpl;

public class MpcBuilderImpl extends VdBuilderImpl implements MpcBuilder {

	@Override
	public MpcConjunction conjunction(Object... operands) {
		return conjunction(MpcJunctionCapture.none, operands);
	}

	@Override
	public MpcConjunction conjunction(List<Object> operandsList) {
		return conjunction(MpcJunctionCapture.none, operandsList);
	}

	@Override
	public MpcDisjunction disjunction(Object... operands) {
		return disjunction(MpcJunctionCapture.none, operands);
	}

	@Override
	public MpcDisjunction disjunction(List<Object> operandsList) {
		return disjunction(MpcJunctionCapture.none, operandsList);
	}

	@Override
	public MpcConjunction conjunction(MpcJunctionCapture capture, Object... mpc) {
		MpcConjunction condition = MpcConjunction.T.create();
		if (mpc != null) {
			List<Object> conditions = new ArrayList<Object>(Arrays.asList(mpc));
			condition.setOperands(conditions);
		}

		condition.setJunctionCapture(capture);
		return condition;
	}

	@Override
	public MpcConjunction conjunction(MpcJunctionCapture capture, List<Object> mpcList) {
		MpcConjunction condition = MpcConjunction.T.create();
		condition.setOperands(mpcList);
		condition.setJunctionCapture(capture);
		return condition;
	}

	@Override
	public MpcDisjunction disjunction(MpcJunctionCapture capture, Object... mpc) {
		MpcDisjunction condition = MpcDisjunction.T.create();
		if (mpc != null) {
			List<Object> conditions = new ArrayList<Object>(Arrays.asList(mpc));
			condition.setOperands(conditions);
		}

		condition.setJunctionCapture(capture);
		return condition;
	}

	@Override
	public MpcDisjunction disjunction(MpcJunctionCapture capture, List<Object> mpcList) {
		MpcDisjunction condition = MpcDisjunction.T.create();
		condition.setOperands(mpcList);
		condition.setJunctionCapture(capture);
		return condition;
	}

	@Override
	public MpcNegation negation(Object operand) {
		MpcNegation condition = MpcNegation.T.create();
		condition.setOperand(operand);
		return condition;
	}

	@Override
	public Conjunction nonMpcConjunction(Object... operands) {
		return super.conjunction(operands);
	}

	@Override
	public Conjunction nonMpcConjunction(List<Object> operandsList) {
		return super.conjunction(operandsList);
	}

	@Override
	public Disjunction nonMpcDisjunction(Object... operands) {
		return super.disjunction(operands);
	}

	@Override
	public Disjunction nonMpcDisjunction(List<Object> operandsList) {
		return super.disjunction(operandsList);
	}

	@Override
	public Negation nonMpcNegation(Object operand) {
		return super.negation(operand);
	}

	@Override
	public MpcQuantifier quantifier(MpcQuantifierStrategy strategy, Object condition, Integer miniumRepetitions, Integer maximumRepetitions) {

		MpcQuantifier quantifier = MpcQuantifier.T.create();
		quantifier.setCondition(condition);
		quantifier.setMaximumRepetition(maximumRepetitions);
		quantifier.setMinimumRepetition(miniumRepetitions);
		quantifier.setQuantifierStrategy(strategy);

		return quantifier;
	}

	@Override
	public MpcQuantifier asteriskQuantifier(MpcQuantifierStrategy strategy, Object condition) {
		return quantifier(strategy, condition, 0, null);
	}

	@Override
	public MpcQuantifier questionQuantifier(MpcQuantifierStrategy strategy, Object condition) {
		return quantifier(strategy, condition, 0, 1);
	}

	@Override
	public MpcQuantifier plusQuantifier(MpcQuantifierStrategy strategy, Object condition) {
		return quantifier(strategy, condition, 1, null);
	}

	@Override
	public MpcQuantifier greedyAsteriskQuantifier(Object condition) {
		return asteriskQuantifier(MpcQuantifierStrategy.greedy, condition);
	}

	@Override
	public MpcQuantifier greedyQuestionQuantifier(Object condition) {
		return questionQuantifier(MpcQuantifierStrategy.greedy, condition);
	}

	@Override
	public MpcQuantifier greedyPlusQuantifier(Object condition) {
		return plusQuantifier(MpcQuantifierStrategy.greedy, condition);
	}

	@Override
	public MpcQuantifier reluctantAsteriskQuantifier(Object condition) {
		return asteriskQuantifier(MpcQuantifierStrategy.reluctant, condition);
	}

	@Override
	public MpcQuantifier reluctantQuestionQuantifier(Object condition) {
		return questionQuantifier(MpcQuantifierStrategy.reluctant, condition);
	}

	@Override
	public MpcQuantifier reluctantPlusQuantifier(Object condition) {
		return plusQuantifier(MpcQuantifierStrategy.reluctant, condition);
	}

	@Override
	public MpcQuantifier possessiveAsteriskQuantifier(Object condition) {
		return asteriskQuantifier(MpcQuantifierStrategy.possessive, condition);
	}

	@Override
	public MpcQuantifier PossessiveQuestionQuantifier(Object condition) {
		return questionQuantifier(MpcQuantifierStrategy.possessive, condition);
	}

	@Override
	public MpcQuantifier PossessivePlusQuantifier(Object condition) {
		return plusQuantifier(MpcQuantifierStrategy.possessive, condition);
	}

	@Override
	public MpcTrue trueLiteral() {
		return trueLiteral(false);
	}

	@Override
	public MpcTrue trueLiteral(boolean nonCapture) {
		MpcTrue trueLiteral = MpcTrue.T.create();
		trueLiteral.setNoneCapture(nonCapture);
		return trueLiteral;
	}

	@Override
	public MpcFalse falseLiteral() {
		MpcFalse falseLiteral = MpcFalse.T.create();
		return falseLiteral;
	}

	@Override
	public MpcAtomic atomic(boolean nonCapture, boolean value) {
		if (value) {
			return trueLiteral(nonCapture);
		} else {
			return falseLiteral();
		}
	}

	@Override
	public MpcAtomic atomic(boolean value) {
		return atomic(false, value);
	}

	@Override
	public MpcSequence sequence(boolean nonCapture, List<Object> mpcList) {
		MpcSequence sequence = MpcSequence.T.create();
		sequence.setElements(mpcList);
		sequence.setNoneCapture(nonCapture);
		return sequence;
	}

	@Override
	public MpcSequence sequence(Object... mpc) {
		MpcSequence sequence = basicSequence(mpc);
		sequence.setNoneCapture(true);
		return sequence;
	}

	@Override
	public MpcSequence sequenceWithoutCapture(Object... mpc) {
		MpcSequence sequence = basicSequence(mpc);
		sequence.setNoneCapture(false);
		return sequence;
	}

	private MpcSequence basicSequence(Object... mpc) {
		MpcSequence sequence = MpcSequence.T.create();
		if (mpc != null) {
			List<Object> conditions = new ArrayList<Object>(Arrays.asList(mpc));
			Collections.reverse(conditions);
			sequence.setElements(conditions);
		}
		return sequence;
	}

	@Override
	public MpcElementType elementType(GmModelPathElementType elementType) {
		MpcElementType element = MpcElementType.T.create();
		element.setElementType(elementType);
		return element;
	}

	@Override
	public MpcElementType property() {
		return elementType(GmModelPathElementType.Property);
	}

	@Override
	public MpcElementType listItem() {
		return elementType(GmModelPathElementType.ListItem);
	}

	@Override
	public MpcElementType setItem() {
		return elementType(GmModelPathElementType.SetItem);
	}

	@Override
	public MpcElementType mapKey() {
		return elementType(GmModelPathElementType.MapKey);
	}

	@Override
	public MpcElementType mapValue() {
		return elementType(GmModelPathElementType.MapValue);
	}

	@Override
	public MpcElementType root() {
		return elementType(GmModelPathElementType.Root);
	}

	@Override
	public MpcPropertyName property(String propertyName) {
		MpcPropertyName property = MpcPropertyName.T.create();
		property.setPropertyName(propertyName);
		return property;
	}

	@Override
	public InstanceOf instanceOf(String typeSignature) {
		return instanceOf(MpcElementAxis.value, typeSignature);
	}

	@Override
	public InstanceOf instanceOf(GenericModelType type) {
		return instanceOf(type.getTypeSignature());
	}

	@Override
	public InstanceOf instanceOf(Class<?> type) {
		return instanceOf(type.getName());
	}

	@Override
	public InstanceOf instanceOf(GmType type) {
		return instanceOf(type.getTypeSignature());
	}

	@Override
	public InstanceOf instanceOf(MpcElementAxis elementAxis, String typeSignature) {
		InstanceOf condition = InstanceOf.T.create();
		condition.setLeftOperand(elementValue(elementAxis));
		condition.setRightOperand(typeSignature);
		return condition;
	}

	@Override
	public InstanceOf instanceOf(MpcElementAxis elementAxis, GenericModelType type) {
		return instanceOf(elementAxis, type.getTypeSignature());
	}

	@Override
	public InstanceOf instanceOf(MpcElementAxis elementAxis, Class<?> type) {
		return instanceOf(elementAxis, type.getName());
	}

	@Override
	public InstanceOf instanceOf(MpcElementAxis elementAxis, GmType type) {
		return instanceOf(elementAxis, type.getTypeSignature());
	}

	@Override
	public Assignable assignableFrom(String typeSignature) {
		return assignableFrom(MpcElementAxis.value, typeSignature);
	}

	@Override
	public Assignable assignableFrom(GenericModelType type) {
		return assignableFrom(type.getTypeSignature());
	}

	@Override
	public Assignable assignableFrom(Class<?> type) {
		return assignableFrom(type.getName());
	}

	@Override
	public Assignable assignableFrom(GmType type) {
		return assignableFrom(type.getTypeSignature());
	}

	@Override
	public Assignable assignableFrom(MpcElementAxis elementAxis, String typeSignature) {
		Assignable condition = Assignable.T.create();
		condition.setLeftOperand(elementValue(elementAxis));
		condition.setRightOperand(typeSignature);
		return condition;
	}

	@Override
	public Assignable assignableFrom(MpcElementAxis elementAxis, GenericModelType type) {
		return assignableFrom(elementAxis, type.getTypeSignature());
	}

	@Override
	public Assignable assignableFrom(MpcElementAxis elementAxis, Class<?> type) {
		return assignableFrom(elementAxis, type.getName());
	}

	@Override
	public Assignable instassignableFromanceOf(MpcElementAxis elementAxis, GmType type) {
		return assignableFrom(elementAxis, type.getTypeSignature());
	}

	@Override
	public MpcElementValue elementValue(MpcElementAxis elementAxis) {
		MpcElementValue value = MpcElementValue.T.create();
		value.setElementAxis(elementAxis);
		return value;
	}

	@Override
	public MpcElementValue elementValue() {
		return elementValue(MpcElementAxis.value);
	}

	@Override
	public MpcMatchesType matchesType(MpcElementAxis axis, TypeCondition typeCondition) {
		MpcMatchesType condition = MpcMatchesType.T.create();
		condition.setElementValue(axis);
		condition.setTypeCondition(typeCondition);
		return condition;
	}

	@Override
	public Equal elementValueEqual(MpcElementAxis elementAxis, Object rightOperand) {
		MpcElementValue elementValue = elementValue(elementAxis);
		return equal(elementValue, rightOperand);
	}

	@Override
	public Greater elementValueGreater(MpcElementAxis elementAxis, Object rightOperand) {
		MpcElementValue elementValue = elementValue(elementAxis);
		return greater(elementValue, rightOperand);
	}

	@Override
	public GreaterOrEqual elementValueGreaterOrEqual(MpcElementAxis elementAxis, Object rightOperand) {
		MpcElementValue elementValue = elementValue(elementAxis);
		return greaterOrEqual(elementValue, rightOperand);
	}

	@Override
	public Ilike elementValueIlike(MpcElementAxis elementAxis, Object rightOperand) {
		MpcElementValue elementValue = elementValue(elementAxis);
		return ilike(elementValue, rightOperand);
	}

	@Override
	public In elementValueIn(MpcElementAxis elementAxis, Object rightOperand) {
		MpcElementValue elementValue = elementValue(elementAxis);
		return in(elementValue, rightOperand);
	}

	@Override
	public Less elementValueLess(MpcElementAxis elementAxis, Object rightOperand) {
		MpcElementValue elementValue = elementValue(elementAxis);
		return less(elementValue, rightOperand);
	}

	@Override
	public LessOrEqual elementValueLessOrEqual(MpcElementAxis elementAxis, Object rightOperand) {
		MpcElementValue elementValue = elementValue(elementAxis);
		return lessOrEqual(elementValue, rightOperand);
	}

	@Override
	public Like elementValueLike(MpcElementAxis elementAxis, Object rightOperand) {
		MpcElementValue elementValue = elementValue(elementAxis);
		return like(elementValue, rightOperand);
	}

	@Override
	public NotEqual elementValueNotEqual(MpcElementAxis elementAxis, Object rightOperand) {
		MpcElementValue elementValue = elementValue(elementAxis);
		return notEqual(elementValue, rightOperand);
	}
}
