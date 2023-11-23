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

import static com.braintribe.model.processing.manipulation.parser.impl.collection.GmmlCollectionTools.putToMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.processing.manipulation.parser.api.CollectionDeltaManipulator;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;
import com.braintribe.model.processing.manipulator.expert.basic.collection.ListManipulator;
import com.braintribe.model.processing.manipulator.expert.basic.collection.ListManipulator.Pair;
import com.braintribe.model.processing.manipulator.expert.basic.collection.ListManipulator.Range;
import com.braintribe.model.processing.manipulator.expert.basic.collection.ListManipulator.RangeBuilder;

/**
 * @author peter.gazdik
 */
public class MapDeltaManipulator implements CollectionDeltaManipulator {

	private final Map<Object, Object> parsedMapLiteral;
	private final GmmlManipulatorErrorHandler errorHandler;

	public MapDeltaManipulator(Map<Object, Object> parsedMapLiteral, GmmlManipulatorErrorHandler errorHandler) {
		this.parsedMapLiteral = parsedMapLiteral;
		this.errorHandler = errorHandler;
	}

	@Override
	public void addToList(List<Object> list, ListType type) {
		List<Range> ranges = prepareRange(parsedMapLiteral, type.getCollectionElementType());
		for (Range range : ranges)
			range.addElementsTo(list);
	}

	private List<Range> prepareRange(Map<Object, Object> values, GenericModelType valueType) {
		Pair[] pairs = new Pair[values.size()];
		int i = 0;
		int lastIndex = parsedMapLiteral.size() + values.size();
		for (Map.Entry<Object, Object> entry : values.entrySet()) {
			Object index = entry.getKey();
			Object value = entry.getValue();

			// We keep the missing information in the map so it is replaced with null for lists and ignored for sets/maps
			if (value == missing)
				value = null;
			
			boolean indexOk = index != null && index.getClass() == Integer.class;
			boolean valueOk = valueType.isValueAssignable(value);

			if (!indexOk || !valueOk)
				errorHandler.wrongTypeForListAdd(index, indexOk, value, valueOk, valueType);

			if (!valueOk)
				continue;

			if (indexOk)
				pairs[i++] = new Pair((Integer) index, value);
			else
				pairs[i++] = new Pair(lastIndex, value);

			lastIndex++;
		}

		RangeBuilder rangeBuilder = new RangeBuilder(pairs);
		return rangeBuilder.getRanges();
	}

	@Override
	public void addToSet(Set<Object> set, SetType type) {
		// remove missing
		parsedMapLiteral.forEach((key, value) -> addToSet(set, type, value));
	}

	private void addToSet(Set<Object> set, SetType type, Object value) {
		if (value != missing) {
			if (type.getCollectionElementType().isValueAssignable(value))
				set.add(value);
			else
				errorHandler.wrongValueTypeToAddToCollection(value, type);
		}
	}

	@Override
	public void addToMap(Map<Object, Object> map, MapType type) {
		parsedMapLiteral.forEach((key, value) -> putToMap(map, type, key, value, errorHandler));
	}

	@Override
	public void removeFromList(List<Object> list, ListType type) {
		ListManipulator.INSTANCE.remove(list, (Map<Integer, Object>) (Map<?, ?>) parsedMapLiteral);
	}

	@Override
	public void removeFromSet(Set<Object> set, SetType type) {
		set.removeAll(parsedMapLiteral.keySet());
	}

	@Override
	public void removeFromMap(Map<Object, Object> map, MapType type) {
		map.keySet().removeAll(parsedMapLiteral.keySet());
	}

}
