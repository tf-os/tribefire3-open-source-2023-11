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
package com.braintribe.model.processing.vde.evaluator.impl.arithmetic;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.bvd.math.ArithmeticOperation;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.vde.evaluator.api.EvaluationFunctionProperty;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.arithmetic.ArithmeticEvalExpert;
import com.braintribe.model.processing.vde.evaluator.api.arithmetic.ArithmeticOperator;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.add.DatePlusDateOffset;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.add.DatePlusTimeSpan;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.add.DatePlusTimeZoneOffset;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.add.DecimalNumericAdd;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.add.DoubleNumericAdd;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.add.FloatNumericAdd;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.add.IntegerNumericAdd;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.add.LongNumericAdd;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.add.TimeSpanPlusTimeSpan;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.divide.DecimalNumericDivide;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.divide.DoubleNumericDivide;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.divide.FloatNumericDivide;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.divide.IntegerNumericDivide;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.divide.LongNumericDivide;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.divide.TimeSpanDivideNumber;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.max.DecimalNumericMax;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.max.DoubleNumericMax;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.max.FloatNumericMax;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.max.IntegerNumericMax;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.max.LongNumericMax;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.min.DecimalNumericMin;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.min.DoubleNumericMin;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.min.FloatNumericMin;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.min.IntegerNumericMin;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.min.LongNumericMin;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.multiply.DecimalNumericMultiply;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.multiply.DoubleNumericMultiply;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.multiply.FloatNumericMultiply;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.multiply.IntegerNumericMultiply;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.multiply.LongNumericMultiply;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.multiply.TimeSpanTimesNumber;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.subtract.DateMinusDateOffset;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.subtract.DateMinusTimeSpan;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.subtract.DateMinusTimeZoneOffset;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.subtract.DecimalNumericSubtract;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.subtract.DoubleNumericSubtract;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.subtract.FloatNumericSubtract;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.subtract.IntegerNumericSubtract;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.subtract.LongNumericSubtract;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.subtract.TimeSpanMinusTimeSpan;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeZoneOffset;

/**
 * Context that is responsible for evaluation of all possible
 * {@link ArithmeticOperation}. It identifies the correct implementation of the
 * {@link ArithmeticOperation} that adheres to the types of the provided
 * operands and returns the result of that implementation
 * 
 */
public class ArithmeticOperatorEval {

	// context experts
	private final static Map<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>> ADD_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>> SUBTRACT_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>> MULTIPLY_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>> DIVIDE_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>> MAX_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>> MIN_EXPERTS;

