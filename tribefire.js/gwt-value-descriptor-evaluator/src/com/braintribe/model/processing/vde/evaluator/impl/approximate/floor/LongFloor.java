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
package com.braintribe.model.processing.vde.evaluator.impl.approximate.floor;

import com.braintribe.model.bvd.math.Floor;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.approximate.ApproximateEvalExpert;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.ApproximateVdeUtil;

/**
 * Expert for {@link Floor} that operates on value of type Long and precision of type Number 
 *
 */
public class LongFloor implements ApproximateEvalExpert<Long, Number> {

	private static LongFloor instance = null;

	protected LongFloor() {
		// empty
	}

	public static LongFloor getInstance() {
		if (instance == null) {
			instance = new LongFloor();
		}
		return instance;
	}
	
	@Override
	public Object evaluate(Long firstOperand, Number secondOperand) throws VdeRuntimeException {
		Long result = null;
		if (firstOperand >= 0){
			result = ApproximateVdeUtil.getFloor(firstOperand, secondOperand).longValue();
		}
		else{
			result = -1 * ApproximateVdeUtil.getCeil(-1*firstOperand, secondOperand).longValue();
		}
		return result;
	}

}
