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
package com.braintribe.thread.api;

import java.util.concurrent.Callable;

public class EmptyThreadContextScoping implements ThreadContextScoping {

	public static final EmptyThreadContextScoping INSTANCE = new EmptyThreadContextScoping();

	private EmptyThreadContextScoping() {
	}

	@Override
	public Runnable bindContext(Runnable runnable) {
		return runnable;
	}

	@Override
	public <T> Callable<T> bindContext(Callable<T> callable) {
		return callable;
	}

	@Override
	public void runWithContext(Runnable runnable) {
		runnable.run();
	}

	@Override
	public <T> T runWithContext(Callable<T> callable) throws Exception {
		return callable.call();
	}

}
