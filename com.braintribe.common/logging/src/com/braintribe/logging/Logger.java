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
package com.braintribe.logging;

import java.awt.EventQueue;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Wrapper class for java.util.logging.Logger providing an interface more like Log4J.
 *
 */
public class Logger {

	public static enum LogLevel {
		TRACE,
		DEBUG,
		INFO,
		WARN,
		ERROR
	}

	public static class AwtExceptionHandler {
		public void handle(final Throwable ex) {
			final Logger logger = Logger.getLogger(EventQueue.class);
			logger.error("uncaught exception in AWT event dispatcher " + Thread.currentThread().getName(), ex);
		}
	}

	private static Class<? extends Logger> loggerImpl = JdkLogger.class;

	public static void setLoggerImpl(final Class<? extends Logger> loggerImpl) {
		Logger.loggerImpl = loggerImpl;
	}

	public static Logger getLogger(final Class<?> cls) {
		try {
			Logger logger = loggerImpl.getConstructor(Class.class).newInstance(cls);
			/* The (now deprecated) constructor {@link #Logger(Class)} used to set the created logger as delegate. Since the methods
			 * of this class expect the delegate to be set, we have to do that too. */
			logger.delegate = logger;
			return logger;
		} catch (final Exception e) {
			throw new RuntimeException("error while creating logger", e);
		}
	}

	public static Logger getLogger(String loggerName, final Class<?> cls) {
		try {
			Logger logger = loggerImpl.getConstructor(String.class, Class.class).newInstance(loggerName, cls);
			logger.delegate = logger;
			return logger;

		} catch (final Exception e) {
			// PGA: I wonder if we should log this error. And how.
			return getLogger(cls);
		}
	}

	public static void installAwtExceptionLogger() {
		installAwtExceptionHandler(AwtExceptionHandler.class);
	}

	public static void installAwtExceptionHandler(final Class<?> cls) {
		try {
			System.setProperty("sun.awt.exception.handler", cls.getName());
		} catch (final SecurityException ex) {
			// ignore.
		}
	}

	private Logger delegate;


	protected Logger() {
	}

	public void trace(final String msg) {
		this.delegate.trace(msg);
	}
	public void trace(final String msg, final Throwable ex) {
		this.delegate.trace(msg, ex);
	}
	public void trace(final Throwable ex) {
		this.delegate.trace(ex);
	}
	public void trace(final Supplier<String> messageSupplier) {
		if (this.delegate.isTraceEnabled()) {
			this.delegate.trace(messageSupplier.get());
		}
	}
	public void trace(final Supplier<String> messageSupplier, final Throwable throwable) {
		if (this.delegate.isTraceEnabled()) {
			this.delegate.trace(messageSupplier.get(), throwable);
		}
	}

	public void trace(final Throwable t, String format, Object... parameters) {
		if (!this.isTraceEnabled()) {
			return;
		}
		Object[] newObjects = this.computerParameters(parameters);
		String message = String.format(format, newObjects);
		this.delegate.trace(message, t);
	}

	public void debug(final String msg) {
		this.delegate.debug(msg);
	}
	public void debug(final String msg, final Throwable ex) {
		this.delegate.debug(msg, ex);
	}
	public void debug(final Throwable ex) {
		this.delegate.debug(ex);
	}
	public void debug(final Supplier<String> messageSupplier) {
		if (this.delegate.isDebugEnabled()) {
			this.delegate.debug(messageSupplier.get());
		}
	}
	public void debug(final Supplier<String> messageSupplier, final Throwable throwable) {
		if (this.delegate.isDebugEnabled()) {
			this.delegate.debug(messageSupplier.get(), throwable);
		}
	}

	public void debug(final Throwable t, String format, Object... parameters) {
		if (!this.isDebugEnabled()) {
			return;
		}
		Object[] newObjects = this.computerParameters(parameters);
		String message = String.format(format, newObjects);
		this.delegate.debug(message, t);
	}

	public void info(final String msg) {
		this.delegate.info(msg);
	}
	public void info(final String msg, final Throwable ex) {
		this.delegate.info(msg, ex);
	}
	public void info(final Throwable ex) {
		this.delegate.info(ex);
	}
	public void info(final Supplier<String> messageSupplier) {
		if (this.delegate.isInfoEnabled()) {
			this.delegate.info(messageSupplier.get());
		}
	}
	public void info(final Supplier<String> messageSupplier, final Throwable throwable) {
		if (this.delegate.isInfoEnabled()) {
			this.delegate.info(messageSupplier.get(), throwable);
		}
	}
	public void info(final Throwable t, String format, Object... parameters) {
		if (!this.isInfoEnabled()) {
			return;
		}
		Object[] newObjects = this.computerParameters(parameters);
		String message = String.format(format, newObjects);
		this.delegate.info(message, t);
	}

	public void warn(final String msg) {
		this.delegate.warn(msg);
	}
	public void warn(final String msg, final Throwable ex) {
		this.delegate.warn(msg, ex);
	}
	public void warn(final Throwable ex) {
		this.delegate.warn(ex);
	}
	public void warn(final Supplier<String> messageSupplier) {
		if (this.delegate.isWarnEnabled()) {
			this.delegate.warn(messageSupplier.get());
		}
	}
	public void warn(final Supplier<String> messageSupplier, final Throwable throwable) {
		if (this.delegate.isWarnEnabled()) {
			this.delegate.warn(messageSupplier.get(), throwable);
		}
	}
	public void warn(final Throwable t, String format, Object... parameters) {
		if (!this.isWarnEnabled()) {
			return;
		}
		Object[] newObjects = this.computerParameters(parameters);
		String message = String.format(format, newObjects);
		this.delegate.warn(message, t);
	}

