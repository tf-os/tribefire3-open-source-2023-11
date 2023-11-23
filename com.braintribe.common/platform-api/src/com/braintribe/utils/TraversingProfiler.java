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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author peter.gazdik
 */
public class TraversingProfiler {

	public static final TraversingProfiler MAIN_INSTANCE = new TraversingProfiler();

	private final ThreadLocal<ThreadSpecificProfiler> tlProfilers = ThreadLocal.withInitial(ThreadSpecificProfiler::new);

	public void onStart(Object key, String overallName) {
		profiler().onStart(key, overallName);
	}

	public void onIntermediate(Object key, String finishedPhaseName) {
		profiler().onIntermediate(key, finishedPhaseName);
	}

	public void onEnd(Object key) {
		onEnd(key, null);
	}

	public void onEnd(Object key, String lastPhaseName) {
		profiler().onEnd(key, lastPhaseName);
	}

	private ThreadSpecificProfiler profiler() {
		return tlProfilers.get();
	}

	private static class ThreadSpecificProfiler {

		/** For a given InstanceHolder multiple beans can be created, e.g. if the scope is prototype. Thus we keep track of all the */
		private final Map<Object, List<InstanceProfile>> instanceProfiles = new HashMap<>();
		private final Deque<InstanceProfile> stack = new ArrayDeque<>();

		public void onStart(Object instanceKey, String overallName) {
			InstanceProfile profile = new InstanceProfile(instanceKey, overallName);

			if (!stack.isEmpty())
				stack.peek().addChild(profile);

			stack.push(profile);

			instanceProfiles.computeIfAbsent(instanceKey, ih -> new ArrayList<>()).add(profile);
		}

		public void onIntermediate(Object key, String finishedPhaseName) {
			checkProfile(stack.peek(), key).onIntermediate(finishedPhaseName);
		}

		public void onEnd(Object key, String lastPhaseName) {
			checkProfile(stack.pop(), key).onEnd(lastPhaseName);
		}

		private InstanceProfile checkProfile(InstanceProfile profile, Object key) {
			if (profile.key != key)
				throw new IllegalStateException("Unexpected instance holder. Current one on stack is: " + profile.key
						+ ", but an actual listener event was called for: " + key);

			return profile;
		}

	}

	private static class InstanceProfile {

		public final Object key;
		public final String overallName;

		public final List<IntermediateEntry> entries = new ArrayList<>();

		private long totalTime = -1;
		private long netTime = -1;

		private IntermediateEntry currentEntry;

		public InstanceProfile(Object key, String overallName) {
			this.key = key;
			this.overallName = overallName;

			pushNewEntry();
		}

		public void addChild(InstanceProfile profile) {
			currentEntry.childProfiles.add(profile);
		}

		public void onIntermediate(String finishedPhaseName) {
			finishEntry(finishedPhaseName);
			pushNewEntry();
		}

		private void pushNewEntry() {
			entries.add(currentEntry = new IntermediateEntry());
		}

		private void finishEntry(String finishedPhaseName) {
			currentEntry.name = finishedPhaseName;
		}

		public void onEnd(String lastPhaseName) {
			onIntermediate(lastPhaseName);

			Iterator<IntermediateEntry> it = entries.iterator();

			IntermediateEntry first = it.next();
			IntermediateEntry prev = first;

			do {
				IntermediateEntry next = it.next();
				prev.totalTime = next.start - prev.start;
				prev.netTime = prev.totalTime - sumTotalTime(prev.childProfiles);

				prev = next;

			} while (it.hasNext());

			totalTime = prev.start - first.start;
			netTime = entries.stream().mapToLong(e -> e.netTime).sum();

			System.out.println("[PF] " + overallName + ": in " + inMs(netTime) + printIntermediates());
		}

		private String printIntermediates() {
			StringJoiner sj = new StringJoiner(", ", "(", ")");

			for (IntermediateEntry ie : entries) {
				if (ie.name == null)
					break;

				sj.add(ie.name + ": " + inMs(ie.netTime));
			}

			return sj.length() > 2 ? sj.toString() : "";
		}

		private long sumTotalTime(List<InstanceProfile> children) {
			return children.stream().mapToLong(ip -> ip.totalTime).sum();
		}

	}

	public static String msSince(long nanoStart) {
		return inMs(System.nanoTime() - nanoStart);
	}

	public static String inMs(long nano) {
		long micro = nano / 1000;

		long milli = micro / 1000;
		long micor_part = micro % 1000;

		return milli + "." + micor_part;
	}

	private static class IntermediateEntry {
		public String name;
		public long start = System.nanoTime();

		public long totalTime = -1;
		public long netTime = -1;

		public final List<InstanceProfile> childProfiles = new ArrayList<>();
	}

	// ##########################################
	// ## . . . . . . . . Demo . . . . . . . . ##
	// ##########################################

	// public static void main(String[] args) throws Exception {
	// for (int i = 0; i < 5; i++)
	// main();
	// }
	//
	// private static void main() throws Exception {
	// System.out.println("\n\n");
	//
	// TraversingProfiler profiler = new TraversingProfiler();
	//
	// new HashMap<>().put(String.class, "d");
	// new ArrayDeque<>().push(Integer.class);
	//
	// profiler.onStart(String.class, "string");
	// Thread.sleep(10); // Note that each sleep might add up to 10 ms of delay
	// {
	// profiler.onStart(Integer.class, "integer");
	// // long start = System.nanoTime();
	// Thread.sleep(50);
	// // System.out.println("Sleep: " + inMs(System.nanoTime() - start));
	//
	// profiler.onEnd(Integer.class);
	// }
	// profiler.onIntermediate(String.class, "first");
	// Thread.sleep(300);
	// profiler.onEnd(String.class);
	//
	// System.out.print("DONE!");
	// }

}
