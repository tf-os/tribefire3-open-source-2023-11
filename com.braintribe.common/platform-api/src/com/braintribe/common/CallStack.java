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
package com.braintribe.common;

public class CallStack {
	public static CallStackFrame getCallerFrame(int callerOffset) {
		StackTraceElement frames[] = Thread.currentThread().getStackTrace();

		int index = 2 + callerOffset;

		if (frames == null || frames.length <= index) {
			return new CallStackFrame(new StackTraceElement("unkown", "unkown", null, -1), 0);
		}

		StackTraceElement element = frames[index];

		return new CallStackFrame(element, frames.length - index - 1);
	}
}
