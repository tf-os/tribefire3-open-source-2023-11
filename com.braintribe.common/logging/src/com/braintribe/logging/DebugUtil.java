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
package com.braintribe.logging;

import java.awt.Component;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.SwingUtilities;

public class DebugUtil {

	private static final Logger logger = Logger.getLogger(DebugUtil.class);

	protected static int debugDelay = 0;
	protected static boolean profiling = false;
	protected static long startTime = System.currentTimeMillis();

	public static int getDebugDelay() {
		return debugDelay;
	}

	public static void setDebugDelay(final int debugDelay) {
		DebugUtil.debugDelay = debugDelay;
	}

	public static boolean isProfiling() {
		return profiling;
	}

	public static void setProfiling(final boolean profiling) {
		DebugUtil.profiling = profiling;
	}

	public static void debugDelay(final int ticks) {
		assertNotAwt("debugDelay()");

		if (ticks == 0) {
			return;
		}
		if (debugDelay <= 0) {
			return;
		}

		try {
			Thread.sleep(debugDelay * ticks);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public static void assertInAwt(final String what) {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new RuntimeException("Must only be called in AWT thread: " + what);
		}
	}

	public static void assertNotAwt(final String what) {
		if (SwingUtilities.isEventDispatchThread()) {
			/* DEACTIVATED DUE TO SYNCHRONIZATION PROBLEMS throw new
			 * RuntimeException("Must not be called in AWT thread: "+what); */
			logger.warn(what + " should not be called in AWT");
		}
	}

	public static long profileIn(@SuppressWarnings("unused") final String name) {
		if (!profiling) {
			return 0;
		}

		final long t = System.currentTimeMillis();

		return t;
	}

	public static void profileOut(final String name, final long inTime) {
		if (!profiling || inTime <= 0) {
			return;
		}

		final long t = System.currentTimeMillis() - inTime;

		logger.debug("PROFILING: " + name + ": " + t + " ms");
	}

	public static void profileTimeSinceStart(final String name) {
		profileOut("TOTAL TIME TO: " + name, startTime);
	}

	public static String debugName(final Object obj) {
		if (obj == null) {
			return "<null>";
		}
		if (obj instanceof Component) {
			return obj.getClass().getName() + "#" + System.identityHashCode(obj);
		}
		return obj.toString();
	}

	public static void dumpClassPath(ClassLoader cl, final PrintStream out) {
		while (cl != null) {
			out.println(cl.getClass().getName());

			if (cl instanceof URLClassLoader) {
				final URLClassLoader ucl = (URLClassLoader) cl;
				final URL[] urls = ucl.getURLs();

				for (int i = 0; i < urls.length; i++) {
					System.out.println("\t" + urls[i]);
				}
			}

			cl = cl.getParent();
		}
	}

	public static void showStackTrace() {
		final Throwable t = new Throwable();
		final StackTraceElement stacktrace[] = t.getStackTrace();
		System.out.println("#### Stacktrace");
		int index = 0;
		for (final StackTraceElement traceElement : stacktrace) {
			if (index > 0) {
				System.out.println(traceElement);
			}
			index++;
		}
	}

}
