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
package com.braintribe.logging.juli.filters.standard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.logging.juli.ConfigurationException;
import com.braintribe.logging.juli.JulExtensionsHelpers;
import com.braintribe.logging.juli.filters.MatchEverythingFilter;
import com.braintribe.logging.juli.filters.MatchNothingFilter;
import com.braintribe.logging.juli.filters.logger.LoggerFilter;
import com.braintribe.logging.juli.formatters.simple.SimpleFormatter1;

/**
 * This is the standard BT {@link Filter} implementation which filters based on a configurable condition (an XML string). There are two ways to set
 * the condition:
 * <ul>
 * <li>via property 'condition'. Example:
 * <code>com.braintribe.logging.juli.filters.standard.StandardFilter.condition = &lt;contains&gt;searchedText&lt;/contains&gt;</code></li>
 * <li>via property 'conditionFile' (for more complex conditions). Example:
 * <code>com.braintribe.logging.juli.filters.standard.StandardFilter.conditionFile=conf/example_filter_condition.xml</code></li>
 * </ul>
 * The condition XML supports the following elements:
 * <ul>
 * <li>true: match everything (mainly for tests). Example: <code>&lt;true/&gt;</code></li>
 * <li>false: match nothing (mainly for tests). Example: <code>&lt;false/&gt;</code></li>
 * <li>contains: only include records where the message contains the specified text. Example:
 * <code>&lt;contains&gt;searched text&lt;/contains&gt;</code></li>
 * <li>matches: only include records where the message matches the specified regular expression. Example:
 * <code>&lt;matches&gt;.*searched text.*&lt;/matches&gt;</code></li>
 * <li>not: match, if the child doesn't match, and vice-versa. Example:
 * <code>&lt;not&gt;&lt;contains&gt;.*text to be filtered out.*&lt;/contains&gt;&lt;/not&gt;</code></li>
 * <li>and: match, unless any child doesn't match. Example:
 * <code>&lt;and&gt;&lt;matches&gt;.*searched text.*&lt;/matches&gt;&lt;contains&gt;other searched text&lt;/contains&gt;&lt;/and&gt;</code></li>
 * <li>or: don't match, unless at least one child matches. Example:
 * <code>&lt;or&gt;&lt;matches&gt;.*searchedText.*&lt;/matches&gt;&lt;contains&gt;otherSearchText&lt;/contains&gt;&lt;/or&gt;</code></li>
 * <li>loggerNames: comma-separated list of logger names (+ optionally also log levels). For further information and examples see
 * {@link LoggerFilter}.</li>
 * </ul>
 * If the condition evaluates to <code>true</code>, the message will be logged, otherwise it will be omitted.
 * <p/>
 * Similar to the {@link com.braintribe.logging.juli.formatters.simple.SimpleFormatter}﻿ one can configure multiple <code>StandardFilter</code>
 * instances by adding a number ( <code>1</code> to <code>10</code>), see {@link SimpleFormatter1}.
 */
public class StandardFilter implements Filter {

	public static final String PROPERTY_CONDITION = "condition";
	public static final String PROPERTY_CONDITION_FILE = "conditionFile";

	private Filter delegate;

	/**
	 * Creates a new <code>StandardFilter</code> instance.
	 *
	 * @throws ConfigurationException
	 *             if any error occurs.
	 */
	public StandardFilter() {
		try {
			final String methodNameForLogging = StandardFilter.class.getSimpleName();
			String conditionString = JulExtensionsHelpers.getProperty(getClass(), PROPERTY_CONDITION, false, null, String.class);
			final String conditionFilePath = JulExtensionsHelpers.getProperty(getClass(), PROPERTY_CONDITION_FILE, false, null, String.class);

			JulExtensionsHelpers.debugIfEnabled(getClass(), methodNameForLogging,
					"Configuring " + getClass().getSimpleName() + ". Condition: " + conditionString + ", condition file: " + conditionFilePath
							+ ", working directory: " + JulExtensionsHelpers.getWorkingDirectoryPath());

			if (conditionString == null && conditionFilePath == null) {
				throw new ConfigurationException(
						"One of the following properties has to be set: " + PROPERTY_CONDITION + ", " + PROPERTY_CONDITION_FILE);
			}

			if (conditionString != null && conditionFilePath != null) {
				throw new ConfigurationException(
						"Only one of the following properties may be set: " + PROPERTY_CONDITION + ", " + PROPERTY_CONDITION_FILE);
			}

			if (conditionFilePath != null) {
				final File conditionFile = new File(conditionFilePath);
				if (!conditionFile.exists()) {
					throw new ConfigurationException("File '" + conditionFile.getAbsolutePath() + "' doesn't exist!");
				}
				try {
					conditionString = JulExtensionsHelpers.readStringFromFile(conditionFile, "UTF-8");
				} catch (final IOException e) {
					throw new ConfigurationException("Error while reading from file '" + conditionFile.getAbsolutePath() + "'!", e);
				}
			}

			Document conditionDocument;
			try {
				conditionDocument = JulExtensionsHelpers.parseXmlString(conditionString);
			} catch (final RuntimeException e) {
				throw new ConfigurationException("Error while parsing condition string '" + conditionString + "'!", e);
			}
			JulExtensionsHelpers.debugIfEnabled(getClass(), methodNameForLogging, "Configuring " + getClass().getSimpleName()
					+ ". Formatted Condition:\n" + JulExtensionsHelpers.toFormattedString(conditionDocument));

			this.delegate = parseConditionAndCreateFilter(conditionDocument.getDocumentElement());

			JulExtensionsHelpers.debugIfEnabled(getClass(), methodNameForLogging, "Successfully instantiated " + getClass().getSimpleName() + ".");
		} catch (final Exception e) { // NOSONAR intentionally not propagating the exception
			// Catch any exception and log it.
			// (We do this, because thrown exceptions are not always shown in Eclipse/Tomcat output.)
			final String exceptionMessage = "Error while instantiating " + getClass().getSimpleName() + "!";
			System.out.println(exceptionMessage);
			e.printStackTrace(System.out);
			throw new ConfigurationException(exceptionMessage);
		}
	}

