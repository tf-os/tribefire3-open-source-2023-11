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
package com.braintribe.utils.lcd;

import static com.braintribe.utils.lcd.CollectionTools.split;
import static java.util.Collections.newSetFromMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.utils.lcd.indexing.Indexer;
import com.braintribe.utils.lcd.indexing.IndexerImpl;

/**
 * An alternative to {@link CollectionTools} which contains only very very simple methods. Basically, if a method requires javadoc, it should probably
 * be moved elsewhere :). (The idea is these methods are all very intuitive, and thus the whole class is easy to use. If something needs more
 * complicated documentation, it should be moved elsewhere.)
 *
 * Please, consider carefully if the method is simple/common enough to be here.
 *
 * There are bigger blocks of similar methods here, namely the following:
 * <ol>
 * <li>Instantiations - methods for creating new empty collections, useful for their short name</li>
 * <li>Instantiations with data - methods for creating new collections with data from existing ones</li>
 * <li>Literals - for the lack of collection literals in Java, here is a collection of methods for creating a specific type of a collection with given
 * elements</li>
 * <li>NullSafe - accessing collection and their basic info (e.g. size) in a null-safe way</li>
 * <li>Acquire Collection for given key - methods which get or create a collection for a given key</li>
 * <li>Get one element from a collection - the name says it all</li>
 * <li>Get N elements from a collection - the name says it all</li>
 * <li>List operations - stuff like concatenation</li>
 * <li>Set operations - stuff like union</li>
 * <li>Sorting - methods that take a collection and return a sorted list</li>
 * <li>Maps - some useful stuff for indexing and updating key/value in a map</li>
 * <li>Miscellaneous - various other, these might be removed in the future</li>
 * </ol>
 *
 * @author peter.gazdik
 */
public class CollectionTools2 {

	// ##########################################################
	// ## . . . . . . . . . Instantiations . . . . . . . . . . ##
	// ##########################################################

	public static <E> ArrayList<E> newList() {
		return new ArrayList<>();
	}

	public static <E> ArrayList<E> newList(int initialCapacity) {
		return new ArrayList<>(initialCapacity);
	}

	public static <E> LinkedList<E> newLinkedList() {
		return new LinkedList<>();
	}

	public static <E> HashSet<E> newSet() {
		return new HashSet<>();
	}

	public static <E> HashSet<E> newSet(int initialCapacity) {
		return new HashSet<>(initialCapacity);
	}

	public static <E> Set<E> newSet(HashingComparator<? super E> hashingComparator) {
		return CodingSet.create(hashingComparator);
	}

	public static <E> LinkedHashSet<E> newLinkedSet() {
		return new LinkedHashSet<>();
	}

	public static <E> TreeSet<E> newTreeSet() {
		return new TreeSet<>();
	}

	public static <E> TreeSet<E> newTreeSet(Comparator<? super E> comparator) {
		return new TreeSet<>(comparator);
	}

	public static <E> Set<E> newIdentitySet() {
		return newSetFromMap(newIdentityMap());
	}

	public static <E> Set<E> newConcurrentSet() {
		return newSetFromMap(newConcurrentMap());
	}

	public static <E> ArrayDeque<E> newDeque() {
		return new ArrayDeque<>();
	}

	public static <E> ArrayDeque<E> newDeque(int initialCapacity) {
		return new ArrayDeque<>(initialCapacity);
	}

	public static <K, V> HashMap<K, V> newMap() {
		return new HashMap<>();
	}

	public static <K, V> HashMap<K, V> newMap(int initialCapacity) {
		return new HashMap<>(initialCapacity);
	}

	public static <K, V> Map<K, V> newMap(HashingComparator<? super K> hashingComparator) {
		return CodingMap.create(hashingComparator);
	}

	public static <K, V> LinkedHashMap<K, V> newLinkedMap() {
		return new LinkedHashMap<>();
	}

