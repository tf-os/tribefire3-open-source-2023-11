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
package com.braintribe.logging.juli.formatters.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import com.braintribe.logging.juli.ConfigurationException;
import com.braintribe.logging.juli.JulExtensionsHelpers;
import com.braintribe.logging.juli.formatters.commons.CompiledFormatter;
import com.braintribe.logging.juli.formatters.commons.LogRecordField;
import com.braintribe.logging.juli.formatters.simple.SimpleFormatter;

/**
 * This is a formatter which produces JSON output. Each record is printed as one line. By the default the formatter will print all
 * {@link LogRecordField}s, which are the same as provided by the simple formatter. One can also specify the fields to include via property
 * {@value #FIELDS_PROPERTY}. The property expects a comma separated list of either field names, e.g. <code>date,level,message</code>. Alternatively
 * one can also specify the field index numbers, e.g. <code>1,4,5</code>. The numbers are the same as in
 * {@link #com.braintribe.logging.juli.formatters.simple.SimpleFormatter.format(LogRecord)}.
 * <p>
 * The format for field {@link LogRecordField#DATE} can be configured via property {@value #DATEFORMAT_PROPERTY}. The format is the same as used by
 * {@link SimpleFormatter#format(LogRecord)} (when formatting the date part). Default is {@value #DATEFORMAT_PROPERTY_DEFAULT}.
 *
 * @author michael.lafite
 */
public class JsonFormatter extends Formatter {

	private final List<Pair<LogRecordField, Function<LogRecord, Object>>> logRecordFields = new ArrayList<>(LogRecordField.values().length);

	private static final String FIELDS_PROPERTY = "fields";
	static final String FIELDS_PROPERTY_DEFAULT = Arrays.asList(LogRecordField.values()).stream().map(LogRecordField::getName)
			.collect(Collectors.joining(","));
	private static final String DATEFORMAT_PROPERTY = "dateFormat";
	static final String DATEFORMAT_PROPERTY_DEFAULT = "%14$s";

	private CompiledFormatter compiledFormatter;

	public JsonFormatter() {
		try {
			String fieldsStringCommaSeparated = JulExtensionsHelpers
					.getProperty(getClass(), FIELDS_PROPERTY, true, FIELDS_PROPERTY_DEFAULT, String.class).trim();
			String dateFormat = JulExtensionsHelpers.getProperty(getClass(), DATEFORMAT_PROPERTY, true, DATEFORMAT_PROPERTY_DEFAULT, String.class)
					.trim();
			initialize(fieldsStringCommaSeparated, dateFormat);
		} catch (final ConfigurationException e) {
			// logging information before re-throwing exception, because the thrown exception may be ignored
			String exceptionMessage = "Error while instantiating " + getClass().getSimpleName() + "!";
			System.out.println(exceptionMessage);
			e.printStackTrace(System.out);
			throw new ConfigurationException(exceptionMessage, e);
		}
	}

	public JsonFormatter(String fieldsStringCommaSeparated, String dateFormat) {
		initialize(fieldsStringCommaSeparated, dateFormat);
	}

	private void initialize(String fieldsStringCommaSeparated, String dateFormat) {
		compiledFormatter = new CompiledFormatter(dateFormat, null);
		setFields(fieldsStringCommaSeparated);
	}

