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
package com.braintribe.model.processing.manipulation.basic.normalization;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.addManipulation;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.changeValue;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.removeManipulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.value.EntityReference;

/**
 * Tracks manipulations for property of type {@link List} and merges all the manipulations into either one {@link ChangeValueManipulation}
 * or up to two manipulations {@link RemoveManipulation}, {@link AddManipulation} (in this order).
 */
class ListTracker extends CollectionTracker {

	private final List<Entry> removes = new ArrayList<Entry>();
	private final List<Entry> adds = new ArrayList<Entry>();

	private List<Object> list = null;

	private static class Entry {
		public Entry(int index, Object value) {
			this.index = index;
			this.value = value;
		}

		public int index;
		public Object value;
	}

	public ListTracker(Owner owner, String propertySignature) {
		super(owner, propertySignature);
	}

	@Override
	public void onClearCollection() {
		setValueDirectly(new ArrayList<Object>());
	}

	@Override
	public void onChangeValue(ChangeValueManipulation m) {
		setValueDirectly((List<?>) m.getNewValue());
	}

	private void setValueDirectly(List<?> newValue) {
		list = newValue != null ? new ArrayList<Object>(newValue) : new ArrayList<Object>();
	}

	@Override
	public void onBulkInsert(AddManipulation m) {
		Map<Object, Object> itemsToAdd = m.getItemsToAdd();

		List<Integer> keys = new ArrayList<Integer>((Set<Integer>) (Set<?>) itemsToAdd.keySet());
		Collections.sort(keys);

		for (Integer key : keys) {
			Object value = itemsToAdd.get(key);
			insert(key, value);
		}
	}

	@Override
	public void onBulkRemove(RemoveManipulation m) {
		Map<Integer, Object> items = cast(m.getItemsToRemove());
		NavigableMap<Integer, Object> itemsToRemove = new TreeMap<Integer, Object>(items);

		for (Map.Entry<Integer, Object> e : itemsToRemove.descendingMap().entrySet()) {
			remove(e.getKey(), e.getValue());
		}
	}

	private void insert(int index, Object value) {
		if (list != null) {
			list.add(index, value);
			return;
		}

		// first count new index - when this will be part of the finalBulkInsert
		int indexInArray = countIndexInArray(adds, index);
		incIndices(indexInArray, adds); // all elements inserted after this one must have their index increased

		// add new insert entry
		Entry e = new Entry(index, value);
		adds.add(indexInArray, e);
	}

	private void remove(int index, Object value) {
		if (list != null) {
			list.remove(index);
			return;
		}

		int indexInArray = countIndexInArray(adds, index);
		if (valueOnPositionHasIndex(adds, indexInArray, index)) {
			adds.remove(indexInArray);
			decIndices(indexInArray, adds);
			return;
		}
		// all inserts after this position must have their index decreased
		decIndices(indexInArray, adds);

		// first let's count what the index would be if there were no inserts before (by subtracting inserts on
		// positions lower than our own)
		int preInsertIndex = index - indexInArray;

		int indexToRemove = countRemoveIndex(preInsertIndex);
		indexInArray = indexToRemove - preInsertIndex;

		Entry e = new Entry(indexToRemove, value);
		removes.add(indexInArray, e);
	}

	private boolean valueOnPositionHasIndex(List<Entry> entries, int indexInArray, int index) {
		if (indexInArray < entries.size()) {
			return entries.get(indexInArray).index == index;
		}
		return false;

	}

