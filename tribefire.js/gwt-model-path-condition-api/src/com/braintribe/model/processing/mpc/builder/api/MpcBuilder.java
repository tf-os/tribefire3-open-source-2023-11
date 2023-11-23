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
package com.braintribe.model.processing.mpc.builder.api;

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
import com.braintribe.model.mpc.ModelPathCondition;
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
import com.braintribe.model.processing.vde.builder.api.VdBuilder;

/**
 * This is the builder for all the different possible {@link ModelPathCondition}
 * s. It extends the functionality provided by {@link VdBuilder}
 *
 */
public interface MpcBuilder extends VdBuilder{

	//atomic
	MpcAtomic atomic(boolean value);
	MpcAtomic atomic(boolean nonCapture, boolean value);
	MpcTrue trueLiteral();
	MpcTrue trueLiteral(boolean nonCapture);
	MpcFalse falseLiteral();

	//quantifier, where the condition can be any VD, thus it is defined as Object
	MpcQuantifier quantifier(MpcQuantifierStrategy strategy, Object condition, Integer miniumRepetitions, Integer maximumRepetitions);
	MpcQuantifier asteriskQuantifier(MpcQuantifierStrategy strategy, Object condition);
	MpcQuantifier questionQuantifier(MpcQuantifierStrategy strategy, Object condition);
	MpcQuantifier plusQuantifier(MpcQuantifierStrategy strategy, Object condition);
	MpcQuantifier greedyAsteriskQuantifier(Object condition);
	MpcQuantifier greedyQuestionQuantifier(Object condition);
	MpcQuantifier greedyPlusQuantifier(Object condition);
	MpcQuantifier reluctantAsteriskQuantifier(Object condition);
	MpcQuantifier reluctantQuestionQuantifier(Object condition);
	MpcQuantifier reluctantPlusQuantifier(Object condition);
	MpcQuantifier possessiveAsteriskQuantifier(Object condition);
	MpcQuantifier PossessiveQuestionQuantifier(Object condition);
	MpcQuantifier PossessivePlusQuantifier(Object condition);
	
	// logic
	MpcConjunction conjunction(MpcJunctionCapture capture, Object... mpc);
	MpcConjunction conjunction(MpcJunctionCapture capture, List<Object> mpcList);
	MpcDisjunction disjunction(MpcJunctionCapture capture, Object... mpc);
	MpcDisjunction disjunction(MpcJunctionCapture capture, List<Object> mpcList);
	@Override
	MpcConjunction conjunction(Object ... operands);
	@Override
	MpcConjunction conjunction(List<Object> operandsList);
	@Override
	MpcDisjunction disjunction(Object ... operands);
	@Override
	MpcDisjunction disjunction(List<Object> operandsList);
	@Override
	MpcNegation negation(Object operand);
	
	Conjunction nonMpcConjunction(Object ... operands);
	Conjunction nonMpcConjunction(List<Object> operandsList);
	Disjunction nonMpcDisjunction(Object ... operands);
	Disjunction nonMpcDisjunction(List<Object> operandsList);
	Negation nonMpcNegation(Object operand);
	
	
	//structure	
	MpcElementType elementType(GmModelPathElementType elementType);
	MpcElementType property();
	MpcElementType listItem();
	MpcElementType setItem();
	MpcElementType mapKey();
	MpcElementType mapValue();
	MpcElementType root();
	
	MpcPropertyName property(String propertyName);
	
	// Because (Object... mpc) and (boolean nonCapture, Object... mpc) will
	// yield ambiguous methods, we have to split them into two methods with
	// different names
	MpcSequence sequence(Object... mpc);
	MpcSequence sequence(boolean nonCapture, List<Object> mpcList);
	MpcSequence sequenceWithoutCapture(Object... mpc);
	
	// value
	InstanceOf instanceOf(String typeSignature);
	InstanceOf instanceOf(GenericModelType type);
	InstanceOf instanceOf(Class<?> type);
	InstanceOf instanceOf(GmType type);
	InstanceOf instanceOf(MpcElementAxis elementAxis, String typeSignature);
	InstanceOf instanceOf(MpcElementAxis elementAxis, GenericModelType type);
	InstanceOf instanceOf(MpcElementAxis elementAxis, Class<?> type);
	InstanceOf instanceOf(MpcElementAxis elementAxis, GmType type);
	Assignable assignableFrom(String typeSignature);
	Assignable assignableFrom(GenericModelType type);
	Assignable assignableFrom(Class<?> type);
	Assignable assignableFrom(GmType type);
	Assignable assignableFrom(MpcElementAxis elementAxis, String typeSignature);
	Assignable assignableFrom(MpcElementAxis elementAxis, GenericModelType type);
	Assignable assignableFrom(MpcElementAxis elementAxis, Class<?> type);
	Assignable instassignableFromanceOf(MpcElementAxis elementAxis, GmType type);
	
	MpcElementValue elementValue(MpcElementAxis elementAxis);
	MpcElementValue elementValue();

	MpcMatchesType matchesType(MpcElementAxis axis, TypeCondition typeCondition);
	 
	Equal elementValueEqual(MpcElementAxis elementAxis, Object rightOperand);
	Greater elementValueGreater(MpcElementAxis elementAxis, Object rightOperand);
	GreaterOrEqual elementValueGreaterOrEqual(MpcElementAxis elementAxis, Object rightOperand);
	Ilike elementValueIlike(MpcElementAxis elementAxis, Object rightOperand);
	In elementValueIn(MpcElementAxis elementAxis, Object rightOperand);
	Less elementValueLess(MpcElementAxis elementAxis, Object rightOperand);
	LessOrEqual elementValueLessOrEqual(MpcElementAxis elementAxis, Object rightOperand);
	Like elementValueLike(MpcElementAxis elementAxis, Object rightOperand);
	NotEqual elementValueNotEqual(MpcElementAxis elementAxis, Object rightOperand);
	
}
