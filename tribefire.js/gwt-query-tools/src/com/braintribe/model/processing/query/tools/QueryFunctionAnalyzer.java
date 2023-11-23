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
package com.braintribe.model.processing.query.tools;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.functions.QueryFunction;

/**
 * 
 * @author peter.gazdik
 */
public class QueryFunctionAnalyzer {

	public static Set<Operand> findOperands(QueryFunction queryFunction) {
		Set<Operand> result = newSet();

		EntityType<?> et = queryFunction.entityType();

		for (Property p: et.getProperties()) {
			GenericModelType type = p.getType();

			switch (type.getTypeCode()) {
				case objectType:
				case entityType:
					handleSingleReferenceProperty(queryFunction, p, result);
					break;

				case listType:
				case setType:
					handleLinearCollectionProperty(queryFunction, p, result);
					break;

				case mapType:
					handleMapProperty(queryFunction, p, result);
					break;

				default:
					break;
			}
		}

		return result;
	}

	private static void handleSingleReferenceProperty(QueryFunction queryFunction, Property p, Set<Operand> result) {
		addOperand(result, p.get(queryFunction));
	}

	private static void handleLinearCollectionProperty(QueryFunction queryFunction, Property p, Set<Operand> result) {
		CollectionType propertyType = (CollectionType) p.getType();
		GenericModelType elementType = propertyType.getCollectionElementType();

		if (couldBeOperand(elementType))
			addOperands(result, (Collection<?>) p.get(queryFunction));
	}

	private static void handleMapProperty(QueryFunction queryFunction, Property p, Set<Operand> result) {
		CollectionType propertyType = (CollectionType) p.getType();

		Map<?, ?> map = (Map<?, ?>) p.get(queryFunction);
		if (map == null)
			return;

		GenericModelType keyType = propertyType.getParameterization()[0];
		if (couldBeOperand(keyType))
			addOperands(result, map.keySet());

		GenericModelType valueType = propertyType.getCollectionElementType();
		if (couldBeOperand(valueType))
			addOperands(result, map.values());
	}

	private static void addOperands(Set<Operand> result, Collection<?> values) {
		if (values != null)
			for (Object value: values)
				addOperand(result, value);
	}

	private static void addOperand(Set<Operand> result, Object value) {
		if (value instanceof Operand)
			result.add((Operand) value);
	}

	private static boolean couldBeOperand(GenericModelType type) {
		return type instanceof BaseType || (type instanceof EntityType && isOperandSubType((EntityType<?>) type));
	}

	private static boolean isOperandSubType(EntityType<?> type) {
		return Operand.T.isAssignableFrom(type);
	}

}
