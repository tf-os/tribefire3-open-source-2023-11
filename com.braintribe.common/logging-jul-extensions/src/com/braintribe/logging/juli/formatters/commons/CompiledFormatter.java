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
package com.braintribe.logging.juli.formatters.commons;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.DuplicateFormatFlagsException;
import java.util.FormatFlagsConversionMismatchException;
import java.util.FormatterClosedException;
import java.util.GregorianCalendar;
import java.util.IllegalFormatCodePointException;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatFlagsException;
import java.util.IllegalFormatPrecisionException;
import java.util.IllegalFormatWidthException;
import java.util.Locale;
import java.util.Map;
import java.util.MissingFormatWidthException;
import java.util.TimeZone;
import java.util.UnknownFormatConversionException;
import java.util.UnknownFormatFlagsException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.juli.handlers.util.NullWriter;
import com.braintribe.logging.ndc.mbean.NestedDiagnosticContext;

@SuppressWarnings("all")
public final class CompiledFormatter {

	protected String format;
	protected FormatString[] fsa = null;

	private final Locale l;

	private final char zero;
	private int maxIndex = 0;

	private static final DateTimeFormatter ISO8601_DATE_WITH_MS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
			.withLocale(Locale.US);

	/**
	 * Used by {@link #format(LogRecord)} to cache short logger names.
	 */
	private static final Map<String, String> loggerNamesToShortLoggerNames = new ConcurrentHashMap<>();

	/**
	 * Used by {@link #format(LogRecord)} to cache logger names with short packages.
	 */
	private static final Map<String, String> loggerNamesToLoggerNamesWithShortPackages = new ConcurrentHashMap<>();

	private Object logRecordFieldNullValue;

	/**
	 * Instantiates a new <code>CompiledFormatter</code>.
	 *
	 * @param format
	 *            the format
	 * @param logRecordFieldNullValue
	 *            the value that is used, if a log record field is not available, e.g. {@link LogRecordField#THROWABLE}.
	 */
	public CompiledFormatter(String format, Object logRecordFieldNullValue) {
		this.l = Locale.getDefault(Locale.Category.FORMAT);
		this.zero = getZero(l);
		this.format = format;
		this.fsa = parse(format);
		this.logRecordFieldNullValue = logRecordFieldNullValue;
	}

	private static char getZero(Locale l) {
		if ((l != null) && !l.equals(Locale.US)) {
			DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(l);
			return dfs.getZeroDigit();
		} else {
			return '0';
		}
	}

	/**
	 * Returns the locale set by the construction of this formatter.
	 *
	 * <p>
	 * The {@link #format(java.util.Locale,String,Object...) format} method for this object which has a locale argument does not change this value.
	 *
	 * @return {@code null} if no localization is applied, otherwise a locale
	 *
	 * @throws FormatterClosedException
	 *             If this formatter has been closed by invoking its {@link #close()} method
	 */
	public Locale locale() {
		return l;
	}

	@Override
	public String toString() {
		return this.format;
	}

	public Function<LogRecord, Object> getFieldAccessFunction(int logRecordFieldIndex) {
		return getFieldAccessFunction(LogRecordField.byIndex(logRecordFieldIndex));
	}

