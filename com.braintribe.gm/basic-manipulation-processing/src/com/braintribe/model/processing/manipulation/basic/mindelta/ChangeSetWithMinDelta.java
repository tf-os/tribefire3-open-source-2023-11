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

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;

/**
 * @author peter.gazdik
 */
public class ChangeSetWithMinDelta {

	public static void apply(GenericEntity entity, Property property, Set<?> currentSet, Set<?> newSet) {
		/* if new size is less than half of old size, then it's definitely better to say new values rather than say what should be removed */
		if (2 * newSet.size() <= currentSet.size()) {
			if (currentSet.isEmpty()) {
				// both sets are empty
				return;
			}
			
			property.set(entity, newSet);
			return;
		}

		Set<Object> common = newSet();
		Set<Object> delta = newSet();

		/* it's worth to iterate over the smaller set, cause we might find out we want to do CVM instead; in other we still have to iterate
		 * over the the other set too */
		boolean currentIsSmaller = currentSet.size() < newSet.size();
		Set<Object> smaller = (Set<Object>) (currentIsSmaller ? currentSet : newSet);
		Set<Object> bigger = (Set<Object>) (currentIsSmaller ? newSet : currentSet);

		for (Object o: smaller) {
			if (bigger.contains(o)) {
				common.add(o);
			} else {
				delta.add(o);
			}
		}

		int changeValueSize = newSet.size();
		int addAndRemoveSize = currentSet.size() + newSet.size() - 2 * common.size();

		if (addAndRemoveSize == 0) {
			// in other words the collections are equal
			return;
		}

		if (changeValueSize <= addAndRemoveSize) {
			property.set(entity, newSet);
			return;
		}

		if (currentIsSmaller) {
			// current is smaller -> delta are those in current and are not in new, thus have to be removed
			if (!delta.isEmpty()) {
				// we need the check otherwise a manipulation would be tracked
				currentSet.removeAll(delta);
			}
			// we add all new values, those that are already in will not be part of manipulation
			((Set<Object>) currentSet).addAll(newSet);

		} else {
			// we remove everything that is not common
			currentSet.retainAll(common);
			// current is bigger -> delta are those in new that are not in current, thus have to be added
			((Set<Object>) currentSet).addAll(delta);
		}
	}

}