	/**
	 * Parses the condition element and returns the respective filter. See {@link StandardFilter} documentation for more information.
	 */
	static Filter parseConditionAndCreateFilter(final Element element) {
		final String type = element.getNodeName();
		final String content = element.getTextContent();
		final List<Element> childElements = JulExtensionsHelpers.getChildElements(element);
		Filter result;
		if (type.equals("matches")) {
			result = new RegexFilter(content);
		} else if (type.equals("contains")) {
			result = new ContainsFilter(content);
		} else if (type.equals(LoggerFilter.PROPERTY_LOGGERNAMES)) {
			result = new LoggerFilter(content);
		} else if (type.equals("not")) {
			if (childElements.size() != 1) {
				throw new ConfigurationException("Cannot process 'not' condition with " + childElements.size() + " element(s)!");
			}
			result = new NotFilter(parseConditionAndCreateFilter(childElements.get(0)));
		} else if (type.equals("and") || type.equals("or")) {
			final List<Filter> childFilters = new ArrayList<>();
			for (final Element childElement : childElements) {
				childFilters.add(parseConditionAndCreateFilter(childElement));
			}
			if (type.equals("and")) {
				result = new AndFilter(childFilters);
			} else {
				result = new OrFilter(childFilters);
			}
		} else if (type.equals("true")) {
			result = new MatchEverythingFilter();
		} else if (type.equals("false")) {
			result = new MatchNothingFilter();
		} else {
			throw new ConfigurationException("Unsupported condition type '" + type + "'!");
		}

		return result;
	}

	@Override
	public boolean isLoggable(final LogRecord logRecord) {
		return this.delegate.isLoggable(logRecord);
	}

	private static abstract class MessageFilter implements Filter {

		@Override
		public final boolean isLoggable(final LogRecord logRecord) {
			String message = logRecord.getMessage();
			if (message != null) {
				return isLoggable(message);
			}
			return false;
		}

		protected abstract boolean isLoggable(String message);
	}

	private static class RegexFilter extends MessageFilter {

		private final Pattern pattern;

		public RegexFilter(final String regex) {
			this.pattern = Pattern.compile(regex);
		}

		@Override
		protected boolean isLoggable(final String message) {
			return this.pattern.matcher(message).matches();
		}

	}

	private static class ContainsFilter extends MessageFilter {

		private final String searchedText;

		public ContainsFilter(final String searchedText) {
			this.searchedText = searchedText;
		}

		@Override
		protected boolean isLoggable(final String message) {
			return message.contains(this.searchedText);
		}

	}

	private static class NotFilter implements Filter {
		private final Filter delegate;

		public NotFilter(final Filter delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean isLoggable(final LogRecord record) {
			return !this.delegate.isLoggable(record);
		}

	}

	private static class AndFilter implements Filter {

		private final List<Filter> delegates;

		public AndFilter(final List<Filter> delegates) {
			this.delegates = delegates;
		}

		@Override
		public boolean isLoggable(final LogRecord record) {
			for (final Filter delegate : this.delegates) {
				if (!delegate.isLoggable(record)) {
					return false;
				}
			}
			return true;
		}
	}

	private static class OrFilter implements Filter {

		private final List<Filter> delegates;

		public OrFilter(final List<Filter> delegates) {
			this.delegates = delegates;
		}

		@Override
		public boolean isLoggable(final LogRecord record) {
			for (final Filter delegate : this.delegates) {
				if (delegate.isLoggable(record)) {
					return true;
				}
			}
			return false;
		}
	}
}