	public Function<LogRecord, Object> getFieldAccessFunction(LogRecordField logRecordField) {
		switch (logRecordField) {
			case CONDENSED_THROWABLE:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						final Throwable throwable = logRecord.getThrown();
						if (throwable == null) {
							return logRecordFieldNullValue;
						}
						return System.lineSeparator() + Exceptions.stringify(logRecord.getThrown());
					}
				};
			case DATE:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						Calendar calendar = new GregorianCalendar(l);
						calendar.setTimeInMillis(logRecord.getMillis());
						return calendar;
					}
				};
			case LEVEL:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						return logRecord.getLevel().toString();
					}
				};
			case LOGGERNAME:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						return logRecord.getLoggerName();
					}
				};
			case LOGGERNAMESHORTPACKAGES:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						return getLoggerNameWithShortPackages(logRecord.getLoggerName());
					}
				};
			case MDC:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						return getMappedDiagnosticContext();
					}
				};
			case MESSAGE:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						return logRecord.getMessage();
					}
				};
			case NDC:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						return getNestedDiagnosticContext();
					}
				};
			case SHORTLOGGERNAME:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						return getShortLoggerName(logRecord.getLoggerName());
					}
				};
			case SOURCE:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						final String loggerName = logRecord.getLoggerName();
						final String sourceMethodName = logRecord.getSourceMethodName();
						String sourceClassName = logRecord.getSourceClassName();
						String sourceInfo = "";
						if (sourceClassName == null) {
							sourceInfo = loggerName;
							sourceClassName = loggerName;
						} else if (sourceMethodName == null) {
							sourceInfo = sourceClassName;
						} else {
							sourceInfo = sourceClassName + "." + sourceMethodName + "()";
						}
						return sourceInfo;
					}
				};
			case SOURCE_CLASS_NAME:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						return logRecord.getSourceClassName();
					}
				};
			case SOURCE_AND_LOGGER:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						return getSourceAndLoggerName(logRecord);
					}
				};
			case THROWABLE:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						final Throwable throwable = logRecord.getThrown();
						if (throwable == null) {
							return logRecordFieldNullValue;
						} else {
							final StackTracePrintWriter writer = new StackTracePrintWriter();
							writer.println();
							throwable.printStackTrace(writer);
							return writer.toString();
						}
					}
				};
			case ISO8601UTC:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						Date date = new Date(logRecord.getMillis());
						ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.systemDefault());
						return ISO8601_DATE_WITH_MS_FORMAT.format(dateTime);
					}
				};
			case THREAD:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						return Thread.currentThread().getName();
					}
				};
			case MODULE:
				return new Function<LogRecord, Object>() {
					@Override
					public Object apply(LogRecord logRecord) {
						String module = logRecord.getSourceMethodName();
						return module != null ? module : "";
					}
				};
			default:
				break;
		}
		return logRecord -> null;
	}

	/**
	 * Returns the short logger name for the passed logger name. See {@link #format(LogRecord)} for more info.
	 */
	private static String getShortLoggerName(final String loggerName) {
		if (loggerName == null) {
			return null;
		}

		String shortLoggerName;
		shortLoggerName = loggerNamesToShortLoggerNames.get(loggerName);
		if (shortLoggerName == null) {
			final int lastDotIndex = loggerName.lastIndexOf('.');
			if (lastDotIndex > 0 && lastDotIndex < loggerName.length() - 1) {
				shortLoggerName = loggerName.substring(lastDotIndex + 1);
			} else {
				shortLoggerName = loggerName;
			}
			loggerNamesToShortLoggerNames.put(loggerName, shortLoggerName);
		}

		return shortLoggerName;
	}

	/**
	 * Returns the logger name with short packages for the passed logger name. See {@link #format(LogRecord)} for more info.
	 */
	private static String getLoggerNameWithShortPackages(final String loggerName) {
		if ((loggerName == null) || (loggerName.trim().length() == 0)) {
			return null;
		}

		String shortLoggerName;

		shortLoggerName = loggerNamesToLoggerNamesWithShortPackages.get(loggerName);
		if (shortLoggerName == null) {

			String[] split = loggerName.split("\\.");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < (split.length - 1); ++i) {
				if (split[i].length() > 0) {
					sb.append(split[i].substring(0, 1));
					sb.append(".");
				}
			}
			sb.append(split[split.length - 1]);
			shortLoggerName = sb.toString();

			loggerNamesToLoggerNamesWithShortPackages.put(loggerName, shortLoggerName);
		}

		return shortLoggerName;
	}

	private static String getNestedDiagnosticContext() {
		StringBuilder sb = new StringBuilder("");

		Deque<String> ndcStack = NestedDiagnosticContext.getNdc();
		if ((ndcStack != null) && (!ndcStack.isEmpty())) {
			for (String ndcElement : ndcStack) {
				if (ndcElement != null) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(ndcElement);
				}
			}
		}

		return sb.toString();
	}

	private static String getMappedDiagnosticContext() {
		StringBuilder sb = new StringBuilder("");

		Map<String, String> mdc = NestedDiagnosticContext.getMdc();
		if ((mdc != null) && (!mdc.isEmpty())) {
			for (Map.Entry<String, String> entry : mdc.entrySet()) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(entry.getKey()).append("=").append(entry.getValue());
			}
		}

		return sb.toString();
	}

	private static String getSourceAndLoggerName(LogRecord logRecord) {
		String source = logRecord.getSourceClassName();
		String logger = logRecord.getLoggerName();

		boolean hasTwoDifferentValues = source != null && !logger.equals(source);
		return hasTwoDifferentValues ? source + "[" + logger + "]" : logger;
	}

	private static class StackTracePrintWriter extends PrintWriter {

		public StackTracePrintWriter() {
			super(NullWriter.INSTANCE, false);
		}

		private StringBuilder sb = new StringBuilder(10240);

		@Override
		public void println(String x) {
			sb.append(x);
			sb.append(System.lineSeparator());
		}
		@Override
		public void println() {
			sb.append(System.lineSeparator());
		}
		@Override
		public void println(Object x) {
			String s = String.valueOf(x);
			sb.append(s);
			sb.append(System.lineSeparator());
		}
		@Override
		public String toString() {
			return sb.toString();
		}
	}

	public String formatLogRecord(LogRecord logRecord) throws Exception {
		if (this.fsa == null) {
			throw new RuntimeException("No format was compiled.");
		}

		StringBuilder sb = new StringBuilder();

		// TODO: As soon as we switched to Java9+, this should be replaced by logRecord.getInstant()
		Calendar calendar = new GregorianCalendar(l == null ? Locale.US : l);
		calendar.setTimeInMillis(logRecord.getMillis());

		for (FormatString element : fsa) {
			element.print(sb, calendar, logRecord, l);
		}
		return sb.toString();
	}

	// %[argument_index$][flags][width][.precision][t]conversion
	private static final String formatSpecifier = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";

	private static Pattern fsPattern = Pattern.compile(formatSpecifier);

	/**
	 * Finds format specifiers in the format string.
	 */
	private FormatString[] parse(String s) {
		ArrayList<FormatString> al = new ArrayList<>();
		Matcher m = fsPattern.matcher(s);
		for (int i = 0, len = s.length(); i < len;) {
			if (m.find(i)) {
				// Anything between the start of the string and the beginning
				// of the format specifier is either fixed text or contains
				// an invalid format string.
				if (m.start() != i) {
					// Make sure we didn't miss any invalid format specifiers
					checkText(s, i, m.start());
					// Assume previous characters were fixed text
					al.add(new FixedString(s.substring(i, m.start())));
				}

				al.add(new FormatSpecifier(m));
				i = m.end();
			} else {
				// No more valid format specifiers. Check for possible invalid
				// format specifiers.
				checkText(s, i, len);
				// The rest of the string is fixed text
				al.add(new FixedString(s.substring(i)));
				break;
			}
		}
		return al.toArray(new FormatString[al.size()]);
	}

	private static void checkText(String s, int start, int end) {
		for (int i = start; i < end; i++) {
			// Any '%' found in the region starts an invalid format specifier.
			if (s.charAt(i) == '%') {
				char c = (i == end - 1) ? '%' : s.charAt(i + 1);
				throw new UnknownFormatConversionException(String.valueOf(c));
			}
		}
	}

	private interface FormatString {
		int index();

		void print(StringBuilder a, Calendar cal, LogRecord logRecord, Locale l) throws IOException;

		@Override
		String toString();
	}

	private class FixedString implements FormatString {
		private String s;

		FixedString(String s) {
			this.s = s;
		}

		@Override
		public int index() {
			return -2;
		}

		@Override
		public void print(StringBuilder a, Calendar cal, LogRecord logRecord, Locale l) throws IOException {
			a.append(s);
		}

		@Override
		public String toString() {
			return s;
		}
	}

	public enum BigDecimalLayoutForm {
		SCIENTIFIC,
		DECIMAL_FLOAT
	}

	private class FormatSpecifier implements FormatString {
		private String singleSpec;
		private int index = -1;
		private Flags f = Flags.NONE;
		private int width;
		private int precision;
		private boolean dt = false;
		private char c;
		private Function<LogRecord, Object> fieldAccesFunction;

		private int index(String s) {
			if (s != null) {
				try {
					index = Integer.parseInt(s.substring(0, s.length() - 1));
					this.fieldAccesFunction = getFieldAccessFunction(index);
				} catch (NumberFormatException x) {
					assert (false);
				}
			} else {
				index = 0;
			}
			return index;
		}

		@Override
		public int index() {
			return index;
		}

		private Flags flags(String s) {
			f = Flags.parse(s);
			if (f.contains(Flags.PREVIOUS)) {
				index = -1;
			}
			return f;
		}

		private int width(String s) {
			width = -1;
			if (s != null) {
				try {
					width = Integer.parseInt(s);
					if (width < 0) {
						throw new IllegalFormatWidthException(width);
					}
				} catch (NumberFormatException x) {
					assert (false);
				}
			}
			return width;
		}

		private int precision(String s) {
			precision = -1;
			if (s != null) {
				try {
					// remove the '.'
					precision = Integer.parseInt(s.substring(1));
					if (precision < 0) {
						throw new IllegalFormatPrecisionException(precision);
					}
				} catch (NumberFormatException x) {
					assert (false);
				}
			}
			return precision;
		}

		private char conversion(String s) {
			c = s.charAt(0);
			if (!dt) {
				if (!Conversion.isValid(c)) {
					throw new UnknownFormatConversionException(String.valueOf(c));
				}
				if (Character.isUpperCase(c)) {
					f.add(Flags.UPPERCASE);
				}
				c = Character.toLowerCase(c);
				if (Conversion.isText(c)) {
					index = -2;
				}
			}
			return c;
		}

		FormatSpecifier(Matcher m) {
			int idx = 1;

			index(m.group(idx++));
			flags(m.group(idx++));
			width(m.group(idx++));
			precision(m.group(idx++));

			String tT = m.group(idx++);
			if (tT != null) {
				dt = true;
				if (tT.equals("T")) {
					f.add(Flags.UPPERCASE);
				}
			}

			conversion(m.group(idx));

			if (dt) {
				checkDateTime();
			} else if (Conversion.isGeneral(c)) {
				checkGeneral();
			} else if (Conversion.isCharacter(c)) {
				checkCharacter();
			} else if (Conversion.isInteger(c)) {
				checkInteger();
			} else if (Conversion.isFloat(c)) {
				checkFloat();
				StringBuilder newSpec = new StringBuilder("%");
				newSpec.append(f.toString());
				if (this.width != -1) {
					newSpec.append(this.width);
				}
				if (precision != -1) {
					newSpec.append(precision);
				}
				newSpec.append(c);
				this.singleSpec = newSpec.toString();
			} else if (Conversion.isText(c)) {
				checkText();
			} else {
				throw new UnknownFormatConversionException(String.valueOf(c));
			}
		}

		@Override
		public void print(StringBuilder a, Calendar cal, LogRecord logRecord, Locale l) throws IOException {
			if (dt) {
				print(a, cal, c, l);
				return;
			}
			switch (c) {
				case Conversion.DECIMAL_INTEGER:
				case Conversion.OCTAL_INTEGER:
				case Conversion.HEXADECIMAL_INTEGER:
					printInteger(a, logRecord, l);
					break;
				case Conversion.SCIENTIFIC:
				case Conversion.GENERAL:
				case Conversion.DECIMAL_FLOAT:
				case Conversion.HEXADECIMAL_FLOAT:
					printFloat(a, logRecord, l);
					break;
				case Conversion.CHARACTER:
				case Conversion.CHARACTER_UPPER:
					printCharacter(a, logRecord);
					break;
				case Conversion.BOOLEAN:
					printBoolean(a, logRecord);
					break;
				case Conversion.STRING:
					printString(a, logRecord, l);
					break;
				case Conversion.HASHCODE:
					printHashCode(a, logRecord);
					break;
				case Conversion.LINE_SEPARATOR:
					a.append(System.lineSeparator());
					break;
				case Conversion.PERCENT_SIGN:
					a.append('%');
					break;
				default:
					assert false;
			}
		}

		private void printInteger(StringBuilder a, LogRecord logRecord, Locale l) throws IOException {
			Object arg = this.fieldAccesFunction.apply(logRecord);
			if (arg == null) {
				print(a, "null");
			} else if (arg instanceof Byte) {
				print(a, ((Byte) arg).byteValue(), l);
			} else if (arg instanceof Short) {
				print(a, ((Short) arg).shortValue(), l);
			} else if (arg instanceof Integer) {
				print(a, ((Integer) arg).intValue(), l);
			} else if (arg instanceof Long) {
				print(a, ((Long) arg).longValue(), l);
			} else if (arg instanceof BigInteger) {
				print(a, ((BigInteger) arg), l);
			} else {
				failConversion(c, arg);
			}
		}

		private void printFloat(StringBuilder a, LogRecord logRecord, Locale l) throws IOException {
			Object arg = this.fieldAccesFunction.apply(logRecord);
			if (arg == null) {
				print(a, "null");
			} else if (arg instanceof Float) {
				print(a, ((Float) arg).floatValue(), l);
			} else if (arg instanceof Double) {
				print(a, ((Double) arg).doubleValue(), l);
			} else if (arg instanceof BigDecimal) {
				print(a, ((BigDecimal) arg), l);
			} else {
				failConversion(c, arg);
			}
		}

		private void printCharacter(StringBuilder a, LogRecord logRecord) throws IOException {
			Object arg = this.fieldAccesFunction.apply(logRecord);
			if (arg == null) {
				print(a, "null");
				return;
			}
			String s = null;
			if (arg instanceof Character) {
				s = ((Character) arg).toString();
			} else if (arg instanceof Byte) {
				byte i = ((Byte) arg).byteValue();
				if (Character.isValidCodePoint(i)) {
					s = new String(Character.toChars(i));
				} else {
					throw new IllegalFormatCodePointException(i);
				}
			} else if (arg instanceof Short) {
				short i = ((Short) arg).shortValue();
				if (Character.isValidCodePoint(i)) {
					s = new String(Character.toChars(i));
				} else {
					throw new IllegalFormatCodePointException(i);
				}
			} else if (arg instanceof Integer) {
				int i = ((Integer) arg).intValue();
				if (Character.isValidCodePoint(i)) {
					s = new String(Character.toChars(i));
				} else {
					throw new IllegalFormatCodePointException(i);
				}
			} else {
				failConversion(c, arg);
			}
			print(a, s);
		}

		private void printString(StringBuilder a, LogRecord logRecord, Locale l) throws IOException {
			Object arg = this.fieldAccesFunction.apply(logRecord);

			if (arg == null) {
				print(a, "null");
			} else {
				print(a, arg.toString());
			}
		}

		private void printBoolean(StringBuilder a, LogRecord logRecord) throws IOException {
			String s;
			Object arg = this.fieldAccesFunction.apply(logRecord);
			if (arg != null) {
				s = ((arg instanceof Boolean) ? ((Boolean) arg).toString() : Boolean.toString(true));
			} else {
				s = Boolean.toString(false);
			}
			print(a, s);
		}

		private void printHashCode(StringBuilder a, LogRecord logRecord) throws IOException {
			Object arg = this.fieldAccesFunction.apply(logRecord);
			String s = (arg == null ? "null" : Integer.toHexString(arg.hashCode()));
			print(a, s);
		}

		private void print(StringBuilder a, String s) throws IOException {
			if (precision != -1 && precision < s.length()) {
				s = s.substring(0, precision);
			}
			if (f.contains(Flags.UPPERCASE)) {
				s = s.toUpperCase();
			}
			a.append(justify(s));
		}

		private String justify(String s) {
			if (width == -1) {
				return s;
			}
			StringBuilder sb = new StringBuilder();
			boolean pad = f.contains(Flags.LEFT_JUSTIFY);
			int sp = width - s.length();
			if (!pad) {
				for (int i = 0; i < sp; i++) {
					sb.append(' ');
				}
			}
			sb.append(s);
			if (pad) {
				for (int i = 0; i < sp; i++) {
					sb.append(' ');
				}
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder('%');
			// Flags.UPPERCASE is set internally for legal conversions.
			Flags dupf = f.dup().remove(Flags.UPPERCASE);
			sb.append(dupf.toString());
			if (index > 0) {
				sb.append(index).append('$');
			}
			if (width != -1) {
				sb.append(width);
			}
			if (precision != -1) {
				sb.append('.').append(precision);
			}
			if (dt) {
				sb.append(f.contains(Flags.UPPERCASE) ? 'T' : 't');
			}
			sb.append(f.contains(Flags.UPPERCASE) ? Character.toUpperCase(c) : c);
			return sb.toString();
		}

		private void checkGeneral() {
			if ((c == Conversion.BOOLEAN || c == Conversion.HASHCODE) && f.contains(Flags.ALTERNATE)) {
				failMismatch(Flags.ALTERNATE, c);
			}
			// '-' requires a width
			if (width == -1 && f.contains(Flags.LEFT_JUSTIFY)) {
				throw new MissingFormatWidthException(toString());
			}
			checkBadFlags(Flags.PLUS, Flags.LEADING_SPACE, Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
		}

		private void checkDateTime() {
			if (precision != -1) {
				throw new IllegalFormatPrecisionException(precision);
			}
			if (!DateTime.isValid(c)) {
				throw new UnknownFormatConversionException("t" + c);
			}
			checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE, Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
			// '-' requires a width
			if (width == -1 && f.contains(Flags.LEFT_JUSTIFY)) {
				throw new MissingFormatWidthException(toString());
			}
		}

		private void checkCharacter() {
			if (precision != -1) {
				throw new IllegalFormatPrecisionException(precision);
			}
			checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE, Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
			// '-' requires a width
			if (width == -1 && f.contains(Flags.LEFT_JUSTIFY)) {
				throw new MissingFormatWidthException(toString());
			}
		}

		private void checkInteger() {
			checkNumeric();
			if (precision != -1) {
				throw new IllegalFormatPrecisionException(precision);
			}

			if (c == Conversion.DECIMAL_INTEGER) {
				checkBadFlags(Flags.ALTERNATE);
			} else if (c == Conversion.OCTAL_INTEGER) {
				checkBadFlags(Flags.GROUP);
			} else {
				checkBadFlags(Flags.GROUP);
			}
		}

		private void checkBadFlags(Flags... badFlags) {
			for (Flags badFlag : badFlags) {
				if (f.contains(badFlag)) {
					failMismatch(badFlag, c);
				}
			}
		}

		private void checkFloat() {
			checkNumeric();
			if (c == Conversion.DECIMAL_FLOAT) {
			} else if (c == Conversion.HEXADECIMAL_FLOAT) {
				checkBadFlags(Flags.PARENTHESES, Flags.GROUP);
			} else if (c == Conversion.SCIENTIFIC) {
				checkBadFlags(Flags.GROUP);
			} else if (c == Conversion.GENERAL) {
				checkBadFlags(Flags.ALTERNATE);
			}
		}

		private void checkNumeric() {
			if (width != -1 && width < 0) {
				throw new IllegalFormatWidthException(width);
			}

			if (precision != -1 && precision < 0) {
				throw new IllegalFormatPrecisionException(precision);
			}

			// '-' and '0' require a width
			if (width == -1 && (f.contains(Flags.LEFT_JUSTIFY) || f.contains(Flags.ZERO_PAD))) {
				throw new MissingFormatWidthException(toString());
			}

			// bad combination
			if ((f.contains(Flags.PLUS) && f.contains(Flags.LEADING_SPACE)) || (f.contains(Flags.LEFT_JUSTIFY) && f.contains(Flags.ZERO_PAD))) {
				throw new IllegalFormatFlagsException(f.toString());
			}
		}

		private void checkText() {
			if (precision != -1) {
				throw new IllegalFormatPrecisionException(precision);
			}
			switch (c) {
				case Conversion.PERCENT_SIGN:
					if (f.valueOf() != Flags.LEFT_JUSTIFY.valueOf() && f.valueOf() != Flags.NONE.valueOf()) {
						throw new IllegalFormatFlagsException(f.toString());
					}
					// '-' requires a width
					if (width == -1 && f.contains(Flags.LEFT_JUSTIFY)) {
						throw new MissingFormatWidthException(toString());
					}
					break;
				case Conversion.LINE_SEPARATOR:
					if (width != -1) {
						throw new IllegalFormatWidthException(width);
					}
					if (f.valueOf() != Flags.NONE.valueOf()) {
						throw new IllegalFormatFlagsException(f.toString());
					}
					break;
				default:
					assert false;
			}
		}

		private void print(StringBuilder a, byte value, Locale l) throws IOException {
			long v = value;
			if (value < 0 && (c == Conversion.OCTAL_INTEGER || c == Conversion.HEXADECIMAL_INTEGER)) {
				v += (1L << 8);
				assert v >= 0 : v;
			}
			print(a, v, l);
		}

		private void print(StringBuilder a, short value, Locale l) throws IOException {
			long v = value;
			if (value < 0 && (c == Conversion.OCTAL_INTEGER || c == Conversion.HEXADECIMAL_INTEGER)) {
				v += (1L << 16);
				assert v >= 0 : v;
			}
			print(a, v, l);
		}

		private void print(StringBuilder a, int value, Locale l) throws IOException {
			long v = value;
			if (value < 0 && (c == Conversion.OCTAL_INTEGER || c == Conversion.HEXADECIMAL_INTEGER)) {
				v += (1L << 32);
				assert v >= 0 : v;
			}
			print(a, v, l);
		}

		private void print(StringBuilder a, long value, Locale l) throws IOException {

			StringBuilder sb = new StringBuilder();

			if (c == Conversion.DECIMAL_INTEGER) {
				boolean neg = value < 0;
				char[] va;
				if (value < 0) {
					va = Long.toString(value, 10).substring(1).toCharArray();
				} else {
					va = Long.toString(value, 10).toCharArray();
				}

				// leading sign indicator
				leadingSign(sb, neg);

				// the value
				localizedMagnitude(sb, va, f, adjustWidth(width, f, neg), l);

				// trailing sign indicator
				trailingSign(sb, neg);
			} else if (c == Conversion.OCTAL_INTEGER) {
				checkBadFlags(Flags.PARENTHESES, Flags.LEADING_SPACE, Flags.PLUS);
				String s = Long.toOctalString(value);
				int len = (f.contains(Flags.ALTERNATE) ? s.length() + 1 : s.length());

				// apply ALTERNATE (radix indicator for octal) before ZERO_PAD
				if (f.contains(Flags.ALTERNATE)) {
					sb.append('0');
				}
				if (f.contains(Flags.ZERO_PAD)) {
					for (int i = 0; i < width - len; i++) {
						sb.append('0');
					}
				}
				sb.append(s);
			} else if (c == Conversion.HEXADECIMAL_INTEGER) {
				checkBadFlags(Flags.PARENTHESES, Flags.LEADING_SPACE, Flags.PLUS);
				String s = Long.toHexString(value);
				int len = (f.contains(Flags.ALTERNATE) ? s.length() + 2 : s.length());

				// apply ALTERNATE (radix indicator for hex) before ZERO_PAD
				if (f.contains(Flags.ALTERNATE)) {
					sb.append(f.contains(Flags.UPPERCASE) ? "0X" : "0x");
				}
				if (f.contains(Flags.ZERO_PAD)) {
					for (int i = 0; i < width - len; i++) {
						sb.append('0');
					}
				}
				if (f.contains(Flags.UPPERCASE)) {
					s = s.toUpperCase();
				}
				sb.append(s);
			}

			// justify based on width
			a.append(justify(sb.toString()));
		}

		// neg := val < 0
		private StringBuilder leadingSign(StringBuilder sb, boolean neg) {
			if (!neg) {
				if (f.contains(Flags.PLUS)) {
					sb.append('+');
				} else if (f.contains(Flags.LEADING_SPACE)) {
					sb.append(' ');
				}
			} else {
				if (f.contains(Flags.PARENTHESES)) {
					sb.append('(');
				} else {
					sb.append('-');
				}
			}
			return sb;
		}

		// neg := val < 0
		private StringBuilder trailingSign(StringBuilder sb, boolean neg) {
			if (neg && f.contains(Flags.PARENTHESES)) {
				sb.append(')');
			}
			return sb;
		}

		private void print(StringBuilder a, BigInteger value, Locale l) throws IOException {
			StringBuilder sb = new StringBuilder();
			boolean neg = value.signum() == -1;
			BigInteger v = value.abs();

			// leading sign indicator
			leadingSign(sb, neg);

			// the value
			if (c == Conversion.DECIMAL_INTEGER) {
				char[] va = v.toString().toCharArray();
				localizedMagnitude(sb, va, f, adjustWidth(width, f, neg), l);
			} else if (c == Conversion.OCTAL_INTEGER) {
				String s = v.toString(8);

				int len = s.length() + sb.length();
				if (neg && f.contains(Flags.PARENTHESES)) {
					len++;
				}

				// apply ALTERNATE (radix indicator for octal) before ZERO_PAD
				if (f.contains(Flags.ALTERNATE)) {
					len++;
					sb.append('0');
				}
				if (f.contains(Flags.ZERO_PAD)) {
					for (int i = 0; i < width - len; i++) {
						sb.append('0');
					}
				}
				sb.append(s);
			} else if (c == Conversion.HEXADECIMAL_INTEGER) {
				String s = v.toString(16);

				int len = s.length() + sb.length();
				if (neg && f.contains(Flags.PARENTHESES)) {
					len++;
				}

				// apply ALTERNATE (radix indicator for hex) before ZERO_PAD
				if (f.contains(Flags.ALTERNATE)) {
					len += 2;
					sb.append(f.contains(Flags.UPPERCASE) ? "0X" : "0x");
				}
				if (f.contains(Flags.ZERO_PAD)) {
					for (int i = 0; i < width - len; i++) {
						sb.append('0');
					}
				}
				if (f.contains(Flags.UPPERCASE)) {
					s = s.toUpperCase();
				}
				sb.append(s);
			}

			// trailing sign indicator
			trailingSign(sb, (value.signum() == -1));

			// justify based on width
			a.append(justify(sb.toString()));
		}

		private void print(StringBuilder a, float value, Locale l) throws IOException {
			print(a, (double) value, l);
		}

		private void print(StringBuilder a, double value, Locale l) throws IOException {
			StringBuilder sb = new StringBuilder();
			boolean neg = Double.compare(value, 0.0) == -1;

			if (!Double.isNaN(value)) {
				double v = Math.abs(value);

				// leading sign indicator
				leadingSign(sb, neg);

				// the value
				if (!Double.isInfinite(v)) {
					printInternal(sb, v, l, f, c, precision, neg);
				} else {
					sb.append(f.contains(Flags.UPPERCASE) ? "INFINITY" : "Infinity");
				}

				// trailing sign indicator
				trailingSign(sb, neg);
			} else {
				sb.append(f.contains(Flags.UPPERCASE) ? "NAN" : "NaN");
			}

			// justify based on width
			a.append(justify(sb.toString()));
		}

		// !Double.isInfinite(value) && !Double.isNaN(value)
		private void printInternal(StringBuilder sb, double value, Locale l, Flags f, char c, int precision, boolean neg) throws IOException {
			String result = String.format(l, singleSpec, new Double(value));
			sb.append(result);
		}

		private void print(StringBuilder a, BigDecimal value, Locale l) throws IOException {
			if (c == Conversion.HEXADECIMAL_FLOAT) {
				failConversion(c, value);
			}
			StringBuilder sb = new StringBuilder();
			boolean neg = value.signum() == -1;
			BigDecimal v = value.abs();
			// leading sign indicator
			leadingSign(sb, neg);

			// the value
			printInternal(sb, v, l, f, c, precision, neg);

			// trailing sign indicator
			trailingSign(sb, neg);

			// justify based on width
			a.append(justify(sb.toString()));
		}

		// value > 0
		private void printInternal(StringBuilder sb, BigDecimal value, Locale l, Flags f, char c, int precision, boolean neg) throws IOException {
			if (c == Conversion.SCIENTIFIC) {
				// Create a new BigDecimal with the desired precision.
				int prec = (precision == -1 ? 6 : precision);
				int scale = value.scale();
				int origPrec = value.precision();
				int nzeros = 0;
				int compPrec;

				if (prec > origPrec - 1) {
					compPrec = origPrec;
					nzeros = prec - (origPrec - 1);
				} else {
					compPrec = prec + 1;
				}

				MathContext mc = new MathContext(compPrec);
				BigDecimal v = new BigDecimal(value.unscaledValue(), scale, mc);

				BigDecimalLayout bdl = new BigDecimalLayout(v.unscaledValue(), v.scale(), BigDecimalLayoutForm.SCIENTIFIC);

				char[] mant = bdl.mantissa();

				// Add a decimal point if necessary. The mantissa may not
				// contain a decimal point if the scale is zero (the internal
				// representation has no fractional part) or the original
				// precision is one. Append a decimal point if '#' is set or if
				// we require zero padding to get to the requested precision.
				if ((origPrec == 1 || !bdl.hasDot()) && (nzeros > 0 || (f.contains(Flags.ALTERNATE)))) {
					mant = addDot(mant);
				}

				// Add trailing zeros in the case precision is greater than
				// the number of available digits after the decimal separator.
				mant = trailingZeros(mant, nzeros);

				char[] exp = bdl.exponent();
				int newW = width;
				if (width != -1) {
					newW = adjustWidth(width - exp.length - 1, f, neg);
				}
				localizedMagnitude(sb, mant, f, newW, l);

				sb.append(f.contains(Flags.UPPERCASE) ? 'E' : 'e');

				Flags flags = f.dup().remove(Flags.GROUP);
				char sign = exp[0];
				assert (sign == '+' || sign == '-');
				sb.append(exp[0]);

				char[] tmp = new char[exp.length - 1];
				System.arraycopy(exp, 1, tmp, 0, exp.length - 1);
				sb.append(localizedMagnitude(null, tmp, flags, -1, l));
			} else if (c == Conversion.DECIMAL_FLOAT) {
				// Create a new BigDecimal with the desired precision.
				int prec = (precision == -1 ? 6 : precision);
				int scale = value.scale();

				if (scale > prec) {
					// more "scale" digits than the requested "precision"
					int compPrec = value.precision();
					if (compPrec <= scale) {
						// case of 0.xxxxxx
						value = value.setScale(prec, RoundingMode.HALF_UP);
					} else {
						compPrec -= (scale - prec);
						value = new BigDecimal(value.unscaledValue(), scale, new MathContext(compPrec));
					}
				}
				BigDecimalLayout bdl = new BigDecimalLayout(value.unscaledValue(), value.scale(), BigDecimalLayoutForm.DECIMAL_FLOAT);

				char mant[] = bdl.mantissa();
				int nzeros = (bdl.scale() < prec ? prec - bdl.scale() : 0);

				// Add a decimal point if necessary. The mantissa may not
				// contain a decimal point if the scale is zero (the internal
				// representation has no fractional part). Append a decimal
				// point if '#' is set or we require zero padding to get to the
				// requested precision.
				if (bdl.scale() == 0 && (f.contains(Flags.ALTERNATE) || nzeros > 0)) {
					mant = addDot(bdl.mantissa());
				}

				// Add trailing zeros if the precision is greater than the
				// number of available digits after the decimal separator.
				mant = trailingZeros(mant, nzeros);

				localizedMagnitude(sb, mant, f, adjustWidth(width, f, neg), l);
			} else if (c == Conversion.GENERAL) {
				int prec = precision;
				if (precision == -1) {
					prec = 6;
				} else if (precision == 0) {
					prec = 1;
				}

				BigDecimal tenToTheNegFour = BigDecimal.valueOf(1, 4);
				BigDecimal tenToThePrec = BigDecimal.valueOf(1, -prec);
				if ((value.equals(BigDecimal.ZERO)) || ((value.compareTo(tenToTheNegFour) != -1) && (value.compareTo(tenToThePrec) == -1))) {

					int e = -value.scale() + (value.unscaledValue().toString().length() - 1);

					// xxx.yyy
					// g precision (# sig digits) = #x + #y
					// f precision = #y
					// exponent = #x - 1
					// => f precision = g precision - exponent - 1
					// 0.000zzz
					// g precision (# sig digits) = #z
					// f precision = #0 (after '.') + #z
					// exponent = - #0 (after '.') - 1
					// => f precision = g precision - exponent - 1
					prec = prec - e - 1;

					printInternal(sb, value, l, f, Conversion.DECIMAL_FLOAT, prec, neg);
				} else {
					printInternal(sb, value, l, f, Conversion.SCIENTIFIC, prec - 1, neg);
				}
			} else if (c == Conversion.HEXADECIMAL_FLOAT) {
				// This conversion isn't supported. The error should be
				// reported earlier.
				assert false;
			}
		}

		private class BigDecimalLayout {
			private StringBuilder mant;
			private StringBuilder exp;
			private boolean dot = false;
			private int scale;

			public BigDecimalLayout(BigInteger intVal, int scale, BigDecimalLayoutForm form) {
				layout(intVal, scale, form);
			}

			public boolean hasDot() {
				return dot;
			}

			public int scale() {
				return scale;
			}

			public char[] mantissa() {
				return toCharArray(mant);
			}

			// The exponent will be formatted as a sign ('+' or '-') followed
			// by the exponent zero-padded to include at least two digits.
			public char[] exponent() {
				return toCharArray(exp);
			}

			private char[] toCharArray(StringBuilder sb) {
				if (sb == null) {
					return null;
				}
				char[] result = new char[sb.length()];
				sb.getChars(0, result.length, result, 0);
				return result;
			}

			private void layout(BigInteger intVal, int scale, BigDecimalLayoutForm form) {
				char coeff[] = intVal.toString().toCharArray();
				this.scale = scale;

				// Construct a buffer, with sufficient capacity for all cases.
				// If E-notation is needed, length will be: +1 if negative, +1
				// if '.' needed, +2 for "E+", + up to 10 for adjusted
				// exponent. Otherwise it could have +1 if negative, plus
				// leading "0.00000"
				mant = new StringBuilder(coeff.length + 14);

				if (scale == 0) {
					int len = coeff.length;
					if (len > 1) {
						mant.append(coeff[0]);
						if (form == BigDecimalLayoutForm.SCIENTIFIC) {
							mant.append('.');
							dot = true;
							mant.append(coeff, 1, len - 1);
							exp = new StringBuilder("+");
							if (len < 10) {
								exp.append("0").append(len - 1);
							} else {
								exp.append(len - 1);
							}
						} else {
							mant.append(coeff, 1, len - 1);
						}
					} else {
						mant.append(coeff);
						if (form == BigDecimalLayoutForm.SCIENTIFIC) {
							exp = new StringBuilder("+00");
						}
					}
					return;
				}
				long adjusted = -(long) scale + (coeff.length - 1);
				if (form == BigDecimalLayoutForm.DECIMAL_FLOAT) {
					// count of padding zeros
					int pad = scale - coeff.length;
					if (pad >= 0) {
						// 0.xxx form
						mant.append("0.");
						dot = true;
						for (; pad > 0; pad--) {
							mant.append('0');
						}
						mant.append(coeff);
					} else {
						if (-pad < coeff.length) {
							// xx.xx form
							mant.append(coeff, 0, -pad);
							mant.append('.');
							dot = true;
							mant.append(coeff, -pad, scale);
						} else {
							// xx form
							mant.append(coeff, 0, coeff.length);
							for (int i = 0; i < -scale; i++) {
								mant.append('0');
							}
							this.scale = 0;
						}
					}
				} else {
					// x.xxx form
					mant.append(coeff[0]);
					if (coeff.length > 1) {
						mant.append('.');
						dot = true;
						mant.append(coeff, 1, coeff.length - 1);
					}
					exp = new StringBuilder();
					if (adjusted != 0) {
						long abs = Math.abs(adjusted);
						// require sign
						exp.append(adjusted < 0 ? '-' : '+');
						if (abs < 10) {
							exp.append('0');
						}
						exp.append(abs);
					} else {
						exp.append("+00");
					}
				}
			}
		}

		private int adjustWidth(int width, Flags f, boolean neg) {
			int newW = width;
			if (newW != -1 && neg && f.contains(Flags.PARENTHESES)) {
				newW--;
			}
			return newW;
		}

		// Add a '.' to th mantissa if required
		private char[] addDot(char[] mant) {
			char[] tmp = mant;
			tmp = new char[mant.length + 1];
			System.arraycopy(mant, 0, tmp, 0, mant.length);
			tmp[tmp.length - 1] = '.';
			return tmp;
		}

		// Add trailing zeros in the case precision is greater than the number
		// of available digits after the decimal separator.
		private char[] trailingZeros(char[] mant, int nzeros) {
			char[] tmp = mant;
			if (nzeros > 0) {
				tmp = new char[mant.length + nzeros];
				System.arraycopy(mant, 0, tmp, 0, mant.length);
				for (int i = mant.length; i < tmp.length; i++) {
					tmp[i] = '0';
				}
			}
			return tmp;
		}

		private void print(StringBuilder a, Calendar t, char c, Locale l) throws IOException {
			StringBuilder sb = new StringBuilder();
			printInternal(sb, t, c, l);

			// justify based on width
			String s = justify(sb.toString());
			if (f.contains(Flags.UPPERCASE)) {
				s = s.toUpperCase();
			}

			a.append(s);
		}

		private Appendable printInternal(StringBuilder sb, Calendar t, char c, Locale l) throws IOException {
			assert (width == -1);

			switch (c) {
				case DateTime.HOUR_OF_DAY_0: // 'H' (00 - 23)
				case DateTime.HOUR_0: // 'I' (01 - 12)
				case DateTime.HOUR_OF_DAY: // 'k' (0 - 23) -- like H
				case DateTime.HOUR: { // 'l' (1 - 12) -- like I
					int i = t.get(Calendar.HOUR_OF_DAY);
					if (c == DateTime.HOUR_0 || c == DateTime.HOUR) {
						i = (i == 0 || i == 12 ? 12 : i % 12);
					}
					Flags flags = (c == DateTime.HOUR_OF_DAY_0 || c == DateTime.HOUR_0 ? Flags.ZERO_PAD : Flags.NONE);
					sb.append(localizedMagnitude(null, i, flags, 2, l));
					break;
				}
				case DateTime.MINUTE: { // 'M' (00 - 59)
					int i = t.get(Calendar.MINUTE);
					Flags flags = Flags.ZERO_PAD;
					sb.append(localizedMagnitude(null, i, flags, 2, l));
					break;
				}
				case DateTime.NANOSECOND: { // 'N' (000000000 - 999999999)
					int i = t.get(Calendar.MILLISECOND) * 1000000;
					Flags flags = Flags.ZERO_PAD;
					sb.append(localizedMagnitude(null, i, flags, 9, l));
					break;
				}
				case DateTime.MILLISECOND: { // 'L' (000 - 999)
					int i = t.get(Calendar.MILLISECOND);
					Flags flags = Flags.ZERO_PAD;
					sb.append(localizedMagnitude(null, i, flags, 3, l));
					break;
				}
				case DateTime.MILLISECOND_SINCE_EPOCH: { // 'Q' (0 - 99...?)
					long i = t.getTimeInMillis();
					Flags flags = Flags.NONE;
					sb.append(localizedMagnitude(null, i, flags, width, l));
					break;
				}
				case DateTime.AM_PM: { // 'p' (am or pm)
					// Calendar.AM = 0, Calendar.PM = 1, LocaleElements defines upper
					String[] ampm = { "AM", "PM" };
					if (l != null && l != Locale.US) {
						DateFormatSymbols dfs = DateFormatSymbols.getInstance(l);
						ampm = dfs.getAmPmStrings();
					}
					String s = ampm[t.get(Calendar.AM_PM)];
					sb.append(s.toLowerCase(l != null ? l : Locale.US));
					break;
				}
				case DateTime.SECONDS_SINCE_EPOCH: { // 's' (0 - 99...?)
					long i = t.getTimeInMillis() / 1000;
					Flags flags = Flags.NONE;
					sb.append(localizedMagnitude(null, i, flags, width, l));
					break;
				}
				case DateTime.SECOND: { // 'S' (00 - 60 - leap second)
					int i = t.get(Calendar.SECOND);
					Flags flags = Flags.ZERO_PAD;
					sb.append(localizedMagnitude(null, i, flags, 2, l));
					break;
				}
				case DateTime.ZONE_NUMERIC: { // 'z' ({-|+}####) - ls minus?
					int i = t.get(Calendar.ZONE_OFFSET) + t.get(Calendar.DST_OFFSET);
					boolean neg = i < 0;
					sb.append(neg ? '-' : '+');
					if (neg) {
						i = -i;
					}
					int min = i / 60000;
					// combine minute and hour into a single integer
					int offset = (min / 60) * 100 + (min % 60);
					Flags flags = Flags.ZERO_PAD;

					sb.append(localizedMagnitude(null, offset, flags, 4, l));
					break;
				}
				case DateTime.ZONE: { // 'Z' (symbol)
					TimeZone tz = t.getTimeZone();
					sb.append(tz.getDisplayName((t.get(Calendar.DST_OFFSET) != 0), TimeZone.SHORT, (l == null) ? Locale.US : l));
					break;
				}

				// Date
				case DateTime.NAME_OF_DAY_ABBREV: // 'a'
				case DateTime.NAME_OF_DAY: { // 'A'
					int i = t.get(Calendar.DAY_OF_WEEK);
					Locale lt = ((l == null) ? Locale.US : l);
					DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);
					if (c == DateTime.NAME_OF_DAY) {
						sb.append(dfs.getWeekdays()[i]);
					} else {
						sb.append(dfs.getShortWeekdays()[i]);
					}
					break;
				}
				case DateTime.NAME_OF_MONTH_ABBREV: // 'b'
				case DateTime.NAME_OF_MONTH_ABBREV_X: // 'h' -- same b
				case DateTime.NAME_OF_MONTH: { // 'B'
					int i = t.get(Calendar.MONTH);
					Locale lt = ((l == null) ? Locale.US : l);
					DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);
					if (c == DateTime.NAME_OF_MONTH) {
						sb.append(dfs.getMonths()[i]);
					} else {
						sb.append(dfs.getShortMonths()[i]);
					}
					break;
				}
				case DateTime.CENTURY: // 'C' (00 - 99)
				case DateTime.YEAR_2: // 'y' (00 - 99)
				case DateTime.YEAR_4: { // 'Y' (0000 - 9999)
					int i = t.get(Calendar.YEAR);
					int size = 2;
					switch (c) {
						case DateTime.CENTURY:
							i /= 100;
							break;
						case DateTime.YEAR_2:
							i %= 100;
							break;
						case DateTime.YEAR_4:
							size = 4;
							break;
					}
					Flags flags = Flags.ZERO_PAD;
					sb.append(localizedMagnitude(null, i, flags, size, l));
					break;
				}
				case DateTime.DAY_OF_MONTH_0: // 'd' (01 - 31)
				case DateTime.DAY_OF_MONTH: { // 'e' (1 - 31) -- like d
					int i = t.get(Calendar.DATE);
					Flags flags = (c == DateTime.DAY_OF_MONTH_0 ? Flags.ZERO_PAD : Flags.NONE);
					sb.append(localizedMagnitude(null, i, flags, 2, l));
					break;
				}
				case DateTime.DAY_OF_YEAR: { // 'j' (001 - 366)
					int i = t.get(Calendar.DAY_OF_YEAR);
					Flags flags = Flags.ZERO_PAD;
					sb.append(localizedMagnitude(null, i, flags, 3, l));
					break;
				}
				case DateTime.MONTH: { // 'm' (01 - 12)
					int i = t.get(Calendar.MONTH) + 1;
					Flags flags = Flags.ZERO_PAD;
					sb.append(localizedMagnitude(null, i, flags, 2, l));
					break;
				}

				// Composites
				case DateTime.TIME: // 'T' (24 hour hh:mm:ss - %tH:%tM:%tS)
				case DateTime.TIME_24_HOUR: { // 'R' (hh:mm same as %H:%M)
					char sep = ':';
					printInternal(sb, t, DateTime.HOUR_OF_DAY_0, l).append(sep);
					printInternal(sb, t, DateTime.MINUTE, l);
					if (c == DateTime.TIME) {
						sb.append(sep);
						printInternal(sb, t, DateTime.SECOND, l);
					}
					break;
				}
				case DateTime.TIME_12_HOUR: { // 'r' (hh:mm:ss [AP]M)
					char sep = ':';
					printInternal(sb, t, DateTime.HOUR_0, l).append(sep);
					printInternal(sb, t, DateTime.MINUTE, l).append(sep);
					printInternal(sb, t, DateTime.SECOND, l).append(' ');
					// this may be in wrong place for some locales
					StringBuilder tsb = new StringBuilder();
					print(tsb, t, DateTime.AM_PM, l);
					sb.append(tsb.toString().toUpperCase(l != null ? l : Locale.US));
					break;
				}
				case DateTime.DATE_TIME: { // 'c' (Sat Nov 04 12:02:33 EST 1999)
					char sep = ' ';
					printInternal(sb, t, DateTime.NAME_OF_DAY_ABBREV, l).append(sep);
					printInternal(sb, t, DateTime.NAME_OF_MONTH_ABBREV, l).append(sep);
					printInternal(sb, t, DateTime.DAY_OF_MONTH_0, l).append(sep);
					printInternal(sb, t, DateTime.TIME, l).append(sep);
					printInternal(sb, t, DateTime.ZONE, l).append(sep);
					printInternal(sb, t, DateTime.YEAR_4, l);
					break;
				}
				case DateTime.DATE: { // 'D' (mm/dd/yy)
					char sep = '/';
					printInternal(sb, t, DateTime.MONTH, l).append(sep);
					printInternal(sb, t, DateTime.DAY_OF_MONTH_0, l).append(sep);
					printInternal(sb, t, DateTime.YEAR_2, l);
					break;
				}
				case DateTime.ISO_STANDARD_DATE: { // 'F' (%Y-%m-%d)
					char sep = '-';
					printInternal(sb, t, DateTime.YEAR_4, l).append(sep);
					printInternal(sb, t, DateTime.MONTH, l).append(sep);
					printInternal(sb, t, DateTime.DAY_OF_MONTH_0, l);
					break;
				}
				default:
					assert false;
			}
			return sb;
		}

		// -- Methods to support throwing exceptions --

		private void failMismatch(Flags f, char c) {
			String fs = f.toString();
			throw new FormatFlagsConversionMismatchException(fs, c);
		}

		private void failConversion(char c, Object arg) {
			throw new IllegalFormatConversionException(c, arg.getClass());
		}

		private char getZero(Locale l) {
			if ((l != null) && !l.equals(locale())) {
				DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(l);
				return dfs.getZeroDigit();
			}
			return zero;
		}

		private StringBuilder localizedMagnitude(StringBuilder sb, long value, Flags f, int width, Locale l) {
			char[] va = Long.toString(value, 10).toCharArray();
			return localizedMagnitude(sb, va, f, width, l);
		}

		private StringBuilder localizedMagnitude(StringBuilder sb, char[] value, Flags f, int width, Locale l) {
			if (sb == null) {
				sb = new StringBuilder();
			}
			int begin = sb.length();

			char zero = getZero(l);

			// determine localized grouping separator and size
			char grpSep = '\0';
			int grpSize = -1;
			char decSep = '\0';

			int len = value.length;
			int dot = len;
			for (int j = 0; j < len; j++) {
				if (value[j] == '.') {
					dot = j;
					break;
				}
			}

			if (dot < len) {
				if (l == null || l.equals(Locale.US)) {
					decSep = '.';
				} else {
					DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(l);
					decSep = dfs.getDecimalSeparator();
				}
			}

			if (f.contains(Flags.GROUP)) {
				if (l == null || l.equals(Locale.US)) {
					grpSep = ',';
					grpSize = 3;
				} else {
					DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(l);
					grpSep = dfs.getGroupingSeparator();
					DecimalFormat df = (DecimalFormat) NumberFormat.getIntegerInstance(l);
					grpSize = df.getGroupingSize();
				}
			}

			// localize the digits inserting group separators as necessary
			for (int j = 0; j < len; j++) {
				if (j == dot) {
					sb.append(decSep);
					// no more group separators after the decimal separator
					grpSep = '\0';
					continue;
				}

				char c = value[j];
				sb.append((char) ((c - '0') + zero));
				if (grpSep != '\0' && j != dot - 1 && ((dot - j) % grpSize == 1)) {
					sb.append(grpSep);
				}
			}

			// apply zero padding
			len = sb.length();
			if (width != -1 && f.contains(Flags.ZERO_PAD)) {
				for (int k = 0; k < width - len; k++) {
					sb.insert(begin, zero);
				}
			}

			return sb;
		}
	}

	private static class Flags {
		private int flags;

		static final Flags NONE = new Flags(0); // ''

		// duplicate declarations from Formattable.java
		static final Flags LEFT_JUSTIFY = new Flags(1 << 0); // '-'
		static final Flags UPPERCASE = new Flags(1 << 1); // '^'
		static final Flags ALTERNATE = new Flags(1 << 2); // '#'

		// numerics
		static final Flags PLUS = new Flags(1 << 3); // '+'
		static final Flags LEADING_SPACE = new Flags(1 << 4); // ' '
		static final Flags ZERO_PAD = new Flags(1 << 5); // '0'
		static final Flags GROUP = new Flags(1 << 6); // ','
		static final Flags PARENTHESES = new Flags(1 << 7); // '('

		// indexing
		static final Flags PREVIOUS = new Flags(1 << 8); // '<'

		private Flags(int f) {
			flags = f;
		}

		public int valueOf() {
			return flags;
		}

		public boolean contains(Flags f) {
			return (flags & f.valueOf()) == f.valueOf();
		}

		public Flags dup() {
			return new Flags(flags);
		}

		private Flags add(Flags f) {
			flags |= f.valueOf();
			return this;
		}

		public Flags remove(Flags f) {
			flags &= ~f.valueOf();
			return this;
		}

		public static Flags parse(String s) {
			char[] ca = s.toCharArray();
			Flags f = new Flags(0);
			for (char element : ca) {
				Flags v = parse(element);
				if (f.contains(v)) {
					throw new DuplicateFormatFlagsException(v.toString());
				}
				f.add(v);
			}
			return f;
		}

		// parse those flags which may be provided by users
		private static Flags parse(char c) {
			switch (c) {
				case '-':
					return LEFT_JUSTIFY;
				case '#':
					return ALTERNATE;
				case '+':
					return PLUS;
				case ' ':
					return LEADING_SPACE;
				case '0':
					return ZERO_PAD;
				case ',':
					return GROUP;
				case '(':
					return PARENTHESES;
				case '<':
					return PREVIOUS;
				default:
					throw new UnknownFormatFlagsException(String.valueOf(c));
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (contains(LEFT_JUSTIFY)) {
				sb.append('-');
			}
			if (contains(UPPERCASE)) {
				sb.append('^');
			}
			if (contains(ALTERNATE)) {
				sb.append('#');
			}
			if (contains(PLUS)) {
				sb.append('+');
			}
			if (contains(LEADING_SPACE)) {
				sb.append(' ');
			}
			if (contains(ZERO_PAD)) {
				sb.append('0');
			}
			if (contains(GROUP)) {
				sb.append(',');
			}
			if (contains(PARENTHESES)) {
				sb.append('(');
			}
			if (contains(PREVIOUS)) {
				sb.append('<');
			}
			return sb.toString();
		}
	}

	private static class Conversion {
		// Byte, Short, Integer, Long, BigInteger
		// (and associated primitives due to autoboxing)
		static final char DECIMAL_INTEGER = 'd';
		static final char OCTAL_INTEGER = 'o';
		static final char HEXADECIMAL_INTEGER = 'x';
		static final char HEXADECIMAL_INTEGER_UPPER = 'X';

		// Float, Double, BigDecimal
		// (and associated primitives due to autoboxing)
		static final char SCIENTIFIC = 'e';
		static final char SCIENTIFIC_UPPER = 'E';
		static final char GENERAL = 'g';
		static final char GENERAL_UPPER = 'G';
		static final char DECIMAL_FLOAT = 'f';
		static final char HEXADECIMAL_FLOAT = 'a';
		static final char HEXADECIMAL_FLOAT_UPPER = 'A';

		// Character, Byte, Short, Integer
		// (and associated primitives due to autoboxing)
		static final char CHARACTER = 'c';
		static final char CHARACTER_UPPER = 'C';

		// java.util.Date, java.util.Calendar, long
		/* static final char DATE_TIME = 't'; static final char DATE_TIME_UPPER = 'T'; */

		// if (arg.TYPE != boolean) return boolean
		// if (arg != null) return true; else return false;
		static final char BOOLEAN = 'b';
		static final char BOOLEAN_UPPER = 'B';
		// if (arg instanceof Formattable) arg.formatTo()
		// else arg.toString();
		static final char STRING = 's';
		static final char STRING_UPPER = 'S';
		// arg.hashCode()
		static final char HASHCODE = 'h';
		static final char HASHCODE_UPPER = 'H';

		static final char LINE_SEPARATOR = 'n';
		static final char PERCENT_SIGN = '%';

		static boolean isValid(char c) {
			return (isGeneral(c) || isInteger(c) || isFloat(c) || isText(c) || c == 't' || isCharacter(c));
		}

		// Returns true iff the Conversion is applicable to all objects.
		static boolean isGeneral(char c) {
			switch (c) {
				case BOOLEAN:
				case BOOLEAN_UPPER:
				case STRING:
				case STRING_UPPER:
				case HASHCODE:
				case HASHCODE_UPPER:
					return true;
				default:
					return false;
			}
		}

		// Returns true iff the Conversion is applicable to character.
		static boolean isCharacter(char c) {
			switch (c) {
				case CHARACTER:
				case CHARACTER_UPPER:
					return true;
				default:
					return false;
			}
		}

		// Returns true iff the Conversion is an integer type.
		static boolean isInteger(char c) {
			switch (c) {
				case DECIMAL_INTEGER:
				case OCTAL_INTEGER:
				case HEXADECIMAL_INTEGER:
				case HEXADECIMAL_INTEGER_UPPER:
					return true;
				default:
					return false;
			}
		}

		// Returns true iff the Conversion is a floating-point type.
		static boolean isFloat(char c) {
			switch (c) {
				case SCIENTIFIC:
				case SCIENTIFIC_UPPER:
				case GENERAL:
				case GENERAL_UPPER:
				case DECIMAL_FLOAT:
				case HEXADECIMAL_FLOAT:
				case HEXADECIMAL_FLOAT_UPPER:
					return true;
				default:
					return false;
			}
		}

		// Returns true iff the Conversion does not require an argument
		static boolean isText(char c) {
			switch (c) {
				case LINE_SEPARATOR:
				case PERCENT_SIGN:
					return true;
				default:
					return false;
			}
		}
	}

	private static class DateTime {
		static final char HOUR_OF_DAY_0 = 'H'; // (00 - 23)
		static final char HOUR_0 = 'I'; // (01 - 12)
		static final char HOUR_OF_DAY = 'k'; // (0 - 23) -- like H
		static final char HOUR = 'l'; // (1 - 12) -- like I
		static final char MINUTE = 'M'; // (00 - 59)
		static final char NANOSECOND = 'N'; // (000000000 - 999999999)
		static final char MILLISECOND = 'L'; // jdk, not in gnu (000 - 999)
		static final char MILLISECOND_SINCE_EPOCH = 'Q'; // (0 - 99...?)
		static final char AM_PM = 'p'; // (am or pm)
		static final char SECONDS_SINCE_EPOCH = 's'; // (0 - 99...?)
		static final char SECOND = 'S'; // (00 - 60 - leap second)
		static final char TIME = 'T'; // (24 hour hh:mm:ss)
		static final char ZONE_NUMERIC = 'z'; // (-1200 - +1200) - ls minus?
		static final char ZONE = 'Z'; // (symbol)

		// Date
		static final char NAME_OF_DAY_ABBREV = 'a'; // 'a'
		static final char NAME_OF_DAY = 'A'; // 'A'
		static final char NAME_OF_MONTH_ABBREV = 'b'; // 'b'
		static final char NAME_OF_MONTH = 'B'; // 'B'
		static final char CENTURY = 'C'; // (00 - 99)
		static final char DAY_OF_MONTH_0 = 'd'; // (01 - 31)
		static final char DAY_OF_MONTH = 'e'; // (1 - 31) -- like d
		// * static final char ISO_WEEK_OF_YEAR_2 = 'g'; // cross %y %V
		// * static final char ISO_WEEK_OF_YEAR_4 = 'G'; // cross %Y %V
		static final char NAME_OF_MONTH_ABBREV_X = 'h'; // -- same b
		static final char DAY_OF_YEAR = 'j'; // (001 - 366)
		static final char MONTH = 'm'; // (01 - 12)
		// * static final char DAY_OF_WEEK_1 = 'u'; // (1 - 7) Monday
		// * static final char WEEK_OF_YEAR_SUNDAY = 'U'; // (0 - 53) Sunday+
		// * static final char WEEK_OF_YEAR_MONDAY_01 = 'V'; // (01 - 53) Monday+
		// * static final char DAY_OF_WEEK_0 = 'w'; // (0 - 6) Sunday
		// * static final char WEEK_OF_YEAR_MONDAY = 'W'; // (00 - 53) Monday
		static final char YEAR_2 = 'y'; // (00 - 99)
		static final char YEAR_4 = 'Y'; // (0000 - 9999)

		// Composites
		static final char TIME_12_HOUR = 'r'; // (hh:mm:ss [AP]M)
		static final char TIME_24_HOUR = 'R'; // (hh:mm same as %H:%M)
		// * static final char LOCALE_TIME = 'X'; // (%H:%M:%S) - parse format?
		static final char DATE_TIME = 'c';
		// (Sat Nov 04 12:02:33 EST 1999)
		static final char DATE = 'D'; // (mm/dd/yy)
		static final char ISO_STANDARD_DATE = 'F'; // (%Y-%m-%d)
		// * static final char LOCALE_DATE = 'x'; // (mm/dd/yy)

		static boolean isValid(char c) {
			switch (c) {
				case HOUR_OF_DAY_0:
				case HOUR_0:
				case HOUR_OF_DAY:
				case HOUR:
				case MINUTE:
				case NANOSECOND:
				case MILLISECOND:
				case MILLISECOND_SINCE_EPOCH:
				case AM_PM:
				case SECONDS_SINCE_EPOCH:
				case SECOND:
				case TIME:
				case ZONE_NUMERIC:
				case ZONE:

					// Date
				case NAME_OF_DAY_ABBREV:
				case NAME_OF_DAY:
				case NAME_OF_MONTH_ABBREV:
				case NAME_OF_MONTH:
				case CENTURY:
				case DAY_OF_MONTH_0:
				case DAY_OF_MONTH:
					// * case ISO_WEEK_OF_YEAR_2:
					// * case ISO_WEEK_OF_YEAR_4:
				case NAME_OF_MONTH_ABBREV_X:
				case DAY_OF_YEAR:
				case MONTH:
					// * case DAY_OF_WEEK_1:
					// * case WEEK_OF_YEAR_SUNDAY:
					// * case WEEK_OF_YEAR_MONDAY_01:
					// * case DAY_OF_WEEK_0:
					// * case WEEK_OF_YEAR_MONDAY:
				case YEAR_2:
				case YEAR_4:

					// Composites
				case TIME_12_HOUR:
				case TIME_24_HOUR:
					// * case LOCALE_TIME:
				case DATE_TIME:
				case DATE:
				case ISO_STANDARD_DATE:
					// * case LOCALE_DATE:
					return true;
				default:
					return false;
			}
		}
	}
}
