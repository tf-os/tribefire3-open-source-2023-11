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
package com.braintribe.utils.ldap.invocation;

import java.lang.reflect.Method;

/**
 * This class is used to invoke LDAP-related method calls
 * in a separate thread.
 * 
 * @author roman.kurmanowytsch
 */
public class InvocationThread extends Thread {
	protected Object subject = null;
	protected Method m = null;
	protected Object[] args = null;
	protected Object result = null;
	protected boolean resultReady = false;
	protected boolean exceptionThrown = false;
	protected Exception exception = null;

	public InvocationThread(Object subject, Method m, Object[] args) {
		this.subject = subject;
		this.m = m;
		this.args = args;
	}

	@Override
	public void run() {
		try {
			// run the command and set the result...
			this.result = this.m.invoke(this.subject, this.args);
			this.resultReady = true;
		} catch (Exception e) {
			this.exceptionThrown = true;
			this.exception = e;
		}
	}

	public Exception getException() {
		return exception;
	}

	public Object getResult() {
		return result;
	}

	public boolean isResultReady() {
		return resultReady;
	}

	public boolean isExceptionThrown() {
		return exceptionThrown;
	}
}
