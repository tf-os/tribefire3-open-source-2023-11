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
package com.braintribe.logging.log4j2;

import java.util.Collections;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.net.Priority;

import com.braintribe.logging.Logger;

public class Log4JLogger extends Logger {

	protected org.apache.logging.log4j.Logger delegate;

	// **************************************************************************
	// Constructor
	// **************************************************************************

	/**
	 * Default constructor
	 */
	public Log4JLogger(Class<?> cls) {
		delegate = LogManager.getLogger(cls);
	}

	public Log4JLogger(@SuppressWarnings("unused") String loggerName, Class<?> cls) {
		delegate = LogManager.getLogger(cls);
	}

	@Override
	public void trace(String msg) {
		delegate.trace(msg);
	}

	@Override
	public void trace(Throwable ex) {
		delegate.trace(ex);
	}

	@Override
	public void trace(String msg, Throwable ex) {
		delegate.trace(msg, ex);
	}

	@Override
	public void debug(String msg) {
		delegate.debug(msg);
	}

	@Override
	public void debug(Throwable ex) {
		delegate.debug(ex);
	}

	@Override
	public void debug(String msg, Throwable ex) {
		delegate.debug(msg, ex);
	}

	@Override
	public void info(String msg) {
		delegate.info(msg);
	}

	@Override
	public void info(Throwable ex) {
		delegate.info(ex);
	}

	@Override
	public void info(String msg, Throwable ex) {
		delegate.info(msg, ex);
	}

	@Override
	public void warn(String msg) {
		delegate.warn(msg);
	}

	@Override
	public void warn(Throwable ex) {
		delegate.warn(ex);
	}

	@Override
	public void warn(String msg, Throwable ex) {
		delegate.warn(msg, ex);
	}

	@Override
	public void error(String msg) {
		delegate.error(msg);
	}

	@Override
	public void error(Throwable ex) {
		delegate.error(ex);
	}

	@Override
	public void error(String msg, Throwable ex) {
		delegate.error(msg, ex);
	}

	@Override
	public boolean isTraceEnabled() {
		return delegate.isTraceEnabled();
	}

	@Override
	public boolean isDebugEnabled() {
		return delegate.isDebugEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return delegate.isInfoEnabled();
	}

	/**
	 * see {@link org.apache.log4j.Logger#isEnabledFor(Priority)}
	 */
	@Override
	public boolean isWarnEnabled() {
		return delegate.isWarnEnabled();
	}

	/**
	 * see {@link org.apache.log4j.Logger#isEnabledFor(Priority)}
	 */
	@Override
	public boolean isErrorEnabled() {
		return delegate.isErrorEnabled();
	}

	@Override
	public void pushContext(String context) {
		ThreadContext.push(context);
	}

	@Override
	public void popContext() {
		ThreadContext.pop();
	}

	@Override
	public void removeContext() {
		ThreadContext.clearAll();
	}

	@Override
	public void clearMdc() {
		ThreadContext.clearMap();
	}

	@Override
	public String get(String key) {
		return ThreadContext.get(key);
	}

	@Override
	public void put(String key, String value) {
		ThreadContext.put(key, value);
	}

	@Override
	public void remove(String key) {
		ThreadContext.remove(key);
	}

	protected Level translateLogLevel(LogLevel logLevel) {
		if (logLevel == null) {
			return null;
		}
		switch (logLevel) {
			case TRACE:
				return Level.TRACE;
			case DEBUG:
				return Level.DEBUG;
			case INFO:
				return Level.INFO;
			case WARN:
				return Level.WARN;
			case ERROR:
				return Level.ERROR;
			default:
				return null;
		}
	}

	@Override
	public void setLogLevel(LogLevel logLevel) {
		Level newLevel = this.translateLogLevel(logLevel);
		if (newLevel == null) {
			return;
		}
		try {
			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			Configuration config = ctx.getConfiguration();
			LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
			loggerConfig.setLevel(newLevel);
			ctx.updateLoggers();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void registerManagedLoggerPackage(String packagePrefix) {
		// Not implemented yet
	}

	@Override
	public Set<String> getManagedLoggerPackages() {
		// Not implemented yet
		return Collections.emptySet();
	}

}
