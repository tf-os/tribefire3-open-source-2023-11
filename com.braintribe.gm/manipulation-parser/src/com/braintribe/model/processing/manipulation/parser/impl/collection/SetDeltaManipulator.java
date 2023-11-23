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

import static com.braintribe.model.processing.manipulation.parser.impl.collection.GmmlCollectionTools.addToCollection;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.processing.manipulation.parser.api.CollectionDeltaManipulator;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;

/**
 * @author peter.gazdik
 */
public class SetDeltaManipulator implements CollectionDeltaManipulator {

	private final Set<Object> values;
	private final GmmlManipulatorErrorHandler errorHandler;

	public SetDeltaManipulator(Set<Object> values, GmmlManipulatorErrorHandler errorHandler) {
		this.values = values;
		this.errorHandler = errorHandler;
	}

	@Override
	public void addToList(List<Object> list, ListType type) {
		values.forEach(value -> addToCollection(list, type, value, errorHandler));
	}

	@Override
	public void addToSet(Set<Object> set, SetType type) {
		values.forEach(value -> addToCollection(set, type, value, errorHandler));
	}

	@Override
	public void addToMap(Map<Object, Object> map, MapType type) {
		values.forEach(value -> addToMap(map, type, value));
	}

	private void addToMap(Map<Object, Object> map, MapType type, Object value) {
		if (value != missing) {
			if (type.getKeyType().isValueAssignable(value))
				map.put(value, null);
			else
				errorHandler.wrongValueTypeToAddToCollection(value, type);
		}
	}

	@Override
	public void removeFromList(List<Object> list, ListType type) {
		list.removeAll(values);
	}

	@Override
	public void removeFromSet(Set<Object> set, SetType type) {
		set.removeAll(values);
	}

	@Override
	public void removeFromMap(Map<Object, Object> map, MapType type) {
		map.keySet().removeAll(values);
	}

}
