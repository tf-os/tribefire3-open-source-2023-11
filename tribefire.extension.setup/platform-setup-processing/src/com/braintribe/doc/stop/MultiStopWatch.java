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
package com.braintribe.doc.stop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MultiStopWatch {
	private final Map<Object, MultiStopWatch> timers = new ConcurrentHashMap<>();
	private final Object key;
	private final Collection<TimeMeasure> unattributedTime = new ConcurrentlyGrowableList<>();
	private final Collection<TimeMeasure> overallTime = new ConcurrentlyGrowableList<>();
	private final Set<TimeMeasure> runningTimers = ConcurrentHashMap.newKeySet();
	
	
	public MultiStopWatch(Object key) {
		this.key = key;
	}
	
	public SequentialStopWatch start() {
		TimeMeasure timeMeasure = new TimeMeasure(this);
		
		runningTimers.add(timeMeasure);
		overallTime.add(timeMeasure);
		
		return new SequentialStopWatch(this, timeMeasure);
	}
	
	public ParallelStopWatch startParallel() {
		TimeMeasure timeMeasure = new TimeMeasure(this);
		
		runningTimers.add(timeMeasure);
		overallTime.add(timeMeasure);
		
		return new ParallelStopWatch(this, timeMeasure);
	}
	
	private static long sumTime(Collection<TimeMeasure> timeMeasures) {
		return timeMeasures.stream() //
				.collect(Collectors.summingLong(TimeMeasure::getElapsedTime));
	}
	
	public TimeMeasure startUnattributedTime() {
		TimeMeasure timeMeasure = new TimeMeasure(this);
		unattributedTime.add(timeMeasure);
		runningTimers.add(timeMeasure);
		
		return timeMeasure;
	}
	
	public void returnTimeMeasure(TimeMeasure timeMeasure) {
		runningTimers.remove(timeMeasure);
	}
	
	public SequentialStopWatch start(Object key) {
		MultiStopWatch multiStopWatch = timers.computeIfAbsent(key, MultiStopWatch::new);
		return multiStopWatch.start();
	}
	
	public ParallelStopWatch startParallel(Object key) {
		MultiStopWatch multiStopWatch = timers.computeIfAbsent(key, MultiStopWatch::new);
		return multiStopWatch.startParallel();
	}
	
	/*public void stop(Object subKey) {
		MultiStopWatch currentLinearWatch = timers.get(subKey);
		if (currentLinearWatch == null) {
			throw new IllegalStateException("Can't stop subtimer with key " + subKey + " because it was never registered.");
		}
		
		currentLinearWatch.stop();
		currentLinearWatch = null;
	}*/
	
	public Object getKey() {
		return key;
	}

	public Collection<MultiStopWatch> getSubTimers() {
		return timers.values();
	}
	
	public boolean isRunning() {
		return !runningTimers.isEmpty();
	}
	
	public long getElapsedTime() {
		return sumTime(overallTime);
	}
	
	public void stop() {
		if (!isRunning()) {
			throw new IllegalStateException("Can't stop timer for " + key + " because it was not running.");
		}
		
		timers.forEach((k,v) -> {
			if (v.isRunning()) {
				throw new IllegalStateException("Timer for " + k + " was still running.");
			}
		});
		
	}

	public Map<Object, MultiStopWatch> getTimers() {
		return timers;
	}
	
	private static void newline(StringBuilder s, int indention) {
		s.append('\n');
		for (int i=0; i<indention; i++) {
			s.append(' ');
		}
	}
	
	public String summary() {
		if (isRunning()) {
//			stop();
			throw new RuntimeException("Can't summarize MultiStopWatch when it is still running");
		}
		return summarize(0);
	}
	
	private static String millis(long nanos) {
		return nanos/1000000 + "ms";
	}

	private String summarize(int indent) {
		StringBuilder summaryBuilder = new StringBuilder();
		
		if (indent > 0) {
			summaryBuilder.append(executionCount() + "x ");
		}
		summaryBuilder.append("[" + getKey().toString() + "]: ");
		
		if (!getSubTimers().isEmpty()) {
			long totalMeasuredTime = 0;
			
			for (MultiStopWatch stopWatch : getSubTimers()) {
				newline(summaryBuilder, indent);
				summaryBuilder.append(stopWatch.summarize(indent + 2));
				totalMeasuredTime += stopWatch.getElapsedTime();
			}
			
			newline(summaryBuilder, indent);
			summaryBuilder.append("==================");
			newline(summaryBuilder, indent);
			summaryBuilder.append("Together:\t" + millis(totalMeasuredTime));
			if (!unattributedTime.isEmpty()) {
				newline(summaryBuilder, indent);
				summaryBuilder.append("Unattributed:\t" + millis(sumTime(unattributedTime)));
			}
			newline(summaryBuilder, indent);
			summaryBuilder.append("Total:\t");
		}
		
		summaryBuilder.append(millis(getElapsedTime()));
		
		return summaryBuilder.toString();
	}

	private int executionCount() {
		return overallTime.size();
	}

}

abstract class AbstractStopWatch {
	protected final MultiStopWatch multiStopWatch;
	protected final TimeMeasure timeMeasure;
	
	public AbstractStopWatch(MultiStopWatch multiStopWatch, TimeMeasure timeMeasure) {
		this.multiStopWatch = multiStopWatch;
		this.timeMeasure = timeMeasure;
	}
	
	
	protected abstract void stop();
	protected abstract boolean hasSubWatchRunning();
	
	protected boolean isRunning() {
		return timeMeasure.isRunning();
	}
}

class TimeMeasure {
	private final long startNanos = System.nanoTime();
	private long endNanos = -1;
	private final MultiStopWatch multiStopWatch;
	
	public TimeMeasure(MultiStopWatch multiStopWatch) {
		this.multiStopWatch = multiStopWatch;
	}
	
	public long getElapsedTime() {
		if (isRunning()) {
			System.out.println("Timer for " + multiStopWatch.getKey() + " is still running ");
		}
		return endNanos - startNanos;
	}

	public boolean isRunning() {
		return endNanos == -1;
	}

	public void stop() {
		if (!isRunning()) {
			throw new RuntimeException("There was an attempt to stop a TimeMeasure multiple times for key " + multiStopWatch.getKey());
		}
		endNanos = System.nanoTime();
		multiStopWatch.returnTimeMeasure(this);
	}
}

class ConcurrentlyGrowableList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 1L;

	@Override
	synchronized public boolean add(E e) {
		return super.add(e);
	}
}
