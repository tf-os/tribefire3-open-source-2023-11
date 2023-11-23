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
package com.braintribe.model.processing.vde.evaluator.impl.predicate;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.vde.evaluator.api.EvaluationFunctionProperty;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.predicate.PredicateEvalContext;
import com.braintribe.model.processing.vde.evaluator.api.predicate.PredicateEvalExpert;
import com.braintribe.model.processing.vde.evaluator.api.predicate.PredicateOperator;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.assignable.ObjectAssignableToObject;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.assignable.ObjectAssignableToString;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.equal.BooleanEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.equal.DateEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.equal.DecimalNumericEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.equal.EnumEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.equal.NumberEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.equal.ObjectEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.equal.StringEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.greater.DateGreater;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.greater.DecimalNumericGreater;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.greater.EnumGreater;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.greater.NumberGreater;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.greater.ObjectGreater;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.greater.StringGreater;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.greaterorequal.DateGreaterOrEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.greaterorequal.DecimalNumericGreaterOrEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.greaterorequal.EnumGreaterOrEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.greaterorequal.NumberGreaterOrEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.greaterorequal.ObjectGreaterOrEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.greaterorequal.StringGreaterOrEqual;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.ilike.StringIlike;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.in.ObjectInCollection;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.instance.ObjectInstanceOfObject;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.instance.ObjectInstanceOfString;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.like.StringLike;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.TimeZoneOffset;

/**
 * Implementation for {@link PredicateEvalContext}
 * 
 */
public class PredicateEvalContextImpl implements PredicateEvalContext {

	// context experts
	private final static Map<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>> EQUAL_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>> GREATER_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>> GREATER_OR_EQUAL_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>> LESS_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>> LESS_OR_EQUAL_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>> LIKE_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>> ILIKE_EXPERTS;

	private final static Map<PredicateEvalExpert<?, ?>, PredicateEvalExpert<?, ?>> DYNAMIC_EXPERTS; // not
																									// equal,
																									// less
																									// and
																									// less
																									// or
																									// equal

