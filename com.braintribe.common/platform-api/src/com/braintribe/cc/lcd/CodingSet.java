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
package com.braintribe.cc.lcd;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

/**
 * a set that allows for codec based auto-wrapping management <br/>
 * <br/>
 * {@literal Set<B> codingSet = new CodingSet<BW, B>( new AnySetYouLike<BW>(), new YourWrapperCodec());}<br/>
 * or<br/>
 * {@literal Set<B> codingSet = CodingSet.createHashBased( new YourWrapperCodec());}<br/>
 * <br/>
 * where<br/>
 * B : the thing you want to store, i.e. a Bean<br/>
 * BW : thing that wraps the bean and implements hashCode and equals(object), i.e. defines the identity behavior, a BeanWrapper<br/>
 * AnySetYouLike : the set implementation you want to have as a delegate<br/>
 * YourWrapperCodec : a codec that translates B,BW. <br/>
 * <br/>
 * if you need any concrete examples, have a look that the test cases in com.braintribe:PlatformApiTest#1.0, package com.braintribe.coding<br/>
 *
 * @author pit
 *
 *
 * @param <WE>
 *            - wrapper element
 * @param <E>
 *            - element
 */
public class CodingSet<WE, E> implements Set<E> {

	private final Set<WE> delegate;

	private final Codec<E, WE> codec;

	/** Create a hash set based on the hash set */
	public static <E, WE> CodingSet<WE, E> createHashSetBased(Codec<? super E, WE> codec) {
		return new CodingSet<>(new LinkedHashSet<>(), codec);
	}

	public static <E, WE> CodingSet<WE, E> createHashSetBased(Codec<? super E, WE> codec, Supplier<Set<?>> backupFactory) {
		return new CodingSet<>((Set<WE>) backupFactory.get(), codec);
	}

	/**
	 * Equivalent to calling {@link #createHashSetBased(Codec)} with a {@link HashingComparatorWrapperCodec} backed by this comparator.
	 */
	public static <E> Set<E> create(HashingComparator<? super E> comparator) {
		return CodingSet.createHashSetBased(new HashingComparatorWrapperCodec<E>(comparator, comparator.isHashImmutable()));
	}

	/**
	 * Equivalent to calling {@link #createHashSetBased(Codec)} with a {@link HashingComparatorWrapperCodec} backed by this comparator.
	 */
	public static <E> Set<E> create(HashingComparator<? super E> comparator, Supplier<Set<?>> backupFactory) {
		return createHashSetBased(new HashingComparatorWrapperCodec<E>(comparator, comparator.isHashImmutable()), backupFactory);
	}

	public CodingSet(Set<WE> delegate, Codec<? super E, WE> codec) {
		this.delegate = delegate;
		this.codec = (Codec<E, WE>) codec;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean contains(Object object) {
		return delegate.contains(wrap((E) object));
	}

	@Override
	public Iterator<E> iterator() {
		// wrap?
		return new CodingIterator<>(delegate.iterator(), codec);
	}

	@Override
	public Object[] toArray() {
		Object[] result = delegate.toArray();

		for (int i = 0; i < delegate.size(); i++) {
			result[i] = unwrap((WE) result[i]);
		}

		return result;
	}

	@Override
	public <T> T[] toArray(T[] array) {
		Objects.requireNonNull(array, "array cannot be null");

		Object[] intermediateResult = delegate.toArray();
		if (array.length < intermediateResult.length) {
			List<T> list = newList(intermediateResult.length);
			for (Object obj : intermediateResult) {
				Object unwrapped = obj == null ? null : unwrap((WE) obj);
				list.add((T) unwrapped);
			}

			return list.toArray(array);

		} else {
			for (int i = 0; i < intermediateResult.length; i++) {
				Object obj = intermediateResult[i];
				Object unwrapped = obj == null ? null : unwrap((WE) obj);
				array[i] = (T) unwrapped;
			}

			if (array.length > intermediateResult.length) {
				array[intermediateResult.length] = null;
			}

			return array;
		}
	}

	@Override
	public boolean add(Object object) {
		return delegate.add(wrap((E) object));
	}

	@Override
	public boolean remove(Object object) {
		return delegate.remove(wrap((E) object));
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		for (Object object : collection) {
			if (!delegate.contains(wrap((E) object))) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> collection) {
		boolean retval = false;
		for (E object : collection) {
			retval |= delegate.add(wrap(object));
		}

		return retval;
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		return delegate.removeIf(we -> filter.test(unwrap(we)));
	}

	private WE wrap(E element) {
		try {
			return codec.encode(element);
		} catch (CodecException e) {
			throw new IllegalStateException("cannot encode element: " + element, e);
		}
	}

	private E unwrap(WE we) {
		try {
			return codec.decode(we);

		} catch (CodecException e) {
			throw new IllegalStateException("cannot decode encoded element: " + we, e);
		}
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		boolean result = false;

		Iterator<WE> it = delegate.iterator();
		while (it.hasNext()) {
			if (!collection.contains(unwrap(it.next()))) {
				it.remove();
				result = true;
			}
		}

		return result;
	}

	/**
	 * NOTE that this implementation does not follow the {@link Collection#removeAll(Collection)} specification, i.e. it does not check if collection
	 * given as parameter contains elements of this set, but it simply calls {@link #remove(Object)} with every element contained in given collection.
	 */
	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean retval = false;
		for (Object o : collection) {
			retval |= remove(o);
		}

		return retval;
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	/** Copied from {@link AbstractSet} */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof Set)) {
			return false;
		}

		Collection<?> c = (Collection<?>) o;
		if (c.size() != size()) {
			return false;
		}
		try {
			return containsAll(c);
		} catch (ClassCastException | NullPointerException unused) {
			return false;
		}
	}

	/** Copied from {@link AbstractSet} */
	@Override
	public int hashCode() {
		int h = 0;
		Iterator<E> i = iterator();
		while (i.hasNext()) {
			E obj = i.next();
			if (obj != null) {
				h += obj.hashCode();
			}
		}
		return h;
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "[]";
		}

		Iterator<E> it = iterator();
		StringBuilder sb = new StringBuilder();

		sb.append('[');
		while (true) {
			sb.append(it.next());
			if (!it.hasNext()) {
				return sb.append(']').toString();
			}
			sb.append(',').append(' ');
		}
	}
}
