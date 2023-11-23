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

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Set;

import com.braintribe.model.processing.query.eval.set.aggregate.ArithmeticExpert;

/**
 * 
 */
public class CountDistinctAggregateExpert implements ArithmeticExpert<Object> {

	private final Set<Object> visited = newSet();

	@Override
	public Object add(Object v1, Object v2) {
		if (v2 == null || visited.contains(v2))
			return v1;

		visited.add(v2);

		return (Long) v1 + 1;
	}

	@Override
	public Object divide(Object value, int count) {
		throw new UnsupportedOperationException("Cannot divide Object by int. This is an expert for count distinct!");
	}

	@Override
	public int compare(Object v1, Object v2) {
		throw new UnsupportedOperationException("Comparing not expected. This is an expert for count distinct!");
	}

}
