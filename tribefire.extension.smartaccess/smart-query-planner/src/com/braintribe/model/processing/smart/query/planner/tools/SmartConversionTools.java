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
package com.braintribe.model.processing.smart.query.planner.tools;

import com.braintribe.model.accessdeployment.smart.meta.conversion.IntegerToString;
import com.braintribe.model.accessdeployment.smart.meta.conversion.LongToString;
import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.query.Operator;

/**
 * 
 */
public class SmartConversionTools {

	public static boolean isEqualityBasedOperator(Operator operator) {
		switch (operator) {
			case contains:
			case in:
			case equal:
			case notEqual:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Some conversions can be delegated when such converted property is used in a conditions. For example, if our "convertedProperty" is integer in
	 * the delegate, but a String in smart, and we use conditions <tt>convertedProperty like '.*2'"</tt> (ends with two), we can delegate this as
	 * <tt>asString(convertedProperty) like '.*2'</tt>.
	 * 
	 * For now, we only consider non-parameter [number]ToString conversions delegate-able, i.e. only {@link IntegerToString} and {@link LongToString}.
	 */
	public static boolean isDelegateableToStringConversion(SmartConversion c) {
		if (c.getInverse())
			return false;

		return c instanceof IntegerToString || c instanceof LongToString;
	}

}
