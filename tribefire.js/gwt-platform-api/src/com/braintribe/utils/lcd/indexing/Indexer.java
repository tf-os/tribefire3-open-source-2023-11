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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Fluent API on top of some collection of items of type <code>V</code> to provide the information necessary to obtain
 * an index of that collection, i.e. something that maps certain values to elements of this collection. This resulting
 * index can either be of type {@code Map<K, V>}, if the key is unique for each v, or {@code Map<K, List<V>>} otherwise.
 * This distinction happens by either using the {@link #by(Function)} or {@link #byMany(Function)} method.
 * 
 * @author peter.gazdik
 */
public interface Indexer<V> {

	/**
	 * Use this when each element of the Specify the function which resolves a single key you want to index by. For
	 * example, if you have a collection of Items, and you are indexing by a unique item id.
	 */
	<K> BoundIndexer<K, V> by(Function<? super V, ? extends K> indexFunction);

	/**
	 * Specify the function which provides multiple keys you want to index that item by. For example, if you have Folder
	 * and you want to index it by all it's sub-folders.
	 */
	<K> BoundIndexer<K, V> byMany(Function<? super V, ? extends Iterable<? extends K>> indexFunction);

	public static interface BoundIndexer<K, V> {
		/**
		 * Build an index mapping each key to the single corresponding value based on the indexFunction selected in the
		 * previous step (see {@link Indexer}). This means we do not expect any two values to share the same key. If
		 * that was the case with the values provided, this method throws an {@link IllegalArgumentException}.
		 */
		Map<K, V> unique();

		/** Just like {@link #unique()}, but you can specify the result map. */
		Map<K, V> unique(Map<K, V> index);

		/**
		 * Specify that the key is not unique and thus the index which maps key to a list of values should be prepared.
		 */
		MultiIndexer<K, V> multi();
	}

	public static interface MultiIndexer<K, V> {
		/**
		 * Specifies that any duplicates should be removed from the the list of values mapped for each key.
		 */
		MultiIndexer<K, V> distinct();

		/**
		 * Build an index mapping each key to all the relevant values based on the indexFunction selected in the
		 * previous step (see {@link Indexer}).
		 */
		Map<K, List<V>> please();

		
		/** Just like {@link #please()}, but you can specify the result map. */
		Map<K, List<V>> to(Map<K, List<V>> index);

	}

}
