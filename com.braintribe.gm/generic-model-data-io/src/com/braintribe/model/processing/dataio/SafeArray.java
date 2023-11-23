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
package com.braintribe.model.processing.dataio;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SafeArray<T> implements Iterable<T> {
	protected Object array[];
	protected Map<Integer, T> extension;
	protected int arraySize;
	protected int size;
	
	public SafeArray(int arraySize) {
		array = new Object[arraySize];
		this.arraySize = arraySize; 
	}
	
	public int size() {
		return size;
	}
	
	public void put(int index, T value) {
		T oldValue = null;
		if (index < arraySize) {
			oldValue = (T)array[index];
			array[index] = value;
		}
		else {
			if (extension == null)
				extension = new HashMap<>();
		
			oldValue = extension.put(index, value);
		}
		
		if (oldValue == null)
			size++;
	}
	
	public T get(int index) {
		if (index < arraySize)
			return (T)array[index];
		else if (extension != null)
			return extension.get(index);
		else
			return null;
	}
	
	@Override
	public Iterator<T> iterator() {
		Stream<T> stream = Stream.of(array).filter(v -> v != null).map(v -> (T)v);
		
		if (extension != null) {
			stream = Stream.concat(stream, extension.values().stream());
		}
		
		return stream.iterator();
	}
	
	public static void main(String[] args) {
		SafeArray<String> l = new SafeArray<>(10);
		
		l.put(1, "one");
		l.put(2, "two");
		l.put(10, "ten");
		l.put(100, "hundret");
		
		System.out.println(l.get(1));
		System.out.println(l.get(2));
		System.out.println(l.get(10));
		System.out.println(l.get(100));
		System.out.println(l.size());
	}
}
