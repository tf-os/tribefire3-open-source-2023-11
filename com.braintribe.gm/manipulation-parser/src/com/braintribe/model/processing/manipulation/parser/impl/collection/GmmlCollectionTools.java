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
package com.braintribe.model.processing.manipulation.parser.impl.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.processing.manipulation.parser.api.GmmlConstants;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;

/**
 * @author peter.gazdik
 */
public class GmmlCollectionTools implements GmmlConstants {

	public static Collection<Object> convertToCollection(Object value) {
		if (value instanceof Collection)
			return (Collection<Object>) value;
		else if (value instanceof Map)
			// TODO really values? cause opposite direction uses keys
			return ((Map<Object, Object>) value).values();
		else
			return Collections.singleton(value);
	}

	public static void addToCollection(Collection<Object> collection, LinearCollectionType type, Object value, GmmlManipulatorErrorHandler errorHandler) {
		if (value != missing) {
			if (type.getCollectionElementType().isValueAssignable(value)) {
				collection.add(value);
				return;

			} else {
				errorHandler.wrongValueTypeToAddToCollection(value, type);
			}
		}

		// value is missing OR incompatible type and we are lenient
		if (type.getCollectionKind() == CollectionKind.list)
			collection.add(null);
	}

	public static Map<Object, Object> convertToMap(Object value) {
		if (value instanceof Map)
			return (Map<Object, Object>) value;
		else if (value instanceof Collection)
			return ((Collection<Object>) value).stream().collect(Collectors.toMap(Function.identity(), x -> null));
		else
			return Collections.singletonMap(value, value);
	}

	public static void putToMap(Map<Object, Object> map, MapType type, Object key, Object value, GmmlManipulatorErrorHandler errorHandler) {
		if (key == missing || value == missing)
			return;

		boolean keyOk = type.getKeyType().isValueAssignable(key);
		boolean valueOk = type.getValueType().isValueAssignable(value);

		if (!keyOk || !valueOk)
			errorHandler.wrongTypeForMapPut(key, keyOk, value, valueOk, type);

		if (!keyOk)
			return;

		if (!valueOk) {
			map.put(key, null);
			return;
		}

		map.put(key, value);
	}

}
