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

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * 
 * @author Neidhart.Orlich
 *
 */
public class ParallelStopWatch extends AbstractStopWatch {
	private final Collection<AbstractStopWatch> currentSubWatches = new HashSet<>();
	
	public ParallelStopWatch(MultiStopWatch multiStopWatch, TimeMeasure timeMeasure) {
		super(multiStopWatch, timeMeasure);
	}
	
	@Override
	synchronized protected void stop() {
		if (hasSubWatchRunning())
			throw new IllegalStateException("Cant't stop " + multiStopWatch.getKey() + " as subtimers " + currentSubKeys() + " are still running.");
		
		timeMeasure.stop();
	}
	
	private Collection<Object> currentSubKeys() {
		return currentSubWatches.stream()//
				.map(w -> w.multiStopWatch.getKey()) //
				.collect(Collectors.toList());
	}
	
	synchronized public SequentialStopWatch hatch(Object key) {
		if (!isRunning()) {
			throw new IllegalStateException("Can't start a new timer (" + key + ") in " + multiStopWatch.getKey() + " because it is already stopped.");
		}
		SequentialStopWatch linearStopWatch = multiStopWatch.start(key);
		currentSubWatches.add(linearStopWatch);
		
		return linearStopWatch;
	}
	
	synchronized public void terminate(AbstractStopWatch stopWatch) {
		Object key = stopWatch.multiStopWatch.getKey();
		if (!currentSubWatches.contains(stopWatch)) {
			throw new IllegalStateException("Can't stop subtimer for key " + key + " because it is not running as subtimer of this stop watch.");
		}
			
		stopWatch.stop();
		currentSubWatches.remove(stopWatch);
	}
	
	@Override
	synchronized public boolean hasSubWatchRunning() {
		return !currentSubWatches.isEmpty();
	}
}