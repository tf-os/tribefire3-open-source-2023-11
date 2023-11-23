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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.Not;
import com.braintribe.utils.lcd.NullSafe;

/**
 * This class may contain Java-only (i.e. GWT incompatible) code. For further information please see {@link com.braintribe.utils.lcd.ArrayTools}.
 *
 * @author michael.lafite
 */
public final class ArrayTools extends com.braintribe.utils.lcd.ArrayTools {

	private ArrayTools() {
		// no instantiation required
	}

	/**
	 * Converts a list of lists to a 2-dimensional array. Each row in the returned array has the same (i.e. maximum) length even if the lists are not
	 * the same size.
	 *
	 * @param listOfLists
	 *            A list of lists of objects.
	 * @return the two dimensional array or <code>null</code>, if the passed list is <code>null</code>.
	 */

	public static Object[][] createTwoDimensionalArrayFromListofLists(final List<List<?>> listOfLists) {
		if (listOfLists == null) {
			return null;
		}
		final int rowCount = listOfLists.size();
		int columnCount = 0;
		for (final List<?> list : listOfLists) {
			if (list == null) {
				continue;
			}
			if (list.size() > columnCount) {
				columnCount = list.size();
			}
		}
		final Object[][] matrix = new Object[rowCount][columnCount];
		int rowIndex = 0;
		for (final List<?> list : listOfLists) {
			if (list == null) {
				continue;
			}
			int columnIndex = 0;
			for (final Object object : list) {
				matrix[rowIndex][columnIndex] = object;
				columnIndex++;
			}
			rowIndex++;
		}

		return matrix;
	}

	/**
	 * Converts a list of lists of strings to a 2-dimensional array of strings. Each row in the returned array has the same (i.e. maximum) length even
	 * if the lists are not the same size. (There is also a method to create an array of objects (see
	 * {@link #createTwoDimensionalArrayFromListofLists(List)}, but no generic method yet.)
	 *
	 * @param listOfLists
	 *            A list of lists of strings.
	 * @return the two dimensional array or <code>null</code>, if the passed list is <code>null</code>.
	 */

	public static String[][] createTwoDimensionalArrayFromListofListsOfStrings(final List<List<String>> listOfLists) {
		if (listOfLists == null) {
			return null;
		}
		final int rowCount = listOfLists.size();
		int columnCount = 0;
		for (final List<String> list : listOfLists) {
			if (list == null) {
				continue;
			}
			if (list.size() > columnCount) {
				columnCount = list.size();
			}
		}
		final String[][] matrix = new String[rowCount][columnCount];
		int rowIndex = 0;
		for (final List<String> list : listOfLists) {
			if (list == null) {
				continue;
			}
			int columnIndex = 0;
			for (final String string : list) {
				matrix[rowIndex][columnIndex] = string;
				columnIndex++;
			}
			rowIndex++;
		}

		return matrix;
	}

	/**
	 * Creates a copy of the passed array (using {@link System#arraycopy(Object, int, Object, int, int)}).
	 *
	 * @param <T>
	 *            the type of the Objects in the array.
	 * @param source
	 *            the source array.
	 * @param target
	 *            the target array. It must be the same size as the source array.
	 * @return the copied array.
	 */
	public static <T> T[] copyArray(final T[] source, final T[] target) {
		Arguments.notNullWithNames("source", source, "target", target);

		if (source.length != target.length) {
			throw new IllegalArgumentException("Source and target array are not the same size! "
					+ CommonTools.getParametersString("source", source.length, "target", target.length));
		}

		System.arraycopy(source, 0, target, 0, source.length);

		return target;
	}

	/**
	 * Creates a copy of the passed 2 dimensional array.
	 *
	 * @param <T>
	 *            the type of the Objects in the array.
	 * @param source
	 *            the source array.
	 * @param target
	 *            the target array. It must be the same size as the source array.
	 * @return the copied array.
	 */
	public static <T> T[][] copy2DimensionalArray(final T[][] source, final T[][] target) {
		Arguments.notNullWithNames("source", source, "target", target);

		if (source.length != target.length) {
			throw new IllegalArgumentException(
					String.format("Source (%d) and target array (%d) are not the same size!", source.length, target.length));
		}

		for (int i = 0; i < source.length; i++) {
			for (int j = 0; j < source[i].length; j++) {
				target[i][j] = source[i][j];
			}
		}

		return target;
	}

	/**
	 * Similar to {@link #merge(Class, Object[], Object...)}, but the component type of the resulting array is taken as the component type of the
	 * first parameter, which must however be the exact same as component type of second parameter (otherwise {@link ArrayStoreException} may be
	 * thrown).
	 */
	public static <T> T[] merge(final T[] array1, final T... array2) {
		return merge(Not.Null((Class<T>) array1.getClass().getComponentType()), array1, array2);
	}

