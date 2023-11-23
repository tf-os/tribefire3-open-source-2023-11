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
package com.braintribe.collections;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class StaticArrayList<T> extends AbstractList<T> {
	private Object[] array;
	private int size;
	private int offset;
	private int terminatorIndex;
	private boolean unmodifiable;

	public StaticArrayList(Object[] array, int offset, int l, boolean unmodifiable) {
		super();
		this.array = array;
		this.size = l;
		this.offset = offset;
		this.unmodifiable = unmodifiable;
		this.terminatorIndex = offset + l;
	}

	public StaticArrayList(Object[] array, boolean unmodifiable) {
		super();
		this.array = array;
		this.unmodifiable = unmodifiable;
		this.terminatorIndex = this.size = array.length;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size != 0;
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}

	@Override
	public Iterator<T> iterator() {
		return new StaticArrayListIterator<>(array, offset, offset, terminatorIndex);
	}

	@Override
	public Object[] toArray() {
		Object[] clonedArray = new Object[size];
		System.arraycopy(array, offset, clonedArray, 0, size);
		return clonedArray;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> X[] toArray(X[] a) {
		Object clonedArray[] = a.length == size ? a : (Object[]) Array.newInstance(a.getClass().getComponentType(), size);
		System.arraycopy(array, offset, clonedArray, 0, size);
		return (X[]) clonedArray;
	}

	@Override
	public boolean add(T e) {
		throw new UnsupportedOperationException("not supported by this implementation");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("not supported by this implementation");
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException("not supported by this implementation");
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException("not supported by this implementation");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("not supported by this implementation");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("not supported by this implementation");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("not supported by this implementation");
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(int index) {
		return (T) array[index + offset];
	}

	@SuppressWarnings("unchecked")
	@Override
	public T set(int index, T element) {
		if (unmodifiable) {
			throw new IllegalStateException("list elements may not be changed in a unmodifiable instance");
		}

		int i = index + offset;
		Object oldValue = array[i];
		array[i] = element;
		return (T) oldValue;
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException("not supported by this implementation");
	}

	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException("not supported by this implementation");
	}

	@Override
	public int indexOf(Object o) {
		int term = terminatorIndex;
		for (int i = offset; i < term; i++) {
			Object e = array[i];
			if (e == null) {
				if (o == null) {
					return i - offset;
				} else {
					continue;
				}
			}

			if (e.equals(o)) {
				return i - offset;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		for (int i = terminatorIndex - 1; i >= 0; i--) {
			Object e = array[i];
			if (e == null) {
				if (o == null) {
					return i - offset;
				} else {
					continue;
				}
			}

			if (e.equals(o)) {
				return i - offset;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<T> listIterator() {
		return new StaticArrayListIterator<>(array, offset, offset, terminatorIndex);
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return new StaticArrayListIterator<>(array, offset, index + offset, terminatorIndex);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return new StaticArrayList<>(array, offset + fromIndex, toIndex - fromIndex, unmodifiable);
	}

	private class StaticArrayListIterator<E> implements ListIterator<E> {
		@SuppressWarnings("hiding")
		private Object array[];
		@SuppressWarnings("hiding")
		private int offset;
		private int index;
		private int term;

		public StaticArrayListIterator(Object[] array, int offset, int index, int term) {
			this.array = array;
			this.offset = offset;
			this.index = index;
			this.term = term;
		}

		@Override
		public boolean hasNext() {
			return index < term;
		}

		@Override
		public E next() {
			@SuppressWarnings("unchecked")
			E value = (E) array[index];
			index++;
			return value;
		}

		@Override
		public boolean hasPrevious() {
			return index > 0;
		}

		@Override
		public E previous() {
			index--;
			@SuppressWarnings("unchecked")
			E value = (E) array[index];
			return value;
		}

		@Override
		public int nextIndex() {
			return index + offset;
		}

		@Override
		public int previousIndex() {
			return index - 1 + offset;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("not implemented by this implementation");
		}

		@Override
		public void set(E e) {
			if (unmodifiable) {
				throw new IllegalStateException("list elements may not be changed in a unmodifiable instance");
			}
			array[index] = e;
		}

		@Override
		public void add(E e) {
			throw new UnsupportedOperationException("not implemented by this implementation");
		}

	}

	// public static void main(String[] args) {
	// try {
	// Integer[] numbers = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	// List<Integer> list = new StaticArrayList<Integer>(numbers, true);
	//
	// System.out.println("===============");
	// Iterator<Integer> it = list.iterator();
	// while (it.hasNext()) {
	// System.out.println(it.next());
	// }
	// System.out.println("---------");
	// for (Integer element : list) {
	// System.out.println(element);
	// }
	// System.out.println("---------");
	// for (int i = 0; i < list.size(); i++) {
	// Integer e = list.get(i);
	// System.out.println(e);
	// }
	// System.out.println("===============");
	//
	// list = list.subList(2, 8);
	//
	// it = list.iterator();
	// while (it.hasNext()) {
	// System.out.println(it.next());
	// }
	// System.out.println("---------");
	// for (Integer element : list) {
	// System.out.println(element);
	// }
	// System.out.println("---------");
	// for (int i = 0; i < list.size(); i++) {
	// Integer e = list.get(i);
	// System.out.println(e);
	// }
	// System.out.println("---------");
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
}
