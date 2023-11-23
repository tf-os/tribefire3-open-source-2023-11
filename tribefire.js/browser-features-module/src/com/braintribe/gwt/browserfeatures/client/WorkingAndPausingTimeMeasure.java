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
package com.braintribe.gwt.browserfeatures.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;

public class WorkingAndPausingTimeMeasure {
	private static WorkingAndPausingTimeMeasure instance = new WorkingAndPausingTimeMeasure();
	//private boolean pause;
	private long start = System.currentTimeMillis();
	/*private long retrospectionFrameSize = 2000;
	private long pauseInFrame;
	private long workInFrame;*/
	
	public static WorkingAndPausingTimeMeasure getInstance() {
		return instance;
	}
	
	private RepeatingCommand entryCommand = new RepeatingCommand() {
		
		@Override
		public boolean execute() {
			start = System.currentTimeMillis();
			return true;
		}
	};
	
	private RepeatingCommand finallyCommand = new RepeatingCommand() {
		
		@Override
		public boolean execute() {
			//long end = System.currentTimeMillis();
			//long delta = end - start;
			//TODO: what is the point here?
			return true;
		}
	};
	
	private WorkingAndPausingTimeMeasure() {
		Scheduler.get().scheduleEntry(entryCommand);
		Scheduler.get().scheduleFinally(finallyCommand);
	}
	
	public long getMilliesSinceEntry() {
		return System.currentTimeMillis() - start;
	}
	
	public boolean isRunningLongerThan(long ms) {
		return getMilliesSinceEntry() > ms;
	}
	
	public LoopBreakerCheck createLoopBreakerCheck(long maxTime, long doCheckOnlyNthTime) {
		return new LoopBreakerCheck(maxTime, doCheckOnlyNthTime);
	}
	
	public class LoopBreakerCheck {
		private long maxTime;
		private long doCheckOnlyNthTime;
		private int counter = 0;
		
		public LoopBreakerCheck(long maxTime, long doCheckOnlyNthTime) {
			super();
			this.maxTime = maxTime;
			this.doCheckOnlyNthTime = doCheckOnlyNthTime;
		}

		public boolean shouldBreak() {
			counter++;
			if (counter == doCheckOnlyNthTime) {
				counter = 0;
				return isRunningLongerThan(maxTime);
			}
			else
				return false;
		}
	}

}
