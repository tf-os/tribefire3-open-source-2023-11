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
package com.braintribe.model.processing.manipulation.basic.mindelta;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.utils.lcd.CommonTools;

/**
 * @author peter.gazdik
 */
public class ChangeMapWithMinDelta {

	private static final Object DEFAULT = new Object();

	public static void apply(GenericEntity entity, Property property, Map<?, ?> currentMap, Map<?, ?> newMap) {
		/* if new size is less than half of old size, then it's definitely better to say new values rather than say what should be removed */
		if (2 * newMap.size() <= currentMap.size()) {
			if (currentMap.isEmpty()) {
				// both sets are empty
				return;
			}

			property.set(entity, newMap);
			return;
		}

		Set<Object> same = newSet();
		Set<Object> removed = newSet();
		Map<Object, Object> changed = newMap();

		Map<Object, Object> currentCasted = (Map<Object, Object>) currentMap;
		Map<Object, Object> newCasted = (Map<Object, Object>) newMap;

		for (Entry<Object, Object> entry: currentCasted.entrySet()) {
			Object key = entry.getKey();
			Object currentValue = entry.getValue();
			Object newValue = newCasted.getOrDefault(key, DEFAULT);

			if (newValue == DEFAULT) {
				removed.add(key);
				continue;
			}

			if (CommonTools.equalsOrBothNull(currentValue, newValue)) {
				same.add(key);

			} else {
				changed.put(key, newValue);
			}
		}

		int changeValueSize = newMap.size();
		int addAndRemoveSize = currentMap.size() + newMap.size() - 2 * same.size() - changed.size();

		if (addAndRemoveSize == 0) {
			// in other words the collections are equal
			return;
		}

		if (changeValueSize <= addAndRemoveSize) {
			property.set(entity, newMap);
			return;
		}

		for (Entry<Object, Object> entry: newCasted.entrySet()) {
			Object key = entry.getKey();
			if (!same.contains(key)) {
				changed.putIfAbsent(key, entry.getValue());
			}
		}

		currentCasted.keySet().removeAll(removed);
		currentCasted.putAll(changed);
	}

}
