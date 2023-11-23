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
package com.braintribe.model.processing.mpc.evaluator.utils;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.api.IListItemModelPathElement;
import com.braintribe.model.generic.path.api.IMapEntryRelatedModelPathElement;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.mpc.value.MpcElementAxis;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;

/**
 * A utility class used by several MPC evaluators
 * 
 */
public class MpcEvaluationTools {

	/**
	 * Resolve a ModelPathElement against an MpcElementAxis
	 */
	public static Object resolve(MpcElementAxis axis, IModelPathElement element) throws MpcEvaluatorRuntimeException {
		// init result
		Object result = null;

		// switch according to axis
		switch (axis) {
		// if depth return element's depth
			case depth:
				result = element.getDepth();
				break;
			// if listIndex
			case listIndex:

				// check the element is of the correct type
				if (element instanceof IListItemModelPathElement) {

					// cast the to the list item interface
					IListItemModelPathElement listElement = (IListItemModelPathElement) element;
					result = listElement.getIndex();
				} else { // if wrong instance, throw an exception
					throw new MpcEvaluatorRuntimeException("IModelPathElement " + element
							+ " is NOT an instance of IListItemModelPathElement");
				}

				break;
			// if map related items
			case mapKey:
			case mapValue:
				// check the element is of the correct type
				if (element instanceof IMapEntryRelatedModelPathElement) {

					// cast the to the map entry interface
					IMapEntryRelatedModelPathElement mapElement = (IMapEntryRelatedModelPathElement) element;
					// return result according to the switch case
					result = (axis == MpcElementAxis.mapKey) ? mapElement.getKey() : mapElement.getMapValue();
				} else { // if wrong instance, throw an exception
					throw new MpcEvaluatorRuntimeException("IModelPathElement " + element
							+ " is NOT an instance of IMapEntryRelatedModelPathElement");
				}

				break;
			// if value
			case value:
				// simply return the value itself
				result = element.getValue();

				break;
			default:
				throw new MpcEvaluatorRuntimeException("Unsupported MpcElementAxis: " + axis);

		}

		return result;

	}

	/**
	 * Get type of object using GMF type reflection
	 */
	public static GenericModelType getObjectType(Object value) {
		return GMF.getTypeReflection().getType(value);
	}

	/**
	 * Get type of ValueDescriptor using GMF type reflection
	 */
	public static GenericModelType getValueDescriptorType() {
		return GMF.getTypeReflection().getType(ValueDescriptor.class);
	}

	// TODO verify the second parameter
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

	/**
	 * Compare two objects, where both implement comparable
	 * 
	 * @param left
	 *            first object
	 * @param right
	 *            second object
	 */
	public static int compare(Object left, Object right) throws MpcEvaluatorRuntimeException {
		if (left == null) {
			return right == null ? 0 : -1;
		}

		if (right == null) {
			return 1;
		}

		try {
			return ((Comparable<Object>) left).compareTo(right);
		} catch (ClassCastException e) {
			throw new MpcEvaluatorRuntimeException("Unsupported left comparison operand: " + left + ". Right operand: "
					+ right, e);
		}
	}

	/**
	 * Check objects equality
	 * 
	 * @return true if both are equal, false otherwise
	 */
	public static boolean equal(Object left, Object right) {
		if (left == null) {
			return right == null;
		}

		return left.equals(right);
	}

	// TODO find a way that these regex methods (convertToRegexPattern, appendToken and quote) are not repeated twice in
	// the framework (here and QueryPlanEvalutor) if possible
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
