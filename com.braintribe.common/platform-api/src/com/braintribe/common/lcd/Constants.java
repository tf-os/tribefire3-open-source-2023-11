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
package com.braintribe.common.lcd;

/**
 * Provides frequently used constants such as the {@link #LINE_SEPARATOR} or the {@link #FILE_SEPARATOR}.
 *
 * @author michael.lafite
 *
 * @see Numbers
 */
public class Constants {

	public static final int NO_OFFSET = Numbers.NO_OFFSET;

	public static final String LINE_SEPARATOR;
	public static final String FILE_SEPARATOR;

	/**
	 * The <code>Boolean</code> <code>true</code> representing success. This can be used to improve code readability when a <code>Boolean</code>
	 * represents success or error.
	 *
	 * @see #ERROR
	 */
	public static final Boolean SUCCESS = Boolean.TRUE;

	/**
	 * The <code>Boolean</code> <code>false</code> representing an error. This can be used to improve code readability when a <code>Boolean</code>
	 * represents success or error.
	 *
	 * @see #SUCCESS
	 */
	public static final Boolean ERROR = Boolean.FALSE;

	/**
	 * The value <code>null</code> used to indicate that a status (i.e. {@link #SUCCESS} or {@link #ERROR}) is not known or that any status is fine.
	 * This can be used to improve code readability when a <code>Boolean</code> represents success or error.
	 */
	public static final Boolean SUCCESS_OR_ERROR = null;

	/**
	 * Regular expression that matches line separators on Windows (\r\n), Unix/Linux/MacOS (\n) and old MacOS (\r).
	 */
	public static final String SYSTEM_INDEPENDENT_LINE_SEPARATOR_REGEX = "(\\r\\n|\\n|\\r)";

	/**
	 * The regex that matches one or more whitespace characters.
	 */
	public static final String WHITESPACE_REGEX = "\\s+";

	/**
	 * UTF-8 encoding for java.io and java.lang (see <code>http://docs.oracle.com/javase/1.5.0/docs/guide/intl/encoding.doc.html</code>).
	 */
	public static final String ENCODING_UTF8 = "UTF8";

	/**
	 * UTF-8 encoding for java.nio (see <code>http://docs.oracle.com/javase/1.5.0/docs/guide/intl/encoding.doc.html</code>).
	 */
	public static final String ENCODING_UTF8_NIO = "UTF-8";

	/**
	 * The name of the system property which holds the path to the temporary files directory: {@value #SYSTEMPROPERTY_TEMP_DIR}
	 */
	public static final String SYSTEMPROPERTY_TEMP_DIR = "java.io.tmpdir";

	static {
		LINE_SEPARATOR = LineSeparatorProvider.getSeparator();
		FILE_SEPARATOR = FileSeparatorProvider.getSeparator();
	}

	protected Constants() {
		// no instantiation required (expect for subclass)
	}

	public static String lineSeparator() {
		return LINE_SEPARATOR;
	}

	public static String fileSeparator() {
		return FILE_SEPARATOR;
	}

	public static String encodingUTF8() {
		return ENCODING_UTF8;
	}
}
