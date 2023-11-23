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
package com.braintribe.utils.lcd;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This class provides utility methods related to arrays.
 *
 * @author michael.lafite
 */
public class ArrayTools {

	protected ArrayTools() {
		// nothing to do
	}

	public static boolean isEmpty(final byte... array) {
		return CommonTools.isEmpty(array);
	}

	public static boolean isEmpty(final boolean... array) {
		return CommonTools.isEmpty(array);
	}

	public static boolean isEmpty(final char... array) {
		return CommonTools.isEmpty(array);
	}

	public static boolean isEmpty(final double... array) {
		return CommonTools.isEmpty(array);
	}

	public static boolean isEmpty(final float... array) {
		return CommonTools.isEmpty(array);
	}

	public static boolean isEmpty(final int... array) {
		return CommonTools.isEmpty(array);
	}

	public static boolean isEmpty(final long... array) {
		return CommonTools.isEmpty(array);
	}

	public static boolean isEmpty(final short... array) {
		return CommonTools.isEmpty(array);
	}

	public static boolean isEmpty(final Object... array) {
		return CommonTools.isEmpty(array);
	}

	/**
	 * Returns <code>true</code> if at least one object is <code>null</code>.
	 */
	public static boolean isAnyNull(final Object... objects) {
		return CommonTools.isAnyNull(objects);
	}

	/**
	 * Returns <code>true</code> if all objects are <code>null</code>.
	 */
	public static boolean isAllNull(final Object... objects) {
		if (isEmpty(objects)) {
			throw new IllegalArgumentException("Cannot perform check. No objects specified!");
		}
		for (final Object object : objects) {
			if (object != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the number of null pointers.
	 *
	 * @see #getNonNullPointerCount(Object...)
	 */
	public static int getNullPointerCount(final Object... objects) {
		return CommonTools.getNullPointerCount(objects);
	}

	/**
	 * Returns the number of objects that are not <code>null</code>.
	 *
	 * @see #getNullPointerCount(Object...)
	 */
	public static int getNonNullPointerCount(final Object... objects) {
		return objects.length - getNullPointerCount(objects);
	}

	/**
	 * Returns the first reference from the passed <code>objects</code> array that is not a null pointer (or <code>null</code> if the
	 * <code>objects</code> array is <code>null</code>, empty or contains only null pointers).
	 */

	public static Object getFirstNonNullPointer(final Object... objects) {
		if (!isEmpty(objects)) {
			for (final Object object : objects) {
				if (object != null) {
					return object;
				}
			}
		}
		return null;
	}

	public static List<Byte> toList(final byte... array) {
		return CommonTools.toList(array);
	}

	public static List<Boolean> toList(final boolean... array) {
		return CommonTools.toList(array);
	}

	public static List<Character> toList(final char... array) {
		return CommonTools.toList(array);
	}

	public static List<Double> toList(final double... array) {
		return CommonTools.toList(array);
	}

	public static List<Float> toList(final float... array) {
		return CommonTools.toList(array);
	}

	public static List<Integer> toList(final int... array) {
		return CommonTools.toList(array);
	}

	public static List<Long> toList(final long... array) {
		return CommonTools.toList(array);
	}

	public static List<Short> toList(final short... array) {
		return CommonTools.toList(array);
	}

	public static <T> List<T> toList(final T... array) {
		return CommonTools.toList(array);
	}

	/**
	 * Returns an array containing all the elements of the passed collection. If no <code>collection</code> is passed, an empty array is returned.
	 *
	 * @see #toArray(Collection, Object[])
	 */
	public static Object[] toArray(final Collection<?> collection) {
		final Object[] array = new Object[NullSafe.size(collection)];
		return toArray(collection, array);
	}

	/**
	 * Inserts all the elements of the <code>collection</code> into the target array. The target array must have the same size as the collection! The
	 * collection may be <code>null</code> (in which case an empty array is returned).
	 *
	 * @return the passed <code>array</code> (for convenience it is also returned)
	 * @see #toArray(Collection)
	 */
	public static <E> E[] toArray(final Collection<? extends E> collection, final E[] targetArrayWithCorrectSize) {
		final int size = NullSafe.size(collection);
		if (targetArrayWithCorrectSize.length != size) {
			throw new IllegalArgumentException("Invalid target array size! " + CommonTools.getParametersString("collection size", size, "array size",
					targetArrayWithCorrectSize.length, "collection", collection, "array", Arrays.asList(targetArrayWithCorrectSize)));
		}
		if (!CommonTools.isEmpty(collection)) {
			collection.toArray(targetArrayWithCorrectSize);
		}
		return targetArrayWithCorrectSize;
	}
}
