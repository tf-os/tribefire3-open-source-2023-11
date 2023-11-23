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
package com.braintribe.transport.http.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;

public class AccessStatisticsOnConnections {

	private static final Logger logger = Logger.getLogger(AccessStatisticsOnConnections.class);

	protected Map<Integer, String> leasedObjectsToStackMap = new HashMap<>();
	protected final ReentrantLock leasedObjectsMapLock = new ReentrantLock();

	protected Map<String, Integer> allObjectsToStackMap = new HashMap<>();
	protected final ReentrantLock allObjectsMapLock = new ReentrantLock();

	protected Logger delegateLogger;
	protected boolean verbose = true;
	protected String objectType = "";
	protected long lastOutput = 0;
	protected final static long OUTPUT_INTERVAL = 1000 * 60 * 20; // Output every 20 minutes

	public AccessStatisticsOnConnections(Logger logger, boolean verbose, String objectType) {
		this.delegateLogger = logger;
		this.verbose = verbose;
		this.objectType = objectType;
	}

	public void registerAccess(Object o) {
		if (o == null) {
			return;
		}
		String stack = getStackAsString();
		leasedObjectsMapLock.lock();
		try {
			this.leasedObjectsToStackMap.put(o.hashCode(), stack);
		} finally {
			leasedObjectsMapLock.unlock();
		}

		allObjectsMapLock.lock();
		try {
			Integer count = allObjectsToStackMap.get(stack);
			if (count == null) {
				count = Integer.valueOf(1);
			} else {
				count = count + 1;
			}
			allObjectsToStackMap.put(stack, count);
		} finally {
			allObjectsMapLock.unlock();
		}
	}

	public void registerDispose(Object o) {
		if (o == null) {
			return;
		}
		leasedObjectsMapLock.lock();
		try {
			this.leasedObjectsToStackMap.remove(o.hashCode());
		} finally {
			leasedObjectsMapLock.unlock();
		}
	}

	public String getConnectionStats() {
		StringBuilder sb = new StringBuilder();

		leasedObjectsMapLock.lock();
		try {
			Map<String, Integer> stackCount = new HashMap<String, Integer>();
			for (String stack : this.leasedObjectsToStackMap.values()) {
				Integer count = stackCount.get(stack);
				if (count == null) {
					count = Integer.valueOf(1);
				} else {
					count = Integer.valueOf(count.intValue() + 1);
				}
				stackCount.put(stack, count);
			}

			for (Map.Entry<String, Integer> entry : stackCount.entrySet()) {
				sb.append("Current leases of type " + this.objectType + ": " + entry.getValue() + " times:\n" + entry.getKey() + "\n");
			}

		} catch (Exception e) {
			logger.debug("Error while printing access statistics on objects.", e);
		} finally {
			leasedObjectsMapLock.unlock();
		}

		sb.append("\n\n");

		leasedObjectsMapLock.lock();
		try {
			TreeMap<Integer, List<String>> sortedMap = new TreeMap<>();
			for (Map.Entry<String, Integer> entry : allObjectsToStackMap.entrySet()) {
				Integer count = entry.getValue();
				List<String> list = sortedMap.get(count);
				if (list == null) {
					list = new ArrayList<>();
					sortedMap.put(count, list);
				}
				list.add(entry.getKey());
			}
			for (Map.Entry<Integer, List<String>> entry : sortedMap.entrySet()) {
				sb.append("Accessed " + entry.getKey() + " times: ");
				for (String e : entry.getValue()) {
					sb.append(e);
					sb.append("\n");
				}
			}
		} finally {
			leasedObjectsMapLock.unlock();
		}

		return sb.toString();
	}

	public void printConnectionStats(boolean force) {
		if (!delegateLogger.isTraceEnabled() && !force) {
			return;
		}
		if (force) {
			printConnectionStats(force, LogLevel.WARN);
		} else {
			printConnectionStats(force, LogLevel.TRACE);
		}
	}

	public void printConnectionStats(boolean force, LogLevel level) {
		boolean show = force;
		long now = (new Date()).getTime();
		if (!force) {
			show = ((now - this.lastOutput) > OUTPUT_INTERVAL);
		}
		if (show) {
			this.lastOutput = now;

			delegateLogger.log(level, this.getConnectionStats());

		}
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public static String getStackAsString() {

		try {
			Exception e = new Exception();
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			PrintStream p = new PrintStream(b);
			e.printStackTrace(p);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(b.toByteArray())));
			String s = null;
			int i = 0;
			int idx = -1;
			StringBuilder sb = new StringBuilder();

			sb.append("---------------------\n");
			// The first 3 lines should be discarded, and we only want the 4th.
			while ((s = reader.readLine()) != null) {
				if (++i > 2) {
					idx = s.indexOf("at ");
					if (idx != -1) {
						s = s.substring(idx + "at ".length());
					}
					sb.append("called from ");
					sb.append(s);
					sb.append("\n");
				}

			}
			sb.append("---------------------\n");

			return sb.toString();

		} catch (Exception e) {
			logger.error("Could not get current stack trace", e);
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		(new Exception()).printStackTrace(pw);
		return sw.toString();
	}
}
