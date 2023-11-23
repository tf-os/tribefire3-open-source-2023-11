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
package tribefire.cortex.testing.junit.impl;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

import com.braintribe.logging.Logger;

/**
 * @author peter.gazdik
 */
public class LoggingRunListener extends RunListener {

	private static final Logger log = Logger.getLogger(LoggingRunListener.class);

	ThreadLocal<Long> testStart = new ThreadLocal<>();

	@Override
	public void testStarted(Description description) throws Exception {
		testStart.set(System.nanoTime());
		log(description, "started");
	}

	@Override
	public void testFinished(Description description) throws Exception {
		long nano = System.nanoTime() - testStart.get();
		log(description, "finished in " + (nano / 1000_000) + " ms");
	}

	private void log(Description description, String msg) {
		log.info("[" + descriptor(description) + "] " + msg + ".    Thread: " + Thread.currentThread().getName());
	}

	private String descriptor(Description d) {
		return d.getTestClass().getSimpleName() + "->" + d.getMethodName();
	}

}
