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
package com.braintribe.model.processing.query.eval.context;

import java.util.Collection;
import java.util.Map;

import com.braintribe.common.lcd.RegexTools;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.query.Operator;

/**
 * 
 */
public class ConditionEvaluationTools {

	public static int compare(Object left, Object right) {
		if (left == null)
			return right == null ? 0 : -1;

		if (right == null)
			return 1;

		try {
			return ((Comparable<Object>) left).compareTo(right);
		} catch (ClassCastException e) {
			throw new RuntimeQueryEvaluationException("Unsupported left comparison operand: " + left + ". Right operand: " + right);
		}
	}

	public static boolean equal(Object left, Object right) {
		return left == null ? right == null : left.equals(right);
	}

	public static boolean like(String left, String right) {
		return left != null && likeHelper(left, right);
	}

	public static boolean ilike(String left, String right) {
		return left != null && likeHelper(left.toLowerCase(), right.toLowerCase());
	}

	private static boolean likeHelper(String left, String right) {
		return left.matches(convertToRegexPattern(right));
	}

	public static String convertToRegexPattern(String pattern) {
		StringBuilder builder = new StringBuilder();
		StringBuilder tokenBuilder = new StringBuilder();

		int escapeLock = -1;
		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			switch (c) {
				case '*':
					if (escapeLock == i) {
						tokenBuilder.append(c);
					} else {
						appendToken(builder, tokenBuilder);
						builder.append(".*");
					}
					break;
				case '?':
					if (escapeLock == i) {
						tokenBuilder.append(c);
					} else {
						appendToken(builder, tokenBuilder);
						builder.append(".");
					}
					break;
				case '\\':
					if (escapeLock == i) {
						tokenBuilder.append(c);
					} else {
						escapeLock = i + 1;
					}
					break;
				default:
					tokenBuilder.append(c);
			}
		}
		appendToken(builder, tokenBuilder);

		return builder.toString();
	}

	private static void appendToken(StringBuilder builder, StringBuilder tokenBuilder) {
		String token = tokenBuilder.toString();
		builder.append(RegexTools.quote(token));
		tokenBuilder.setLength(0);
	}

	public static boolean contains(Object collection, Object element, Operator operator) {
		if (collection == null)
			return false;

		if (collection instanceof Collection<?>)
			return ((Collection<?>) collection).contains(element);

		if (collection instanceof Map<?, ?>)
			return ((Map<?, ?>) collection).containsValue(element);

		throw new RuntimeQueryEvaluationException("Cannot evaluate " + operator + " operator. Operand is not a collection: " + collection + "["
				+ collection.getClass().getName() + "]");
	}

	public static boolean instanceOf(Object value, String className) {
		GenericModelType valueType = GMF.getTypeReflection().getType(value);
		GenericModelType classType = GMF.getTypeReflection().getType(className);

		return classType.isAssignableFrom(valueType);
	}

}
