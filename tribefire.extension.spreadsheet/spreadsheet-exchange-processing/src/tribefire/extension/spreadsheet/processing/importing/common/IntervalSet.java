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
package tribefire.extension.spreadsheet.processing.importing.common;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class IntervalSet extends AbstractSet<Integer> {
	public static abstract class AbstractInterval implements Comparable<AbstractInterval> {
		public abstract int getStart();
		public abstract int getEnd();
		
		public abstract boolean isSingleNumber();
		public boolean contains(int i) {
			return i >= getStart() && i <= getEnd();
		}
		
		@Override
		public int compareTo(AbstractInterval o) {
			return this.getStart() - o.getStart();
		}
		
		@Override
		public String toString() {
			return "Interval[" + getStart() + "," + getEnd() + "]";
		}
	}
	
	private static class MinimalisticInterval extends AbstractInterval {
		private int start;
		
		public MinimalisticInterval(int start) {
			super();
			this.start = start;
		}

		public int getStart() {
			return start;
		}
		
		public int getEnd() {
			return start;
		}
		
		@Override
		public boolean isSingleNumber() {
			return true;
		}
		
	}
	
	private static class Interval extends MinimalisticInterval {
		private int end;
		
		public Interval(int start, int end) {
			super(start);
			this.end = end;
		}


		public int getEnd() {
			return end;
		}
		
		@Override
		public boolean isSingleNumber() {
			return false;
		}
	}
	
	private TreeSet<AbstractInterval> backup = new TreeSet<AbstractInterval>();
	private int size = 0;
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return backup.isEmpty();
	}
	
	public Set<AbstractInterval> getIntervals() {
		return backup;
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Integer))
			return false;
		Integer i = (Integer)o;
		
		MinimalisticInterval mi = new MinimalisticInterval(i);
		Iterator<AbstractInterval> sit = backup.tailSet(mi).iterator();

		if (sit.hasNext() && sit.next().contains(i)) {
				return true;
		}
		
		SortedSet<AbstractInterval> headSet = backup.headSet(mi);

		if (!headSet.isEmpty() && headSet.last().contains(i)) {
				return true;
		}

		return false;
	}
	
	public boolean areInSameInterval(int i1, int i2, boolean cycle) {
		AbstractInterval interval1 = getIntervalContaining(i1);
		AbstractInterval interval2 = getIntervalContaining(i2);
		
		if (interval1 == null || interval2 == null)
			return false;
		
		if (interval1 == interval2)
			return true;
		else
			return (cycle && (
					interval1.getStart() == 0 && interval2.getEnd() == size() - 1 ||
					interval2.getStart() == 0 && interval1.getEnd() == size() - 1));
	}
	
	public AbstractInterval getIntervalContaining(int i) {
		MinimalisticInterval mi = new MinimalisticInterval(i);
		Iterator<AbstractInterval> sit = backup.tailSet(mi).iterator();

		if (sit.hasNext()) {
			AbstractInterval interval = sit.next();
			if (interval.contains(i))
				return interval;
		}
		
		SortedSet<AbstractInterval> headSet = backup.headSet(mi);

		if (!headSet.isEmpty()) {
			AbstractInterval interval = headSet.last();
			if (interval.contains(i))
				return interval;
		}

		return null;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			private Iterator<AbstractInterval> delegate = backup.iterator();
			private int i;
			private AbstractInterval curInterval = null;
			private int lastResult;
					
			public boolean hasNext() {
				return curInterval != null || delegate.hasNext();
			}
			
			public Integer next() {
				if (curInterval == null) {
					curInterval = delegate.next();
					i = curInterval.getStart();
				}
				
				lastResult = i;
				
				i++;
				if (i > curInterval.getEnd()) { 
					curInterval = null;
				}							
				
				return lastResult;
			}
			
			public void remove() {
				//TODO: this remove will NEVER work. We are removing an Integer from an AbstractInterval set.
				backup.remove(lastResult);
				delegate = backup.tailSet(new MinimalisticInterval(lastResult)).iterator();
				curInterval = null;
			}
		};
	}

	@Override
	public boolean add(Integer i) {
		MinimalisticInterval mi = new MinimalisticInterval(i);
		SortedSet<AbstractInterval> tailSet = backup.tailSet(mi);

		AbstractInterval successor = null;
		AbstractInterval predecessor = null;
		
		if (!tailSet.isEmpty()) {
			successor = tailSet.first();
			if (successor.contains(i))
				return false;
		}
		
		SortedSet<AbstractInterval> headSet = backup.headSet(mi);

		if (!headSet.isEmpty()) {
			predecessor = headSet.last();
			if (predecessor.contains(i))
				return false;
		}
		
		int s = i;
		int e = i;
		
		if (predecessor != null && predecessor.getEnd() + 1 == i) {
			s = predecessor.getStart();
			backup.remove(predecessor);
		}
		
		if (successor != null && successor.getStart() - 1 == i) {
			e = successor.getEnd();
			backup.remove(successor);
		}
		
		AbstractInterval newInterval = createInterval(s, e);
		backup.add(newInterval);
		
		size++;

		return true;
	}
	
	protected AbstractInterval createInterval(int s, int e) {
		AbstractInterval newInterval = null;
		if (s == e) {
			newInterval = new MinimalisticInterval(s);
		}
		else {
			newInterval = new Interval(s, e);
		}
		return newInterval;
	}
	
	@Override
	public boolean remove(Object o) {
		if (!(o instanceof Integer))
			return false;
		Integer i = (Integer)o;

		MinimalisticInterval mi = new MinimalisticInterval(i);
		Iterator<AbstractInterval> sit = backup.tailSet(mi).iterator();

		AbstractInterval matchedInterval = null;
		
		if (sit.hasNext()) {
			AbstractInterval interval = sit.next();
			if (interval.contains(i)) {
				matchedInterval = interval;
				sit.remove();
			}
		}
		
		if (matchedInterval == null) {
			SortedSet<AbstractInterval> headSet = backup.headSet(mi);

			if (!headSet.isEmpty()) {
				AbstractInterval interval = headSet.last();
				if (interval.contains(i)) {
					matchedInterval = interval;
					backup.remove(matchedInterval);
				}
			}
		}
		
		if (matchedInterval != null) {
			backup.remove(matchedInterval);
			
			if (matchedInterval.getStart() < i) {
				AbstractInterval newInterval = createInterval(matchedInterval.getStart(), i - 1);
				backup.add(newInterval);
			}
			if (matchedInterval.getEnd() > i) {
				AbstractInterval newInterval = createInterval(i + 1, matchedInterval.getEnd());
				backup.add(newInterval);
			}
			size--;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void clear() {
		size = 0;
		backup.clear();
	}
	

	public static void main(String[] args) {
		/*
		Integer numbers[] = {1,2,3,4,5,6,7,8};
		
		IntervalSet set = new IntervalSet();
		
		set.addAll(Arrays.asList(numbers));
		set.remove(3);
		set.remove(4);
		set.remove(5);
		set.add(4);
		
		
		for (int i: set) {
			System.out.println(i);
		}
		*/
		
		
		
		Integer numbersList[][] = new Integer[][]{
				{1,2,3,4,5,6,7,8},
				{1,3,5,7},
				
				{1,10,20,22, 2, 9, 21},
				{1,3,11,13, 2, 12, 4, 14, 10},
				
		};
		
		for (Integer[] numbers: numbersList) {
			IntervalSet set = new IntervalSet();
			List<Integer> list = Arrays.asList(numbers);
			System.out.println(list);
			for (int number: list) {
				boolean res = set.add(number);
				System.out.print("adding " + number + " = " + res + " -> contains " + number + " " + set.contains(number));
				System.out.println(" -> resulting set " + set);
			}
			System.out.println(set.backup);
		}
		
		
	}
}