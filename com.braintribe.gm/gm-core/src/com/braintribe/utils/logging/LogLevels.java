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
package com.braintribe.utils.logging;

import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.logging.Logger;
import com.braintribe.utils.CommonTools;

/**
 * Utility class for operations related to {@link com.braintribe.logging.Logger.LogLevel} and
 * {@link com.braintribe.model.logging.LogLevel}
 * 
 *
 */
public class LogLevels {

	/**
	 * Converts modeled {@link com.braintribe.model.logging.LogLevel} to the corresponding {@link com.braintribe.logging.Logger.LogLevel} of the
	 * {@link Logger}. If logLevel is <tt>null</tt> then <tt>null</tt> is returned.
	 */
	public static com.braintribe.logging.Logger.LogLevel convert(com.braintribe.model.logging.LogLevel logLevel) {
		return convert(logLevel, null);
	}
		
	public static com.braintribe.logging.Logger.LogLevel convert(com.braintribe.model.logging.LogLevel logLevel, com.braintribe.logging.Logger.LogLevel defaultLevel) {
		if (logLevel == null)
			return defaultLevel;

		switch (logLevel) {
			case TRACE:
				return com.braintribe.logging.Logger.LogLevel.TRACE;
			case DEBUG:
				return com.braintribe.logging.Logger.LogLevel.DEBUG;
			case INFO:
				return com.braintribe.logging.Logger.LogLevel.INFO;
			case WARN:
				return com.braintribe.logging.Logger.LogLevel.WARN;
			case ERROR:
				return com.braintribe.logging.Logger.LogLevel.ERROR;
			default:
				throw new UnknownEnumException(logLevel);
		}
	}

	/**
	 * Convert a LogLevel as {@link String} based on {@link com.braintribe.model.logging.LogLevel} to the corresponding
	 * {@link com.braintribe.logging.Logger.LogLevel} of the logger.
	 * 
	 * @param logLevelAsString
	 *            {@link com.braintribe.model.logging.LogLevel} as {@link String}
	 * @return {@link com.braintribe.logging.Logger.LogLevel}
	 */
	public static com.braintribe.logging.Logger.LogLevel convert(String logLevelAsString) {
		if (CommonTools.isEmpty(logLevelAsString)) {
			throw new IllegalArgumentException("LogLevel needs to be set but is '" + logLevelAsString + "'");
		}
		com.braintribe.model.logging.LogLevel logLevel = com.braintribe.model.logging.LogLevel.valueOf(logLevelAsString);
		return convert(logLevel);
	}

}
