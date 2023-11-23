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
package com.braintribe.utils;

import static com.braintribe.utils.SysPrint.spOut;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;

import java.math.BigDecimal;
import java.util.Map;

import com.braintribe.utils.lcd.StopWatch;

/**
 * Debug (profiling) utility class for measuring computation time. Note that due to it's static nature it is only suitable for a single-threaded
 * environment.
 *
 * DISCLAIMER: This class is really meant for debugging only, and no usage is expected in our codebase (meaning code using this should not be
 * committed). This code is not very mature and might change, so let's avoid compilation problems.
 * <p>
 * It is capable of keeping track of various measurements concurrently as every measurement has it's own identifier.
 *
 * Example:
 *
 * <pre>
 *  void someMethod() {
 *  	TimeTracker.clear();
 *  	TimeTracker.start("entireMethod")
 *
 *  	TimeTracker.start("parsing");
 *  	Data data = parser.parse(inputFile);
 *  	TimeTracker.stopAndPrint("parsing");
 *
 *  	TimeTracker.start("processing");
 *  	process(data);
 *  	TimeTracker.stopAndPrint("processing");
 *
 *  	TimeTracker.stopAndPrint("entireMethod")
 *  }
 *
 *  Expected Output (short of format accuracy):
 *
 *  	(SomClass.java:35) [parsing] 1.000000)
 *  	(SomClass.java:39) [processing] 2.500000)
 *  	(SomClass.java:41) [entireMethod] 3.500001)
 * </pre>
 *
 * Comment on placement in this package: I use this functionality every now and then so I wanted to store this class somewhere. I initially chose the
 * "lcd" sub-package, as it contains a similar-purpose class - {@link StopWatch}, but then moved it here when stared using {@link SysPrint}. If you
 * however think this should be moved elsewhere, feel free to do it, but please contact me about it if possible.
 *
 * @author peter.gazdik
 */
public class TimeTracker {

	private static Map<String, TimeEntry> entries = newLinkedMap();

	public static void clear() {
		entries.clear();
	}

	public static void clear(String id) {
		entries.remove(id);
	}

	public static void startNew(String id) {
		clear(id);
		_start(id);
	}

	public static void start(String id) {
		_start(id);
	}

	/** This exists so that the stack contains same number of extra frames for both {@link #start(String)} and {@link #startNew(String)} methods. */
	private static void _start(String id) {
		TimeEntry te = entries.get(id);

		if (te == null) {
			te = new TimeEntry();
			te.startLocation = SysPrint.callerLocation(2);
			entries.put(id, te);
		}

		te.start();
	}

	public static void stopAndPrint(String id) {
		stop(id);
		print(2, id, null);
	}

	public static void stopAndPrint(String id, String extraInfo) {
		stop(id);
		print(2, id, extraInfo);
	}

	public static long stop(String id) {
		TimeEntry entry = entries.get(id);
		return entry != null ? entry.stop() : 0L;
	}

	/**
	 * Prints a report consisting of all entries.
	 * <p>
	 * The location for each entry is the location where the first start was invoked for that entry.
	 */
	public static void printAll() {
		spOut(1, "TIMER ENTRIES:\n--");
		for (String entryName : entries.keySet())
			print(-1, entryName, null);
	}

	public static void print(String id) {
		print(2, id, null);
	}

	public static void print(String id, String extraInfo) {
		print(2, id, extraInfo);
	}

	private static void print(int stackShift, String id, String extraInfo) {
		TimeEntry entry = entries.get(id);

		String time = entryTime(entry);

		String location = stackShift > 0 ? SysPrint.callerLocation(stackShift) : entry.startLocation;

		System.out.println(location + "[" + id + "] " + time + (StringTools.isEmpty(extraInfo) ? "" : " " + extraInfo));
	}

	private static String entryTime(TimeEntry entry) {
		if (entry == null) {
			return "-- NO ENTRY --";
		}

		String countInfo = entry.count > 1 ? " (" + entry.count + "x)" : "";
		return "" + format(entry.duration) + countInfo;
	}

	public static String format(long durationInNano) {
		BigDecimal bd = new BigDecimal(durationInNano);
		bd = bd.divide(new BigDecimal(1000l * 1000));

		BigDecimal THOUSAND = new BigDecimal(1000l);

		if (bd.compareTo(THOUSAND) < 0) {
			return "" + bd + " ms";
		}

		bd = bd.divide(THOUSAND);
		return "" + bd + " s";
	}

	public static long getDurationInNano(String id) {
		return entries.get(id).duration;
	}

	static class TimeEntry {

		int count = 0;
		long start;
		long duration;
		String startLocation;

		public void start() {
			start = System.nanoTime();
		}

		public long stop() {
			count++;
			duration += System.nanoTime() - start;
			return duration;
		}

	}

}
