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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class JdkLogger extends Logger {

	/**
	 * Single line formatter for log output. To activate, use this line in the logger configuration property file:
	 * 
	 * <pre>
	 * java.util.logging.ConsoleHandler.formatter = com.braintribe.csp.workflow.client.Logger$LogFormatter
	 * </pre>
	 * 
	 */
	public static class LogFormatter extends Formatter {

		private final static String newline = "\r\n"; // HACK! get from system property, but handle security
		// exception...
		private final static String format = "{0} [{1,date} {1,time}] {2}: {3}";

		private final MessageFormat formatter = new MessageFormat(format);

		private final Date dat = new Date();
		private final Object args[] = new Object[4];
		private final StringBuffer buffer = new StringBuffer();
		private final ReentrantLock bufferLock = new ReentrantLock();

		@Override
		public String format(final LogRecord record) {
			bufferLock.lock();
			try {
				this.buffer.setLength(0);

				this.args[0] = record.getLevel().getName();

				this.dat.setTime(record.getMillis());
				this.args[1] = this.dat;

				if (record.getSourceClassName() != null) {
					this.args[2] = record.getSourceClassName();
				} else {
					this.args[2] = record.getLoggerName();
				}

				if (record.getSourceMethodName() != null) {
					this.args[2] = ((String) this.args[2]) + record.getSourceMethodName();
				}

				this.args[3] = formatMessage(record);

				this.buffer.append(this.formatter.format(this.args));

				if (record.getThrown() != null) {
					try {
						final StringWriter sw = new StringWriter();
						final PrintWriter pw = new PrintWriter(sw);
						pw.println();
						record.getThrown().printStackTrace(pw);
						pw.close();

						this.buffer.append(sw.toString());
					} catch (final Exception ex) {
						// Ignore
					}
				}

				this.buffer.append(newline);
				return this.buffer.toString();
			} finally {
				bufferLock.unlock();
			}
		}
	}

	public static class AwtExceptionHandler {
		public void handle(final Throwable ex) {
			final Logger logger = Logger.getLogger(EventQueue.class);
			logger.error("uncaught exception in AWT event dispatcher " + Thread.currentThread().getName(), ex);
		}
	}

	public static void readConfiguration(final URL config) throws IOException {
		final InputStream in = config.openStream();
		LogManager.getLogManager().readConfiguration(in);
		in.close();
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

	protected String sourceClass;
	protected String module;
	protected java.util.logging.Logger logger;
	protected String loggerName;
	protected static Set<String> btLoggerPackages = new HashSet<>();

	static {
		btLoggerPackages.add("com.braintribe");
		btLoggerPackages.add("tribefire");
	}

	public JdkLogger(final Class<?> cls) {
		this(cls.getName(), cls);
	}

	public JdkLogger(String loggerName, final Class<?> cls) {
		this(java.util.logging.Logger.getLogger(loggerName), cls);
	}

	public JdkLogger(final java.util.logging.Logger logger, final Class<?> sourceClass) {
		this.logger = logger;
		this.sourceClass = sourceClass.getName();
		this.loggerName = logger.getName();

		ClassLoader classLoader = sourceClass.getClassLoader();
		String classLoaderToString = classLoader.toString();
		this.module = extractModuleFromClassLoaderInfo(classLoaderToString);
	}

	private String extractModuleFromClassLoaderInfo(String classLoaderToString) {
		if (classLoaderToString != null && classLoaderToString.trim().length() > 0) {
			String lines[] = classLoaderToString.split("\\r?\\n");
			if (lines != null && lines.length > 0) {
				if (lines[0].startsWith("ParallelWebappClassLoader")) {
					if (lines.length > 1 && lines[1].contains("context:")) {
						int idx = lines[1].indexOf("context:");
						if (idx >= 0) {
							String context = lines[1].substring(idx + 8).trim();
							return context;
						}
					}
				} else if (lines[0].contains("WireEnricherClassLoader")) {
					return "Wire";
				} else if (lines[0].startsWith("ModuleClassLoader:")) {
					String context = lines[0].substring(18).trim();
					int idx = context.lastIndexOf(':');
					if (idx > 0) {
						context = context.substring(idx + 1);
					}
					return context;
				}
			}
		}
		return null;
	}

	@Override
	public void trace(final String msg) {
		createLogRecordAndLog(Level.FINER, this.sourceClass, msg, null);
	}

	@Override
	public void trace(final Throwable ex) {
		createLogRecordAndLog(Level.FINER, this.sourceClass, ((ex == null) ? "" : ex.getMessage()), ex);
	}

	@Override
	public void trace(final String msg, final Throwable ex) {
		createLogRecordAndLog(Level.FINER, this.sourceClass, msg, ex);
	}

	@Override
	public void debug(final String msg) {
		createLogRecordAndLog(Level.FINE, this.sourceClass, msg, null);
	}

	@Override
	public void debug(final Throwable ex) {
		createLogRecordAndLog(Level.FINE, this.sourceClass, ((ex == null) ? "" : ex.getMessage()), ex);
	}

	@Override
	public void debug(final String msg, final Throwable ex) {
		createLogRecordAndLog(Level.FINE, this.sourceClass, msg, ex);
	}

	@Override
	public void info(final String msg) {
		createLogRecordAndLog(Level.INFO, this.sourceClass, msg, null);
	}

	@Override
	public void info(final Throwable ex) {
		createLogRecordAndLog(Level.INFO, this.sourceClass, ((ex == null) ? "" : ex.getMessage()), ex);
	}

	@Override
	public void info(final String msg, final Throwable ex) {
		createLogRecordAndLog(Level.INFO, this.sourceClass, msg, ex);
	}

	@Override
	public void warn(final String msg) {
		createLogRecordAndLog(Level.WARNING, this.sourceClass, msg, null);
	}

	@Override
	public void warn(final Throwable ex) {
		createLogRecordAndLog(Level.WARNING, this.sourceClass, ((ex == null) ? "" : ex.getMessage()), ex);
	}

	@Override
	public void warn(final String msg, final Throwable ex) {
		createLogRecordAndLog(Level.WARNING, this.sourceClass, msg, ex);
	}

	@Override
	public void error(final String msg) {
		createLogRecordAndLog(Level.SEVERE, this.sourceClass, msg, null);
	}

	@Override
	public void error(final Throwable ex) {
		createLogRecordAndLog(Level.SEVERE, this.sourceClass, ((ex == null) ? "" : ex.getMessage()), ex);
	}

	@Override
	public void error(final String msg, final Throwable ex) {
		createLogRecordAndLog(Level.SEVERE, this.sourceClass, msg, ex);
	}

	protected void createLogRecordAndLog(Level level, String sourceClass, String msg, Throwable thrown) {
		// Implementation note: we do not use/set the sourceMethod, so we will use it as a placeholder for the module
		this.logger.logp(level, sourceClass, module, msg, thrown);
	}

	@Override
	public boolean isTraceEnabled() {
		return this.logger.isLoggable(Level.FINER);
	}

	@Override
	public boolean isDebugEnabled() {
		return this.logger.isLoggable(Level.FINE);
	}

	@Override
	public boolean isInfoEnabled() {
		return this.logger.isLoggable(Level.INFO);
	}

	@Override
	public boolean isWarnEnabled() {
		return this.logger.isLoggable(Level.WARNING);
	}

	@Override
	public boolean isErrorEnabled() {
		return this.logger.isLoggable(Level.SEVERE);
	}

	@Override
	public void pushContext(String context) {
		com.braintribe.logging.ndc.mbean.NestedDiagnosticContext.pushContext(context);
	}
	@Override
	public void popContext() {
		com.braintribe.logging.ndc.mbean.NestedDiagnosticContext.popContext();
	}
	@Override
	public void removeContext() {
		com.braintribe.logging.ndc.mbean.NestedDiagnosticContext.removeContext();
	}

	@Override
	public void clearMdc() {
		com.braintribe.logging.ndc.mbean.NestedDiagnosticContext.clearMdc();
	}
	@Override
	public Object get(String key) {
		return com.braintribe.logging.ndc.mbean.NestedDiagnosticContext.get(key);
	}
	@Override
	public void put(String key, String value) {
		com.braintribe.logging.ndc.mbean.NestedDiagnosticContext.put(key, value);
	}
	@Override
	public void remove(String key) {
		com.braintribe.logging.ndc.mbean.NestedDiagnosticContext.remove(key);
	}

	protected Level translateLogLevel(LogLevel logLevel) {
		if (logLevel == null) {
			return null;
		}
		switch (logLevel) {
			case TRACE:
				return Level.FINEST;
			case DEBUG:
				return Level.FINE;
			case INFO:
				return Level.INFO;
			case WARN:
				return Level.WARNING;
			case ERROR:
				return Level.SEVERE;
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

			List<java.util.logging.Logger> btLoggers = getBtLoggers();
			List<Handler> allHandlers = this.getHandlers();

			List<Handler> handlers = this.getMostProbableHandlers(allHandlers);

			if (handlers != null) {
				handlers.forEach(h -> h.setLevel(newLevel));
			}

			Level lowestCommonLevel = this.getLowestCommonLevel(allHandlers, newLevel);
			for (java.util.logging.Logger btLogger : btLoggers) {
				btLogger.setLevel(lowestCommonLevel);
			}

		} catch (Throwable t) {
			this.logger.logp(Level.FINE, JdkLogger.class.getName(), "setLogLevel", "Could not assign log level " + newLevel, t);
		}
	}

	protected Level getLowestCommonLevel(List<Handler> allHandlers, Level newLevel) {
		if (allHandlers == null || allHandlers.isEmpty()) {
			return newLevel;
		}
		Level lowestLevel = newLevel;
		for (Handler handler : allHandlers) {
			Level handlerLevel = handler.getLevel();
			if (handlerLevel != null) {
				if (lowestLevel == null) {
					lowestLevel = handlerLevel;
				} else {
					if (handlerLevel.intValue() < lowestLevel.intValue()) {
						lowestLevel = handlerLevel;
					}
				}
			}
		}
		return lowestLevel;
	}

	protected List<Handler> getMostProbableHandlers(List<Handler> handlers) {
		if (handlers == null || handlers.isEmpty()) {
			return null;
		}
		List<Handler> winners = new ArrayList<>();
		List<Handler> others = new ArrayList<>();
		for (Handler candidate : handlers) {
			// We're not primarily interested in ConsoleHandlers
			if (!(candidate instanceof ConsoleHandler)) {

				String candidateFilename = this.getHandlerFileName(candidate);

				// Prefer the handler that has not warn in the name
				if (candidateFilename.toLowerCase().indexOf("warn") != -1) {
					others.add(0, candidate);
				} else {
					winners.add(candidate);
				}

			} else {
				others.add(candidate);
			}
		}
		if (winners.isEmpty() && !others.isEmpty()) {
			winners.add(others.get(0));
		}
		return winners;
	}

	protected String getHandlerFileName(Handler handler) {
		if (handler == null) {
			return "none";
		}
		try {
			// Note: we had to drop support for java.util.logging.FileHandler as reflection access to private members will be
			// prohibited in Java 11
			// and above
			// Same goes for org.apache.juli.AsyncFileHandler and org.apache.juli.FileHandler
			if (handler instanceof ConsoleHandler) {
				return "console";
			} else if (handler.getClass().getName().equals("com.braintribe.logging.juli.handlers.FileHandler")) {
				Method method = handler.getClass().getMethod("getFileKey", (Class[]) null);
				String fileKeyValue = (String) method.invoke(handler, (Object[]) null);
				return fileKeyValue;
			}
		} catch (Throwable t) {
			this.logger.logp(Level.FINE, JdkLogger.class.getName(), "getHandlerFileName", "Could not get the filename of " + handler, t);
		}
		return "unknown";
	}

	protected List<Handler> getHandlers() {

		List<Handler> handlerList = new ArrayList<>();

		try {
			// This is interesting but true: in the Cloud installation the Tomcat instance may have multiple
			// root loggers, cascaded by parent-relationship.
			java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
			while (rootLogger != null) {
				Handler[] handlerArray = rootLogger.getHandlers();
				if (handlerArray != null && handlerArray.length > 0) {
					handlerList.addAll(Arrays.asList(handlerArray));
				}
				rootLogger = rootLogger.getParent();
			}
		} catch (Throwable t) {
			this.logger.logp(Level.FINE, JdkLogger.class.getName(), "getHandlers", "Could not get handlers", t);
		}

		return handlerList;
	}

	protected Method getGetClassLoaderMethod(Class<?> cls) {
		while (cls != null) {
			try {
				Method m = cls.getDeclaredMethod("getClassLoaderInfo", ClassLoader.class);
				return m;
			} catch (Throwable t) {
				cls = cls.getSuperclass();
			}
		}
		this.logger.logp(Level.FINE, JdkLogger.class.getName(), "getGetClassLoaderMethod", "Could not find any getClassLoaderInfo method.");
		return null;
	}

	protected List<java.util.logging.Logger> getBtLoggers() {
		LogManager logManager = LogManager.getLogManager();

		List<java.util.logging.Logger> result = new ArrayList<>();
		try {

			Enumeration<String> names = logManager.getLoggerNames();
			if (names != null) {
				while (names.hasMoreElements()) {
					String name = names.nextElement();

					if (btLoggerPackages.stream().anyMatch(p -> name.equals(p))) {
						java.util.logging.Logger l = logManager.getLogger(name);
						result.add(l);
					}
				}
			}

		} catch (Throwable t) {
			this.logger.logp(Level.FINE, JdkLogger.class.getName(), "getBtLoggers", "Error while trying to change the log level.", t);
		}
		if (result.size() <= 1) {
			this.logger.logp(Level.FINE, JdkLogger.class.getName(), "getBtLoggers", "Could not find any BT Loggers");
		}
		return result;
	}

	@Override
	public void registerManagedLoggerPackage(String packagePrefix) {

		LogManager logManager = LogManager.getLogManager();
		java.util.logging.Logger customLogger = logManager.getLogger(packagePrefix);
		java.util.logging.Logger btLogger = logManager.getLogger("com.braintribe");
		if (btLogger != null) {
			customLogger.setLevel(btLogger.getLevel());
		}

		Set<String> newSet = new HashSet<>(btLoggerPackages);
		newSet.add(packagePrefix);
		btLoggerPackages = newSet;
	}

	@Override
	public Set<String> getManagedLoggerPackages() {
		return new HashSet<>(btLoggerPackages);
	}
}
