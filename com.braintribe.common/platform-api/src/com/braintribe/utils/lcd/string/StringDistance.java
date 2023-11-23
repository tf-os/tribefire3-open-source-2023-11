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
package com.braintribe.utils.lcd.string;

import java.util.Collection;
import java.util.stream.Stream;

import com.braintribe.utils.lcd.NullSafe;

/**
 * @author peter.gazdik
 */
public class StringDistance {

	/**
	 * For given string returns all the strings from the passed collection whose {@link #damerauLevenshteinDistance(CharSequence, CharSequence)} is
	 * less than given threshold.
	 */
	public static Stream<String> findCloseStrings(String string, Collection<String> strings, int threshold) {
		return strings.stream() //
				.filter(s -> areCloseStrings(s, string, threshold));
	}

	private static boolean areCloseStrings(String s, String string, int threshold) {
		int dist = damerauLevenshteinDistance(s, string, threshold);
		return dist < threshold && dist < s.length() && dist < string.length();
	}

	/**
	 * Optimization version of {@link #damerauLevenshteinDistance(CharSequence, CharSequence)} which returns "threshold" if the length difference is
	 * at least threshold.
	 * <p>
	 * This optimizes the case when we are searching for strings "closer" than given threshold by not computing the distance if it obviously is equal
	 * or higher than said threshold.
	 */
	public static int damerauLevenshteinDistance(CharSequence a, CharSequence b, int max) {
		a = NullSafe.get(a, "");
		b = NullSafe.get(b, "");

		if (Math.abs(a.length() - b.length()) >= max) {
			return max;
		}

		return damerauLevenshteinDistance(a, b);
	}

	/**
	 * Calculates the <a href="https://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance">Damareau-Levenshtein distance</a> of two strings.
	 * More precisely, it computes the "Optimal string alignment" distance, as that one is way easier to compute.
	 * <p>
	 * The order of arguments doesn't matter, but the distance is case sensitive.
	 * <p>
	 * <tt>null</tt> is considered an empty string.
	 *
	 * @see #damerauLevenshteinDistance(CharSequence, CharSequence, int)
	 */
	public static int damerauLevenshteinDistance(CharSequence a, CharSequence b) {
		a = NullSafe.get(a, "");
		b = NullSafe.get(b, "");

		int aLen = a.length();
		int bLen = b.length();

		if (aLen == 0) {
			return bLen;
		}
		if (bLen == 0) {
			return aLen;
		}

		int[] dist = new int[aLen + 1];
		int[] dist_1 = new int[aLen + 1];
		int[] dist_2 = new int[aLen + 1];

		for (int i = 0; i <= aLen; i++) {
			dist[i] = i;
		}

		for (int j = 1; j <= bLen; j++) {
			int[] tmp = dist_2;
			dist_2 = dist_1;
			dist_1 = dist;
			dist = tmp;

			char bChar = b.charAt(j - 1);
			dist[0] = j;

			for (int i = 1; i <= aLen; i++) {
				int cost = a.charAt(i - 1) == bChar ? 0 : 1;
				dist[i] = min( //
						dist[i - 1] + 1, //
						dist_1[i] + 1, //
						dist_1[i - 1] + cost //
				);

				// this makes it different from Levenshtein distance, we also consider swapping two chars
				if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == bChar) {
					dist[i] = Math.min(dist[i], dist_2[i - 2] + cost);
				}
			}
		}
		return dist[aLen];
	}

	/**
	 * Calculates the <a href="https://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein distance</a> of two strings.
	 */
	public static int levenshteinDistance(String a, String b) {
		a = NullSafe.get(a, "");
		b = NullSafe.get(b, "");

		int aLen = a.length();
		int bLen = b.length();

		if (aLen == 0) {
			return bLen;
		}
		if (bLen == 0) {
			return aLen;
		}

		int[] dist_1 = new int[aLen + 1];
		int[] dist = new int[aLen + 1];
		int[] tmp; // helper to swap arrays

		int cost;

		for (int i = 0; i <= aLen; i++) {
			dist[i] = i;
		}

		for (int j = 1; j <= bLen; j++) {
			tmp = dist_1;
			dist_1 = dist;
			dist = tmp;

			char bChar = b.charAt(j - 1);
			dist[0] = j;

			for (int i = 1; i <= aLen; i++) {
				cost = (a.charAt(i - 1) == bChar) ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, to the
				// diagonally left and to the up +cost
				dist[i] = min( //
						dist[i - 1] + 1, //
						dist_1[i] + 1, //
						dist_1[i - 1] + cost //
				);
			}
		}

		return dist[aLen];
	}

	private static int min(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

}