	public void error(final String msg) {
		this.delegate.error(msg);
	}
	public void error(final String msg, final Throwable ex) {
		this.delegate.error(msg, ex);
	}
	public void error(final Throwable ex) {
		this.delegate.error(ex);
	}
	public void error(final Supplier<String> messageSupplier) {
		if (this.delegate.isErrorEnabled()) {
			this.delegate.error(messageSupplier.get());
		}
	}
	public void error(final Supplier<String> messageSupplier, final Throwable throwable) {
		if (this.delegate.isErrorEnabled()) {
			this.delegate.error(messageSupplier.get(), throwable);
		}
	}
	public void error(final Throwable t, String format, Object... parameters) {
		if (!this.isErrorEnabled()) {
			return;
		}
		Object[] newObjects = this.computerParameters(parameters);
		String message = String.format(format, newObjects);
		this.delegate.error(message, t);
	}

	public void log(final LogLevel logLevel, final String msg) {
		switch (logLevel) {
			case TRACE:
				this.delegate.trace(msg);
				break;
			case DEBUG:
				this.delegate.debug(msg);
				break;
			case INFO:
				this.delegate.info(msg);
				break;
			case WARN:
				this.delegate.warn(msg);
				break;
			case ERROR:
				this.delegate.error(msg);
				break;
			default:
				throw new RuntimeException("Cannot process unknown LogLevel " + logLevel + "!");
		}
	}

	public void log(final LogLevel logLevel, final Supplier<String> messageSupplier) {
		switch (logLevel) {
			case TRACE:
				this.delegate.trace(messageSupplier);
				break;
			case DEBUG:
				this.delegate.debug(messageSupplier);
				break;
			case INFO:
				this.delegate.info(messageSupplier);
				break;
			case WARN:
				this.delegate.warn(messageSupplier);
				break;
			case ERROR:
				this.delegate.error(messageSupplier);
				break;
			default:
				throw new RuntimeException("Cannot process unknown LogLevel " + logLevel + "!");
		}
	}

	public void log(final LogLevel logLevel, final String msg, final Throwable ex) {
		switch (logLevel) {
			case TRACE:
				this.delegate.trace(msg, ex);
				break;
			case DEBUG:
				this.delegate.debug(msg, ex);
				break;
			case INFO:
				this.delegate.info(msg, ex);
				break;
			case WARN:
				this.delegate.warn(msg, ex);
				break;
			case ERROR:
				this.delegate.error(msg, ex);
				break;
			default:
				throw new RuntimeException("Cannot process unknown LogLevel " + logLevel + "!");
		}
	}

	public void log(final LogLevel logLevel, final Supplier<String> messageSupplier, final Throwable throwable) {
		switch (logLevel) {
			case TRACE:
				this.delegate.trace(messageSupplier, throwable);
				break;
			case DEBUG:
				this.delegate.debug(messageSupplier, throwable);
				break;
			case INFO:
				this.delegate.info(messageSupplier, throwable);
				break;
			case WARN:
				this.delegate.warn(messageSupplier, throwable);
				break;
			case ERROR:
				this.delegate.error(messageSupplier, throwable);
				break;
			default:
				throw new RuntimeException("Cannot process unknown LogLevel " + logLevel + "!");
		}
	}

	public boolean isTraceEnabled() {
		return this.delegate.isTraceEnabled();
	}

	public boolean isDebugEnabled() {
		return this.delegate.isDebugEnabled();
	}

	public boolean isInfoEnabled() {
		return this.delegate.isInfoEnabled();
	}

	public boolean isWarnEnabled() {
		return this.delegate.isWarnEnabled();
	}

	public boolean isErrorEnabled() {
		return this.delegate.isErrorEnabled();
	}

	public boolean isLevelEnabled(final LogLevel logLevel) {
		switch (logLevel) {
			case TRACE:
				return this.delegate.isTraceEnabled();
			case DEBUG:
				return this.delegate.isDebugEnabled();
			case INFO:
				return this.delegate.isInfoEnabled();
			case WARN:
				return this.delegate.isWarnEnabled();
			case ERROR:
				return this.delegate.isErrorEnabled();
			default:
				throw new RuntimeException("Cannot process unknown LogLevel " + logLevel + "!");
		}
	}

	public void pushContext(String context) {
		this.delegate.pushContext(context);
	}
	public void popContext() {
		this.delegate.popContext();
	}
	public void removeContext() {
		this.delegate.removeContext();
	}

	public void clearMdc() {
		this.delegate.clearMdc();
	}
	public Object get(String key) {
		return this.delegate.get(key);
	}
	public void put(String key, String value) {
		this.delegate.put(key, value);
	}
	public void remove(String key) {
		this.delegate.remove(key);
	}

	public void setLogLevel(LogLevel logLevel) {
		this.delegate.setLogLevel(logLevel);
	}

	public void registerManagedLoggerPackage(String packagePrefix) {
		this.delegate.registerManagedLoggerPackage(packagePrefix);
	}

	public Set<String> getManagedLoggerPackages() {
		return this.delegate.getManagedLoggerPackages();
	}

	protected Object[] computerParameters(Object... parameters) {
		if (parameters == null) {
			return null;
		}
		if (parameters.length == 0) {
			return parameters;
		}
		Object[] newObjects = new Object[parameters.length];
		int index = 0;
		for (Object o : parameters) {
			if (o instanceof Supplier<?>) {
				Object realObject = ((Supplier<?>) o).get();
				newObjects[index] = realObject;
			} else {
				newObjects[index] = o;
			}
			index++;
		}
		return newObjects;
	}

}
