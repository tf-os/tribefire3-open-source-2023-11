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
package tribefire.extension.spreadsheet.processing.importing.common;

import java.math.BigDecimal;
import java.util.StringTokenizer;
import java.util.function.Function;

import com.braintribe.model.generic.reflection.SimpleType;

public interface Conversions {
	static Boolean stringToBoolean(String s) {
		switch (s.toLowerCase()) {
		case "true":
		case "t":
		case "yes":
		case "y":
		case "on":
		case "active":
		case "enabled":
		case "1":
			return true;
		default:
			return false;
		}
	}
	
	static Function<String, Number> stringToNumberFunction(String decimalSeparator, String digitGroupingSymbol, SimpleType targetType) {
		return s -> {
			StringBuilder builder = new StringBuilder(s.length());
			StringTokenizer tokenizer = new StringTokenizer(s, decimalSeparator + digitGroupingSymbol, true);
			
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				
				if (token.equals(digitGroupingSymbol)) {
					// noop
				}
				else if (token.equals(decimalSeparator)) {
					builder.append('.');
				}
				else {
					builder.append(token);
				}
			}
			
			String normalized = builder.toString();
			
			switch (targetType.getTypeCode()) {
				case floatType:
					return Float.parseFloat(normalized);
				case doubleType:
					return Double.parseDouble(normalized);
				case integerType: 
					return stringToIntegerNumber(normalized).intValue();
				case longType:
					return stringToIntegerNumber(normalized).longValue();
				case decimalType:
					return new BigDecimal(s);
				default:
					throw new IllegalStateException("Target type [" + targetType + "] is not a number type");
			}
			
		};
	}
	
	static Integer stringToInteger(String s) {
		return stringToIntegerNumber(s).intValue();
	}
	
	static Long stringToLong(String s) {
		return stringToIntegerNumber(s).longValue();
	}
	
	static Double stringToIntegerNumber(String s) {
		double number = Double.parseDouble(s);
		
		if (Math.floor(number) == number)
			return number;
		
		throw new IllegalStateException("String [" + s + "] is not an integer.");
	}
}
