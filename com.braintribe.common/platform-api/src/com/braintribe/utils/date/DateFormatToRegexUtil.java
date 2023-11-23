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
package com.braintribe.utils.date;

import java.util.zip.DataFormatException;

/**
 * This class will parse the date format string into a RegEx string.
 *
 */
public class DateFormatToRegexUtil {
	private static final char DEFAULT_CHAR = '\0';
	private static final char TEXT_CHAR = '\'';

	/**
	 * This method will parse the date format string into a RegEx string.
	 *
	 * @param dateFormatString
	 *            The date format string, where you want the RegEx for
	 * @return The RegEx to match the date format string.
	 */
	public static String getRegexFromDateFormatString(final String dateFormatString) {
		try {
			// Result String containing the RegEx
			final StringBuilder result = new StringBuilder();
			result.append('^');

			// Needed variables to create RegEx
			char lastFormatChar = DEFAULT_CHAR;
			boolean inTextFlag = false;
			int digitsCount = 0;

			// Check all chars of the date format string
			for (int index = 0; index < dateFormatString.length(); index++) {
				final char currentFormatChar = dateFormatString.charAt(index);

				// Set flags needed for the RegEx convert
				final boolean lastStringChar = (index + 1 == dateFormatString.length());
				final boolean charChanged = checkCharChanged(currentFormatChar, lastFormatChar);

				// Check flags
				if (inTextFlag) {
					// Check current format char
					if (currentFormatChar == TEXT_CHAR) {
						// End of text reached
						inTextFlag = false;
						continue;
					}

					// Check for the last char
					if (lastStringChar) {
						// End of text is missing
						throw new DataFormatException();
					}

					// Add char to RegEx
					result.append(currentFormatChar);
					continue;
				} else if (!charChanged) {
					// Increase char counter
					digitsCount++;

					// Resolve char, if it's the last one
					if (!lastStringChar) {
						continue;
					}
				}

				// Check current format char
				if (currentFormatChar == TEXT_CHAR) {
					// Begin of text reached
					inTextFlag = true;
				}

				// Resolve last format char, needed to get the format char count
				if (lastStringChar || (!lastStringChar && charChanged)) {
					// Get RegEx part for the last format char
					final String regexPart = getRegexPart(lastFormatChar, digitsCount);
					if (regexPart != null) {
						// Add RegEx part to result
						result.append(regexPart);
					}

					// Set correct format variables
					if (!inTextFlag) {
						// Change last format char
						lastFormatChar = currentFormatChar;
						digitsCount = 1;
					} else {
						// Set default (Begin of text)
						lastFormatChar = DEFAULT_CHAR;
						digitsCount = 0;
					}
				}

				// Special case: Resolve current format char
				if (lastStringChar && charChanged) {
					// Get RegEx part for the current format char
					final String regexPart = getRegexPart(currentFormatChar, digitsCount);
					if (regexPart != null) {
						// Add RegEx part to result
						result.append(regexPart);
					}

					// Set default (End of convert)
					lastFormatChar = DEFAULT_CHAR;
					digitsCount = 0;
				}
			}

			// Return RegEx
			result.append('$');
			return result.toString();
		} catch (final DataFormatException ex) {
			// Error result
			return null;
		}
	}

	/**
	 * This method will check, if the format char was changed or not.
	 *
	 * @param currentFormatChar
	 *            The current format char (E.g.: y for year)
	 * @param lastFormatChar
	 *            The last format char (E.g.: y for year)
	 * @return true/false
	 */
	private static boolean checkCharChanged(final char currentFormatChar, final char lastFormatChar) {
		return (lastFormatChar == DEFAULT_CHAR || currentFormatChar != lastFormatChar);
	}

	/**
	 * This format will return specific RegEx parts depending of the received format Char and digits count.
	 *
	 * @param formatChar
	 *            The format char (E.g.: y for year)
	 * @param digitsCount
	 *            The count how often the format char was defined
	 * @return The RegEx of the defined format Char and digits count.
	 * @throws DataFormatException
	 *             if the format is invalid
	 */
	private static String getRegexPart(final char formatChar, final int digitsCount) throws DataFormatException {
		// Check the received format char
		if (Character.toLowerCase(formatChar) == 'y') {
			// Check digits count
			if (digitsCount == 2) {
				// 2 digits year
				return "(\\d{2})";
			} else if (digitsCount == 4) {
				// 3 or 4 digits year
				return "(\\d{3,4})";
			} else {
				// Error not valid
				throw new DataFormatException();
			}
		} else if (formatChar == 'M') {
			// Check digits count
			if (digitsCount == 1) {
				// Month (1 to 12)
				return "(([1-9])|(1[0-2]))";
			} else if (digitsCount == 2) {
				// Month (01 to 12)
				return "((0[1-9])|(1[0-2]))";
			} else if (digitsCount == 3) {
				// Month name (Jan)
				return "(\\D{3})";
			} else {
				// Full month name
				return "(\\D{3,9})";
			}
		} else if (formatChar == 'd') {
			// Check digits count
			if (digitsCount == 1) {
				// Day (1 to 31)
				return "(([1-9])|([12]\\d)|(3[01]))";
			} else if (digitsCount == 2) {
				// Day (01 to 31)
				return "((0[1-9])|([12]\\d)|(3[01]))";
			} else {
				// Error not valid
				throw new DataFormatException();
			}
		} else if (formatChar == 'H') {
			// Check digits count
			if (digitsCount == 1) {
				// Hour (0 to 23)
				return "((\\d)|(1\\d)|(2[0-3]))";
			} else if (digitsCount == 2) {
				// Hour (00 to 23)
				return "(([01]\\d)|(2[0-3]))";
			} else {
				// Error not valid
				throw new DataFormatException();
			}
		} else if (formatChar == 'm' || formatChar == 's') {
			// Check digits count
			if (digitsCount == 1) {
				// Minute/Second (0 to 59)
				return "((\\d)|([1-5]\\d))";
			} else if (digitsCount == 2) {
				// Minute/Second (00 to 59)
				return "([0-5]\\d)";
			} else {
				// Error not valid
				throw new DataFormatException();
			}
		} else if (formatChar == 'E') {
			// Check digits count
			if (digitsCount < 3) {
				// Error not valid
				throw new DataFormatException();
			} else if (digitsCount == 3) {
				// Weekday (Mon)
				return "(\\D{3})";
			} else {
				// Full weekday
				return "(\\D{3,9})";
			}
		} else if (formatChar != DEFAULT_CHAR) {
			// Special case interpreted as text
			return String.valueOf(formatChar);
		}

		// Invalid
		return null;
	}
}
