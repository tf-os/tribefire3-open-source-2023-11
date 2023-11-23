// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.logging;

import java.util.function.Supplier;

import com.braintribe.gwt.logging.client.LogManager;

/**
 * Emulating Wrapper class for com.braintribe.logging.Logger which supports this logging for gwt by using com.braintribe.gwt.logging.Logger
 * 
 * @author dirk
 */
public class Logger {

	public static enum LogLevel {
		TRACE, DEBUG, INFO, WARN, ERROR
	}

	public static Logger getLogger(Class<?> cls) {
		return new Logger(cls);
	}

	public static Logger getLogger(String loggerName, Class<?> cls) {
		return new Logger(cls);
	}
	
	protected String sourceClass;
	protected com.braintribe.gwt.logging.client.Logger logger;

	public Logger(Class<?> cls) {
		this(new com.braintribe.gwt.logging.client.Logger(cls), cls);
	}

	public Logger(com.braintribe.gwt.logging.client.Logger logger, Class<?> sourceClass) {
		this.logger = logger;
		this.sourceClass = sourceClass.getName();
	}
	
	public void log(LogLevel logLevel, Supplier<String> messageSupplier) {
		switch (logLevel) {
			case DEBUG:
				logger.debug(messageSupplier.get());
				break;
			case ERROR:
				logger.error(messageSupplier.get());
				break;
			case INFO:
				logger.info(messageSupplier.get());
				break;
			case TRACE:
				logger.trace(messageSupplier.get());
				break;
			case WARN:
				logger.warn(messageSupplier.get());
		}
	}

	public void trace(String msg) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.TRACE, msg);
	}
	
	public void trace(Supplier<String> msgSupplier) {
		trace(msgSupplier.get());
	}
	
	public void trace(String msg, Throwable t) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.TRACE, msg, t);
	}
	public void trace(Throwable t) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.TRACE, t);
	}

	public void debug(String msg) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.DEBUG, msg);
	}
	
	public void debug(final Supplier<String> messageSupplier) {
		debug(messageSupplier.get());
	}
	
	public void debug(String msg, Throwable t) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.DEBUG, msg, t);
	}
	public void debug(Throwable t) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.DEBUG, t);
	}

	public void info(String msg) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.INFO, msg);
	}
	public void info(String msg, Throwable t) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.INFO, msg, t);
	}
	public void info(Throwable t) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.INFO, t);
	}
	public void info(Supplier<String> messageSupplier) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.INFO, messageSupplier.get());
	}
	public void info(Supplier<String> messageSupplier, Throwable throwable) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.INFO, messageSupplier.get(), throwable);
	}

	public void warn(String msg) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.WARN, msg);
	}
	public void warn(String msg, Throwable ex) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.WARN, msg, ex);
	}
	public void warn(Throwable ex) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.WARN, ex);
	}

	public void error(String msg) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.ERROR, msg);
	}
	public void error(String msg, Throwable ex) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.ERROR, msg, ex);
	}
	public void error(Throwable ex) {
		logger.log(com.braintribe.gwt.logging.client.LogLevel.ERROR, ex);
	}
	

	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}
	
	public boolean isLevelEnabled(final LogLevel logLevel) {
		switch (logLevel) {
		case TRACE:
			return logger.isTraceEnabled();
		case DEBUG:
			return logger.isDebugEnabled();
		case INFO:
			return logger.isInfoEnabled();
		case WARN:
			return logger.isWarnEnabled();
		case ERROR:
			return logger.isErrorEnabled();
		default:
			throw new RuntimeException("Cannot process unknown LogLevel " + logLevel + "!");
		}
	}
}
