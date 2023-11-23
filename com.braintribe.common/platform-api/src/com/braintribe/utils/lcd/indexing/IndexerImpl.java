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
package com.braintribe.utils.lcd.indexing;

import static com.braintribe.utils.lcd.CollectionTools2.acquireLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.braintribe.utils.lcd.CollectionTools2;

/**
 * @author peter.gazdik
 */
public class IndexerImpl<V> implements Indexer<V> {

	private final Iterable<? extends V> values;

	public IndexerImpl(Stream<? extends V> values) {
		this(((Stream<V>) values)::iterator);
	}

	public IndexerImpl(Iterable<? extends V> values) {
		this.values = nullSafe(values);
	}

	@Override
	public <K> BoundIndexer<K, V> by(Function<? super V, ? extends K> indexFunction) {
		return new BoundIndexerImpl<>(v -> Arrays.asList(indexFunction.apply(v)));
	}

	@Override
	public <K> BoundIndexer<K, V> byMany(Function<? super V, ? extends Iterable<? extends K>> indexFunction) {
		return new BoundIndexerImpl<>(indexFunction);
	}

	// ############################################
	// ## . . . . . . . . Bound . . . . . . . . .##
	// ############################################

	private class BoundIndexerImpl<K> implements BoundIndexer<K, V> {

		private final Function<? super V, ? extends Iterable<? extends K>> indexFunction;

		public BoundIndexerImpl(Function<? super V, ? extends Iterable<? extends K>> indexFunction) {
			this.indexFunction = indexFunction;
		}

		@Override
		public Map<K, V> unique() {
			return unique(newLinkedMap());
		}

		@Override
		public Map<K, V> unique(Map<K, V> result) {
			Objects.requireNonNull(result, "Map for index cannot be null!");

			V v2;
			for (V v : values) {
				for (K k : indexFunction.apply(v)) {
					if ((v2 = result.put(k, v)) != null) {
						throw new IllegalArgumentException("Duplicate mapping for key '" + k + "'. Values: '" + v2 + "', '" + v + "'");
					}
				}
			}

			return result;
		}

		@Override
		public MultiIndexer<K, V> multi() {
			return new MultiIndexerImpl();
		}

		// ############################################
		// ## . . . . . . . . Multi . . . . . . . . ##
		// ############################################

		private class MultiIndexerImpl implements MultiIndexer<K, V> {

			private boolean distinct;

			@Override
			public MultiIndexer<K, V> distinct() {
				this.distinct = true;
				return this;
			}

			@Override
			public Map<K, List<V>> please() {
				return to(newLinkedMap());
			}

			@Override
			public Map<K, List<V>> to(Map<K, List<V>> map) {
				return distinct ? distinct(map) : regular(map);
			}

			private Map<K, List<V>> distinct(Map<K, List<V>> result) {
				Objects.requireNonNull(result, "Map for index cannot be null!");

				Map<K, Set<V>> distinctResult = newLinkedMap();

				for (V v : values) {
					for (K k : indexFunction.apply(v)) {
						acquireLinkedSet(distinctResult, k).add(v);
					}
				}

				CollectionTools2.mapValues(distinctResult, set -> newList(set), result);

				return result;
			}

			private Map<K, List<V>> regular(Map<K, List<V>> result) {
				for (V v : values) {
					for (K k : indexFunction.apply(v)) {
						acquireList(result, k).add(v);
					}
				}

				return result;
			}

		}
	}
}
