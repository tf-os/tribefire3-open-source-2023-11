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
package tribefire.extension.messaging.integration.test.util;

import java.util.concurrent.TimeUnit;

import org.junit.AssumptionViolatedException;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;

import com.braintribe.logging.Logger;

public class CustomJUnitStopWatch extends Stopwatch {

	private final static Logger logger = Logger.getLogger(CustomJUnitStopWatch.class);

	private static void logInfo(Description description, String status, long nanos) {
		String testName = description.getMethodName();
		logger.info(String.format("Test '%s' '%s' took: '%d'ms", testName, status, TimeUnit.NANOSECONDS.toMillis(nanos)));
	}

	@Override
	protected void succeeded(long nanos, Description description) {
		logInfo(description, "succeeded", nanos);
	}

	@Override
	protected void failed(long nanos, Throwable e, Description description) {
		logInfo(description, "failed", nanos);
	}

	@Override
	protected void skipped(long nanos, AssumptionViolatedException e, Description description) {
		logInfo(description, "skipped", nanos);
	}
}
