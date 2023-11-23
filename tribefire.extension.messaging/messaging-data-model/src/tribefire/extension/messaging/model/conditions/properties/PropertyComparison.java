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
package tribefire.extension.messaging.model.conditions.properties;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.*;
import com.braintribe.model.meta.selector.Operator;
import tribefire.extension.messaging.model.conditions.Comparison;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SelectiveInformation("IF '${property}' ${operator} '${value}'")
public interface PropertyComparison extends Comparison, PropertyCondition {

	EntityType<PropertyComparison> T = EntityTypes.T(PropertyComparison.class);

	@Name("Property")
	@Description("The property or property path (foo.bar) used as left side of comparsion. ")
	@Mandatory
	@Priority(1.0d)
	String getProperty();
	void setProperty(String property);

	@Name("Operator")
	@Mandatory
	@Priority(0.9d)
	@Initializer("enum(com.braintribe.model.meta.selector.Operator,equal)")
	Operator getOperator();
	void setOperator(Operator operator);

	@Name("Value")
	@Description("The string representation of the compare value (right side).")
	@Mandatory
	@Priority(0.8d)
	String getValue();
	void setValue(String value);

	@Override
	default boolean matches(GenericEntity entity) {
		Object propertyValue = resolvePropertyValue(entity, getProperty());
		Operator operator = getOperator();
		Object compareValue = getValue();

		return compare(propertyValue, operator, compareValue);
	}

	default boolean compare(Object propertyValue, Operator operator, Object compareValue) {
		return switch (operator) {
			case contains -> Comparison.compareContains(propertyValue, compareValue, operator);
			case in -> Comparison.compareContains(compareValue, propertyValue, operator);
			case equal -> Comparison.compareEquality(propertyValue, compareValue);
			case notEqual -> !Comparison.compareEquality(propertyValue, compareValue);
			case greater -> Comparison.compare(propertyValue, compareValue) > 0;
			case greaterOrEqual -> Comparison.compare(propertyValue, compareValue) >= 0;
			case less -> Comparison.compare(propertyValue, compareValue) < 0;
			case lessOrEqual -> Comparison.compare(propertyValue, compareValue) <= 0;
			case ilike, like -> Comparison.compareLike(propertyValue, compareValue, operator == Operator.ilike);
			default -> throw new IllegalArgumentException("Operator: " + operator + " not supported for PropertyValueComparison.");
		};
	}

	static Object resolvePropertyValue(GenericEntity entity, String propertyPath) {
		if (entity == null) {
			return null;
		}

		if (propertyPath == null) {
			return null;
		}

		TypeValuePair typeValuePair = new TypeValuePair();
		typeValuePair.type = entity.entityType();
		typeValuePair.value = entity;

		TypeValuePair resolvedPair = resolveValue(typeValuePair, propertyPath);
		return resolvedPair.value;
	}

	static TypeValuePair resolveValue(TypeValuePair base, String propertyPath) {
		if (propertyPath != null && propertyPath.length() > 0) {
			int index = propertyPath.indexOf('.');

			String remainingPropertyPath = null;
			String key;

			if (index != -1) {
				key = propertyPath.substring(0, index);
				remainingPropertyPath = propertyPath.substring(index + 1);
			} else
				key = propertyPath;

			GenericModelType type = base.type;
			Object value = base.value;
			TypeValuePair nextBase = new TypeValuePair();

			switch (type.getTypeCode()) {
				case entityType -> {
					EntityType<?> entityType = (EntityType<?>) type;
					Property property = entityType.findProperty(key);
					if (property != null && value instanceof GenericEntity) {

						nextBase.value = property.get((GenericEntity) base.value);
						nextBase.type = property.getType();
					}
				}
				case mapType -> {
					if (handleSizeComparison(key, value, nextBase)) {
						break;
					}
					CollectionType mapType = (CollectionType) type;
					GenericModelType keyType = mapType.getParameterization()[0];
					GenericModelType valueType = mapType.getParameterization()[1];
					Object decodedKey = null;
					try {
						switch (keyType.getTypeCode()) {
							case enumType -> decodedKey = ((EnumType) keyType).getInstance(key);
							case stringType -> decodedKey = key;
							case integerType -> decodedKey = Integer.parseInt(key);
							case longType -> decodedKey = Long.parseLong(key);
							default -> {/*do nothing*/}
						}

						if (decodedKey != null && value instanceof Map<?, ?>) {
							Map<?, ?> map = (Map<?, ?>) value;
							nextBase.value = map.get(decodedKey);
							nextBase.type = valueType;
						}
					} catch (Exception e) {
						// ignore as this is lenient
					}
				}
				case listType -> {
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
				}
				case setType -> handleSizeComparison(key, value, nextBase);
				default -> {/*do nothing*/}
			}

			if (remainingPropertyPath != null && nextBase.type != null && nextBase.value != null) {
				return resolveValue(nextBase, remainingPropertyPath);
			} else
				return nextBase;
		} else
			return base;
	}

	static boolean handleSizeComparison(String key, Object value, TypeValuePair nextBase) {
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

	class TypeValuePair {
		public Object value;
		public GenericModelType type;
	}

}