	/**
	 * Creates a new array of {@code componentType}, which is a simple concatenation of arrays provided as parameters in respective order. Note that
	 * this may be a little tricky due to a mixture of generics a arrays, so one can for example do something like:
	 * {@code merge(Collection.class, arrayofLists, arrayOfSets)}.
	 */
	public static <T> T[] merge(final Class<T> componentType, final T[] array1, final T... array2) {
		final T[] result = Not.Null((T[]) Array.newInstance(componentType, array1.length + array2.length));
		System.arraycopy(array1, 0, result, 0, array1.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		return result;
	}

	public static <T> T[] mergeArrays(final T[]... arrays) {
		return (T[]) merge((Object[]) arrays);
	}

	/**
	 * merges all kind of arrays (also primitive ones) by appending them into one array that exactly has the required length which is the sum of the
	 * lengths of the given arrays
	 */
	public static Object merge(Object... arrays) {
		int size = 0;
		for (Object array : arrays) {
			size += Array.getLength(array);
		}

		Object mergedArray = Array.newInstance(arrays[0].getClass().getComponentType(), size);

		int offset = 0;

		for (Object array : arrays) {
			int length = Array.getLength(array);
			System.arraycopy(array, 0, mergedArray, offset, length);
			offset += length;
		}

		return mergedArray;
	}

	/**
	 * Returns an array containing all the elements of the passed collection. For more info see {@link #toArray(Collection, Object[])}.
	 */
	public static <E> E[] toArray(final Collection<? extends E> collection, final Class<E> componentType) {
		final E[] array = Not.Null((E[]) Array.newInstance(componentType, NullSafe.size(collection)));
		return toArray(collection, array);
	}

	/**
	 * Splits a byte array into multiple chunks, based on the specified chunkSize.
	 *
	 * @param sourceArray
	 *            The array that should be split.
	 * @param chunkSize
	 *            The maximum size
	 * @return An array of byte arrays. If the sourceArray is null, the method also returns null.
	 * @throws IllegalArgumentException
	 *             Thrown when the chunkSize is 0 or less.
	 */
	public static byte[][] splitArray(byte[] sourceArray, int chunkSize) {
		if (sourceArray == null) {
			return null;
		}
		if (chunkSize <= 0) {
			throw new IllegalArgumentException("ChunkSize must not be 0 or less: " + chunkSize);
		}
		// first we have to check if the array can be split in multiple
		// arrays of equal 'chunk' size
		int rest = sourceArray.length % chunkSize; // if rest>0 then our last array will have less elements than the
													// others
		// then we check in how many arrays we can split our input array
		int chunks = sourceArray.length / chunkSize + (rest > 0 ? 1 : 0); // we may have to add an additional array for
																			// the 'rest'
		// now we know how many arrays we need and create our result array
		byte[][] arrays = new byte[chunks][];
		// we create our resulting arrays by copying the corresponding
		// part from the input array. If we have a rest (rest>0), then
		// the last array will have less elements than the others. This
		// needs to be handled separately, so we iterate 1 times less.
		for (int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++) {
			// this copies 'chunk' times 'chunkSize' elements into a new array
			arrays[i] = Arrays.copyOfRange(sourceArray, i * chunkSize, i * chunkSize + chunkSize);
		}
		if (rest > 0) {
			// only when we have a rest
			// we copy the remaining elements into the last chunk
			arrays[chunks - 1] = Arrays.copyOfRange(sourceArray, (chunks - 1) * chunkSize, (chunks - 1) * chunkSize + rest);
		}
		return arrays;
	}

	/**
	 * Splits an int array into multiple chunks, based on the specified chunkSize.
	 *
	 * @param sourceArray
	 *            The array that should be split.
	 * @param chunkSize
	 *            The maximum size
	 * @return An array of int arrays. If the sourceArray is null, the method also returns null.
	 * @throws IllegalArgumentException
	 *             Thrown when the chunkSize is 0 or less.
	 */
	public static int[][] splitArray(int[] sourceArray, int chunkSize) {
		if (sourceArray == null) {
			return null;
		}
		if (chunkSize <= 0) {
			throw new IllegalArgumentException("ChunkSize must not be 0 or less: " + chunkSize);
		}
		// first we have to check if the array can be split in multiple
		// arrays of equal 'chunk' size
		int rest = sourceArray.length % chunkSize; // if rest>0 then our last array will have less elements than the
													// others
		// then we check in how many arrays we can split our input array
		int chunks = sourceArray.length / chunkSize + (rest > 0 ? 1 : 0); // we may have to add an additional array for
																			// the 'rest'
		// now we know how many arrays we need and create our result array
		int[][] arrays = new int[chunks][];
		// we create our resulting arrays by copying the corresponding
		// part from the input array. If we have a rest (rest>0), then
		// the last array will have less elements than the others. This
		// needs to be handled separately, so we iterate 1 times less.
		for (int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++) {
			// this copies 'chunk' times 'chunkSize' elements into a new array
			arrays[i] = Arrays.copyOfRange(sourceArray, i * chunkSize, i * chunkSize + chunkSize);
		}
		if (rest > 0) {
			// only when we have a rest
			// we copy the remaining elements into the last chunk
			arrays[chunks - 1] = Arrays.copyOfRange(sourceArray, (chunks - 1) * chunkSize, (chunks - 1) * chunkSize + rest);
		}
		return arrays;
	}
}
