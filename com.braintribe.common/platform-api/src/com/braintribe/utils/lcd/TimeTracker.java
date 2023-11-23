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
package com.braintribe.utils.lcd;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @deprecated Use {@link com.braintribe.utils.TimeTracker} i.e. the one outside of lcd package.
 */
@Deprecated
public class TimeTracker {

	private static Map<String, TimeEntry> entries = newMap();

	public static void clear() {
		entries.clear();
	}

	public static void clear(String id) {
		entries.remove(id);
	}

	public static void startNew(String id) {
		clear(id);
		start(id);
	}

	public static void start(String id) {
		TimeEntry te = entries.get(id);

		if (te == null) {
			te = new TimeEntry();
			entries.put(id, te);
		}

		te.start();
	}

	public static void stopAndPrint(String id) {
		stop(id);
		print(2, id);
	}

	public static long stop(String id) {
		TimeEntry entry = entries.get(id);
		return entry != null ? entry.stop() : 0L;
	}

	public static void print(String id) {
		print(2, id);
	}

	private static void print(@SuppressWarnings("unused") int stackShift, String id) {
		TimeEntry entry = entries.get(id);

		if (entry == null) {
			System.out.println("[" + id + "] -- NO ENTRY --");
			return;
		}

		String countInfo = entry.count > 1 ? " (" + entry.count + "x)" : "";
		System.out.println("[" + id + "] " + format(entry.duration) + countInfo);
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

		public void start() {
			start = System.currentTimeMillis();
		}

		public long stop() {
			count++;
			duration += System.currentTimeMillis() - start;
			return duration;
		}

	}

}
