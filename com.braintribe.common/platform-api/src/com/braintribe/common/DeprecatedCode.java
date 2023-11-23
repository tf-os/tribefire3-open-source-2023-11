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

import static com.braintribe.utils.lcd.CollectionTools2.newConcurrentMap;

import java.util.Map;

import com.braintribe.logging.Logger;
import com.braintribe.utils.lcd.StringTools;

/**
 * @author peter.gazdik
 */
public class DeprecatedCode {

	private static final Logger log = Logger.getLogger(DeprecatedCode.class);

	/**
	 * {@code
		 StackTraceElement[0]: Thread.getStackTrace()
		 StackTraceElement[1]: DeprecatedCode.callerDescription
		 StackTraceElement[2]: DeprecatedCode.logWarn
		 StackTraceElement[3]: >> Deprecated Code <<
		 StackTraceElement[4]: >> Caller of Deprecated Code <<
	 * }
	 */
	private static final int CALLER_POSITION_WHEN_RESOLVING_LOCATION = 3;

	private static final Map<String, String> loggedDeprecatedCallers = newConcurrentMap();

	/**
	 * Logs a warning with message saying the caller of this method is a deprecated method, and also giving info about this code's caller.
	 * <p>
	 * To void spamming, for each call-site where this method is invoked only the first invocation is logged. All subsequent calls have no effect.
	 */
	public static void logWarn() {
		String msg = deprecatedCodeLocationIfFirstOccurence(0);
		if (msg != null) {
			log.warn(fullMessage(msg));
		}
	}

	/** Just like {@link #logWarn()} but with printing to System.out instead. */
	public static void printWarn() {
		String msg = deprecatedCodeLocationIfFirstOccurence(0);
		if (msg != null) {
			System.out.println(fullMessage(msg));
		}
	}

	private static String fullMessage(String msg) {
		String border = StringTools.repeat("#", 50);
		return "\n" + border + "\nDeprecated code detected!!! Code and it's caller: \n" + msg + "\n" + border;
	}

	private static String deprecatedCodeLocationIfFirstOccurence(int stackShift) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement deprecatedCode = stackTrace[CALLER_POSITION_WHEN_RESOLVING_LOCATION + stackShift];
		StackTraceElement callerOfDeprecatedCode = stackTrace[CALLER_POSITION_WHEN_RESOLVING_LOCATION + stackShift + 1];

		String result = callSiteDescription(deprecatedCode) + "\n" + callSiteDescription(callerOfDeprecatedCode);
		return loggedDeprecatedCallers.put(result, result) == null ? result : null;
	}

	private static String callSiteDescription(StackTraceElement callSite) {
		boolean hasDebugInfo = callSite.getLineNumber() >= 0 && callSite.getFileName() != null;

		return callSite.getClassName() + "." + callSite.getMethodName()
				+ (!hasDebugInfo ? "" : " (" + callSite.getFileName() + ":" + callSite.getLineNumber() + ") ");
	}
}