	public static <K, V> TreeMap<K, V> newTreeMap() {
		return new TreeMap<>();
	}

	public static <K, V> TreeMap<K, V> newTreeMap(Comparator<? super K> keyComparator) {
		return new TreeMap<>(keyComparator);
	}

	public static <K, V> IdentityHashMap<K, V> newIdentityMap() {
		return new IdentityHashMap<>();
	}

	public static <K, V> ConcurrentHashMap<K, V> newConcurrentMap() {
		return new ConcurrentHashMap<>();
	}

	// ##########################################################
	// ## . . Instantiations based on existing collections . . ##
	// ##########################################################

	public static <E> ArrayList<E> newList(Collection<? extends E> c) {
		return new ArrayList<>(c);
	}

	public static <E> LinkedList<E> newLinkedList(Collection<? extends E> c) {
		return new LinkedList<>(c);
	}

	public static <E> HashSet<E> newSet(Collection<? extends E> c) {
		return new HashSet<>(c);
	}

	public static <E> Set<E> newSet(HashingComparator<? super E> hashingComparator, Collection<? extends E> c) {
		Set<E> result = CodingSet.create(hashingComparator);
		result.addAll(c);
		return result;
	}

	public static <E> LinkedHashSet<E> newLinkedSet(Collection<? extends E> c) {
		return new LinkedHashSet<>(c);
	}

	public static <E> TreeSet<E> newTreeSet(Collection<? extends E> c) {
		return new TreeSet<>(c);
	}

	public static <E> TreeSet<E> newTreeSet(Comparator<? super E> comparator, Collection<? extends E> c) {
		TreeSet<E> result = newTreeSet(comparator);
		result.addAll(c);
		return result;
	}

	public static <E> Set<E> newConcurrentSet(Collection<? extends E> c) {
		Set<E> result = newSetFromMap(newConcurrentMap());
		result.addAll(c);
		return result;
	}

	public static <E> ArrayDeque<E> newDeque(Collection<? extends E> c) {
		return new ArrayDeque<>(c);
	}

	public static <K, V> HashMap<K, V> newMap(Map<? extends K, ? extends V> m) {
		return new HashMap<>(m);
	}

	public static <K, V> Map<K, V> newMap(HashingComparator<? super K> hashingComparator, Map<? extends K, ? extends V> m) {
		Map<K, V> result = CodingMap.create(hashingComparator);
		result.putAll(m);
		return result;
	}

	public static <K, V> LinkedHashMap<K, V> newLinkedMap(Map<? extends K, ? extends V> m) {
		return new LinkedHashMap<>(m);
	}

	public static <K, V> TreeMap<K, V> newTreeMap(Map<? extends K, ? extends V> m) {
		return new TreeMap<>(m);
	}

	public static <K, V> TreeMap<K, V> newTreeMap(Comparator<? super K> keyComparator, Map<? extends K, ? extends V> m) {
		TreeMap<K, V> result = newTreeMap(keyComparator);
		result.putAll(m);
		return result;
	}

	public static <K, V> IdentityHashMap<K, V> newIdentityMap(Map<? extends K, ? extends V> m) {
		return new IdentityHashMap<>(m);
	}

	public static <K, V> ConcurrentHashMap<K, V> newConcurrentMap(Map<? extends K, ? extends V> m) {
		return new ConcurrentHashMap<>(m);
	}

	// ##########################################################
	// ## . . . . . . . . . . . Literals . . . . . . . . . . . ##
	// ##########################################################

	@SafeVarargs
	public static <E> ArrayList<E> asList(E... elements) {
		return new ArrayList<>(list(elements));
	}

	@SafeVarargs
	public static <E> LinkedList<E> asLinkedList(E... elements) {
		return new LinkedList<>(list(elements));
	}

	@SafeVarargs
	public static <E> HashSet<E> asSet(E... elements) {
		return new HashSet<>(list(elements));
	}

