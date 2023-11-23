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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.braintribe.common.lcd.Pair;
import com.braintribe.utils.lcd.CommonTools;

/**
 * {@link Pair} related utility methods. Please note that GWT compatible utility methods can also be added directly to the {@link Pair} class.
 *
 * @author michael.lafite
 */
public final class PairTools {

	private PairTools() {
		// no instantiation required
	}

	/**
	 * Creates a list of {@link Pair}s of {@link Object}s. See {@link #getPairList(Class, Class, Object...)}.
	 */
	public static List<Pair<Object, Object>> getPairList(final Object... objectPairs) {
		return getPairList(Object.class, Object.class, objectPairs);
	}

	/**
	 * Creates a list of {@link Pair}s using the passed first/second pairs.
	 *
	 * @param firstAndSecondPairs
	 *            first/second pairs (i.e. first, second, first, second, ...)
	 * @return the list.
	 */
	public static <F, S> List<Pair<F, S>> getPairList(final Class<F> firstClass, final Class<S> secondClass, final Object... firstAndSecondPairs) {
		final List<Pair<F, S>> pairs = new ArrayList<>();

		if (firstAndSecondPairs != null) {

			if (!CommonTools.isEven(firstAndSecondPairs.length)) {
				throw new IllegalArgumentException(
						"Cannot create pairs because the number of objects is not even! " + Arrays.asList(firstAndSecondPairs));
			}

			for (int i = 0; i < firstAndSecondPairs.length - 1; i += 2) {
				final F first = firstClass.cast(firstAndSecondPairs[i]);
				final S second = secondClass.cast(firstAndSecondPairs[i + 1]);
				final Pair<F, S> pair = new Pair<>(first, second); // SuppressPMDWarnings (instantiation in loop
																	// is
				// fine here)
				pairs.add(pair);
			}
		}
		return pairs;
	}
}
