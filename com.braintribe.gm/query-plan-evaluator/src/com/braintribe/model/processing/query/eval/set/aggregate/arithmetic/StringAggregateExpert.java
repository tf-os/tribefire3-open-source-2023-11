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
public class StringAggregateExpert implements ArithmeticExpert<String> {

	public static final StringAggregateExpert INSTANCE = new StringAggregateExpert();

	private StringAggregateExpert() {
	}

	@Override
	public String add(String v1, String v2) {
		throw new UnsupportedOperationException("Cannot add two strings.");
	}

	@Override
	public String divide(String value, int count) {
		throw new UnsupportedOperationException("Cannot divide string by int.");
	}

	@Override
	public int compare(String v1, String v2) {
		return v1.compareTo(v2);
	}

}
