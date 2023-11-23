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
package com.braintribe.model.processing.vde.evaluator.impl.approximate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.approximate.ApproximateEvalContext;
import com.braintribe.model.processing.vde.evaluator.api.approximate.ApproximateEvalExpert;
import com.braintribe.model.processing.vde.evaluator.api.approximate.ApproximateOperator;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.ceil.DateCeil;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.ceil.DecimalCeil;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.ceil.DoubleCeil;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.ceil.FloatCeil;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.ceil.IntegerCeil;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.ceil.LongCeil;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.floor.DateFloor;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.floor.DecimalFloor;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.floor.DoubleFloor;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.floor.FloatFloor;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.floor.IntegerFloor;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.floor.LongFloor;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.round.DateRound;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.round.DecimalRound;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.round.DoubleRound;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.round.FloatRound;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.round.IntegerRound;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.round.LongRound;
import com.braintribe.model.time.DateOffset;

/**
 * Implementation for {@link ApproximateEvalContext}
 * 
 */
public class ApproximateEvalContextImpl implements ApproximateEvalContext {

	// context experts
	private final static Map<Pair<GenericModelType, GenericModelType>, ApproximateEvalExpert<?, ?>> FLOOR_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, ApproximateEvalExpert<?, ?>> CEIL_EXPERTS;
	private final static Map<Pair<GenericModelType, GenericModelType>, ApproximateEvalExpert<?, ?>> ROUND_EXPERTS;

