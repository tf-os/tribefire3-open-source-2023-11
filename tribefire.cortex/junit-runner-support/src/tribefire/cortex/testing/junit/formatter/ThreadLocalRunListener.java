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
package tribefire.cortex.testing.junit.formatter;

import java.util.function.Supplier;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * A simple way to enable thread safety when tests are executed in parallel. Works fine in combination with {@link JUnitResultFormatterAsRunListener}
 *
 * @author Neidhart.Orlich
 *
 */
public class ThreadLocalRunListener extends RunListener {

	private ThreadLocal<RunListener> delegateThreadLocal;

	public ThreadLocalRunListener(Supplier<RunListener> delegateFactory) {
		super();
		delegateThreadLocal = ThreadLocal.withInitial(delegateFactory);
	}

	private RunListener delegate() {
		return delegateThreadLocal.get();
	}

	@Override
	public void testRunStarted(Description description) throws Exception {
		delegate().testRunStarted(description);
	}

	@Override
	public void testRunFinished(Result result) throws Exception {
		delegate().testRunFinished(result);
	}

	@Override
	public void testStarted(Description description) throws Exception {
		delegate().testStarted(description);
	}

	@Override
	public void testFinished(Description description) throws Exception {
		delegate().testFinished(description);
	}

	@Override
	public void testFailure(Failure failure) throws Exception {
		delegate().testFailure(failure);
	}

	@Override
	public void testAssumptionFailure(Failure failure) {
		delegate().testAssumptionFailure(failure);
	}

	@Override
	public void testIgnored(Description description) throws Exception {
		delegate().testIgnored(description);
	}

	@Override
	public String toString() {
		return delegate().toString();
	}
}
