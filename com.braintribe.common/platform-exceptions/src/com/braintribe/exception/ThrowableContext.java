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
package com.braintribe.exception;

public class ThrowableContext extends Throwable {

	private static final long serialVersionUID = 1L;

	protected StackTraceElement[] reducedStackTraceElements;
	
	public ThrowableContext(String msg) {
		super(msg, null, false, false);
	}
	
	@Override
	public StackTraceElement[] getStackTrace() {
		if (reducedStackTraceElements != null) {
			return reducedStackTraceElements;
		}
		return super.getStackTrace();
	}
}