	@SafeVarargs
	public static <E> LinkedHashSet<E> asLinkedSet(E... elements) {
		return new LinkedHashSet<>(list(elements));
	}

	@SafeVarargs
	public static <E> TreeSet<E> asTreeSet(E... elements) {
		return new TreeSet<>(list(elements));
	}

	@SafeVarargs
	public static <E> TreeSet<E> asTreeSet(Comparator<? super E> comparator, E... elements) {
		return newTreeSet(comparator, list(elements));
	}

	@SafeVarargs
	public static <E> Set<E> asIdentitySet(E... elements) {
		return asSet(newIdentitySet(), elements);
	}

	@SafeVarargs
	public static <E> Set<E> asConcurrentSet(E... elements) {
		return asSet(newConcurrentSet(), elements);
	}

	@SafeVarargs
	private static <E> Set<E> asSet(Set<E> newSet, E... elements) {
		newSet.addAll(list(elements));
		return newSet;
	}

	public static <E> ArrayDeque<E> asDeque(E... elements) {
		return new ArrayDeque<>(list(elements));
	}

	@SafeVarargs
	private static <E> List<E> list(E... elements) {
		return Arrays.asList(elements);
	}

	public static <K, V> HashMap<K, V> asMap(Object... elements) {
		return (HashMap<K, V>) (Map<?, ?>) MapTools.putAllToMap(new HashMap<K, V>(), elements);
	}

	public static <K, V> Map<K, V> asLinkedMap(Object... elements) {
		return (Map<K, V>) (Map<?, ?>) MapTools.putAllToMap(new LinkedHashMap<K, V>(), elements);
	}

	public static <K, V> Map<K, V> asTreeMap(Object... elements) {
		return (Map<K, V>) (Map<?, ?>) MapTools.putAllToMap(new TreeMap<K, V>(), elements);
	}

	public static <K, V> Map<K, V> asTreeMap(Comparator<? super K> keyComparator, Object... elements) {
		return (Map<K, V>) (Map<?, ?>) MapTools.putAllToMap(new TreeMap<K, V>(keyComparator), elements);
	}

	public static <K, V> ConcurrentHashMap<K, V> asConcurrentMap(Object... elements) {
		return (ConcurrentHashMap<K, V>) (Map<?, ?>) MapTools.putAllToMap(new ConcurrentHashMap<K, V>(), elements);
	}

	public static <K, V> Map<K, V> asIdentityMap(Object... elements) {
		return (Map<K, V>) (Map<?, ?>) MapTools.putAllToMap(new IdentityHashMap<K, V>(), elements);
	}

	public static <K, V> Map<K, V> putAllToMap(Map<K, V> map, Object... elements) {
		return MapTools.putAllToMap(map, elements);
	}

	// ##########################################################
	// ## . . . . . . . . . . Null Safe . . . . . . . . . . . .##
	// ##########################################################

	public static boolean isEmpty(Collection<?> c) {
		return c == null || c.isEmpty();
	}

	public static boolean isEmpty(Map<?, ?> m) {
		return m == null || m.isEmpty();
	}

	public static int size(Collection<?> c) {
		return c != null ? c.size() : 0;
	}

	public static int size(Map<?, ?> m) {
		return m != null ? m.size() : 0;
	}

	public static <T> Iterable<T> nullSafe(Iterable<T> i) {
		return nullSafeIterable(i);
	}

	private static <T> Iterable<T> nullSafeIterable(Iterable<T> i) {
		return i == null ? Collections.<T> emptySet() : i;
	}

	public static <T> Collection<T> nullSafe(Collection<T> c) {
		return nullSafeCollection(c);
	}

	private static <T> Collection<T> nullSafeCollection(Collection<T> c) {
		return c == null ? Collections.<T> emptySet() : c;
	}

	public static <T> List<T> nullSafe(List<T> l) {
		return nullSafeList(l);
	}

