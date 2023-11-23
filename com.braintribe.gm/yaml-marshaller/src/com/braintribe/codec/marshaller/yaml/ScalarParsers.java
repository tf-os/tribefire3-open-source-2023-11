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
package com.braintribe.codec.marshaller.yaml;

import java.util.function.BiFunction;

import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.generic.reflection.TypeCode;

public class ScalarParsers {

	/**
	 * This method parses a yaml scalar value into the respective java type in the fastest way possible, optimized for
	 * the canonical forms of each yaml data type. So the user should be encouraged to only use the canonical forms:
	 * plain integers (17) instead of octals (0o21), booleans in lower case etc...
	 */
	public static Object parse(ScalarType inferredType, String valueAsString) {
		TypeCode typeCode = inferredType.getTypeCode();
		switch (typeCode) {
			case booleanType:
				if (valueAsString.equals("true"))
					return true;
				if (valueAsString.equals("false"))
					return false;

				if (valueAsString.equals("True") || valueAsString.equals("TRUE"))
					return true;
				if (valueAsString.equals("False") || valueAsString.equals("FALSE"))
					return false;
				throw new IllegalStateException("Illegal boolean value. Only true,false,True,False,TRUE,FALSE are allowed.");
			case decimalType:
			case enumType:
				return inferredType.instanceFromString(valueAsString);
			case integerType:
				return parseInteger(inferredType, valueAsString, Integer::parseInt);
			case longType:
				return parseInteger(inferredType, valueAsString, Long::parseLong);
			case floatType:
				return parseDecimalNumber(inferredType, valueAsString, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN);
			case doubleType:
				return parseDecimalNumber(inferredType, valueAsString, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN);
			default:
				throw new RuntimeException("Unexpected type code " + typeCode);
		}
	}

	private static Object parseInteger(ScalarType inferredType, String valueAsString, BiFunction<String, Integer, Object> parseWithBase) {
		if (valueAsString.length() > 2) {
			if (valueAsString.charAt(0) == '0') {
				char secondChar = valueAsString.charAt(1);
				if (secondChar == 'x') {
					return parseWithBase.apply(valueAsString.substring(2), 16);
				} else if (secondChar == 'o') {
					return parseWithBase.apply(valueAsString.substring(2), 8);
				}
			}
		}
		return inferredType.instanceFromString(valueAsString);
	}

	private static Object parseDecimalNumber(ScalarType inferredType, String valueAsString, Object inf, Object negInf, Object nan) {
		if (valueAsString.length() == 4 || valueAsString.length() == 5) { // .NaN, -.Inf
			String specialNumberIdentifier;
			char initialChar = valueAsString.charAt(0);
			boolean negative = initialChar == '-';

			if (negative || initialChar == '+') {
				initialChar = valueAsString.charAt(1);
				specialNumberIdentifier = valueAsString.substring(2);
			} else {
				specialNumberIdentifier = valueAsString.substring(1);
			}
			if (initialChar == '.') { // we start either with '.' or '-.' -> this is already non-canonical
				if (specialNumberIdentifier.matches("inf|Inf|INF")) {
					if (negative) {
						return negInf;
					}
					return inf;
				}
				if (specialNumberIdentifier.matches("nan|NaN|NAN")) {
					if (!negative) {
						return nan;
					}
				}
			}
		}
		return inferredType.instanceFromString(valueAsString);
	}

}
