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
package com.braintribe.model.processing.query.eval.set.aggregate.arithmetic;

import com.braintribe.model.processing.query.eval.set.aggregate.ArithmeticExpert;

/**
 * 
 */
public class FloatAggregateExpert implements ArithmeticExpert<Float> {

	public static final FloatAggregateExpert INSTANCE = new FloatAggregateExpert();

	private FloatAggregateExpert() {
	}

	@Override
	public Float add(Float v1, Float v2) {
		return v1 + v2;
	}

	@Override
	public Float divide(Float value, int count) {
		return value / count;
	}

	@Override
	public int compare(Float v1, Float v2) {
		return v1.compareTo(v2);
	}

}