	private void setFields(String fieldsStringCommaSeparated) {
		for (String fieldNameOrIndex : fieldsStringCommaSeparated.split(",")) {
			LogRecordField logRecordField = null;
			int index = -1;
			try {
				index = Integer.parseInt(fieldNameOrIndex);
			} catch (NumberFormatException e) {
				// this is expected, if the field is specified via name
				for (LogRecordField i : LogRecordField.values()) {
					if (i.getName().equals(fieldNameOrIndex)) {
						logRecordField = i;
						break;
					}
				}
				if (logRecordField == null) {
					throw new ConfigurationException("Found unknown field name '" + fieldNameOrIndex + "' in property " + FIELDS_PROPERTY + "'"
							+ fieldsStringCommaSeparated + "'. Allowed values are "
							+ Arrays.asList(LogRecordField.values()).stream().map(LogRecordField::getName).collect(Collectors.toList()) + ".");
				}
			}
			if (index > -1) {
				try {
					logRecordField = LogRecordField.byIndex(index);
				} catch (IllegalArgumentException e) {
					throw new ConfigurationException(
							"Found invalid index " + index + " in property " + FIELDS_PROPERTY + "'" + fieldsStringCommaSeparated + "'.", e);
				}
			}

			logRecordFields.add(new Pair<>(logRecordField, compiledFormatter.getFieldAccessFunction(logRecordField)));
		}
	}

	@Override
	public String format(LogRecord logRecord) {
		String fieldsString;
		if (!logRecordFields.isEmpty()) {
			StringBuilder fieldsStringBuilder = new StringBuilder("{");
			for (Pair<LogRecordField, Function<LogRecord, Object>> pair : logRecordFields) {
				Object fieldValue = pair.second().apply(logRecord);
				if (fieldValue != null) {

					if (fieldsStringBuilder.length() > 1) {
						fieldsStringBuilder.append(",");
					}
					fieldsStringBuilder.append('\"');
					fieldsStringBuilder.append(pair.first().getName());
					fieldsStringBuilder.append("\":");

					String fieldValueAsString;
					if (pair.first() == LogRecordField.DATE) {
						try {
							fieldValueAsString = compiledFormatter.formatLogRecord(logRecord);
						} catch (Exception e) {
							throw new RuntimeException("Error while formatting date for log record " + logRecord + ".", e);
						}
					} else {
						fieldValueAsString = fieldValue.toString();
					}

					try {
						fieldsStringBuilder.append('\"');
						fieldsStringBuilder.append(escape(fieldValueAsString));
						fieldsStringBuilder.append('\"');
					} catch (IOException e) {
						throw new UncheckedIOException(
								"Error while escaping value of field " + pair.first().getName() + " '" + fieldValueAsString + "'", e);
					}

				}
			}
			fieldsStringBuilder.append("}\n");
			fieldsString = fieldsStringBuilder.toString();
		} else {
			fieldsString = "{}\n";
		}
		return fieldsString;
	}

	/* The methods below have been copied from com.braintribe.codec.marshaller.json.JsonStreamMarshaller, since we want to avoid adding another
	 * dependency to this very low level library. */

	// ********************** COPY START *******************************************
	public static String escape(String s) throws IOException {
		StringWriter writer = new StringWriter();
		writeEscaped(writer, s);
		return writer.toString();
	}

	private final static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
	private static final char[][] ESCAPES = new char[128][];

	static {
		ESCAPES['"'] = "\\\"".toCharArray();
		ESCAPES['\\'] = "\\\\".toCharArray();
		ESCAPES['\t'] = "\\t".toCharArray();
		ESCAPES['\f'] = "\\f".toCharArray();
		ESCAPES['\n'] = "\\n".toCharArray();
		ESCAPES['\r'] = "\\r".toCharArray();

		for (int i = 0; i < 32; i++) {
			if (ESCAPES[i] == null) {
				ESCAPES[i] = ("\\u00" + HEX_CHARS[i >> 4] + HEX_CHARS[i & 0xF]).toCharArray();
			}
		}
	}

	public static void writeEscaped(Writer writer, String string) throws IOException {
		int len = string.length();
		int s = 0;
		int i = 0;
		char[] esc = null;
		for (; i < len; i++) {
			char c = string.charAt(i);

			if (c < 128) {
				esc = ESCAPES[c];
				if (esc != null) {
					writer.write(string, s, i - s);
					writer.write(esc);
					s = i + 1;
				}
			}
		}
		if (i > s) {
			if (s == 0) {
				writer.write(string);
			} else {
				writer.write(string, s, i - s);
			}
		}
	}
	// ********************** COPY END *******************************************
}
