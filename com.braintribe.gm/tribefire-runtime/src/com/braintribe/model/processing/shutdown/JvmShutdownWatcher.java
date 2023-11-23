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
package com.braintribe.model.processing.shutdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.utils.lcd.StringTools;

public class JvmShutdownWatcher {

	private static Logger logger = Logger.getLogger(JvmShutdownWatcher.class);

	private final static String TIMEOUT_PROPERTY = "TRIBEFIRE_SHUTDOWN_TIMEOUT";
	private final static String INTERVAL_PROPERTY = "TRIBEFIRE_SHUTDOWN_WATCH_INTERVAL";
	private final static String HARD_TIMEOUT_PROPERTY = "TRIBEFIRE_SHUTDOWN_TIMEOUT_HARD";
	private final static String IGNORE_THREADS_PROPERTY = "TRIBEFIRE_SHUTDOWN_IGNORE";

	private static Set<Pattern> threadsToIgnore = Collections.synchronizedSet(new HashSet<>());
	private long timeout = 3_000L;
	private long interval = 1_000L;
	private long lastChancePeriod = 1_000L;
	private long hardLimit = -1L;

	private boolean terminate = false;

	public JvmShutdownWatcher() {
		String timeoutString = TribefireRuntime.getProperty(TIMEOUT_PROPERTY);
		if (!StringTools.isBlank(timeoutString)) {
			try {
				timeout = Long.parseLong(timeoutString);
			} catch (NumberFormatException nfe) {
				logger.error("Could not parse " + TIMEOUT_PROPERTY + ": " + timeoutString, nfe);
			}
		}

		String hardTimeoutString = TribefireRuntime.getProperty(HARD_TIMEOUT_PROPERTY);
		if (!StringTools.isBlank(hardTimeoutString)) {
			try {
				hardLimit = Long.parseLong(hardTimeoutString);
			} catch (NumberFormatException nfe) {
				logger.error("Could not parse " + HARD_TIMEOUT_PROPERTY + ": " + hardTimeoutString, nfe);
			}
		}

		String intervalString = TribefireRuntime.getProperty(INTERVAL_PROPERTY);
		if (!StringTools.isBlank(intervalString)) {
			try {
				interval = Long.parseLong(intervalString);
			} catch (NumberFormatException nfe) {
				logger.error("Could not parse " + INTERVAL_PROPERTY + ": " + intervalString, nfe);
			}
		}

		String ignoreString = TribefireRuntime.getProperty(IGNORE_THREADS_PROPERTY);
		if (!StringTools.isBlank(ignoreString)) {
			addThreadToIgnore(ignoreString);
		} else {
			addThreadToIgnore("(?i)destroy.*");
			addThreadToIgnore("(?i)awt-shutdown.*");
		}

	}

	public static void addThreadToIgnore(String pattern) {
		try {
			Pattern p = Pattern.compile(pattern);
			threadsToIgnore.add(p);
		} catch (Exception e) {
			logger.warn(() -> "Could not parse pattern " + pattern, e);
		}
	}

	public void startShutdownWatch() {

		if (interval <= 0 || hardLimit <= 0) {
			log(LogLevel.DEBUG, "The watch interval is " + interval + " and the hard limit " + hardLimit + ". The shutdown will not be monitored.");
			return;
		}

		Thread t = new Thread(this::enforceTermination);
		t.setName("Shutdown Termination");
		t.setDaemon(true);
		t.start();
	}

	private void enforceTermination() {

		long start = System.currentTimeMillis();

		if (!waitInitialGracePeriod()) {
			return;
		}

		while (true) {
			monitorThreads(start);

			if (terminate) {
				break;
			}

			try {
				Thread.sleep(interval);
			} catch (InterruptedException ie) {
				log(LogLevel.DEBUG, "Got interrupted while monitoring termination signal. Will stop watching now.");
				break;
			}
		}

		log(LogLevel.INFO, "Shutting down now.");
		Runtime.getRuntime().halt(0);
	}

	private boolean waitInitialGracePeriod() {
		// Let's wait for the grace period. Only start monitoring threads if the JVM is still alive after that
		try {
			Thread.sleep(timeout);
			return true;
		} catch (InterruptedException e) {
			log(LogLevel.DEBUG, "Got interrupted while waiting for shutdown. Will stop watching now.");
			return false;
		}
	}

	private void monitorThreads(long start) {

		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();

		List<String> daemonThreads = new ArrayList<>(allStackTraces.size());
		List<String> ignoredNonDaemonThreads = new ArrayList<>(allStackTraces.size());
		List<String> vitalNonDaemonThreads = new ArrayList<>(allStackTraces.size());

		for (Map.Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {

			Thread t = entry.getKey();

			if (t.isDaemon()) {
				daemonThreads.add(t.getName());
			} else {
				if (ignoreThread(t)) {
					ignoredNonDaemonThreads.add(t.getName());
				} else {
					vitalNonDaemonThreads.add(t.getName());
				}
			}
		}

		log(LogLevel.DEBUG, "Daemon Threads: " + daemonThreads);
		log(LogLevel.DEBUG, "Ignored Threads: " + ignoredNonDaemonThreads);
		log(LogLevel.DEBUG, "Vital Threads: " + vitalNonDaemonThreads);

		if (vitalNonDaemonThreads.isEmpty()) {

			try {
				Thread.sleep(lastChancePeriod);
			} catch (InterruptedException ie) {
				log(LogLevel.DEBUG, "Got interrupted while monitoring threads. Will stop watching now.");
				return;
			}

			log(LogLevel.INFO, "Signalling the shutdown");
			log(LogLevel.INFO, "Daemon Threads: " + daemonThreads);
			log(LogLevel.INFO, "Ignored Threads: " + ignoredNonDaemonThreads);
			log(LogLevel.INFO, "No vital threads found.");

			terminate = true;

		} else {

			log(LogLevel.INFO, "Shutdown postponed");
			log(LogLevel.INFO, "Daemon Threads: " + daemonThreads);
			log(LogLevel.INFO, "Ignored Threads: " + ignoredNonDaemonThreads);
			log(LogLevel.INFO, "Vital Threads: " + vitalNonDaemonThreads);

		}

		long now = System.currentTimeMillis();
		if (hardLimit > 0 && (now - start) > hardLimit) {
			log(LogLevel.INFO, "Hard limit reached. Giving the signal to terminate now.");

			terminate = true;
		}
	}

	/* Need to duplicate the output to stdout/stderr because the logging might have been shutdown already */
	private static void log(LogLevel level, String message) {
		if (logger.isLevelEnabled(level)) {
			logger.log(level, message);
		}

		switch (level) {
			case ERROR:
				System.err.println(message);
				break;
			case WARN:
			case INFO:
				System.out.println(message);
				break;
			default:
				// Ignore
				break;

		}
	}

	private boolean ignoreThread(Thread t) {
		if (!t.isAlive()) {
			return true;
		}
		String threadName = t.getName();
		if (threadName == null) {
			return false;
		}
		for (Pattern p : threadsToIgnore) {
			Matcher matcher = p.matcher(threadName);
			if (matcher.matches()) {
				return true;
			}
		}
		return false;
	}
}