	public static <T> List<T> nullSafeList(List<T> l) {
		return l == null ? Collections.<T> emptyList() : l;
	}

	public static <T> Set<T> nullSafe(Set<T> c) {
		return nullSafeSet(c);
	}

	private static <T> Set<T> nullSafeSet(Set<T> c) {
		return c == null ? Collections.<T> emptySet() : c;
	}

	public static <K, V> Map<K, V> nullSafe(Map<K, V> m) {
		return m == null ? Collections.<K, V> emptyMap() : m;
	}

	public static <C extends Collection<?>> C requireNonEmpty(C c, String msg) {
		if (isEmpty(c)) {
			throw new IllegalArgumentException(msg);
		}
		return c;
	}

	public static <C extends Collection<?>> C requireNonEmpty(C c, Supplier<String> messageSupplier) {
		if (isEmpty(c)) {
			throw new IllegalArgumentException(messageSupplier.get());
		}
		return c;
	}

	// ##########################################################
	// ## . . . . . . Acquire Mapped Collection . . . . . . . .##
	// ##########################################################

	public static <K, E> List<E> acquireList(Map<K, List<E>> map, K key) {
		return map.computeIfAbsent(key, k -> newList());
	}

	public static <K, E> List<E> acquireLinkedList(Map<K, List<E>> map, K key) {
		return map.computeIfAbsent(key, k -> newLinkedList());
	}

	public static <K, E> Set<E> acquireSet(Map<K, Set<E>> map, K key) {
		return map.computeIfAbsent(key, k -> newSet());
	}

	public static <K, E> Set<E> acquireLinkedSet(Map<K, Set<E>> map, K key) {
		return map.computeIfAbsent(key, k -> newLinkedSet());
	}

	public static <K, E> Set<E> acquireTreeSet(Map<K, Set<E>> map, K key) {
		return map.computeIfAbsent(key, k -> newTreeSet());
	}

	public static <K, E> Deque<E> acquireDeque(Map<K, Deque<E>> map, K key) {
		return map.computeIfAbsent(key, k -> newDeque());
	}

	public static <K1, K2, V> Map<K2, V> acquireMap(Map<K1, Map<K2, V>> map, K1 key) {
		return map.computeIfAbsent(key, k -> newMap());
	}

	public static <K1, K2, V> Map<K2, V> acquireLinkedMap(Map<K1, Map<K2, V>> map, K1 key) {
		return map.computeIfAbsent(key, k -> newLinkedMap());
	}

	public static <K1, K2, V> Map<K2, V> acquireTreeMap(Map<K1, Map<K2, V>> map, K1 key) {
		return map.computeIfAbsent(key, k -> newTreeMap());
	}

	// ##########################################################
	// ## . . . . . . . . . Get one element . . . . . . . . . .##
	// ##########################################################

	public static <T, E extends T> E single(Collection<T> c) {
		if (size(c) == 1) {
			return first(c);
		} else {
			throw new IllegalArgumentException("Not a collection with a single element: " + c);
		}
	}

	public static <T, E extends T> E first(Iterable<T> it) {
		return (E) it.iterator().next();
	}

	public static <T, E extends T> E first(List<T> list) {
		return (E) list.get(0);
	}

	public static <T> T first(T[] array) {
		return array[0];
	}

	public static <T, E extends T> E firstOrNull(Iterable<T> it) {
		if (it == null) {
			return null;
		}

		Iterator<T> i = it.iterator();
		return i.hasNext() ? (E) i.next() : null;
	}

	public static <T, E extends T> E firstOrNull(List<T> list) {
		return isEmpty(list) ? null : (E) list.get(0);
	}

	public static <T, E extends T> E removeFirst(Iterable<T> it) {
		Iterator<T> iterator = it.iterator();

		E result = (E) iterator.next();
		iterator.remove();

		return result;
	}

