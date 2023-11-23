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

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * This class provides utility methods that involve randomly generated numbers, strings, UUIDs, etc. *
 *
 * @author michael.lafite
 */
public final class RandomTools {

	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS").withLocale(Locale.US);

	private RandomTools() {
		// no instantiation required
	}

	/**
	 * Returns a random string of length 32 containing only hexadecimal numbers (much like the GUIDs used in CSP, although the algorithm is not the
	 * same).
	 *
	 * @param dtsPrefixEnabled
	 *            if <code>true</code>, the first 15 characters will contain the timestamp.
	 * @return the random time string.
	 */
	public static String getRandom32CharactersHexString(final boolean dtsPrefixEnabled) {
		String uuidString = UUID.randomUUID().toString().replace("-", "");

		if (dtsPrefixEnabled) {
			uuidString = timeStamp() + uuidString.substring(1, 18);
		}

		return uuidString;
	}

	public static String timeStamp() {
		return DateTools.encode(new Date(), dateFormat);
	}

	/**
	 * Returns the result of {@link #getRandom32CharactersHexString(boolean)} with timestamp prefix enabled.
	 */
	public static String newStandardUuid() {
		return getRandom32CharactersHexString(true);
	}

	/**
	 * Returns a random element from an indicated collection.
	 *
	 * @param elements
	 *            The provided collection.
	 * @return An random element of the collection.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getRandomCollectionElement(final Collection<T> elements) {
		int index = new Random().nextInt(elements.size());
		return (T) elements.toArray()[index];
	}

	/**
	 * Returns a random key from an indicated map collection.
	 *
	 * @param elements
	 *            The provided map collection.
	 * @return An random key of the map collection.
	 */
	public static <K> K getRandomMapKey(final Map<K, ?> elements) {
		return getRandomCollectionElement(elements.keySet());
	}
}
