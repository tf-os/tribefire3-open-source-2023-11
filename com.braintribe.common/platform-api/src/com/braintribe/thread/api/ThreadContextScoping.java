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

/**
 * This interface defines convenience methods for binding threads to a user session. When you want to run code in a separate thread, you typically
 * loose the thread's user session. An implementation of this interface wraps Runnable and Callable objects and inject the user session before
 * executing the actual code.
 *
 * You can either use the bind methods to wrap your Runnable/Callable and run it yourself or you can use the convenience methods to run the
 * Runnable/Callable immediately.
 */
public interface ThreadContextScoping {

	Runnable bindContext(Runnable runnable);

	<T> Callable<T> bindContext(Callable<T> callable);

	void runWithContext(Runnable runnable);

	<T> T runWithContext(Callable<T> callable) throws Exception;

}
