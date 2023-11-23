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
package com.braintribe.utils;

import com.braintribe.utils.lcd.NullSafe;

/**
 * DO NOT REFACTOR !!!
 *
 * Simple tool to make debugging easier.
 *
 * Print to console with the file/line information, using the same format as a stacktrace, thus your IDE makes the text different color and clickable.
 *
 * Eclipse hint: For easy use add this class to your favorites, just start typing the name of the method (sp...) and code-complete.
 *
 * DO NOT REFACTOR - refactoring could break this, as the number of frames between the caller of this class and {@link Thread#getStackTrace()} (which
 * is called from this class) is important!
 *
 * @author peter.gazdik
 */
public class SysPrint {

	/**
	 * {@code
		 StackTraceElement[0]: Thread.getStackTrace()
		 StackTraceElement[1]: SysPrint.location()
		 StackTraceElement[2]: SysPrint.spOut/spErr/callerLocation
		 StackTraceElement[3]: >> Caller <<
	 * }
	 */
	private static final int CALLER_POSITION_WHEN_RESOLVING_LOCATION = 3;

	public static void spOut(Object o) {
		System.out.println(location(0) + o);
	}

	public static void spErr(Object o) {
		System.err.println(location(0) + o);
	}

	public static void spOut(int stackShift, Object o) {
		System.out.println(location(stackShift) + o);
	}

	public static void spErr(int stackShift, Object o) {
		System.err.println(location(stackShift) + o);
	}

	public static String callerLocation(int stackShift) {
		return location(stackShift);
	}

	private static String location(int stackShift) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement caller = stackTrace[CALLER_POSITION_WHEN_RESOLVING_LOCATION + stackShift];

		String classOrFile = NullSafe.get(caller.getFileName(), caller.getClassName());
		String lineOrMethod = caller.getLineNumber() >= 0 ? "" + caller.getLineNumber() : caller.getMethodName();

		return "(" + classOrFile + ":" + lineOrMethod + ") ";
	}

}
