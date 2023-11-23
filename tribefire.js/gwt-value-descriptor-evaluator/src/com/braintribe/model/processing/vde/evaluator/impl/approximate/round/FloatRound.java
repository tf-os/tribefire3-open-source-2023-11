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
package com.braintribe.model.processing.vde.evaluator.impl.approximate.round;

import com.braintribe.model.bvd.math.Round;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.approximate.ApproximateEvalExpert;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.ApproximateVdeUtil;

/**
 * Expert for {@link Round} that operates on value of type Float and precision of type Number 
 *
 */
public class FloatRound implements ApproximateEvalExpert<Float, Number> {

	private static FloatRound instance = null;

	protected FloatRound() {
		// empty
	}

	public static FloatRound getInstance() {
		if (instance == null) {
			instance = new FloatRound();
		}
		return instance;
	}
	
	@Override
	public Object evaluate(Float firstOperand, Number secondOperand) throws VdeRuntimeException {
		Float result = null;
		if (firstOperand >= 0){
			result = ApproximateVdeUtil.getRound(firstOperand, secondOperand).floatValue();
		}
		else{
			result = -1.0f * ApproximateVdeUtil.getRound(-1*firstOperand, secondOperand,false).floatValue();
		}
		return result;
	}

}