	// setting up the list of default experts
	static {
		FLOOR_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, ApproximateEvalExpert<?, ?>>();
		CEIL_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, ApproximateEvalExpert<?, ?>>();
		ROUND_EXPERTS = new HashMap<Pair<GenericModelType, GenericModelType>, ApproximateEvalExpert<?, ?>>();

		// Floor experts
		// Integer
		register(FLOOR_EXPERTS, Integer.class, Integer.class, IntegerFloor.getInstance());
		// Long
		register(FLOOR_EXPERTS, Long.class, Long.class, LongFloor.getInstance());
		register(FLOOR_EXPERTS, Long.class, Integer.class, LongFloor.getInstance());
		// Float
		register(FLOOR_EXPERTS, Float.class, Float.class, FloatFloor.getInstance());
		register(FLOOR_EXPERTS, Float.class, Long.class, FloatFloor.getInstance());
		register(FLOOR_EXPERTS, Float.class, Integer.class, FloatFloor.getInstance());
		// Double
		register(FLOOR_EXPERTS, Double.class, Double.class, DoubleFloor.getInstance());
		register(FLOOR_EXPERTS, Double.class, Float.class, DoubleFloor.getInstance());
		register(FLOOR_EXPERTS, Double.class, Long.class, DoubleFloor.getInstance());
		register(FLOOR_EXPERTS, Double.class, Integer.class, DoubleFloor.getInstance());
		// Decimal
		register(FLOOR_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalFloor.getInstance());
		register(FLOOR_EXPERTS, BigDecimal.class, Double.class, DecimalFloor.getInstance());
		register(FLOOR_EXPERTS, BigDecimal.class, Float.class, DecimalFloor.getInstance());
		register(FLOOR_EXPERTS, BigDecimal.class, Long.class, DecimalFloor.getInstance());
		register(FLOOR_EXPERTS, BigDecimal.class, Integer.class, DecimalFloor.getInstance());
		// Date
		register(FLOOR_EXPERTS, Date.class, DateOffset.class, DateFloor.getInstance());

		// Ceil experts
		// Integer
		register(CEIL_EXPERTS, Integer.class, Integer.class, IntegerCeil.getInstance());
		// Long
		register(CEIL_EXPERTS, Long.class, Long.class, LongCeil.getInstance());
		register(CEIL_EXPERTS, Long.class, Integer.class, LongCeil.getInstance());
		// Float
		register(CEIL_EXPERTS, Float.class, Float.class, FloatCeil.getInstance());
		register(CEIL_EXPERTS, Float.class, Long.class, FloatCeil.getInstance());
		register(CEIL_EXPERTS, Float.class, Integer.class, FloatCeil.getInstance());
		// Double
		register(CEIL_EXPERTS, Double.class, Double.class, DoubleCeil.getInstance());
		register(CEIL_EXPERTS, Double.class, Float.class, DoubleCeil.getInstance());
		register(CEIL_EXPERTS, Double.class, Long.class, DoubleCeil.getInstance());
		register(CEIL_EXPERTS, Double.class, Integer.class, DoubleCeil.getInstance());
		// Decimal
		register(CEIL_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalCeil.getInstance());
		register(CEIL_EXPERTS, BigDecimal.class, Double.class, DecimalCeil.getInstance());
		register(CEIL_EXPERTS, BigDecimal.class, Float.class, DecimalCeil.getInstance());
		register(CEIL_EXPERTS, BigDecimal.class, Long.class, DecimalCeil.getInstance());
		register(CEIL_EXPERTS, BigDecimal.class, Integer.class, DecimalCeil.getInstance());
		// Date
		register(CEIL_EXPERTS, Date.class, DateOffset.class, DateCeil.getInstance());

		// Round experts
		// Integer
		register(ROUND_EXPERTS, Integer.class, Integer.class, IntegerRound.getInstance());
		// Long
		register(ROUND_EXPERTS, Long.class, Long.class, LongRound.getInstance());
		register(ROUND_EXPERTS, Long.class, Integer.class, LongRound.getInstance());
		// Float
		register(ROUND_EXPERTS, Float.class, Float.class, FloatRound.getInstance());
		register(ROUND_EXPERTS, Float.class, Long.class, FloatRound.getInstance());
		register(ROUND_EXPERTS, Float.class, Integer.class, FloatRound.getInstance());
		// Double
		register(ROUND_EXPERTS, Double.class, Double.class, DoubleRound.getInstance());
		register(ROUND_EXPERTS, Double.class, Float.class, DoubleRound.getInstance());
		register(ROUND_EXPERTS, Double.class, Long.class, DoubleRound.getInstance());
		register(ROUND_EXPERTS, Double.class, Integer.class, DoubleRound.getInstance());
		// Decimal
		register(ROUND_EXPERTS, BigDecimal.class, BigDecimal.class, DecimalRound.getInstance());
		register(ROUND_EXPERTS, BigDecimal.class, Double.class, DecimalRound.getInstance());
		register(ROUND_EXPERTS, BigDecimal.class, Float.class, DecimalRound.getInstance());
		register(ROUND_EXPERTS, BigDecimal.class, Long.class, DecimalRound.getInstance());
		register(ROUND_EXPERTS, BigDecimal.class, Integer.class, DecimalRound.getInstance());
		// Date
		register(ROUND_EXPERTS, Date.class, DateOffset.class, DateRound.getInstance());

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
	public static <O1, O2> void register(Map<Pair<GenericModelType, GenericModelType>, ApproximateEvalExpert<?, ?>> expertRegistry, Class<?> clazza,
			Class<?> clazzb, ApproximateEvalExpert<O1, O2> expert) {

		GenericModelType type1 = GMF.getTypeReflection().getType(clazza);
		GenericModelType type2 = GMF.getTypeReflection().getType(clazzb);

		expertRegistry.put(new Pair<GenericModelType, GenericModelType>(type1, type2), expert);

	}

	/**
	 * 
	 * @see ApproximateEvalContext#evaluate(Object, Object, ApproximateOperator)
	 */
	@Override
	public <T> T evaluate(Object firstOperand, Object secondOperand, ApproximateOperator operator) throws VdeRuntimeException {

		ApproximateEvalExpert<Object, Object> expert = findExpertFor(firstOperand, secondOperand, operator);
		Object result = expert.evaluate(firstOperand, secondOperand);

		T returnValue = (T) result;

		return returnValue;
	}

	/**
	 * Retrieves the correct expert based on the type of the operands and the
	 * selected operation
	 * 
	 * @return A valid expert for the provided operands and operation
	 */
	private static ApproximateEvalExpert<Object, Object> findExpertFor(Object firstOperand, Object secondOperand, ApproximateOperator operator)
			throws VdeRuntimeException {

		GenericModelType firstObjectType = GMF.getTypeReflection().getType(firstOperand);
		GenericModelType secondObjectType = GMF.getTypeReflection().getType(secondOperand);

		ApproximateEvalExpert<?, ?> result = null;

		Pair<GenericModelType, GenericModelType> keyPair = new Pair<GenericModelType, GenericModelType>(firstObjectType, secondObjectType);
		switch (operator) {
			case ceil:
				result = CEIL_EXPERTS.get(keyPair);
				break;
			case floor:
				result = FLOOR_EXPERTS.get(keyPair);
				break;
			case round:
				result = ROUND_EXPERTS.get(keyPair);
				break;
		}
		if (result == null) {
			throw new VdeRuntimeException("No approximate evaluator found for firstOperand: " + firstOperand + " with type "
					+ firstOperand.getClass() + ", secondOperand: " + secondOperand + " with type " + secondOperand.getClass() + ", and operator:"
					+ operator);
		}
		ApproximateEvalExpert<Object, Object> resultExpert = (ApproximateEvalExpert<Object, Object>) result;
		return resultExpert;
	}

}
