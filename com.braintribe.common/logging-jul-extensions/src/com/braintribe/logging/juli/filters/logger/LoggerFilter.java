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
package com.braintribe.logging.juli.filters.logger;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.braintribe.logging.juli.JulExtensionsHelpers;

/**
 * This filter can be used to specify which messages should be {@link #isLoggable(LogRecord) loggable} based on the name of the {@link Logger}s. For
 * example, you could define a filter that only accepts messages from logger
 * <code>com.braintribe.model.processing.webrpc.server.GmWebRpcServer</code>, hence creating a log file that only contains messages from this class.
 * It is also possible to specify a package like <code>com.braintribe</code> to include only loggers from this package. <br />
 * The configuration takes just a single parameter: <code>loggerNames</code><br />
 * This parameter should contain a comma-separated list of logger names. Each logger name may also have a log level specification as suffix, which
 * either specifies a single log level or a range. In that case only matching log levels will be passed through the filter. If no log level is
 * configured, all log levels will be accepted by this filter (although this does not necessarily mean that all log levels will show up in the log
 * output, because log levels can also be filtered in general logging configuration). <br />
 * Examples:<br />
 * <code>com.braintribe.Class1</code> - all log levels from this logger will be accepted.<br />
 * <code>com.braintribe.Class2@SEVERE</code> - all SEVERE log lines will be accepted.<br />
 * <code>com.braintribe.Class3@FINEST-FINE</code> - FINEST, FINER, and FINE log lines will be accepted for this class.<br />
 * <code>com.braintribe.Class4@-INFO</code> - FINEST, FINER, FINE, and INFO log lines will be accepted for this class.<br />
 * <code>com.braintribe.Class5@INFO-</code> - INFO, WARNING, and SEVERE log lines will be accepted for this class.<br />
 * <code>com.braintribe</code> - All classes using this package will be accepted on all levels.
 */
public class LoggerFilter implements Filter {

	protected static final Logger logLogger = Logger.getLogger(LoggerFilter.class.getName());

	public static final String PROPERTY_LOGGERNAMES = "loggerNames";

	protected Map<String, EnabledLogLevels> enabledLogLevels = new HashMap<>();

	public LoggerFilter() {
		this(true);
	}

	public LoggerFilter(String loggerNamesString) {
		this.parseEnabledLogLevels(loggerNamesString);
	}

	/**
	 * This constructor is used for testing purposes only.
	 *
	 * @param initialize
	 *            Indicates whether the class should initialize itself automatically.
	 */
	LoggerFilter(boolean initialize) {
		if (initialize) {
			this.parseEnabledLogLevels(JulExtensionsHelpers.getProperty(getClass(), PROPERTY_LOGGERNAMES, true, null, String.class));
		}
	}

	Map<String, EnabledLogLevels> getEnabledLogLevels() {
		return this.enabledLogLevels;
	}

	void parseEnabledLogLevels(String loggerNamesString) {
		String[] entries = loggerNamesString.split(",");
		for (String entry : entries) {
			entry = entry.trim();
			EnabledLogLevels ell = new EnabledLogLevels();

			final String loggerName;

			int index = entry.indexOf('@');
			if (index != -1) {
				String levels = entry.substring(index + 1);
				loggerName = entry.substring(0, index);

				int index2 = levels.indexOf('-');
				if (index2 == -1) {
					ell.setFrom(levels);
					ell.setTo(levels);
				} else {
					String from = levels.substring(0, index2);
					String to = levels.substring(index2 + 1);
					ell.setFrom(from);
					ell.setTo(to);
				}
			} else {
				loggerName = entry;
			}

			this.enabledLogLevels.put(loggerName, ell);
		}
	}

	@Override
	public boolean isLoggable(LogRecord logRecord) {
		if (logRecord == null) {
			return false;
		}

		String loggerName = logRecord.getLoggerName();
		if (loggerName == null) {
			return false;
		}

		EnabledLogLevels ell = this.enabledLogLevels.get(loggerName);
		while (ell == null) {
			if (loggerName.equals(".")) {
				break;
			}

			int idx = loggerName.lastIndexOf('.');
			if (idx == -1) {
				loggerName = ".";
			} else {
				loggerName = loggerName.substring(0, idx);
			}

			ell = this.enabledLogLevels.get(loggerName);
		}

		if (ell == null) {
			return false;
		}

		boolean enabled = ell.enabled(logRecord);
		return enabled;
	}
}