	/**
	 * For a given index <tt>i</tt> (<code>startIndex</code>), we are looking for smallest <tt>k</tt>such that index of <tt>(i+k)</tt> in
	 * <code>removes</code> is <tt>k</tt>. We then return the value <tt>i+k</tt>. (So the index in the <code>removes</code> array (
	 * <tt>k</tt>) may be counted as the result of this call <tt>(i+k)</tt> minus the argument of this call <tt>i</tt>). For the definition
	 * of index, see {@link #countIndexInArray(List, int)}.
	 * <p>
	 * How does this make sense? For example imagine we have a remove with index <tt>1</tt>, but our accumulated <code>removes</code>
	 * already contains values <tt>0, 1, 3, 6</tt>. This means, if we want execute this remove as part of the bulk, we actually want to
	 * remove an element on the position <tt>4</tt>. How do we compute this? Well, since we have at least two elements we want to remove on
	 * positions smaller than our current position (those 0 and 1 must definitely have been removed before this one), we increase our index
	 * by <tt>2</tt> to <tt>3</tt>. But for this new value we now see that there is another position that must be deleted before, namely
	 * <tt>3</tt>, so we increase the value once more. Now we get a value <tt>4</tt>, and we see, that this is the value we are looking for.
	 * 
	 * In other words, we find index of our position in the <code>removes</code> array, and we increase our initial value by that. We
	 * iterate this process until we find such number <tt>k</tt>, that the index of <tt>i+k</tt> is <tt>k</tt>, in which case we may stop.
	 * 
	 * NOTE that this implementation is a little simpler (not using index but simply checking values one by one), which may cause it to be a
	 * bit slower in case lot's of removes are performed. This may be an opportunity for some fine-tuning if necessary.
	 */
	private int countRemoveIndex(int startIndex) {
		int k = 0;

		while (k < removes.size() && (removes.get(k).index <= startIndex)) {
			k++;
			startIndex++;
		}

		return startIndex;
	}

	private void incIndices(int indexInArray, List<Entry> list) {
		for (int i = indexInArray; i < list.size(); i++) {
			list.get(i).index++;
		}
	}

	private void decIndices(int indexInArray, List<Entry> list) {
		for (int i = indexInArray; i < list.size(); i++) {
			list.get(i).index--;
		}
	}

	/**
	 * Assumes the given array is sorted in non-descending order and returns the smallest index on which the new element could be inserted,
	 * so that this property is preserved. E.g. for array <tt>1, 4, 6, 7</tt>, the index of <tt>5</tt> is <tt>2</tt>, the index of
	 * <tt>6</tt> is also <tt>2</tt>.
	 * 
	 * OPTIMIZE this implementation is quite inefficient, use binary search or replace the array with a TreeMap.
	 */
	private int countIndexInArray(List<Entry> array, int value) {
		int result = 0;
		while (result < array.size()) {
			int valueOnPos = array.get(result).index;
			if (valueOnPos < value) {
				result++;
			} else {
				return result;
			}
		}

		return result;
	}

	@Override
	public void appendAggregateManipulations(List<AtomicManipulation> manipulations, Set<EntityReference> entitiesToDelete) {
		removeDeletedEntities(entitiesToDelete);

		if (list != null) {
			ChangeValueManipulation cvm = changeValue(newValue(), owner);
			manipulations.add(cvm);

		} else {
			// create remove manipulation
			if (!removes.isEmpty()) {
				RemoveManipulation rm = removeManipulation(asMap(removes), owner);
				manipulations.add(rm);
			}

			// create insert manipulation
			if (!adds.isEmpty()) {
				AddManipulation im = addManipulation(asMap(adds), owner);
				manipulations.add(im);
			}
		}
	}

	private void removeDeletedEntities(Set<EntityReference> entitiesToDelete) {
		Iterator<Entry> it = removes.iterator();
		while (it.hasNext()) {
			Entry e = it.next();
			if (entitiesToDelete.contains(e.value)) {
				it.remove();
				undoRemove(e);
			}
		}

		int indexInArray = 0;
		it = adds.iterator();
		while (it.hasNext()) {
			if (entitiesToDelete.contains(it.next().value)) {
				it.remove();
				undoAdd(indexInArray);
			}
			indexInArray++;
		}
	}

	private void undoRemove(Entry e) {
		int indexInArray = countIndexInArray(adds, e.index + 1);
		for (int i = indexInArray; i < adds.size(); i++)
			adds.get(i).index++;
	}

	private void undoAdd(int indexInArray) {
		for (int i = indexInArray; i < adds.size(); i++)
			adds.get(i).index--;
	}

	private List<?> newValue() {
		return list;
	}

	private Map<Object, Object> asMap(List<Entry> entries) {
		Map<Object, Object> result = new HashMap<Object, Object>();

		for (Entry e : entries) {
			result.put(e.index, e.value);
		}

		return result;
	}

}
