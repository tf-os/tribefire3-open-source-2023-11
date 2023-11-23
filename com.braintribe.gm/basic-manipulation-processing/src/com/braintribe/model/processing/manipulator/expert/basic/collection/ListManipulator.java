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
package com.braintribe.model.processing.manipulator.expert.basic.collection;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newTreeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import com.braintribe.model.processing.manipulator.api.CollectionManipulator;
import com.braintribe.utils.lcd.CommonTools;

public class ListManipulator implements CollectionManipulator<List<Object>, Integer> {

	public static final ListManipulator INSTANCE = new ListManipulator();

	private ListManipulator() {
	}

	@Override
	public void insert(List<Object> list, Integer index, Object value) {
		if (index < 0)
			index = list.size() + 1 - index;

		if (index < list.size())
			list.add(index, value);
		else
			list.add(value);
	}

	@Override
	public void insert(List<Object> list, Map<Integer, Object> values) {
		List<Range> ranges = prepareRange(values);

		for (Range range : ranges)
			range.addElementsTo(list);
	}

	@Override
	public void remove(List<Object> list, Integer index, Object value) {
		int idx = index;
		if (idx < list.size() && CommonTools.equalsOrBothNull(value, list.get(idx))) {
			list.remove(idx);
		} else {
			list.remove(value);
		}
	}

	@Override
	public void remove(List<Object> list, Map<Integer, Object> values) {
		List<Range> ranges = prepareReversedRange(values);
		for (Range range : ranges) {
			List<Object> subList = range.end < list.size() ? list.subList(range.start, range.end + 1) : null;

			if (range.values.equals(subList)) {
				subList.clear();
			} else {
				for (Object valueToRemove : range.values) {
					list.remove(valueToRemove);
				}
			}
		}
	}

	@Override
	public void clear(List<Object> list) {
		list.clear();
	}

	private List<Range> prepareReversedRange(Map<Integer, Object> values) {
		List<Range> ranges = prepareRange(values);
		Collections.reverse(ranges);
		return ranges;
	}

	private List<Range> prepareRange(Map<Integer, Object> values) {
		Pair[] pairs = new Pair[values.size()];
		int i = 0;

		for (Map.Entry<Integer, Object> entry : values.entrySet()) {
			pairs[i++] = new Pair(entry.getKey(), entry.getValue());
		}

		RangeBuilder rangeBuilder = new RangeBuilder(pairs, true);
		return rangeBuilder.getRanges();
	}

	public static class Pair implements Comparable<Pair> {

		Integer index;
		Object value;

		public Pair(Integer index, Object value) {
			this.index = index;
			this.value = value;
		}

		@Override
		public int compareTo(Pair o) {
			return index.compareTo(o.index);
		}
	}

	public static class Range {
		private final int start;
		private int end;
		private final List<Object> values = new ArrayList<Object>();

		/* package */ Range(Pair pair) {
			start = end = pair.index;
			values.add(pair.value);
		}

		/* package */ boolean addPair(Pair pair) {
			/* We assume pairs are sorted in ascending order. Thus we add a pair to a rang if it's index is one bigger
			 * than the end of the range. One exception is the index 0, because we don't want to merge positive and
			 * negative indices together. */
			if (end + 1 == pair.index && pair.index != 0) {
				end = pair.index;
				values.add(pair.value);
				return true;

			} else {
				return false;
			}
		}

		public void addElementsTo(List<Object> list) {
			list.addAll(getStart(list), values);
		}

		private int getStart(List<?> list) {
			int result = start;

			if (result < 0)
				// if size is 10 and start is -2, we want to add at position 9
				// so original element at position 9 is moved on position 10 and our element is 2nd to last
				result = list.size() + 1 + result;

			if (result > list.size())
				result = list.size();

			return result;
		}
	}

	public static class RangeBuilder {

		private final SortedSet<Pair> pairs = newTreeSet();

		public RangeBuilder(Pair pairs[]) {
			this(pairs, false);
		}

		public RangeBuilder(Pair pairs[], boolean sort) {
			if (sort)
				Arrays.sort(pairs);

			for (Pair pair : pairs)
				this.pairs.add(pair);
		}

		public List<Range> getRanges() {
			List<Range> ranges = newList();
			List<Range> negativeRanges = newList();

			Range range = null;

			for (Pair pair : pairs) {
				if (range == null || !range.addPair(pair)) {
					// prepare new range
					range = new Range(pair);
					if (pair.index < 0)
						negativeRanges.add(range);
					else
						ranges.add(range);
				}
			}

			ranges.addAll(negativeRanges);

			return ranges;
		}

	}
}
