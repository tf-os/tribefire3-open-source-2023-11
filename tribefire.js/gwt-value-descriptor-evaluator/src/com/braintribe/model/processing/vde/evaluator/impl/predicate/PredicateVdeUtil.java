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
package com.braintribe.model.processing.vde.evaluator.impl.predicate;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;

/**
 * Util class that assists with string evaluation for predicate experts
 * 
 */
public class PredicateVdeUtil {

	
	/**
	 * Compare two objects, where both implement comparable
	 * 
	 * @param left
	 *            first object
	 * @param right
	 *            second object
	 */
	public static int compare(Object left, Object right) throws VdeRuntimeException {
		if (left == null) {
			return right == null ? 0 : -1;
		}

		if (right == null) {
			return 1;
		}

		try {
			return ((Comparable<Object>) left).compareTo(right);
		} catch (ClassCastException e) {
			throw new VdeRuntimeException("Unsupported left comparison operand: " + left + ". Right operand: "
					+ right, e);
		}
	}
	
	/**
	 * Identifies the GenericModelTypes associated with the value and className, then invokes {@link #instanceOf(GenericModelType,GenericModelType,boolean)}
	 */
	public static boolean instanceOf(Object value, Object clazz, boolean assignable) {
		GenericModelType valueType = GMF.getTypeReflection().getType(value);
		GenericModelType classType = GMF.getTypeReflection().getType(clazz);
		return instanceOf(valueType, classType, assignable);
	}

	/**
	 * Identifies the GenericModelTypes associated with the value and className, then invokes {@link #instanceOf(GenericModelType,GenericModelType,boolean)}
	 */
	public static boolean instanceOf(Object value, String className, boolean assignable) {
		GenericModelType valueType = GMF.getTypeReflection().getType(value);
		GenericModelType classType = GMF.getTypeReflection().getType(className);
		return instanceOf(valueType, classType, assignable);

	}

	/**
	 * Identifies the GenericModelType associated with the className and then invokes {@link #instanceOf(GenericModelType,GenericModelType,boolean)}
	 */
	public static boolean instanceOf(GenericModelType valueType, String className, boolean assignable) {
		GenericModelType classType = GMF.getTypeReflection().getType(className);
		return instanceOf(valueType, classType, assignable);
	}

	/**
	 * Check if one GenericModelType is an instance of the other. If the assignable boolean is set to true
	 * {@link GenericModelType#isAssignableFrom(GenericModelType)} is used, other wise "==" is invoked.
	 */
	public static boolean instanceOf(GenericModelType valueType, GenericModelType classType, boolean assignable) {
		if (assignable) {
			return classType.isAssignableFrom(valueType);
		} else {
			return classType == valueType;
		}
	}
	
	// TODO this code was copied from the
	// com.braintribe.model.processing.query.eval.context.ConditionEvaluationTools
	// it needs to only exist once in the whole framework
	// Also there is part of this code pertaining to comparison in the MPC
	// somewhere

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
		builder.append(quote(token));
		tokenBuilder.setLength(0);
	}

	/**
	 * copied from java.util.regex.Patter.quote(String)
	 */
	private static String quote(String s) {
		int slashEIndex = s.indexOf("\\E");
		if (slashEIndex == -1)
			return "\\Q" + s + "\\E";

		StringBuilder sb = new StringBuilder(s.length() * 2);
		sb.append("\\Q");
		slashEIndex = 0;
		int current = 0;
		while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
			sb.append(s.substring(current, slashEIndex));
			current = slashEIndex + 2;
			sb.append("\\E\\\\E\\Q");
		}
		sb.append(s.substring(current, s.length()));
		sb.append("\\E");
		return sb.toString();
	}
}
