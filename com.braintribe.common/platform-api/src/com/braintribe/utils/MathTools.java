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
package com.braintribe.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.Not;

/**
 * This class provides utility methods related to mathematics.
 *
 * @author michael.lafite
 */
public final class MathTools {

	private MathTools() {
		// no instantiation required
	}

	/**
	 * Gets the minimum of the passed numbers but ignores all numbers that are lower than the specified lowest number.
	 *
	 * @param lowestValidNumber
	 *            the lowest number that is valid.
	 * @param numbers
	 *            the numbers to check.
	 * @return the minimum number or <code>null</code> if no valid number is found (and also if no numbers are specified at all).
	 */

	public static Long getMinimum(final long lowestValidNumber, final long... numbers) {

		if (CommonTools.isEmpty(numbers)) {
			return null;
		}
		Long minimum = null;

		for (final long number : numbers) {
			if (number == lowestValidNumber) {
				minimum = lowestValidNumber;
				break;
			}

			if (number > lowestValidNumber) {
				if (minimum == null) {
					minimum = number;
				} else {
					if (number < minimum) {
						minimum = number;
					}
				}
			}
		}

		return minimum;
	}

	/**
	 * @see CommonTools#isInRange(double, double, double)
	 */
	public static boolean isInRange(final double value, final double min, final double max) {
		return CommonTools.isInRange(value, min, max);
	}

	/**
	 * @see CommonTools#isInRange(double, Double, Double)
	 */
	public static boolean isInRange(final double value, final Double min, final Double max) {
		return CommonTools.isInRange(value, min, max);
	}

	/**
	 * @see CommonTools#isIntegralNumber(double)
	 */
	public static boolean isIntegralNumber(final double value) {
		return CommonTools.isIntegralNumber(value);
	}

	/**
	 * See {@link CommonTools#isEven(long)}.
	 */
	public static boolean isEven(final long number) {
		return CommonTools.isEven(number);
	}

	/**
	 * Rounds a double value.
	 *
	 * @param doubleToRound
	 *            the value to round.
	 * @param numberOfDecimalPlaces
	 *            the number of decimal places.
	 * @return the rounded value.
	 */
	public static double roundToDecimals(final double doubleToRound, final int numberOfDecimalPlaces) {
		return (Math.round(doubleToRound * Math.pow(10, numberOfDecimalPlaces)) / Math.pow(10, numberOfDecimalPlaces));
	}

	/**
	 * Rounds down a double value.
	 *
	 * @param doubleToRound
	 *            the value to round down.
	 * @param numberOfDecimalPlaces
	 *            the number of decimal places.
	 * @return the rounded value.
	 */
	public static double roundDownToDecimals(final double doubleToRound, final int numberOfDecimalPlaces) {
		return ((int) (doubleToRound * Math.pow(10, numberOfDecimalPlaces))) / Math.pow(10, numberOfDecimalPlaces);
	}

	/**
	 * Rounds down a double value.
	 *
	 * @param doubleToRound
	 *            the value to round.
	 * @param numberOfDecimalPlaces
	 *            the number of decimal places.
	 * @param roundDown
	 *            whether to round or to round down.
	 * @return the rounded value.
	 */
	public static double roundToDecimals(final double doubleToRound, final int numberOfDecimalPlaces, final boolean roundDown) {
		if (roundDown) {
			return roundDownToDecimals(doubleToRound, numberOfDecimalPlaces);
		}
		return roundToDecimals(doubleToRound, numberOfDecimalPlaces);
	}

	/**
	 * Rounds down the passed decimal value.
	 *
	 * @see #roundToDecimals(BigDecimal, int, RoundingMode)
	 */
	public static BigDecimal roundDownToDecimals(final BigDecimal value, final int numberOfDecimalPlaces) {
		return roundToDecimals(value, numberOfDecimalPlaces, Not.Null(RoundingMode.DOWN));
	}

	/**
	 * Rounds the passed decimal value.
	 *
	 * @param value
	 *            the decimal value to round.
	 * @param numberOfDecimalPlaces
	 *            the number of decimal places.
	 * @param roundingMode
	 *            the rounding mode.
	 * @return the rounded value.
	 */
	public static BigDecimal roundToDecimals(final BigDecimal value, final int numberOfDecimalPlaces, final RoundingMode roundingMode) {
		final BigDecimal temp = Not.Null(value.setScale(numberOfDecimalPlaces, roundingMode));
		return removeTrailingZeros(temp);
	}

	/**
	 * Returns a new <code>BigDecimal</code> with removed trailing zeros (after the decimal separator).
	 */
	public static BigDecimal removeTrailingZeros(final BigDecimal value) {
		if (!value.toString().contains(".")) {
			return value;
		}
		return new BigDecimal(StringTools.removeTrailingCharacters(value.toString(), '0'));
	}

