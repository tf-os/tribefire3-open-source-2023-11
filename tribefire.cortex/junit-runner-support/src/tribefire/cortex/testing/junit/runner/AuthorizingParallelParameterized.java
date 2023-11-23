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
package tribefire.cortex.testing.junit.runner;

import org.junit.runners.Parameterized;

import com.braintribe.thread.api.ThreadContextScoping;

/**
 * An extension of the JUnit {@link Parameterized} runner, which executes the tests for each parameter set concurrently.
 * <p>
 * You can specify the maximum number of parallel test threads using the system property <code>maxParallelTestThreads</code>. If this system property
 * is not specified, the maximum number of test threads will be the number of {@link Runtime#availableProcessors() available processors}.
 * 
 * @see <a href="https://github.com/MichaelTamm/junit-toolbox/blob/master/src/main/java/com/googlecode/junittoolbox/ParallelParameterized.java">JUnit
 *      Toolbox</a>
 */
public class AuthorizingParallelParameterized extends Parameterized implements ThreadContextScopingAware {

	private final AuthorizingParallelScheduler scheduler;

	public AuthorizingParallelParameterized(Class<?> klass) throws Throwable {
		super(klass);
		scheduler = new AuthorizingParallelScheduler();
		setScheduler(scheduler);
	}

	@Override
	public void setThreadContextScoping(ThreadContextScoping tcs) {
		scheduler.tcs = tcs;
	}
}