	// setting up the list of default experts
	static {
		EQUAL_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>>();
		GREATER_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>>();
		GREATER_OR_EQUAL_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>>();
		LESS_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>>();
		LESS_OR_EQUAL_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>>();
		LIKE_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>>();
		ILIKE_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>>();

		DYNAMIC_EXPERTS = new HashMap<PredicateEvalExpert<?, ?>, PredicateEvalExpert<?, ?>>();

		// Equal experts
		// Integer
		register(EQUAL_EXPERTS, Integer.class, Integer.class, NumberEqual.getInstance(), EvaluationFunctionProperty.oneway);
		// Long
		register(EQUAL_EXPERTS, Long.class, Long.class, NumberEqual.getInstance(), EvaluationFunctionProperty.oneway);
		register(EQUAL_EXPERTS, Long.class, Integer.class, NumberEqual.getInstance(), EvaluationFunctionProperty.commutative);
		// Float
		register(EQUAL_EXPERTS, Float.class, Float.class, NumberEqual.getInstance(), EvaluationFunctionProperty.oneway);
		register(EQUAL_EXPERTS, Float.class, Long.class, NumberEqual.getInstance(), EvaluationFunctionProperty.commutative);
		register(EQUAL_EXPERTS, Float.class, Integer.class, NumberEqual.getInstance(), EvaluationFunctionProperty.commutative);
		// Double
		register(EQUAL_EXPERTS, Double.class, Double.class, NumberEqual.getInstance(), EvaluationFunctionProperty.oneway);
		register(EQUAL_EXPERTS, Double.class, Float.class, NumberEqual.getInstance(), EvaluationFunctionProperty.commutative);
		register(EQUAL_EXPERTS, Double.class, Long.class, NumberEqual.getInstance(), EvaluationFunctionProperty.commutative);
		register(EQUAL_EXPERTS, Double.class, Integer.class, NumberEqual.getInstance(), EvaluationFunctionProperty.commutative);
		// Decimal
		register(EQUAL_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalNumericEqual.getInstance(), EvaluationFunctionProperty.oneway);
		register(EQUAL_EXPERTS, BigDecimal.class, Double.class, DecimalNumericEqual.getInstance(), EvaluationFunctionProperty.commutative);
		register(EQUAL_EXPERTS, BigDecimal.class, Float.class, DecimalNumericEqual.getInstance(), EvaluationFunctionProperty.commutative);
		register(EQUAL_EXPERTS, BigDecimal.class, Long.class, DecimalNumericEqual.getInstance(), EvaluationFunctionProperty.commutative);
		register(EQUAL_EXPERTS, BigDecimal.class, Integer.class, DecimalNumericEqual.getInstance(), EvaluationFunctionProperty.commutative);
		// Boolean
		register(EQUAL_EXPERTS, Boolean.class, Boolean.class, BooleanEqual.getInstance(), EvaluationFunctionProperty.oneway);
		// String
		register(EQUAL_EXPERTS, String.class, String.class, StringEqual.getInstance(), EvaluationFunctionProperty.oneway);
		// Date
		register(EQUAL_EXPERTS, Date.class, Date.class, DateEqual.getInstance(), EvaluationFunctionProperty.oneway);

		// Greater experts
		// Integer
		register(GREATER_EXPERTS, Integer.class, Integer.class, NumberGreater.getInstance(), EvaluationFunctionProperty.oneway);
		// Long
		register(GREATER_EXPERTS, Long.class, Long.class, NumberGreater.getInstance(), EvaluationFunctionProperty.oneway);
		register(GREATER_EXPERTS, Long.class, Integer.class, NumberGreater.getInstance(), EvaluationFunctionProperty.commutative);
		// Float
		register(GREATER_EXPERTS, Float.class, Float.class, NumberGreater.getInstance(), EvaluationFunctionProperty.oneway);
		register(GREATER_EXPERTS, Float.class, Long.class, NumberGreater.getInstance(), EvaluationFunctionProperty.commutative);
		register(GREATER_EXPERTS, Float.class, Integer.class, NumberGreater.getInstance(), EvaluationFunctionProperty.commutative);
		// Double
		register(GREATER_EXPERTS, Double.class, Double.class, NumberGreater.getInstance(), EvaluationFunctionProperty.oneway);
		register(GREATER_EXPERTS, Double.class, Float.class, NumberGreater.getInstance(), EvaluationFunctionProperty.commutative);
		register(GREATER_EXPERTS, Double.class, Long.class, NumberGreater.getInstance(), EvaluationFunctionProperty.commutative);
		register(GREATER_EXPERTS, Double.class, Integer.class, NumberGreater.getInstance(), EvaluationFunctionProperty.commutative);
		// Decimal
		register(GREATER_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalNumericGreater.getInstance(), EvaluationFunctionProperty.oneway);
		register(GREATER_EXPERTS, BigDecimal.class, Double.class, DecimalNumericGreater.getInstance(), EvaluationFunctionProperty.commutative);
		register(GREATER_EXPERTS, BigDecimal.class, Float.class, DecimalNumericGreater.getInstance(), EvaluationFunctionProperty.commutative);
		register(GREATER_EXPERTS, BigDecimal.class, Long.class, DecimalNumericGreater.getInstance(), EvaluationFunctionProperty.commutative);
		register(GREATER_EXPERTS, BigDecimal.class, Integer.class, DecimalNumericGreater.getInstance(), EvaluationFunctionProperty.commutative);
		// String
		register(GREATER_EXPERTS, String.class, String.class, StringGreater.getInstance(), EvaluationFunctionProperty.oneway);
		// Date
		register(GREATER_EXPERTS, Date.class, Date.class, DateGreater.getInstance(), EvaluationFunctionProperty.oneway);

		// GreaterOrEqual experts
		// Integer
		register(GREATER_OR_EQUAL_EXPERTS, Integer.class, Integer.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.oneway);
		// Long
		register(GREATER_OR_EQUAL_EXPERTS, Long.class, Long.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.oneway);
		register(GREATER_OR_EQUAL_EXPERTS, Long.class, Integer.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.commutative);
		// Float
		register(GREATER_OR_EQUAL_EXPERTS, Float.class, Float.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.oneway);
		register(GREATER_OR_EQUAL_EXPERTS, Float.class, Long.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.commutative);
		register(GREATER_OR_EQUAL_EXPERTS, Float.class, Integer.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.commutative);
		// Double
		register(GREATER_OR_EQUAL_EXPERTS, Double.class, Double.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.oneway);
		register(GREATER_OR_EQUAL_EXPERTS, Double.class, Float.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.commutative);
		register(GREATER_OR_EQUAL_EXPERTS, Double.class, Long.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.commutative);
		register(GREATER_OR_EQUAL_EXPERTS, Double.class, Integer.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.commutative);
		// Decimal
		register(GREATER_OR_EQUAL_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalNumericGreaterOrEqual.getInstance(),
				EvaluationFunctionProperty.oneway);
		register(GREATER_OR_EQUAL_EXPERTS, BigDecimal.class, Double.class, DecimalNumericGreaterOrEqual.getInstance(),
				EvaluationFunctionProperty.commutative);
		register(GREATER_OR_EQUAL_EXPERTS, BigDecimal.class, Float.class, DecimalNumericGreaterOrEqual.getInstance(),
				EvaluationFunctionProperty.commutative);
		register(GREATER_OR_EQUAL_EXPERTS, BigDecimal.class, Long.class, DecimalNumericGreaterOrEqual.getInstance(),
				EvaluationFunctionProperty.commutative);
		register(GREATER_OR_EQUAL_EXPERTS, BigDecimal.class, Integer.class, DecimalNumericGreaterOrEqual.getInstance(),
				EvaluationFunctionProperty.commutative);
		// String
		register(GREATER_OR_EQUAL_EXPERTS, String.class, String.class, StringGreaterOrEqual.getInstance(), EvaluationFunctionProperty.oneway);
		// Date
		register(GREATER_OR_EQUAL_EXPERTS, Date.class, Date.class, DateGreaterOrEqual.getInstance(), EvaluationFunctionProperty.oneway);

		// Less experts
		// Integer
		register(LESS_EXPERTS, Integer.class, Integer.class, NumberGreater.getInstance(), EvaluationFunctionProperty.reverse);
		// Long
		register(LESS_EXPERTS, Long.class, Long.class, NumberGreater.getInstance(), EvaluationFunctionProperty.reverse);
		register(LESS_EXPERTS, Long.class, Integer.class, NumberGreater.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		// Float
		register(LESS_EXPERTS, Float.class, Float.class, NumberGreater.getInstance(), EvaluationFunctionProperty.reverse);
		register(LESS_EXPERTS, Float.class, Long.class, NumberGreater.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		register(LESS_EXPERTS, Float.class, Integer.class, NumberGreater.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		// Double
		register(LESS_EXPERTS, Double.class, Double.class, NumberGreater.getInstance(), EvaluationFunctionProperty.reverse);
		register(LESS_EXPERTS, Double.class, Float.class, NumberGreater.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		register(LESS_EXPERTS, Double.class, Long.class, NumberGreater.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		register(LESS_EXPERTS, Double.class, Integer.class, NumberGreater.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		// Decimal
		register(LESS_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalNumericGreater.getInstance(), EvaluationFunctionProperty.reverse);
		register(LESS_EXPERTS, BigDecimal.class, Double.class, DecimalNumericGreater.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		register(LESS_EXPERTS, BigDecimal.class, Float.class, DecimalNumericGreater.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		register(LESS_EXPERTS, BigDecimal.class, Long.class, DecimalNumericGreater.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		register(LESS_EXPERTS, BigDecimal.class, Integer.class, DecimalNumericGreater.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		// String
		register(LESS_EXPERTS, String.class, String.class, StringGreater.getInstance(), EvaluationFunctionProperty.reverse);
		// Date
		register(LESS_EXPERTS, Date.class, Date.class, DateGreater.getInstance(), EvaluationFunctionProperty.reverse);

		// GreaterOrEqual experts
		// Integer
		register(LESS_OR_EQUAL_EXPERTS, Integer.class, Integer.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.reverse);
		// Long
		register(LESS_OR_EQUAL_EXPERTS, Long.class, Long.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.reverse);
		register(LESS_OR_EQUAL_EXPERTS, Long.class, Integer.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		// Float
		register(LESS_OR_EQUAL_EXPERTS, Float.class, Float.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.reverse);
		register(LESS_OR_EQUAL_EXPERTS, Float.class, Long.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		register(LESS_OR_EQUAL_EXPERTS, Float.class, Integer.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		// Double
		register(LESS_OR_EQUAL_EXPERTS, Double.class, Double.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.reverse);
		register(LESS_OR_EQUAL_EXPERTS, Double.class, Float.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		register(LESS_OR_EQUAL_EXPERTS, Double.class, Long.class, NumberGreaterOrEqual.getInstance(), EvaluationFunctionProperty.reverseCommutative);
		register(LESS_OR_EQUAL_EXPERTS, Double.class, Integer.class, NumberGreaterOrEqual.getInstance(),
				EvaluationFunctionProperty.reverseCommutative);
		// Decimal
		register(LESS_OR_EQUAL_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalNumericGreaterOrEqual.getInstance(),
				EvaluationFunctionProperty.reverse);
		register(LESS_OR_EQUAL_EXPERTS, BigDecimal.class, Double.class, DecimalNumericGreaterOrEqual.getInstance(),
				EvaluationFunctionProperty.reverseCommutative);
		register(LESS_OR_EQUAL_EXPERTS, BigDecimal.class, Float.class, DecimalNumericGreaterOrEqual.getInstance(),
				EvaluationFunctionProperty.reverseCommutative);
		register(LESS_OR_EQUAL_EXPERTS, BigDecimal.class, Long.class, DecimalNumericGreaterOrEqual.getInstance(),
				EvaluationFunctionProperty.reverseCommutative);
		register(LESS_OR_EQUAL_EXPERTS, BigDecimal.class, Integer.class, DecimalNumericGreaterOrEqual.getInstance(),
				EvaluationFunctionProperty.reverseCommutative);
		// String
		register(LESS_OR_EQUAL_EXPERTS, String.class, String.class, StringGreaterOrEqual.getInstance(), EvaluationFunctionProperty.reverse);
		// Date
		register(LESS_OR_EQUAL_EXPERTS, Date.class, Date.class, DateGreaterOrEqual.getInstance(), EvaluationFunctionProperty.reverse);

		// Like Expert
		register(LIKE_EXPERTS, String.class, String.class, StringLike.getInstance(), EvaluationFunctionProperty.oneway);

		// Ilike Expert
		register(ILIKE_EXPERTS, String.class, String.class, StringIlike.getInstance(), EvaluationFunctionProperty.oneway);

		// Invalid equal comparisons
		registerDefaultFalse(EQUAL_EXPERTS, Boolean.class, BigDecimal.class);
		registerDefaultFalse(EQUAL_EXPERTS, Boolean.class, Double.class);
		registerDefaultFalse(EQUAL_EXPERTS, Boolean.class, Float.class);
		registerDefaultFalse(EQUAL_EXPERTS, Boolean.class, Long.class);
		registerDefaultFalse(EQUAL_EXPERTS, Boolean.class, Integer.class);
		registerDefaultFalse(EQUAL_EXPERTS, String.class, Boolean.class);
		registerDefaultFalse(EQUAL_EXPERTS, String.class, BigDecimal.class);
		registerDefaultFalse(EQUAL_EXPERTS, String.class, Double.class);
		registerDefaultFalse(EQUAL_EXPERTS, String.class, Float.class);
		registerDefaultFalse(EQUAL_EXPERTS, String.class, Long.class);
		registerDefaultFalse(EQUAL_EXPERTS, String.class, Integer.class);
		registerDefaultFalse(EQUAL_EXPERTS, Date.class, String.class);
		registerDefaultFalse(EQUAL_EXPERTS, Date.class, Boolean.class);
		registerDefaultFalse(EQUAL_EXPERTS, Date.class, BigDecimal.class);
		registerDefaultFalse(EQUAL_EXPERTS, Date.class, Double.class);
		registerDefaultFalse(EQUAL_EXPERTS, Date.class, Float.class);
		registerDefaultFalse(EQUAL_EXPERTS, Date.class, Long.class);
		registerDefaultFalse(EQUAL_EXPERTS, Date.class, Integer.class);
		registerDefaultFalse(EQUAL_EXPERTS, DateOffset.class, Date.class);
		registerDefaultFalse(EQUAL_EXPERTS, DateOffset.class, String.class);
		registerDefaultFalse(EQUAL_EXPERTS, DateOffset.class, Boolean.class);
		registerDefaultFalse(EQUAL_EXPERTS, DateOffset.class, BigDecimal.class);
		registerDefaultFalse(EQUAL_EXPERTS, DateOffset.class, Double.class);
		registerDefaultFalse(EQUAL_EXPERTS, DateOffset.class, Float.class);
		registerDefaultFalse(EQUAL_EXPERTS, DateOffset.class, Long.class);
		registerDefaultFalse(EQUAL_EXPERTS, DateOffset.class, Integer.class);
		registerDefaultFalse(EQUAL_EXPERTS, TimeZoneOffset.class, DateOffset.class);
		registerDefaultFalse(EQUAL_EXPERTS, TimeZoneOffset.class, Date.class);
		registerDefaultFalse(EQUAL_EXPERTS, TimeZoneOffset.class, String.class);
		registerDefaultFalse(EQUAL_EXPERTS, TimeZoneOffset.class, Boolean.class);
		registerDefaultFalse(EQUAL_EXPERTS, TimeZoneOffset.class, BigDecimal.class);
		registerDefaultFalse(EQUAL_EXPERTS, TimeZoneOffset.class, Double.class);
		registerDefaultFalse(EQUAL_EXPERTS, TimeZoneOffset.class, Float.class);
		registerDefaultFalse(EQUAL_EXPERTS, TimeZoneOffset.class, Long.class);
		registerDefaultFalse(EQUAL_EXPERTS, TimeZoneOffset.class, Integer.class);

	}

	/**
	 * Adds a defaultFalseExpert to a registry with the key as a pair of
	 * GenericModelTypes based on the provided Classes
	 * 
	 * @param clazza
	 *            First element of Pair that will be used as a key for the
	 *            registry
	 * @param clazzb
	 *            Second element of Pair that will be used as a key for the
	 *            registry
	 */
	private static void registerDefaultFalse(Map<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>> expertReigsitery,
			Class<?> clazza, Class<?> clazzb) {
		register(expertReigsitery, clazza, clazzb, DefaultFalse.getInstance(), EvaluationFunctionProperty.commutative);
	}

	/**
	 * Adds an expert to a registry with the key as a pair of GenericModelTypes
	 * based on the provided Classes
	 * 
	 * @param expertRegistry
	 *            Target registry
	 * @param clazza
	 *            First element of Pair that will be used as a key for the
	 *            registry
	 * @param clazzb
	 *            Second element of Pair that will be used as a key for the
	 *            registry
	 * @param expert
	 *            Expert that will be registered
	 */
	private static <O1, O2> void register(Map<Pair<GenericModelType, GenericModelType>, PredicateEvalExpert<?, ?>> expertRegistry, Class<?> clazza,
			Class<?> clazzb, PredicateEvalExpert<O1, O2> expert, EvaluationFunctionProperty status) {

		GenericModelType type1 = GMF.getTypeReflection().getType(clazza);
		GenericModelType type2 = GMF.getTypeReflection().getType(clazzb);

		Pair<GenericModelType, GenericModelType> forwardKeyPair = new Pair<GenericModelType, GenericModelType>(type1, type2);
		Pair<GenericModelType, GenericModelType> reverseKeyPair = new Pair<GenericModelType, GenericModelType>(type2, type1);
		switch (status) {
			case commutative:
				expertRegistry.put(reverseKeyPair, expert);
				//$FALL-THROUGH$
			case oneway:
				expertRegistry.put(forwardKeyPair, expert);
				break;
			case reverseCommutative:
				InvertingPredicateEvalExpert<O1, O2> reverseExpert = new InvertingPredicateEvalExpert<O1, O2>(expert);
				expertRegistry.put(reverseKeyPair, reverseExpert);
				expertRegistry.put(forwardKeyPair, reverseExpert);
				break;
			case reverse:
				expertRegistry.put(forwardKeyPair, new InvertingPredicateEvalExpert<O1, O2>(expert));
				break;
			default:
				break;
		}
	}

	/**
	 * Some of the evaluation experts are just an inversion or a negation of
	 * other existing experts. This method handles the process of identifying
	 * the intended expert given the original expert. It stores the computed
	 * expert at a registry to preserve memory
	 * 
	 * @param expertRegistry
	 *            Registry that houses the dynmically computed experts
	 * @param key
	 *            the expert that will be used as the basis for the identifying
	 *            the result
	 * @param invert
	 *            Target operation, if true an inverting expert will use
	 *            otherwise a negating operation is used
	 */
	private static <O1, O2> PredicateEvalExpert<?, ?> getDynamicExpert(Map<PredicateEvalExpert<?, ?>, PredicateEvalExpert<?, ?>> expertRegistry,
			PredicateEvalExpert<O1, O2> key, boolean invert) {

		PredicateEvalExpert<?, ?> result = expertRegistry.get(key);
		if (result == null) {
			if (invert) {
				result = new InvertingPredicateEvalExpert<O1, O2>(key);
				expertRegistry.put(key, result);
			} else { // negation
				result = new NegatingPredicateEvalExpert<O1, O2>(key);
				expertRegistry.put(key, result);
			}
		}
		return result;
	}

	/**
	 * 
	 * @see PredicateEvalContext#evaluate(Object, Object, PredicateOperator)
	 */
	@Override
	public <T> T evaluate(Object firstOperand, Object secondOperand, PredicateOperator operator) throws VdeRuntimeException {

		PredicateEvalExpert<Object, Object> expert = findExpertFor(firstOperand, secondOperand, operator);
		Object result = expert.evaluate(firstOperand, secondOperand);

		@SuppressWarnings("unchecked")
		T returnValue = (T) result;

		return returnValue;
	}

	/**
	 * Retrieves the correct expert based on the type of the operands and the
	 * selected operator
	 * 
	 * @return A valid expert for the provided operands and operator
	 */
	private static PredicateEvalExpert<Object, Object> findExpertFor(Object firstOperand, Object secondOperand, PredicateOperator operator)
			throws VdeRuntimeException {

		GenericModelType firstObjectType = GMF.getTypeReflection().getType(firstOperand);
		GenericModelType secondObjectType = GMF.getTypeReflection().getType(secondOperand);

		PredicateEvalExpert<?, ?> result = null;
		Pair<GenericModelType, GenericModelType> keyPair = new Pair<GenericModelType, GenericModelType>(firstObjectType, secondObjectType);

		boolean enumExpertCheck = firstOperand instanceof Enum && secondOperand instanceof Enum;

		boolean comparableExpertCheck = firstOperand instanceof Comparable<?> && secondOperand instanceof Comparable<?>;

		switch (operator) {
			case equal:
				result = EQUAL_EXPERTS.get(keyPair);
				if (result == null) {
					if (enumExpertCheck) {
						result = EnumEqual.getInstance();
					} else if (comparableExpertCheck) {
						result = ObjectEqual.getInstance();
					}
				}
				break;
			case greater:
				result = GREATER_EXPERTS.get(keyPair);
				if (result == null) {
					if (enumExpertCheck) {
						result = EnumGreater.getInstance();
					} else if (comparableExpertCheck) {
						result = ObjectGreater.getInstance();
					}
				}
				break;
			case greaterOrEqual:
				result = GREATER_OR_EQUAL_EXPERTS.get(keyPair);
				if (result == null) {
					if (enumExpertCheck) {
						result = EnumGreaterOrEqual.getInstance();
					} else if (comparableExpertCheck) {
						result = ObjectGreaterOrEqual.getInstance();
					}
				}
				break;
			case ilike:
				result = ILIKE_EXPERTS.get(keyPair);
				break;
			case in:
				if (secondOperand instanceof Collection<?>) {
					result = ObjectInCollection.getInstance();
				}
				break;
			case less:
				result = LESS_EXPERTS.get(keyPair);
				if (result == null) {
					if (enumExpertCheck) {
						result = getDynamicExpert(DYNAMIC_EXPERTS, EnumGreater.getInstance(), true);
					} else if (comparableExpertCheck) {
						result = getDynamicExpert(DYNAMIC_EXPERTS, ObjectGreater.getInstance(), true);
					}
				}

				break;
			case lessOrEqual:
				result = LESS_OR_EQUAL_EXPERTS.get(keyPair);
				if (result == null) {
					if (enumExpertCheck) {
						result = getDynamicExpert(DYNAMIC_EXPERTS, EnumGreaterOrEqual.getInstance(), true);
					} else if (comparableExpertCheck) {
						result = getDynamicExpert(DYNAMIC_EXPERTS, ObjectGreaterOrEqual.getInstance(), true);
					}
				}

				break;
			case like:
				result = LIKE_EXPERTS.get(keyPair);
				break;
			case notEqual:
				result = EQUAL_EXPERTS.get(keyPair);
				if (result == null) {
					if (enumExpertCheck) {
						result = EnumEqual.getInstance();
					} else if (comparableExpertCheck) {
						result = ObjectEqual.getInstance();
					}
				}
				if (result != null) {
					result = getDynamicExpert(DYNAMIC_EXPERTS, result, false);
				}
				break;
			case assignable:
				if (secondOperand instanceof String) {
					result = ObjectAssignableToString.getInstance();
				} else {
					result = ObjectAssignableToObject.getInstance();
				}
				break;
			case instance:
				if (secondOperand instanceof String) {
					result = ObjectInstanceOfString.getInstance();
				} else {
					result = ObjectInstanceOfObject.getInstance();
				}
				break;
			default:
				break;
		}
		if (result == null) {
			throw new VdeRuntimeException("No predicate evaluator found for leftOperand: " + firstOperand + " with type " + firstOperand.getClass()
					+ ", rightOperand: " + secondOperand + " with type " + secondOperand.getClass() + ", and operator:" + operator);
		}
		@SuppressWarnings("unchecked")
		PredicateEvalExpert<Object, Object> resultExpert = (PredicateEvalExpert<Object, Object>) result;
		return resultExpert;
	}

}
