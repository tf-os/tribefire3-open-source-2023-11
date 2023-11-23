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

import java.util.Date;

import com.braintribe.model.processing.query.eval.set.aggregate.ArithmeticExpert;

/**
 * 
 */
public class DateAggregateExpert implements ArithmeticExpert<Date> {

	public static final DateAggregateExpert INSTANCE = new DateAggregateExpert();

	private DateAggregateExpert() {
	}

	@Override
	public Date add(Date v1, Date v2) {
		return new Date(v1.getTime() + v2.getTime());
	}

	@Override
	public Date divide(Date value, int count) {
		return new Date(value.getTime() / count);
	}

	@Override
	public int compare(Date v1, Date v2) {
		return v1.compareTo(v2);
	}

}
