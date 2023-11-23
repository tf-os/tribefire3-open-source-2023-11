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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.cast;

import java.math.BigDecimal;

public class AbstractCastVde {

	protected static boolean isValidCastOperand(Object operand) {
		boolean result = isInteger(operand) || isDouble(operand) || isFloat(operand) || isLong(operand) || isBigDecimal(operand);
		return result;
	}

	private static boolean isInteger(Object operand) {
		return operand instanceof Integer;
	}

	private static boolean isDouble(Object operand) {
		return operand instanceof Double;
	}

	private static boolean isFloat(Object operand) {
		return operand instanceof Float;
	}

	private static boolean isLong(Object operand) {
		return operand instanceof Long;
	}

	private static boolean isBigDecimal(Object operand) {
		return operand instanceof BigDecimal;
	}
}