	/**
	 * Returns <code>true</code> if any of the values is <code>0</code>.
	 */
	public static boolean isAnyZero(final double... values) {
		if (CommonTools.isEmpty(values)) {
			throw new IllegalArgumentException("Values array must not be null or empty! " + CommonTools.getParametersString("values", values));
		}
		for (final double value : values) {
			if (value == Numbers.ZERO) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Converts the passed value to a string. The decimal place is omitted if the value is a <code>long</code> value.
	 *
	 * @param value
	 *            the value to convert to a string.
	 * @return the string representation of the passed value.
	 */

	public static String toStringWithoutDecimalPlaceIfPossible(final double value) {
		if (CommonTools.isIntegralNumber(value)) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}

	public static String format(final Number number, final String decimalFormatPattern) {
		// get english locale instance because (by default) we want to use '.' and not ',' as decimal separator
		final DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		format.applyPattern(decimalFormatPattern);
		return Not.Null(format.format(number));
	}

	public static String format(final Number number, final int numberOfDecimals) {
		String decimalsPattern = null;
		if (numberOfDecimals > Numbers.ZERO) {
			decimalsPattern = "." + com.braintribe.utils.lcd.StringTools.getFilledString(numberOfDecimals, '0');
		} else {
			decimalsPattern = "";
		}
		return format(number, "#" + decimalsPattern);
	}

	public static BigDecimal getBigDecimalValue(final String valueString) {
		return getBigDecimalValue(valueString, false, false);
	}

	public static BigDecimal getBigDecimalValueOrZero(final String valueString) {
		return getBigDecimalValue(valueString, true, true);
	}

	public static BigDecimal getBigDecimalValue(final String valueString, final boolean ignoreEmptyValue, final boolean ignoreIllegalValue) {
		BigDecimal defaultValueForEmptyNumberString = null;
		if (ignoreEmptyValue) {
			defaultValueForEmptyNumberString = BigDecimal.ZERO;
		}

		BigDecimal defaultValueForInvalidNumberString = null;
		if (ignoreIllegalValue) {
			defaultValueForInvalidNumberString = BigDecimal.ZERO;
		}

		return getBigDecimalValue(valueString, defaultValueForEmptyNumberString, defaultValueForInvalidNumberString, true);

	}

	public static BigDecimal getBigDecimalValue(final String valueString, final BigDecimal defaultValueForEmptyNumberString,
			final BigDecimal defaultValueForInvalidNumberString, final boolean replaceCommasWithDots) {
		if (CommonTools.isEmpty(valueString)) {
			if (defaultValueForEmptyNumberString != null) {
				return defaultValueForEmptyNumberString;
			}
			throw new IllegalArgumentException(
					"Cannot parse BigDecimal from empty string! " + CommonTools.getParametersString("string", valueString));
		}

		try {
			String normalizedValueString = valueString;
			if (replaceCommasWithDots) {
				normalizedValueString = normalizedValueString.replace(",", ".");
			}
			return new BigDecimal(normalizedValueString);
		} catch (final NumberFormatException e) {
			if (defaultValueForInvalidNumberString != null) {
				return defaultValueForInvalidNumberString;
			}
			throw new RuntimeException("Couldn't parse decimal value from string " + CommonTools.getStringRepresentation(valueString) + "!", e);
		}
	}

	public static Number getNumber(final Object value, final Number defaultValueForNullPointerOrEmptyNumberString,
			final Number defaultValueForInvalidNumberString, final boolean replaceCommasWithDots) {

		if (value instanceof Number) {
			return (Number) value;
		}

		final String valueString = CommonTools.toStringOrNull(value);
		if (CommonTools.isEmpty(valueString)) {
			return defaultValueForNullPointerOrEmptyNumberString;
		}

		return getBigDecimalValue(Not.Null(valueString), null,
				(defaultValueForInvalidNumberString == null ? null : new BigDecimal(defaultValueForInvalidNumberString.toString())),
				replaceCommasWithDots);
	}

	public static BigDecimal getBigDecimalValue(final Object value, final Number defaultValueForNullPointerOrEmptyNumberString,
			final Number defaultValueForInvalidNumberString, final boolean replaceCommasWithDots) {
		return getBigDecimalValue(
				getNumber(value, defaultValueForNullPointerOrEmptyNumberString, defaultValueForInvalidNumberString, replaceCommasWithDots));
	}

	public static BigDecimal getBigDecimalValue(final Number number) {
		if (number == null) {
			return null;
		}
		if (number instanceof BigDecimal) {
			return (BigDecimal) number;
		}
		if (number instanceof BigInteger) {
			return new BigDecimal((BigInteger) number);
		}
		return new BigDecimal(number.doubleValue());
	}

	/**
	 * Normalizes the passed <code>decimalString</code>. The method converts <code>,</code> to <code>.</code> and then removes all but the last
	 * <code>.</code> (since that should be the decimal separator). Furthermore the string is trimmed.
	 */

	public static String normalizeDecimalString(final String decimalString) {
		if (decimalString == null) {
			return null;
		}

		String normalizedString = decimalString;
		normalizedString = Not.Null(normalizedString.trim());
		normalizedString = Not.Null(normalizedString.replace(",", "."));
		normalizedString = StringTools.removeAllButLastOccurrence(normalizedString, ".");
		return normalizedString;
	}

	/**
	 * {@link #normalizeDecimalString(String) Normalizes} and parsed the passed <code>decimalString</code>.
	 */
	public static BigDecimal normalizeAndParseDecimalString(final String decimalString) {
		final String normalizedDecimalString = normalizeDecimalString(decimalString);
		return new BigDecimal(normalizedDecimalString);
	}

	/**
	 * See {@link #getValueOrMinimumOrMaximum(Number, Number, Number)}.
	 */
	public static <T extends Number> T getValueOrMinimum(T value, T minimumValue) {
		return getValueOrMinimumOrMaximum(value, minimumValue, null);
	}

	/**
	 * See {@link #getValueOrMinimumOrMaximum(Number, Number, Number)}.
	 */
	public static <T extends Number> T getValueOrMaximum(T value, T maximumValue) {
		return getValueOrMinimumOrMaximum(value, null, maximumValue);
	}

	/**
	 * Returns the passed <code>value</code> unless it's less than the <code>minimumValue</code> or greater than the specified
	 * <code>maximumValue</code> (which both may be <code>null</code>), in which case the minimum/maximum is returned.
	 */
	public static <T extends Number> T getValueOrMinimumOrMaximum(T value, T minimumValue, T maximumValue) {
		Arguments.notNull(value);

		Double doubleValue = value.doubleValue();
		Double doubleMinimumValue = minimumValue != null ? minimumValue.doubleValue() : null;
		Double doubleMaximumValue = maximumValue != null ? maximumValue.doubleValue() : null;

		if (doubleMinimumValue != null && doubleMaximumValue != null && doubleMinimumValue > doubleMaximumValue) {
			throw new IllegalArgumentException(
					"The specified minimum " + minimumValue + " is greater than the specified maximum " + maximumValue + "!");
		}

		if (doubleMinimumValue != null && doubleValue < doubleMinimumValue) {
			return minimumValue;
		}

		if (doubleMaximumValue != null && doubleValue > doubleMaximumValue) {
			return maximumValue;
		}

		return value;
	}

	/**
	 * Convenience method to convert a long value to a Double number and divided it by 1 billion. This is useful if you have a number (e.g., disk
	 * space, cpu speed, network speed, etc) and want to have the same number in GB, Ghz, ...
	 *
	 * @param bytes
	 *            The actual value.
	 * @param si
	 *            Indicates whether you want GibiByte or GigaByte (see https://en.wikipedia.org/wiki/Gibibyte).
	 * @param scale
	 *            The number of digits after the dot.
	 */
	public static Double getNumberInG(long bytes, boolean si, int scale) {
		int unit = si ? 1_000_000_000 : 1_073_741_824;
		Long longValue = Long.valueOf(bytes);
		Double doubleValue = longValue.doubleValue();
		doubleValue = doubleValue / unit;

		BigDecimal bd = new BigDecimal(doubleValue);
		bd = bd.setScale(scale, RoundingMode.HALF_UP);

		return bd.doubleValue();
	}

	/**
	 * Applies a minimum and maximum to a given number. Hence, the provided value must be within the range of min and max. This is basically the same
	 * as using {@link Math#min(int, int)} and {@link Math#max(int, int)} on the same input value.
	 *
	 * @param value
	 *            The value that should be clipped.
	 * @param min
	 *            The minimum allowed value.
	 * @param max
	 *            The maximum allowed value.
	 * @throws IllegalArgumentException
	 *             Thrown if the maximum is smaller than the minimum
	 * @return The clipped number.
	 */
	public static int clip(int value, int min, int max) throws IllegalArgumentException {
		if (max < min) {
			throw new IllegalArgumentException("The minimum " + min + " must not be larger than the maximum " + max);
		}
		value = Math.max(value, min);
		value = Math.min(value, max);
		return value;
	}

	/**
	 * Applies a minimum and maximum to a given number. Hence, the provided value must be within the range of min and max. This is basically the same
	 * as using {@link Math#min(long, long)} and {@link Math#max(long, long)} on the same input value.
	 *
	 * @param value
	 *            The value that should be clipped.
	 * @param min
	 *            The minimum allowed value.
	 * @param max
	 *            The maximum allowed value.
	 * @throws IllegalArgumentException
	 *             Thrown if the maximum is smaller than the minimum
	 * @return The clipped number.
	 */
	public static long clip(long value, long min, long max) throws IllegalArgumentException {
		if (max < min) {
			throw new IllegalArgumentException("The minimum " + min + " must not be larger than the maximum " + max);
		}
		value = Math.max(value, min);
		value = Math.min(value, max);
		return value;
	}

	/**
	 * Computes the provided number to the power as provided in the second parameter. This is a convenience method as the standard
	 * {@link Math#pow(double, double)} method only accepts double parameters.
	 *
	 * @param number
	 *            The number that should be operated on.
	 * @param power
	 *            The power.
	 * @return The result.
	 */
	public static long power(long number, int power) {
		long result = number;
		power--;
		while (power > 0) {
			result *= number;
			power--;
		}
		return result;
	}

}
