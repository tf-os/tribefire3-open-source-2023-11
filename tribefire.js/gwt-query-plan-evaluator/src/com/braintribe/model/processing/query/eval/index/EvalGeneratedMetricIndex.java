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
package com.braintribe.model.processing.query.eval.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.queryplan.index.GeneratedMetricIndex;

/**
 * 
 */
public class EvalGeneratedMetricIndex extends EvalGeneratedIndex implements EvalMetricIndex {

	protected NavigableMap<Object, Set<Tuple>> treeMap;

	public EvalGeneratedMetricIndex(QueryEvaluationContext context, GeneratedMetricIndex index) {
		super(context, index, new TreeMap<>());

		this.treeMap = (NavigableMap<Object, Set<Tuple>>) indexMap;
	}

	@Override
	public Iterable<Tuple> getIndexRange(Object from, Boolean fromInclusive, Object to, Boolean toInclusive) {
		Map<Object, Set<Tuple>> subMap;

		if (fromInclusive == null)
			subMap = treeMap.headMap(to, toInclusive);
		else if (toInclusive == null)
			subMap = treeMap.tailMap(from, fromInclusive);
		else
			subMap = treeMap.subMap(from, fromInclusive, to, toInclusive);

		return asIterable(subMap);
	}

	@Override
	public Iterable<Tuple> getFullRange(boolean reverseOrder) {
		Map<Object, Set<Tuple>> fullMap = reverseOrder ? treeMap.descendingMap() : treeMap;

		return asIterable(fullMap);
	}

	private CollectionOfCollectionsIterable<Tuple> asIterable(Map<Object, Set<Tuple>> map) {
		return new CollectionOfCollectionsIterable<>(map.values());
	}

	static class CollectionOfCollectionsIterable<T> implements Iterable<T> {

		private final Collection<? extends Collection<? extends T>> collection2D;

		public CollectionOfCollectionsIterable(Collection<? extends Collection<? extends T>> collection2D) {
			this.collection2D = collection2D;
		}

		@Override
		public Iterator<T> iterator() {
			return stream().iterator();
		}

		public Stream<T> stream() {
			return collection2D.stream().flatMap(Collection::stream);
		}

	}
}
