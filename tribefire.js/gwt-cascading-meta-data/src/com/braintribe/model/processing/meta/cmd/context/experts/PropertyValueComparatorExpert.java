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
package com.braintribe.model.processing.meta.cmd.context.experts;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.braintribe.common.lcd.RegexTools;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.selector.Operator;
import com.braintribe.model.meta.selector.PropertyValueComparator;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.EntityAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.EntityTypeAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.GmPropertyAspect;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataTools;

@SuppressWarnings("unusable-by-js")
public class PropertyValueComparatorExpert implements CmdSelectorExpert<PropertyValueComparator> {

	@Override
	public Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(PropertyValueComparator selector) throws Exception {
		return MetaDataTools.aspects(EntityAspect.class, EntityTypeAspect.class);
	}

	@Override
	public boolean matches(PropertyValueComparator selector, SelectorContext context) throws Exception {
		Object propertyValue = resolvePropertyValue(selector, context);
		Operator operator = selector.getOperator();
		Object compareValue = selector.getValue();

		return compare(propertyValue, operator, compareValue);
	}

	private boolean compare(Object propertyValue, Operator operator, Object compareValue) {
		switch (operator) {
			case contains:
				return compareContains(propertyValue, compareValue, operator);
			case in:
				return compareContains(compareValue, propertyValue, operator);
			case equal:
				return compareEquality(propertyValue, compareValue);
			case notEqual:
				return !compareEquality(propertyValue, compareValue);
			case greater:
				return compare(propertyValue, compareValue) > 0;
			case greaterOrEqual:
				return compare(propertyValue, compareValue) >= 0;
			case less:
				return compare(propertyValue, compareValue) < 0;
			case lessOrEqual:
				return compare(propertyValue, compareValue) <= 0;
			case ilike:
			case like:
				return compareLike(propertyValue, compareValue, operator == Operator.ilike);
			default:
				throw new IllegalArgumentException("Operator: " + operator + " not supported for PropertyValueComparison.");
		}
	}

	private boolean compareLike(Object left, Object right, boolean caseInsensitvie) {
		if (left instanceof String && right instanceof String)
			return caseInsensitvie ? ilike((String) left, (String) right) : like((String) left, (String) right);
		else
			return false;
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

	private static int compare(Object left, Object right) {
		if (left == null) {
			return right == null ? 0 : -1;
		}

		if (right == null) {
			return 1;
		}

		try {
			return ((Comparable<Object>) left).compareTo(right);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Unsupported left comparison operand: " + left + ". Right operand: " + right);
		}
	}

	private boolean compareEquality(Object a, Object b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}

	private boolean compareContains(Object collection, Object element, Operator operator) {
		if (collection == null) {
			return false;
		}

		if (collection instanceof Collection<?>) {
			return ((Collection<?>) collection).contains(element);

		} else if (collection instanceof Map<?, ?>) {
			return ((Map<?, ?>) collection).containsValue(element);

		} else {
			throw new IllegalArgumentException("Cannot evaluate " + operator + " operator. Operand is not a collection: " + collection + "["
					+ collection.getClass().getName() + "]");
		}
	}

	protected Object resolvePropertyValue(PropertyValueComparator selector, SelectorContext context) throws CascadingMetaDataException {
		GenericEntity entity = context.get(EntityAspect.class);
		EntityType<?> et = context.get(EntityTypeAspect.class);

		if (entity == null) {
			return null;
		}

		String propertyPath = selector.getPropertyPath();
		if (propertyPath == null) {
			// Check if we have a current context property to inspect.
			GmProperty contextProperty = context.get(GmPropertyAspect.class);
			if (contextProperty == null) {
				return null;
			}
			propertyPath = contextProperty.getName();
		}

		TypeValuePair typeValuePair = new TypeValuePair();
		typeValuePair.type = et;
		typeValuePair.value = entity;

		TypeValuePair resolvedPair = resolveValue(typeValuePair, propertyPath);
		return resolvedPair.value;
	}

	private TypeValuePair resolveValue(TypeValuePair base, String propertyPath) {
		if (propertyPath != null && propertyPath.length() > 0) {
			int index = propertyPath.indexOf('.');

			String remainingPropertyPath = null;
			String key = null;

			if (index != -1) {
				key = propertyPath.substring(0, index);
				remainingPropertyPath = propertyPath.substring(index + 1);
			} else
				key = propertyPath;

			GenericModelType type = base.type;
			Object value = base.value;
			TypeValuePair nextBase = new TypeValuePair();

			switch (type.getTypeCode()) {
				case entityType:
					EntityType<?> entityType = (EntityType<?>) type;
					Property property = entityType.findProperty(key);

					if (property != null && value instanceof GenericEntity) {

						nextBase.value = property.get((GenericEntity) base.value);
						nextBase.type = property.getType();
					}
					break;
				case mapType:
					if (handleSizeComparison(key, value, nextBase)) {
						break;
					}

					CollectionType mapType = (CollectionType) type;

					GenericModelType keyType = mapType.getParameterization()[0];
					GenericModelType valueType = mapType.getParameterization()[1];
					Object decodedKey = null;

					try {
						switch (keyType.getTypeCode()) {
							case enumType:
								decodedKey = ((EnumType) keyType).getInstance(key);
								break;
							case stringType:
								decodedKey = key;
								break;
							case integerType:
								decodedKey = Integer.parseInt(key);
								break;
							case longType:
								decodedKey = Long.parseLong(key);
								break;
							default:
								break;
						}

						if (decodedKey != null && value instanceof Map<?, ?>) {
							Map<?, ?> map = (Map<?, ?>) value;
							nextBase.value = map.get(decodedKey);
							nextBase.type = valueType;
						}
					} catch (Exception e) {
						// ignore as this is lenient
					}

					break;

				case listType:
					if (handleSizeComparison(key, value, nextBase)) {
						break;
					}
					CollectionType listType = (CollectionType) type;
					try {
						if (value instanceof List<?>) {
							List<?> list = (List<?>) value;
							int listIndex = Integer.parseInt(key);

							if (listIndex < list.size()) {
								nextBase.value = list.get(listIndex);
								nextBase.type = listType.getCollectionElementType();
							}
						}
					} catch (NumberFormatException e) {
						// ignore as this is lenient
					}

					break;

				case setType:
					handleSizeComparison(key, value, nextBase);
					break;
				default:
					break;
			}

			if (remainingPropertyPath != null && nextBase.type != null && nextBase.value != null) {
				return resolveValue(nextBase, remainingPropertyPath);
			} else
				return nextBase;
		} else
			return base;
	}

	private boolean handleSizeComparison(String key, Object value, TypeValuePair nextBase) {
		if ("size".equals(key)) {
			if (value instanceof Collection<?>) {
				nextBase.value = ((Collection<?>) value).size();
			} else if (value instanceof Map<?, ?>) {
				nextBase.value = ((Map<?,?>) value).size();
			} else {
				throw new IllegalArgumentException("Unsupported collection value: "+value+" provided for size comparison.");
			}
			nextBase.type = SimpleType.TYPE_INTEGER;
			return true;
		}
		return false;
	}

	protected static class TypeValuePair {
		public Object value;
		public GenericModelType type;
	}
}
