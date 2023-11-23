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
package com.braintribe.logging.juli.formatters.simple;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.braintribe.logging.juli.ConfigurationException;
import com.braintribe.logging.juli.JulExtensionsHelpers;
import com.braintribe.logging.juli.formatters.commons.CompiledFormatter;

/**
 * This {@link Formatter} implementation supports different format patterns per handler (see {@link SimpleFormatter1} for more information). It uses
 * {@link String#format(String, Object...)} to format messages. For more information see {@link #format(LogRecord)}.
 *
 * @author michael.lafite
 */
public class SimpleFormatter extends Formatter {

	/**
	 * The format pattern. See {@link #format(LogRecord)} for more info.
	 */
	private final String format;
	private CompiledFormatter compiledFormatter;

	/**
	 * Creates a new <code>SimpleFormatter</code> instance.
	 *
	 * @throws ConfigurationException
	 *             if the format pattern is not specified via property <code>[fully qualified class name].format</code>.
	 */
	public SimpleFormatter() throws ConfigurationException {
		try {
			this.format = JulExtensionsHelpers.getProperty(getClass(), "format", true, null, String.class);
		} catch (final ConfigurationException e) {
			// logging information before re-throwing exception, because the thrown exception may be ignored
			String exceptionMessage = "Error while instantiating " + getClass().getSimpleName() + "!";
			System.out.println(exceptionMessage);
			e.printStackTrace(System.out);
			throw new ConfigurationException(exceptionMessage, e);
		}
		this.initializeFormat();
	}

	public SimpleFormatter(String format) throws ConfigurationException {
		this.format = format;
		this.initializeFormat();
	}

	protected void initializeFormat() throws ConfigurationException {
		try {
			this.compiledFormatter = new CompiledFormatter(this.format, "");

		} catch (final ConfigurationException e) {
			// logging information before re-throwing exception, because the thrown exception may be ignored
			String exceptionMessage = "Error while instantiating " + getClass().getSimpleName() + "!";
			System.out.println(exceptionMessage);
			e.printStackTrace(System.out);
			throw new ConfigurationException(exceptionMessage, e);
		}
		JulExtensionsHelpers.debugIfEnabled(getClass(), SimpleFormatter.class.getSimpleName(),
				"Successfully instantiated " + getClass().getSimpleName() + ". Configured format: " + this.format);
	}

	/**
	 * This method returns a formatted string representation of the passed <code>logRecord</code>. Like {@link java.util.logging.SimpleFormatter} it
	 * uses {@link String#format(String, Object...)} for formatting. Therefore the pattern syntax is similar, but the passed arguments are slightly
	 * different:
	 * <ol>
	 * <li>format: the configured <code>format</code> pattern (which is mandatory and specified via property
	 * <code>[fully qualified class name].format</code>).</li>
	 *
	 * <li>date: a {@link Date} instance representing the <code>logRecord</code>'s {@link LogRecord#getMillis() timestamp}.</li>
	 *
	 * <li>source info: a string providing information about the source. If the {@link LogRecord#getSourceClassName() source class name} and the
	 * {@link LogRecord#getSourceMethodName() source method name} are available, the source info contains both. If only the class name is available,
	 * the class name is used. Otherwise the logger name is used as fallback.</li>
	 *
	 * <li>logger name: the logger name (as returned by {@link LogRecord#getLoggerName()}).</li>
	 *
	 * <li>level: the log level name. Note that the name is not localized.</li>
	 *
	 * <li>message: the original message (as returned by {@link LogRecord#getMessage()}). This method does not get a
	 * {@link Formatter#formatMessage(LogRecord) localized version} of the message.</li>
	 *
	 * <li>throwableInfo: a multi-line string containing the throwable message and stack trace or an empty string, if there is no
	 * {@link LogRecord#getThrown() throwable}.</li>
	 *
	 * <li>short logger name: the short logger name (if available): if the logger name contains at least one dot ('.') and that dot is neither at the
	 * beginning nor at the end of the string (e.g. a fully qualified class name like <code>com.example.Test</code>), the short logger name is just
	 * the substring after the last dot. In other words, for class names this method returns the {@link Class#getSimpleName() simple class name}.
	 * Otherwise the logger name itself is used as fallback.</li>
	 *
	 * <li>logger name with short packages:</li>
	 *
	 * <li>NDC- Nested Diagnostic Context</li>
	 *
	 * <li>MDC- Mapped Diagnostic Context</li>
	 *
	 * <li>source and logger: full source class name, which might be followed by a logger name in square brackets, in case the two names are
	 * different</li>
	 *
	 * <li>source class name: full name of the source class if available, logger name otherwise</li>
	 *
	 * <li>condensed exception: The condensed stack trace (if available) that improves readability.</li>
	 * </ol>
	 */
	@Override
	public String format(final LogRecord logRecord) {
		try {
			return this.compiledFormatter.formatLogRecord(logRecord);
		} catch (Exception e) { // NOSONAR: return string representation of exception instead of re-throw
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			return sw.toString();
		}
	}

	/* Can be used to show the format string and all parameters (for debugging purposes) */
	protected void createLogDebugOutput(String formatParam, Object... args) {
		StringBuilder sb = new StringBuilder();
		sb.append("Log Output\n");
		sb.append("Format: " + formatParam + "\n");
		if (args != null) {
			int index = 1;
			for (Object o : args) {
				sb.append("" + index + ": \"" + o + "\"\n");
				index++;
			}
		}
		sb.append("Result: " + String.format(formatParam, args));
		System.out.println(sb.toString());
	}

}
