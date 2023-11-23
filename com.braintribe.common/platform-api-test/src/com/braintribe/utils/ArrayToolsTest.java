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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.junit.Test;

/**
 * Provides tests for {@link ArrayTools}.
 *
 * @author michael.lafite
 */
public class ArrayToolsTest {

	@SuppressWarnings("unused")
	@Test
	public void testToArray() {
		final Collection<Integer> collection = new ArrayList<>();
		final Integer obj = Integer.valueOf(1);
		collection.add(obj);

		final Object[] objectArray = ArrayTools.toArray(collection);
		Integer[] integerArray = ArrayTools.toArray(collection, new Integer[1]);
		integerArray = ArrayTools.toArray(collection, Integer.class);
		try {
			ArrayTools.toArray(collection, new Integer[0]);
			fail();
		} catch (final IllegalArgumentException e) {
			// expected (wrong target array size)
		}
		final Integer[] emptyArray = ArrayTools.toArray(null, new Integer[0]);
		assertEquals(0, emptyArray.length);
	}

	@Test
	public void testSplitArray() {

		Random rnd = new Random();
		byte[] source = new byte[10];
		rnd.nextBytes(source);

		byte[][] result = ArrayTools.splitArray(source, 2);
		verifySplitArrayResult(source, result, 5, 2);

		result = ArrayTools.splitArray(source, 3);
		verifySplitArrayResult(source, result, 4, 3);

		result = ArrayTools.splitArray(source, 1);
		verifySplitArrayResult(source, result, 10, 1);

		result = ArrayTools.splitArray(source, 100);
		verifySplitArrayResult(source, result, 1, 10);

	}

	@Test
	public void testSplitIntArray() {

		Random rnd = new Random();
		int[] source = new int[10];
		for (int i = 0; i < source.length; ++i) {
			source[i] = rnd.nextInt();
		}

		int[][] result = ArrayTools.splitArray(source, 2);
		verifySplitArrayResult(source, result, 5, 2);

		result = ArrayTools.splitArray(source, 3);
		verifySplitArrayResult(source, result, 4, 3);

		result = ArrayTools.splitArray(source, 1);
		verifySplitArrayResult(source, result, 10, 1);

		result = ArrayTools.splitArray(source, 100);
		verifySplitArrayResult(source, result, 1, 10);

	}

	protected void verifySplitArrayResult(byte[] source, byte[][] result, int expectedArrayCount, int expectedArraySize) {
		assertThat(result.length).isEqualTo(expectedArrayCount);
		int idx = 0;
		for (int arrayIndex = 0; arrayIndex < expectedArrayCount; ++arrayIndex) {
			byte[] a = result[arrayIndex];

			if (arrayIndex == (expectedArrayCount - 1)) {
				assertThat(a.length).isLessThanOrEqualTo(expectedArraySize);
			} else {
				assertThat(a.length).isEqualTo(expectedArraySize);
			}

			for (int i = 0; i < expectedArraySize && i < a.length; ++i) {
				assertThat(a[i]).isEqualTo(source[idx++]);
			}
		}
	}

	protected void verifySplitArrayResult(int[] source, int[][] result, int expectedArrayCount, int expectedArraySize) {
		assertThat(result.length).isEqualTo(expectedArrayCount);
		int idx = 0;
		for (int arrayIndex = 0; arrayIndex < expectedArrayCount; ++arrayIndex) {
			int[] a = result[arrayIndex];

			if (arrayIndex == (expectedArrayCount - 1)) {
				assertThat(a.length).isLessThanOrEqualTo(expectedArraySize);
			} else {
				assertThat(a.length).isEqualTo(expectedArraySize);
			}

			for (int i = 0; i < expectedArraySize && i < a.length; ++i) {
				assertThat(a[i]).isEqualTo(source[idx++]);
			}
		}
	}

	@Test
	public void testMerge() {
		assertThat(ArrayTools.merge(new int[] { 1, 2, 3 }, new int[] { 4, 5, 6 })).isEqualTo(new int[] { 1, 2, 3, 4, 5, 6 });
		// calling simply merge here works, but issues warnings when compiling with javac
		assertThat(ArrayTools.mergeArrays(new String[] { "hello" }, new String[] { "world" })).isEqualTo(new String[] { "hello", "world" });
	}
}