	public static <T, E extends T> E removeLast(List<T> list) {
		return (E) list.remove(list.size() - 1);
	}

	public static <T, E extends T> E last(List<T> list) {
		return (E) list.get(list.size() - 1);
	}

	public static <T> T last(T[] array) {
		return array[array.length - 1];
	}

	// ##########################################################
	// ## . . . . . . . . . Get N elements . . . . . . . . . . ##
	// ##########################################################

	public static <T, E extends T> List<E> firstN(List<T> list, int n) {
		return (List<E>) (List<?>) list.subList(0, n);
	}

	public static <T, E extends T> List<E> firstN(Iterable<T> iterable, int n) {
		return (List<E>) (List<?>) StreamSupport.stream(iterable.spliterator(), false) //
				.limit(n) //
				.collect(Collectors.toList());
	}

	public static <T, E extends T> List<E> skipFirstN(List<T> list, int n) {
		return (List<E>) (List<?>) list.subList(n, list.size());
	}

	public static <T, E extends T> List<E> skipFirstN(Iterable<T> iterable, int n) {
		return (List<E>) (List<?>) toStream(iterable) //
				.skip(n) //
				.collect(Collectors.toList());
	}

	public static <T, E extends T> List<E> lastN(List<T> list, int n) {
		int size = list.size();
		return (List<E>) (List<?>) list.subList(size - n, size);
	}

	public static <T, E extends T> List<E> removeLastN(List<T> list, int n) {
		return (List<E>) (List<?>) list.subList(0, list.size() - n);
	}

	// ##########################################################
	// ## . . . . . . . . . . List Operations . . . . . . . . . ##
	// ##########################################################

	/** Special case of {@link #concat(List, List, List...) list concatenation}, for normalization we keep this here. */
	public static <T> List<T> concat(List<? extends T> l1) {
		return (List<T>) l1;
	}

	/** Concatenation of lists */
	public static <T> List<T> concat(List<? extends T> l1, List<? extends T> l2, List<? extends T>... rest) {
		List<T> result = newList();

		result.addAll(l1);
		result.addAll(l2);

		if (rest != null) {
			for (List<? extends T> list : rest) {
				result.addAll(list);
			}
		}

		return result;
	}

	// ##########################################################
	// ## . . . . . . . . . . Set Operations . . . . . . . . . ##
	// ##########################################################

	/** Union of 2 sets. */
	public static <T> Set<T> union(Set<? extends T> set1, Set<? extends T> set2) {
		Set<T> result = newSet();

		result.addAll(set1);
		result.addAll(set2);

		return result;
	}

	public static <T> Set<T> substract(Collection<? extends T> set, Set<? extends T> setToSubstract) {
		Set<T> result = new HashSet<>(set);
		result.removeAll(setToSubstract);

		return result;
	}

	public static <T> List<Set<T>> splitToSets(Iterable<? extends T> elements, int limitSize) {
		return split(elements, limitSize, CollectionTools2::newSet);
	}

	public static <T> List<List<T>> splitToLists(Iterable<? extends T> elements, int limitSize) {
		return split(elements, limitSize, CollectionTools2::newList);
	}

	// ##########################################################
	// ## . . . . . . . . . . . Sorting . . . . . . . . . . . .##
	// ##########################################################

	/** Sort that assumes all the elements implement {@link Comparable} and are compatible with each other. */
	@SuppressWarnings("rawtypes")
	public static List<?> unsafeSort(Collection<?> collection) {
		return sort((Collection<Comparable>) collection);
	}

	/** @return new {@link ArrayList} consisting of elements of given collection, sorted in their natural order. */
	public static <T extends Comparable<T>> List<T> sort(Collection<T> collection) {
		ArrayList<T> list = new ArrayList<>(collection);
		Collections.sort(list);

		return list;
	}

