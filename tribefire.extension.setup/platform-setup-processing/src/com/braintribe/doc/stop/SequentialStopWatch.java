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

/**
 * 
 * @author Neidhart.Orlich
 *
 */
public class SequentialStopWatch extends AbstractStopWatch {
	
	private AbstractStopWatch currentSubWatch;
	private TimeMeasure unattributedTimer;
	
	public SequentialStopWatch(MultiStopWatch multiStopWatch, TimeMeasure timeMeasure) {
		super(multiStopWatch, timeMeasure);
		unattributedTimer = multiStopWatch.startUnattributedTime();
	}
	
	public static SequentialStopWatch newTimer(Object key) {
		MultiStopWatch multiStopWatch = new MultiStopWatch(key);
		SequentialStopWatch linearStopWatch = multiStopWatch.start();
		return linearStopWatch;
	}
	
	public static void forceJIT() {
		SequentialStopWatch timer = newTimer("");
		timer.start("");
		timer.stop("");
		ParallelStopWatch parallel = timer.startParallel("p");
		
		SequentialStopWatch p1 = parallel.hatch("");
		parallel.terminate(p1);
		
		timer.stop("p");
		summarize(timer);
	}
	
	public static String summarize(SequentialStopWatch linearStopWatch) {
		if (linearStopWatch.timeMeasure.isRunning())
			linearStopWatch.stop();
		return linearStopWatch.multiStopWatch.summary();
	}
	
	@Override
	protected void stop() {
		if (hasSubWatchRunning())
			throw new IllegalStateException("Cant't stop " + multiStopWatch.getKey() + " as subwatch " + currentSubKey() + " is still running.");
		
		timeMeasure.stop();
		unattributedTimer.stop();
	}
	
	private Object currentSubKey() {
		if (hasSubWatchRunning())
			return currentSubWatch.multiStopWatch.getKey();
		
		return null;
	}
	
	public SequentialStopWatch start(Object key) {
		if (hasSubWatchRunning()) {
			throw new IllegalStateException("Can't start a new subtimer for key " + key + " because " + currentSubKey() + " is still running.");
		}
		
		unattributedTimer.stop();
		unattributedTimer = null;
		
		SequentialStopWatch linearStopWatch = multiStopWatch.start(key);
		currentSubWatch = linearStopWatch;
		
		return linearStopWatch;
	}
	
	public ParallelStopWatch startParallel(Object key) {
		if (hasSubWatchRunning()) {
			throw new IllegalStateException("Can't start a new subtimer for key " + key + " because " + currentSubKey() + " is still running.");
		}
		
		unattributedTimer.stop();
		unattributedTimer = null;
		
		ParallelStopWatch linearStopWatch = multiStopWatch.startParallel(key);
		currentSubWatch = linearStopWatch;
		
		return linearStopWatch;
	}
	
	public void stop(Object key) {
		if (!hasSubWatchRunning()) {
			throw new IllegalStateException("Can't stop subtimer for key " + key + " because currently there is no subtimmer running.");
		}
		
		Object currentSubKey = currentSubKey();
		
		if (currentSubKey == key)
			currentSubWatch.stop();
		else
			throw new IllegalStateException("Current subtimer has key " + currentSubKey + " but you tried to stop " + key);
		
		currentSubWatch = null;
		unattributedTimer = multiStopWatch.startUnattributedTime();
	}
	
	@Override
	public boolean hasSubWatchRunning() {
		return currentSubWatch != null;
	}

}