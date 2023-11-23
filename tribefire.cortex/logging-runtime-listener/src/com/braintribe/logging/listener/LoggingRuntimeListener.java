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
package com.braintribe.logging.listener;

import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.bootstrapping.listener.RuntimePropertyChangeListener;

/**
 * Implementation of the {@link RuntimePropertyChangeListener} interface. It specifically listens for changes of the
 * logging level in the TribefireRuntime properties and changes the log level accordingly.
 */
public class LoggingRuntimeListener implements RuntimePropertyChangeListener {

	private static Logger logger = Logger.getLogger(LoggingRuntimeListener.class);

	protected static LoggingRuntimeListener listener = null;

	public static void register() {
		if (listener != null) {
			return;
		}

		listener = new LoggingRuntimeListener();

		LogLevel logLevel = listener.getLogLevel();
		if (logLevel != null) {
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_LOG_LEVEL, logLevel.name());
		}

		TribefireRuntime.addPropertyChangeListener(TribefireRuntime.ENVIRONMENT_LOG_LEVEL, listener);
	}

	public static void unregister() {
		if (listener == null) {
			return;
		}
		TribefireRuntime.removePropertyChangeListener(TribefireRuntime.ENVIRONMENT_LOG_LEVEL, listener);
		TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_LOG_LEVEL, null);
		listener = null;
	}

	protected LogLevel getLogLevel() {
		if (logger.isTraceEnabled()) {
			return LogLevel.TRACE;
		} else if (logger.isDebugEnabled()) {
			return LogLevel.DEBUG;
		} else if (logger.isInfoEnabled()) {
			return LogLevel.INFO;
		} else if (logger.isWarnEnabled()) {
			return LogLevel.WARN;
		} else if (logger.isErrorEnabled()) {
			return LogLevel.ERROR;
		} else {
			return null;
		}
	}

	protected LogLevel translateLogLevel(String levelName) {
		if (levelName == null) {
			return null;
		}
		levelName = levelName.trim().toUpperCase();
		switch (levelName) {
			case "TRACE":
			case "FINEST":
			case "FINER":
				return LogLevel.TRACE;
			case "DEBUG":
			case "FINE":
				return LogLevel.DEBUG;
			case "INFO":
				return LogLevel.INFO;
			case "WARN":
			case "WARNING":
				return LogLevel.WARN;
			case "ERROR":
			case "SEVERE":
				return LogLevel.ERROR;
			default:
				return null;
		}
	}

	@Override
	public void propertyChanged(String propertyName, String oldValue, String newValue) {
		if (propertyName == null || newValue == null) {
			return;
		}

		if (propertyName.equals(TribefireRuntime.ENVIRONMENT_LOG_LEVEL)) {
			logger.info(() -> "Received log level change request to " + newValue + " on logger " + logger);

			LogLevel logLevel = this.translateLogLevel(newValue);
			if (logLevel != null) {
				if (logLevel.equals(getLogLevel()) == false) {
					logger.setLogLevel(logLevel);

					logger.info(() -> "Log level has been changed to " + logLevel.name());
				} else if (logger.isDebugEnabled()) {
					logger.info(() -> "Log level is already " + logLevel.name());
				}
			} else if (logger.isDebugEnabled()) {
				logger.info(() -> "Invalid log level " + newValue);
			}
		}
	}
}