	/** @return new {@link ArrayList} consisting of elements of given collection, sorted by given comparator. */
	public static <T> List<T> sort(Collection<? extends T> collection, Comparator<? super T> comparator) {
		ArrayList<T> list = new ArrayList<>(collection);
		Collections.sort(list, comparator);

		return list;
	}

	// ##########################################################
	// ## . . . . . . . . . . . Maps . . . . . . . . . . . . . ##
	// ##########################################################

	public static <V> Indexer<V> index(V... values) {
		return index(Arrays.asList(values));
	}

	public static <V> Indexer<V> index(Stream<? extends V> values) {
		return new IndexerImpl<>(values);
	}

	public static <V> Indexer<V> index(Iterable<? extends V> values) {
		return new IndexerImpl<>(values);
	}

	public static <K, V> Map<K, V> mapBy(Iterable<? extends V> values, Function<? super V, ? extends K> indexFunction) {
		return (Map<K, V>) index(values).by(indexFunction).unique();
	}

	/** @see MapTools#mapValues(Map, Function) */
	public static <K, V, W> Map<K, W> mapValues(Map<K, V> map, Function<? super V, ? extends W> valueMapping) {
		return MapTools.mapValues(map, valueMapping);
	}

	/** @see MapTools#mapValues(Map, Function, Map) */
	public static <K, V, W> Map<K, W> mapValues(Map<K, V> map, Function<? super V, ? extends W> valueMapping, Map<K, W> result) {
		return MapTools.mapValues(map, valueMapping, result);
	}

	/** I'm really not sure if this should be here. */
	public static <K, V> V updateMapKey(Map<K, V> map, K originalKey, K newKey) {
		V value = map.remove(originalKey);
		map.put(newKey, value);

		return value;
	}

	/**
	 * Creates a {@link Map} in the opposite direction compared to the Map given.
	 *
	 * @throws IllegalStateException
	 *             if the original map contained multiple values for the same key
	 */
	public static <K, V> Map<V, K> swapKeysAndValues(Map<K, V> map) {
		return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
	}

	/**
	 * External implementation of {@link Map#computeIfAbsent(Object, Function)}. This is useful when the semantics of computeIfAbsent is desired, but
	 * some implementation specific issues arise. Most common example is a ConcurrenHashMap which prohibits you to call this method recursively (or
	 * just update any other mapping in general).
	 */
	public static <K, V> V computeIfAbsent(Map<K, V> map, K key, Function<? super K, ? extends V> mappingFunction) {
		V v = map.get(key);
		if (v != null) {
			return v;
		}

		v = mappingFunction.apply(key);
		if (v != null) {
			map.put(key, v);
		}

		return v;
	}

	// ##########################################################
	// ## . . . . . . . . . . . Misc . . . . . . . . . . . . . ##
	// ##########################################################

	/** @return {@link ListIterator} located at the end of given list. */
	public static <T> ListIterator<T> iteratorAtTheEndOf(List<T> list) {
		return list.listIterator(list.size());
	}

	/** @return index of first element of given list matching given predicate. */
	public static <T> int findFirstIndex(List<T> list, Predicate<? super T> predicate) {
		return IntStream.range(0, list.size()) //
				.filter(i -> predicate.test(list.get(i))) //
				.findFirst() //
				.orElse(-1);
	}

	public static boolean sameSize(Collection<?> c1, Collection<?> c2) {
		return size(c1) == size(c2);
	}

	public static <T> Stream<T> toStream(Iterable<T> it) {
		return it instanceof Collection<?> ? ((Collection<T>) it).stream() : StreamSupport.stream(it.spliterator(), false);
	}

	public static String toStringWithElementTypes(List<?> params) {
		if (isEmpty(params)) {
			return "" + params;
		}

		StringJoiner sj = new StringJoiner(", ", "[", "]");
		for (Object o : params) {
			String typeInfo = o == null ? "" : " (" + o.getClass().getSimpleName() + ")";
			sj.add("" + o + typeInfo);
		}

		return sj.toString();
	}

}
