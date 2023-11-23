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
package com.braintribe.gwt.quickaccess.continuation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class QuickAccessPrefilterPopulation<T> implements Iterable<T> {
	private Iterator<T> populationIt;
	private List<T> prefiltered = new ArrayList<>();
	private Predicate<T> filter;
	
	public QuickAccessPrefilterPopulation(Iterator<T> populationIt, Predicate<T> filter) {
		super();
		this.populationIt = populationIt;
		this.filter = filter;
	}

	@Override
	public Iterator<T> iterator() {
		return new IteratorImpl();
	}
	
	private class IteratorImpl implements Iterator<T> {
		private Iterator<T> it = prefiltered.iterator();
		
		@Override
		public boolean hasNext() {
			if (it != null) {
				boolean hasNext = it.hasNext();
				
				if (hasNext)
					return true;
				else {
					it = null;
					return hasNext();
				}
			}
			else {
				return populationIt.hasNext();
			}
		}

		@Override
		public T next() {
			if (it != null) {
				T element = it.next();
				if (!it.hasNext())
					it = null;
				
				return element;
			}
			else {
				T element = populationIt.next();
				
				if (filter.test(element)) {
					prefiltered.add(element);
					return element;
				}
				else
					return null;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("not implementated as it makes no sense for this iterator");
		}
	}
	
	public static void main(String[] args) {
		List<String> population = Arrays.asList("Hallo", "Ballo", "Schnallo", "Wurst", "Rallo", "Hub");
		
		Predicate<String> prefilter = new Predicate<String>() {
			@Override
			public boolean test(String obj) {
				return obj.contains("allo");
			}
		};
		QuickAccessPrefilterPopulation<String> prefilteredPopulation = new QuickAccessPrefilterPopulation<>(
				population.iterator(), prefilter);

		for (int i = 0; i < 3; i++) {
			int count = 0;
			System.out.println("---- Run #" + i);
			for (String value: prefilteredPopulation) {
				System.out.println(value);
				if (count > 2 && i == 0)
					break;
				count++;
			}
		}
	}

}
