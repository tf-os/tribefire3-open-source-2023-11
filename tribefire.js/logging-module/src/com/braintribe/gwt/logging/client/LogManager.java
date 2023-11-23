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
package com.braintribe.gwt.logging.client;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.ioc.client.Configurable;

/**
 * This class distributes {@link LogEvent} instances to any registered
 * {@link LogListener}. Any {@link Logger} sends its events to this
 * manager.
 * @author Dirk
 *
 */
public class LogManager {
	
	protected static LogLevel logLevel = LogLevel.INFO;
	
	private static List<LogListener> listeners = new ArrayList<>();
	
	public static void addListener(LogListener listener) {
		listeners.add(listener);
	}
	
	public static void removeListener(LogListener listener) {
		listeners.remove(listener);
	}
	
	public static void fireLogEvent(LogEvent event) {
		for (LogListener listener: listeners) {
			listener.onLogEvent(event);
		}
	}
	
	public static boolean isTraceEnabled() {
		return logLevel.ordinal() >= LogLevel.TRACE.ordinal();
	}
	public static boolean isDebugEnabled() {
		return logLevel.ordinal() >= LogLevel.DEBUG.ordinal();
	}
	public static boolean isInfoEnabled() {
		return logLevel.ordinal() >= LogLevel.INFO.ordinal();
	}
	public static boolean isWarnEnabled() {
		return logLevel.ordinal() >= LogLevel.WARN.ordinal();
	}
	public static boolean isErrorEnabled() {
		return logLevel.ordinal() >= LogLevel.ERROR.ordinal();
	}
	public static boolean isLevelEnabled(final LogLevel logLevelParam) { //this.logelevl = INFO (6)..... isTRaceEnabled(9)?
		return logLevel.ordinal() >= logLevelParam.ordinal();
	}	
	
	public static LogLevel getLogLevel() {
		return logLevel;
	}
	@Configurable
	public static void setLogLevel(LogLevel logLevel) {
		LogManager.logLevel = logLevel;
	}
	
}
