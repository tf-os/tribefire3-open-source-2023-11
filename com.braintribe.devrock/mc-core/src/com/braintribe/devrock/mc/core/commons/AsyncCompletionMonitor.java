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
package com.braintribe.devrock.mc.core.commons;

import com.braintribe.exception.Exceptions;

public class AsyncCompletionMonitor {
	private volatile int asyncCount;
	private Object monitor = new Object();

	public void incAsync() {
		synchronized (monitor) {
			asyncCount++;
		}
	}
	
	public void decAsync() {
		synchronized (monitor) {
			if (--asyncCount == 0)
				monitor.notify();
		}
	}
	
	public void waitForCompletion() {
		synchronized (monitor) {
			if (asyncCount == 0)
				return;
			
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				Exceptions.unchecked(e, "Unexpected interruption");
			}
		}
	}
}