	// setting up the list of default experts
	static {
		ADD_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>>();
		SUBTRACT_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>>();
		MULTIPLY_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>>();
		DIVIDE_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>>();
		MAX_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>>();
		MIN_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>>();

		// Addition experts
		// Integer
		register(ADD_EXPERTS, Integer.class, Integer.class, IntegerNumericAdd.getInstance(), EvaluationFunctionProperty.oneway);
		// Long
		register(ADD_EXPERTS, Long.class, Long.class, LongNumericAdd.getInstance(), EvaluationFunctionProperty.oneway);
		register(ADD_EXPERTS, Long.class, Integer.class, LongNumericAdd.getInstance(), EvaluationFunctionProperty.commutative);
		// Float
		register(ADD_EXPERTS, Float.class, Float.class, FloatNumericAdd.getInstance(), EvaluationFunctionProperty.oneway);
		register(ADD_EXPERTS, Float.class, Long.class, FloatNumericAdd.getInstance(), EvaluationFunctionProperty.commutative);
		register(ADD_EXPERTS, Float.class, Integer.class, FloatNumericAdd.getInstance(), EvaluationFunctionProperty.commutative);
		// Double
		register(ADD_EXPERTS, Double.class, Double.class, DoubleNumericAdd.getInstance(), EvaluationFunctionProperty.oneway);
		register(ADD_EXPERTS, Double.class, Float.class, DoubleNumericAdd.getInstance(), EvaluationFunctionProperty.commutative);
		register(ADD_EXPERTS, Double.class, Long.class, DoubleNumericAdd.getInstance(), EvaluationFunctionProperty.commutative);
		register(ADD_EXPERTS, Double.class, Integer.class, DoubleNumericAdd.getInstance(), EvaluationFunctionProperty.commutative);
		// BigDecimal
		register(ADD_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalNumericAdd.getInstance(), EvaluationFunctionProperty.oneway);
		register(ADD_EXPERTS, BigDecimal.class, Double.class, DecimalNumericAdd.getInstance(), EvaluationFunctionProperty.commutative);
		register(ADD_EXPERTS, BigDecimal.class, Float.class, DecimalNumericAdd.getInstance(), EvaluationFunctionProperty.commutative);
		register(ADD_EXPERTS, BigDecimal.class, Long.class, DecimalNumericAdd.getInstance(), EvaluationFunctionProperty.commutative);
		register(ADD_EXPERTS, BigDecimal.class, Integer.class, DecimalNumericAdd.getInstance(), EvaluationFunctionProperty.commutative);
		// Date
		register(ADD_EXPERTS, Date.class, TimeSpan.class, DatePlusTimeSpan.getInstance(), EvaluationFunctionProperty.invertible);
		register(ADD_EXPERTS, Date.class, DateOffset.class, DatePlusDateOffset.getInstance(), EvaluationFunctionProperty.invertible);
		register(ADD_EXPERTS, Date.class, TimeZoneOffset.class, DatePlusTimeZoneOffset.getInstance(), EvaluationFunctionProperty.invertible);
		// TimeSpan
		register(ADD_EXPERTS, TimeSpan.class, TimeSpan.class, TimeSpanPlusTimeSpan.getInstance(), EvaluationFunctionProperty.oneway);

		// Subtraction experts
		// Integer
		register(SUBTRACT_EXPERTS, Integer.class, Integer.class, IntegerNumericSubtract.getInstance(), EvaluationFunctionProperty.oneway);
		// Long
		register(SUBTRACT_EXPERTS, Long.class, Long.class, LongNumericSubtract.getInstance(), EvaluationFunctionProperty.oneway);
		register(SUBTRACT_EXPERTS, Long.class, Integer.class, LongNumericSubtract.getInstance(), EvaluationFunctionProperty.commutative);
		// Float
		register(SUBTRACT_EXPERTS, Float.class, Float.class, FloatNumericSubtract.getInstance(), EvaluationFunctionProperty.oneway);
		register(SUBTRACT_EXPERTS, Float.class, Long.class, FloatNumericSubtract.getInstance(), EvaluationFunctionProperty.commutative);
		register(SUBTRACT_EXPERTS, Float.class, Integer.class, FloatNumericSubtract.getInstance(), EvaluationFunctionProperty.commutative);
		// Double
		register(SUBTRACT_EXPERTS, Double.class, Double.class, DoubleNumericSubtract.getInstance(), EvaluationFunctionProperty.oneway);
		register(SUBTRACT_EXPERTS, Double.class, Float.class, DoubleNumericSubtract.getInstance(), EvaluationFunctionProperty.commutative);
		register(SUBTRACT_EXPERTS, Double.class, Long.class, DoubleNumericSubtract.getInstance(), EvaluationFunctionProperty.commutative);
		register(SUBTRACT_EXPERTS, Double.class, Integer.class, DoubleNumericSubtract.getInstance(), EvaluationFunctionProperty.commutative);
		// BigDecimal
		register(SUBTRACT_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalNumericSubtract.getInstance(), EvaluationFunctionProperty.oneway);
		register(SUBTRACT_EXPERTS, BigDecimal.class, Double.class, DecimalNumericSubtract.getInstance(), EvaluationFunctionProperty.commutative);
		register(SUBTRACT_EXPERTS, BigDecimal.class, Float.class, DecimalNumericSubtract.getInstance(), EvaluationFunctionProperty.commutative);
		register(SUBTRACT_EXPERTS, BigDecimal.class, Long.class, DecimalNumericSubtract.getInstance(), EvaluationFunctionProperty.commutative);
		register(SUBTRACT_EXPERTS, BigDecimal.class, Integer.class, DecimalNumericSubtract.getInstance(), EvaluationFunctionProperty.commutative);
		// Date
		register(SUBTRACT_EXPERTS, Date.class, TimeSpan.class, DateMinusTimeSpan.getInstance(), EvaluationFunctionProperty.oneway);
		register(SUBTRACT_EXPERTS, Date.class, DateOffset.class, DateMinusDateOffset.getInstance(), EvaluationFunctionProperty.oneway);
		register(SUBTRACT_EXPERTS, Date.class, TimeZoneOffset.class, DateMinusTimeZoneOffset.getInstance(), EvaluationFunctionProperty.oneway);
		// TimeSpan
		register(SUBTRACT_EXPERTS, TimeSpan.class, TimeSpan.class, TimeSpanMinusTimeSpan.getInstance(), EvaluationFunctionProperty.oneway);

		// Multiply experts
		// Integer
		register(MULTIPLY_EXPERTS, Integer.class, Integer.class, IntegerNumericMultiply.getInstance(), EvaluationFunctionProperty.oneway);
		// Long
		register(MULTIPLY_EXPERTS, Long.class, Long.class, LongNumericMultiply.getInstance(), EvaluationFunctionProperty.oneway);
		register(MULTIPLY_EXPERTS, Long.class, Integer.class, LongNumericMultiply.getInstance(), EvaluationFunctionProperty.commutative);
		// Float
		register(MULTIPLY_EXPERTS, Float.class, Float.class, FloatNumericMultiply.getInstance(), EvaluationFunctionProperty.oneway);
		register(MULTIPLY_EXPERTS, Float.class, Long.class, FloatNumericMultiply.getInstance(), EvaluationFunctionProperty.commutative);
		register(MULTIPLY_EXPERTS, Float.class, Integer.class, FloatNumericMultiply.getInstance(), EvaluationFunctionProperty.commutative);
		// Double
		register(MULTIPLY_EXPERTS, Double.class, Double.class, DoubleNumericMultiply.getInstance(), EvaluationFunctionProperty.oneway);
		register(MULTIPLY_EXPERTS, Double.class, Float.class, DoubleNumericMultiply.getInstance(), EvaluationFunctionProperty.commutative);
		register(MULTIPLY_EXPERTS, Double.class, Long.class, DoubleNumericMultiply.getInstance(), EvaluationFunctionProperty.commutative);
		register(MULTIPLY_EXPERTS, Double.class, Integer.class, DoubleNumericMultiply.getInstance(), EvaluationFunctionProperty.commutative);
		// BigDecimal
		register(MULTIPLY_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalNumericMultiply.getInstance(), EvaluationFunctionProperty.oneway);
		register(MULTIPLY_EXPERTS, BigDecimal.class, Double.class, DecimalNumericMultiply.getInstance(), EvaluationFunctionProperty.commutative);
		register(MULTIPLY_EXPERTS, BigDecimal.class, Float.class, DecimalNumericMultiply.getInstance(), EvaluationFunctionProperty.commutative);
		register(MULTIPLY_EXPERTS, BigDecimal.class, Long.class, DecimalNumericMultiply.getInstance(), EvaluationFunctionProperty.commutative);
		register(MULTIPLY_EXPERTS, BigDecimal.class, Integer.class, DecimalNumericMultiply.getInstance(), EvaluationFunctionProperty.commutative);
		// TimeSpan
		register(MULTIPLY_EXPERTS, TimeSpan.class, BigDecimal.class, TimeSpanTimesNumber.getInstance(), EvaluationFunctionProperty.invertible);
		register(MULTIPLY_EXPERTS, TimeSpan.class, Double.class, TimeSpanTimesNumber.getInstance(), EvaluationFunctionProperty.invertible);
		register(MULTIPLY_EXPERTS, TimeSpan.class, Float.class, TimeSpanTimesNumber.getInstance(), EvaluationFunctionProperty.invertible);
		register(MULTIPLY_EXPERTS, TimeSpan.class, Long.class, TimeSpanTimesNumber.getInstance(), EvaluationFunctionProperty.invertible);
		register(MULTIPLY_EXPERTS, TimeSpan.class, Integer.class, TimeSpanTimesNumber.getInstance(), EvaluationFunctionProperty.invertible);

		// Divide experts
		// Integer
		register(DIVIDE_EXPERTS, Integer.class, Integer.class, IntegerNumericDivide.getInstance(), EvaluationFunctionProperty.oneway);
		// Long
		register(DIVIDE_EXPERTS, Long.class, Long.class, LongNumericDivide.getInstance(), EvaluationFunctionProperty.oneway);
		register(DIVIDE_EXPERTS, Long.class, Integer.class, LongNumericDivide.getInstance(), EvaluationFunctionProperty.commutative);
		// Float
		register(DIVIDE_EXPERTS, Float.class, Float.class, FloatNumericDivide.getInstance(), EvaluationFunctionProperty.oneway);
		register(DIVIDE_EXPERTS, Float.class, Long.class, FloatNumericDivide.getInstance(), EvaluationFunctionProperty.commutative);
		register(DIVIDE_EXPERTS, Float.class, Integer.class, FloatNumericDivide.getInstance(), EvaluationFunctionProperty.commutative);
		// Double
		register(DIVIDE_EXPERTS, Double.class, Double.class, DoubleNumericDivide.getInstance(), EvaluationFunctionProperty.oneway);
		register(DIVIDE_EXPERTS, Double.class, Float.class, DoubleNumericDivide.getInstance(), EvaluationFunctionProperty.commutative);
		register(DIVIDE_EXPERTS, Double.class, Long.class, DoubleNumericDivide.getInstance(), EvaluationFunctionProperty.commutative);
		register(DIVIDE_EXPERTS, Double.class, Integer.class, DoubleNumericDivide.getInstance(), EvaluationFunctionProperty.commutative);
		// BigDecimal
		register(DIVIDE_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalNumericDivide.getInstance(), EvaluationFunctionProperty.oneway);
		register(DIVIDE_EXPERTS, BigDecimal.class, Double.class, DecimalNumericDivide.getInstance(), EvaluationFunctionProperty.commutative);
		register(DIVIDE_EXPERTS, BigDecimal.class, Float.class, DecimalNumericDivide.getInstance(), EvaluationFunctionProperty.commutative);
		register(DIVIDE_EXPERTS, BigDecimal.class, Long.class, DecimalNumericDivide.getInstance(), EvaluationFunctionProperty.commutative);
		register(DIVIDE_EXPERTS, BigDecimal.class, Integer.class, DecimalNumericDivide.getInstance(), EvaluationFunctionProperty.commutative);
		// TimeSpan
		register(DIVIDE_EXPERTS, TimeSpan.class, BigDecimal.class, TimeSpanDivideNumber.getInstance(), EvaluationFunctionProperty.oneway);
		register(DIVIDE_EXPERTS, TimeSpan.class, Double.class, TimeSpanDivideNumber.getInstance(), EvaluationFunctionProperty.oneway);
		register(DIVIDE_EXPERTS, TimeSpan.class, Float.class, TimeSpanDivideNumber.getInstance(), EvaluationFunctionProperty.oneway);
		register(DIVIDE_EXPERTS, TimeSpan.class, Long.class, TimeSpanDivideNumber.getInstance(), EvaluationFunctionProperty.oneway);
		register(DIVIDE_EXPERTS, TimeSpan.class, Integer.class, TimeSpanDivideNumber.getInstance(), EvaluationFunctionProperty.oneway);
		
		// Max experts
		// Integer
		register(MAX_EXPERTS, Integer.class, Integer.class, IntegerNumericMax.getInstance(), EvaluationFunctionProperty.oneway);
		// Long
		register(MAX_EXPERTS, Long.class, Long.class, LongNumericMax.getInstance(), EvaluationFunctionProperty.oneway);
		register(MAX_EXPERTS, Long.class, Integer.class, LongNumericMax.getInstance(), EvaluationFunctionProperty.commutative);
		// Float
		register(MAX_EXPERTS, Float.class, Float.class, FloatNumericMax.getInstance(), EvaluationFunctionProperty.oneway);
		register(MAX_EXPERTS, Float.class, Long.class, FloatNumericMax.getInstance(), EvaluationFunctionProperty.commutative);
		register(MAX_EXPERTS, Float.class, Integer.class, FloatNumericMax.getInstance(), EvaluationFunctionProperty.commutative);
		// Double
		register(MAX_EXPERTS, Double.class, Double.class, DoubleNumericMax.getInstance(), EvaluationFunctionProperty.oneway);
		register(MAX_EXPERTS, Double.class, Float.class, DoubleNumericMax.getInstance(), EvaluationFunctionProperty.commutative);
		register(MAX_EXPERTS, Double.class, Long.class, DoubleNumericMax.getInstance(), EvaluationFunctionProperty.commutative);
		register(MAX_EXPERTS, Double.class, Integer.class, DoubleNumericMax.getInstance(), EvaluationFunctionProperty.commutative);
		// BigDecimal
		register(MAX_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalNumericMax.getInstance(), EvaluationFunctionProperty.oneway);
		register(MAX_EXPERTS, BigDecimal.class, Double.class, DecimalNumericMax.getInstance(), EvaluationFunctionProperty.commutative);
		register(MAX_EXPERTS, BigDecimal.class, Float.class, DecimalNumericMax.getInstance(), EvaluationFunctionProperty.commutative);
		register(MAX_EXPERTS, BigDecimal.class, Long.class, DecimalNumericMax.getInstance(), EvaluationFunctionProperty.commutative);
		register(MAX_EXPERTS, BigDecimal.class, Integer.class, DecimalNumericMax.getInstance(), EvaluationFunctionProperty.commutative);
		
		// Min experts
		// Integer
		register(MIN_EXPERTS, Integer.class, Integer.class, IntegerNumericMin.getInstance(), EvaluationFunctionProperty.oneway);
		// Long
		register(MIN_EXPERTS, Long.class, Long.class, LongNumericMin.getInstance(), EvaluationFunctionProperty.oneway);
		register(MIN_EXPERTS, Long.class, Integer.class, LongNumericMin.getInstance(), EvaluationFunctionProperty.commutative);
		// Float
		register(MIN_EXPERTS, Float.class, Float.class, FloatNumericMin.getInstance(), EvaluationFunctionProperty.oneway);
		register(MIN_EXPERTS, Float.class, Long.class, FloatNumericMin.getInstance(), EvaluationFunctionProperty.commutative);
		register(MIN_EXPERTS, Float.class, Integer.class, FloatNumericMin.getInstance(), EvaluationFunctionProperty.commutative);
		// Double
		register(MIN_EXPERTS, Double.class, Double.class, DoubleNumericMin.getInstance(), EvaluationFunctionProperty.oneway);
		register(MIN_EXPERTS, Double.class, Float.class, DoubleNumericMin.getInstance(), EvaluationFunctionProperty.commutative);
		register(MIN_EXPERTS, Double.class, Long.class, DoubleNumericMin.getInstance(), EvaluationFunctionProperty.commutative);
		register(MIN_EXPERTS, Double.class, Integer.class, DoubleNumericMin.getInstance(), EvaluationFunctionProperty.commutative);
		// BigDecimal
		register(MIN_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalNumericMin.getInstance(), EvaluationFunctionProperty.oneway);
		register(MIN_EXPERTS, BigDecimal.class, Double.class, DecimalNumericMin.getInstance(), EvaluationFunctionProperty.commutative);
		register(MIN_EXPERTS, BigDecimal.class, Float.class, DecimalNumericMin.getInstance(), EvaluationFunctionProperty.commutative);
		register(MIN_EXPERTS, BigDecimal.class, Long.class, DecimalNumericMin.getInstance(), EvaluationFunctionProperty.commutative);
		register(MIN_EXPERTS, BigDecimal.class, Integer.class, DecimalNumericMin.getInstance(), EvaluationFunctionProperty.commutative);
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
	public static <O1, O2> void register(Map<Pair<GenericModelType, GenericModelType>, ArithmeticEvalExpert<?, ?>> expertRegistry, Class<?> clazza,
			Class<?> clazzb, ArithmeticEvalExpert<O1, O2> expert, EvaluationFunctionProperty status) {

		GenericModelType type1 = GMF.getTypeReflection().getType(clazza);
		GenericModelType type2 = GMF.getTypeReflection().getType(clazzb);

		expertRegistry.put(new Pair<GenericModelType, GenericModelType>(type1, type2), expert);

		switch (status) {
			case commutative:
				expertRegistry.put(new Pair<GenericModelType, GenericModelType>(type2, type1), expert);
				break;
			case invertible:
				expertRegistry.put(new Pair<GenericModelType, GenericModelType>(type2, type1), new InvertingArithmeticEvalExpert<O1, O2>(expert));
				break;
			default:
				// nothing needs to be done
				break;
		}
	}

	/**
	 * Evaluates the operands according to the {@link ArithmeticOperator}. It
	 * selects the correct {@link ArithmeticEvalExpert} implementation that
	 * satisfies the types of the operands and the given operator
	 * 
	 * @param firstOperand
	 *            The first value of the arithmetic operation
	 * @param secondOperand
	 *            The second value of the arithmetic operation
	 * @param operator
	 *            The type of arithmetic that will be performed
	 * @return The result of applying operator on the first and second operands
	 */
	public static <T> T evaluate(Object firstOperand, Object secondOperand, ArithmeticOperator operator) throws VdeRuntimeException {

		ArithmeticEvalExpert<Object, Object> expert = findExpertFor(firstOperand, secondOperand, operator);
		Object result = expert.evaluate(firstOperand, secondOperand);

		@SuppressWarnings("unchecked")
		T returnValue = (T) result;

		return returnValue;
	}

	/**
	 * Retrieves the correct expert based on the type of the operands and the
	 * selected operation
	 * 
	 * @return A valid expert for the provided operands and operation
	 */
	private static ArithmeticEvalExpert<Object, Object> findExpertFor(Object firstOperand, Object secondOperand, ArithmeticOperator operator)
			throws VdeRuntimeException {

		GenericModelType firstObjectType = GMF.getTypeReflection().getType(firstOperand);
		GenericModelType secondObjectType = GMF.getTypeReflection().getType(secondOperand);

		ArithmeticEvalExpert<?, ?> result = null;

		Pair<GenericModelType, GenericModelType> keyPair = new Pair<GenericModelType, GenericModelType>(firstObjectType, secondObjectType);
		switch (operator) {
			case plus:
				result = ADD_EXPERTS.get(keyPair);
				break;
			case minus:
				result = SUBTRACT_EXPERTS.get(keyPair);
				break;
			case times:
				result = MULTIPLY_EXPERTS.get(keyPair);
				break;
			case divide:
				result = DIVIDE_EXPERTS.get(keyPair);
				break;
			case maximum:
				result = MAX_EXPERTS.get(keyPair);
				break;	
			case minimum:
				result = MIN_EXPERTS.get(keyPair);
				break;	
			default:
				break;
		}
		if (result == null) {
			throw new VdeRuntimeException("No arithmetic evaluator found for firstOperand: " + firstOperand + " with type " + firstOperand.getClass()
					+ ", secondOperand: " + secondOperand + " with type " + secondOperand.getClass() + ", and operator:" + operator);
		}
		@SuppressWarnings("unchecked")
		ArithmeticEvalExpert<Object, Object> resultExpert = (ArithmeticEvalExpert<Object, Object>) result;
		return resultExpert;
	}

}
