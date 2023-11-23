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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.ProtectionDomain;
import java.security.ProviderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Condition;
import com.braintribe.common.lcd.Constants;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.UnexpectedNullPointerException;
import com.braintribe.common.uncheckedcounterpartexceptions.UncheckedClassNotFoundException;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.Not;
import com.braintribe.utils.lcd.NullSafe;

/**
 * This class may contain Java-only (i.e. GWT incompatible) code. For further information please see {@link com.braintribe.utils.lcd.CommonTools}.
 *
 * @author michael.lafite
 */
public final class CommonTools extends com.braintribe.utils.lcd.CommonTools {

	private static final char[] hexCode;
	static {
		hexCode = "0123456789ABCDEF".toCharArray();
	}

	private CommonTools() {
		// no instantiation required
	}

	/**
	 * Parses the passed string, casts the parsed value to the expected type and then returns it. This method can be used to parse Integer, Long,
	 * Float, Double, Boolean, Enum (and String) values from strings. One can e.g. use the method to parse parameter values.
	 *
	 * @param <T>
	 *            the expected type of the value.
	 * @param name
	 *            the name (or description) of the value, e.g. if the value to be parsed is a parameter value, the name should be the parameter name.
	 *            The name is optional though, since it's only used in error messages.
	 * @param valueAsString
	 *            the value to be parsed as string.
	 * @param valueIsMandatory
	 *            whether the value is mandatory or not. If the value is not set (i.e. <code>null</code>), the default value is returned. If a
	 *            mandatory value is not set and there is no default value, an exception is thrown.
	 * @param defaultValue
	 *            an optional default value that is returned if the <code>valueAsString</code> is <code>null</code>.
	 * @param expectedValueType
	 *            the class of the expected type of the value.
	 * @return the parsed value.
	 * @throws IllegalArgumentException
	 *             if the <code>expectedValueType</code> is <code>null</code> or if the value cannot be parsed.
	 */

	public static <T> T parseValue(final String name, final String valueAsString, final boolean valueIsMandatory, final T defaultValue,
			final Class<T> expectedValueType) throws IllegalArgumentException {

		Arguments.notNull(expectedValueType, "ExpectedValueType must not be null!");

		if (valueAsString == null) {
			if (defaultValue == null && valueIsMandatory) {
				throw new IllegalArgumentException("Error while parsing value of " + getStringRepresentation(name)
						+ ": value is mandatory but not set and no default has been specified!");
			}
			return defaultValue;
		}

		// valueAsString is not empty, hence we try to parse the value
		Object parsedValue;
		try {
			if (expectedValueType == String.class) {
				parsedValue = valueAsString;
			} else if (expectedValueType.isEnum()) {
				@SuppressWarnings("unchecked")
				final Class<Enum<?>> expectedEnumType = (Class<Enum<?>>) expectedValueType;
				parsedValue = parseEnum(expectedEnumType, valueAsString);
			} else if (expectedValueType == Boolean.class) {
				if (valueAsString.equalsIgnoreCase("true") || valueAsString.equalsIgnoreCase("yes")) {
					parsedValue = Boolean.TRUE;
				} else if (valueAsString.equalsIgnoreCase("false") || valueAsString.equalsIgnoreCase("no")) {
					parsedValue = Boolean.FALSE;
				} else {
					throw new Exception("String " + getStringRepresentation(valueAsString) + " is not a boolean value!");
				}
			} else if (expectedValueType == Integer.class) {
				parsedValue = Integer.parseInt(valueAsString);
			} else if (expectedValueType == Long.class) {
				parsedValue = Long.parseLong(valueAsString);
			} else if (expectedValueType == Double.class) {
				parsedValue = Double.parseDouble(valueAsString);
			} else if (expectedValueType == Float.class) {
				parsedValue = Float.parseFloat(valueAsString);
			} else {
				throw new Exception("Expected value type " + valueAsString + " is not supported by this method! The method has to be extended.");
			}
		} catch (final Exception e) {
			throw new IllegalArgumentException("Error while parsing value of " + getStringRepresentation(name) + "! "
					+ getParametersString("valueAsString", valueAsString, "defaultValue", defaultValue, "expectedValueType", expectedValueType), e);
		}

		try {
			// now we can perform the cast and return the result
			return expectedValueType.cast(parsedValue);
		} catch (final ClassCastException e) {
			// this code should be unreachable
			throw new RuntimeException("Caught ClassCastException. This shouldn't happen. There is an error in the code!"
					+ getParametersString("name", name, "valueAsString", valueAsString, "defaultValue", defaultValue, "expectedValueType",
							expectedValueType, "parsedValue", parsedValue),
					e);
		}
	}

	/**
	 * Invokes {@link Thread#sleep(long)} and catches (and ignores) an {@link InterruptedException} if thrown.
	 *
	 * @param milliseconds
	 *            the time to sleep.
	 */
	public static void sleep(final long milliseconds) {
		try {
			if (milliseconds > Numbers.ZERO) {
				Thread.sleep(milliseconds);
			}
		} catch (final InterruptedException e) {
			// ignore
		}
	}

	/**
	 * Waits for the specified amount of time. Note that this method ignores {@link InterruptedException}s. Instead one can specify an optional
	 * <code>breakCondition</code> to cause the method to return early.
	 */
	public static void wait(final long millisecondsToWait, final Condition breakCondition, final Integer breakConditionCheckInterval) {
		if (breakConditionCheckInterval != null && breakConditionCheckInterval < 0) {
			throw new IllegalArgumentException("Illegal break condition check interval: " + breakConditionCheckInterval);
		}

		final long endTime = System.currentTimeMillis() + millisecondsToWait;

		long maxSleepTime;
		if (breakCondition != null && breakConditionCheckInterval != null) {
			maxSleepTime = breakConditionCheckInterval;
		} else {
			maxSleepTime = Long.MAX_VALUE;
		}

		while (true) {

			if (NullSafe.evaluate(breakCondition)) {
				break;
			}

			final long millisecondsToSleep = Math.min(endTime - System.currentTimeMillis(), maxSleepTime);

			if (millisecondsToSleep < Numbers.ZERO) {
				break;
			}

			sleep(millisecondsToSleep);
		}
	}

	/**
	 * Returns the <code>Class</code> for the specified class name.
	 *
	 * @throws UncheckedClassNotFoundException
	 *             if the class with the specified class name doesn't exist.
	 */
	public static void checkClassExistence(final String className) throws UncheckedClassNotFoundException {
		try {
			Class.forName(className);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("Error while checking existence of class '" + className + "'!", e);
		}
	}

	/**
	 * Returns <code>true</code>, if the class with the specified class name exists, otherwise <code>false</code>.
	 */
	public static boolean classExists(final String className) {
		try {
			checkClassExistence(className);
		} catch (final RuntimeException e) {
			return false;
		}
		return true;
	}

	/**
	 * Creates a new {@link File} instance using the specified <code>filepath</code>. The file must be a file (and not a directory) and must already
	 * exist.
	 *
	 * @see #getFile(String, boolean, boolean, boolean, boolean)
	 */
	public static File getExistingFile(final String filepath) throws IllegalArgumentException, IOException {
		return getFile(filepath, true, false, true, false);
	}

	/**
	 * Creates a new {@link File} instance and using the specified <code>filepath</code>. The file must not exist yet. The file itself is also created
	 * (i.e. an empty file).
	 *
	 * @see #getFile(String, boolean, boolean, boolean, boolean)
	 * @see #getNotExistingFileWithoutCreatingIt(String)
	 */
	public static File getNotExistingFile(final String filepath) throws IllegalArgumentException, IOException {
		return getFile(filepath, false, true, true, false);
	}

	/**
	 * Creates a new {@link File} instance using the specified <code>filepath</code>. The file must not exist yet. The file itself is not created.
	 *
	 * @see #getFile(String, boolean, boolean, boolean, boolean, boolean)
	 * @see #getNotExistingFile(String)
	 */
	public static File getNotExistingFileWithoutCreatingIt(final String filepath) throws IllegalArgumentException, IOException {
		return getFile(filepath, false, true, true, false, true);
	}

	/**
	 * Creates a new {@link File} instance using the specified <code>filepath</code>. The file must be an existing directory.
	 *
	 * @see #getFile(String, boolean, boolean, boolean, boolean)
	 */
	public static File getExistingDirectory(final String filepath) throws IllegalArgumentException, IOException {
		return getFile(filepath, true, false, false, true);
	}

	/**
	 * Creates the specified directory which must not exist yet (otherwise an exception is thrown).
	 *
	 * @see #getFile(String, boolean, boolean, boolean, boolean)
	 */
	public static File getNotExistingDirectory(final String filepath) throws IllegalArgumentException, IOException {
		return getFile(filepath, false, true, false, true);
	}

	/**
	 * Creates a new {@link File} instance using the specified <code>filepath</code>. The file must be a directory. If it doesn't exist yet, it is
	 * created.
	 *
	 * @see #getFile(String, boolean, boolean, boolean, boolean)
	 */
	public static File getDirectory(final String filepath) throws IllegalArgumentException, IOException {
		return getFile(filepath, false, false, false, true);
	}

	/**
	 * Invokes {@link #getFile(String, boolean, boolean, boolean, boolean, boolean)} without skipping file/directory creation.
	 */
	public static File getFile(final String filepath, final boolean mustExist, final boolean mustNotExist, final boolean isFile,
			final boolean isDirectory) throws IllegalArgumentException, IOException {
		return getFile(filepath, mustExist, mustNotExist, isFile, isDirectory, false);
	}

	/**
	 * Creates a new {@link File} instance using the specified <code>filepath</code>. If the file (or directory) doesn't exist yet, it is created,
	 * unless either <code>mustExist</code> is <code>true</code> (in which case an exception is thrown) or
	 * <code>skipFileCreationForNotExistingFiles</code> is <code>true</code> (in which case neither the file/directory nor any missing parent
	 * directories are created).
	 *
	 * @param filepath
	 *            the filename and path of the file.
	 * @param mustExist
	 *            whether the file must exist.
	 * @param mustNotExist
	 *            whether the file must not exist.
	 * @param isFile
	 *            whether the file must be a file.
	 * @param isDirectory
	 *            whether the file must be a directory
	 * @param skipFileCreationForNotExistingFiles
	 *            if <code>true</code>, only the <code>File</code> instance is returned, the file/directory and any missing parent directoies are not
	 *            created.
	 * @return the {@code File}.
	 * @throws IllegalArgumentException
	 *             if the passed arguments are not valid, e.g. if both, <code>mustExist</code> and <code>mustNotExist</code> are <code>true</code> or
	 *             if the specified <code>filepath</code> is empty.
	 * @throws IOException
	 *             if the file doens't exist (although it should), if it can't be read, etc.
	 */
	public static File getFile(final String filepath, final boolean mustExist, final boolean mustNotExist, final boolean isFile,
			final boolean isDirectory, final boolean skipFileCreationForNotExistingFiles) throws IllegalArgumentException, IOException {

		if (isEmpty(filepath)) {
			throw new IllegalArgumentException("Filepath must not be empty! filepath==" + filepath);
		}

		if (mustExist && mustNotExist) {
			throw new IllegalArgumentException("File cannot exist and not exist at the same time!");
		}

		if (isFile && isDirectory) {
			throw new IllegalArgumentException("File cannot be a diretory AND a file!");
		}

		if (!(isFile || isDirectory)) {
			throw new IllegalArgumentException("File must be either a file or a directory, but both arguments are false!");
		}

		final File file = new File(filepath);

		if (file.exists() && mustNotExist) {
			throw new IOException("File '" + file + "' exists although it shouldn't!");
		}

		if (!file.exists() && mustExist) {
			throw new IOException("File '" + file + "' doesn't exist although it should!");
		}

		boolean fileReadCheckEnabled = true;

		if (file.exists()) {
			// make sure file is a directory/file (as expected)
			if (file.isDirectory() != isDirectory) {
				throw new IOException(
						"File '" + filepath + "' is a directory: " + file.isDirectory() + ", file should be a directory: " + isDirectory);
			}
		} else {
			if (skipFileCreationForNotExistingFiles) {
				// since we skip the file creation, we can disable the read check.
				fileReadCheckEnabled = false;
			} else {
				// create parent dir, if it doesn't exist
				if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
					throw new IOException("Couldn't create parent dirs of '" + file + "' (reason unknown)!");
				}

				if (isDirectory) {
					// create directory
					if (!file.mkdir()) {
						throw new IOException("Couldn't create directory '" + file + "' (reason unknown)!");
					}
				} else {
					// create new file
					if (!file.createNewFile()) {
						throw new IOException("Couldn't create file '" + file + "' (reason unknown)!");
					}
				}
			}
		}

		if (fileReadCheckEnabled && !file.canRead()) {
			throw new IOException("Cannot read file " + file + ".");
		}

		return file;
	}

	/**
	 * Returns a sorted list containing the passed <code>elements</code>.
	 */
	public static <E extends Comparable<E>> List<E> getSortedList(final Collection<E> elements) {
		final List<E> result = new ArrayList<>();
		if (!isEmpty(elements)) {
			result.addAll(elements);
			Collections.sort(result);
		}
		return result;
	}

	/**
	 * Gets the {@link Enum#name() names} of the enum constants of the specified enum.
	 */
	public static Set<String> getEnumNames(final Class<? extends Enum<?>> enumClass) {
		final Set<String> result = new HashSet<>();
		for (final Enum<?> enumObject : enumClass.getEnumConstants()) {
			result.add(enumObject.name());
		}
		return result;
	}

	/**
	 * Gets the {@link Enum#name() names} of the enum constants (in the order returned by {@link Class#getEnumConstants()} of the specified enum.
	 */
	public static List<String> getOrderedEnumNames(final Class<? extends Enum<?>> enumClass) {
		final List<String> result = new ArrayList<>();
		for (final Enum<?> enumObject : enumClass.getEnumConstants()) {
			result.add(enumObject.name());
		}
		return result;
	}

	/**
	 * Gets the {@link Enum#name() names} of the passed enums.
	 */
	public static <E extends Enum<?>> List<String> getEnumNames(final Collection<E> enums) {
		final List<String> result = new ArrayList<>();
		for (final Enum<?> enumObject : enums) {
			result.add(enumObject.name());
		}
		return result;
	}

	/**
	 * See {@link #getAllEnumConstantsExceptFor(Class, Set)}.
	 */
	public static <E extends Enum<?>> List<E> getAllEnumConstantsExceptFor(final Class<E> enumClass, final E... excludedEnumConstants) {
		return getAllEnumConstantsExceptFor(enumClass, getSet(excludedEnumConstants));
	}

	/**
	 * Gets all the {@link Class#getEnumConstants() enum constants} of the passed enum class except for the specified
	 * <code>excludedEnumConstants</code>.
	 */
	public static <E extends Enum<?>> List<E> getAllEnumConstantsExceptFor(final Class<E> enumClass, final Set<E> excludedEnumConstants) {
		final List<E> result = new ArrayList<>();
		for (final E enumConstant : enumClass.getEnumConstants()) {
			if (!excludedEnumConstants.contains(enumConstant)) {
				result.add(enumConstant);
			}
		}
		return result;
	}

	/**
	 * Gets the component type of the passed <code>array</code> and returns the corresponding wrapper type. If the component type is not primitive,
	 * the component type itself is returned. Otherwise the method returns the {@link PrimitivesTools#getWrapper(Class) wrapper} of the primitive
	 * type.
	 */
	public static Class<?> getCollectionElementTypeForArrayComponentType(final Object array) {
		assertIsArray(array);
		Class<?> componentType = array.getClass().getComponentType();
		if (componentType.isPrimitive()) {
			componentType = Not.Null(PrimitivesTools.getWrapper(componentType));
		}
		return componentType;
	}

	/**
	 * Returns a list containing the elements of the passed <code>array</code>. The method first determines the list element type (using
	 * {@link #getCollectionElementTypeForArrayComponentType(Object)}) and then just invokes {@link #arrayToList(Object, Class)}.
	 */
	public static List<? extends Object> arrayToList(final Object array) {
		return arrayToList(array, getCollectionElementTypeForArrayComponentType(array));
	}

	/**
	 * Returns a list containing the elements of the passed <code>array</code>. The <code>listElementType</code> specifies the type of the elements in
	 * the list.
	 *
	 * @throws IllegalArgumentException
	 *             if the component type of the array is not assignable to the specified <code>listElementType</code>.
	 * @see #arrayToList(Object)
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> arrayToList(final Object array, final Class<T> listElementType) {
		assertIsArray(array);
		final Class<?> arrayElementType = array.getClass().getComponentType();
		if (arrayElementType.isPrimitive()) {
			PrimitivesTools.assertIsWrapperOf(listElementType, arrayElementType);
		} else {
			if (!listElementType.isAssignableFrom(arrayElementType)) {
				throw new IllegalArgumentException("Cannot convert array to list: incompatible element types! "
						+ getParametersString("actual array element type", arrayElementType, "specified list element type", listElementType));
			}
		}

		if (!arrayElementType.isPrimitive()) {
			return toList((T[]) array);
		}
		if (arrayElementType == byte.class) {
			return (List<T>) toList((byte[]) array);
		}
		if (arrayElementType == boolean.class) {
			return (List<T>) toList((boolean[]) array);
		}
		if (arrayElementType == char.class) {
			return (List<T>) toList((char[]) array);
		}
		if (arrayElementType == double.class) {
			return (List<T>) toList((double[]) array);
		}
		if (arrayElementType == float.class) {
			return (List<T>) toList((Float[]) array);
		}
		if (arrayElementType == int.class) {
			return (List<T>) toList((int[]) array);
		}
		if (arrayElementType == long.class) {
			return (List<T>) toList((long[]) array);
		}
		if (arrayElementType == short.class) {
			return (List<T>) toList((short[]) array);
		}
		throw new AssertionError("Unsupported array element type: " + listElementType);
	}

	/**
	 * Returns the result of {@link #replaceFileSeparators(String, String)} with the {@link #FILE_SEPARATOR system dependent default separator}.
	 */
	public static String replaceFileSeparators(final String path) {
		return replaceFileSeparators(path, Constants.fileSeparator());
	}

	/**
	 * Replaces all file separators (i.e. '/' and '\') with the specified new separator
	 */
	public static String replaceFileSeparators(final String path, final String newFileSeparator) {
		String result = path;
		// we have to escape the "\" (even in the replacement string).
		// this also works, if the separator is a "/", so we just always add the escape character.
		final String escapedNewFileSeparator = "\\" + newFileSeparator;
		result = Not.Null(result.replaceAll("\\\\|/", escapedNewFileSeparator));
		return result;
	}

	/**
	 * Returns the {@link File#getCanonicalPath()} of the passed <code>path</code> or throws a {@link RuntimeException}.
	 */
	public static String getCanonicalPath(final String path) {
		return getCanonicalPath(new File(path));
	}

	/**
	 * Returns the {@link File#getCanonicalPath()} of the passed <code>file</code> or throws a {@link RuntimeException}.
	 */
	public static String getCanonicalPath(final File file) {
		String result;
		try {
			result = file.getCanonicalPath();
		} catch (final IOException e) {
			throw new RuntimeException("Couldn't get canonical path for file with absolute path " + file.getAbsolutePath() + "!", e);
		}

		return result;
	}

	/**
	 * Normalizes the passed <code>path</code> by {@link #getCanonicalPath(String) getting the canonical path} and then
	 * {@link #replaceFileSeparators(String, String) replacing all file separators} with <code>/</code> (since in Java that separator works in Windows
	 * as well).
	 */
	public static String normalizePath(final String path) {
		final String canonicalPath = getCanonicalPath(path);
		final String result = replaceFileSeparators(canonicalPath, "/");
		return result;
	}

	/**
	 * Gets the resource name (as expected by {@link ClassLoader#getResource(String)}) for the specified class. For example, for class name
	 * <code>java.lang.Integer</code> the method returns <code>java/lang/Integer.class</code>.
	 *
	 * @see #getResource(String, ClassLoader)
	 */
	public static String getResourceName(final String classname) {
		if (NullSafe.startsWith(classname, '[')) {
			throw new IllegalArgumentException("Cannot get resource for class name '" + classname + "'. Arrays are not supported!");
		}

		if (PrimitivesTools.isPrimitive(classname) || classname.equals("void")) {
			throw new IllegalArgumentException("Cannot get resource for class name '" + classname + "'. Primitive types are not supported!");
		}

		final String resourceName = classname.replace('.', '/') + ".class";
		return resourceName;
	}

	/**
	 * Finds the resource for the specified class.
	 *
	 * @param classname
	 *            the name of the class.
	 * @param loader
	 *            the class loader (if <code>null</code>, the system class loader is used).
	 * @return the resource
	 * @see #getResourceName(String)
	 */

	public static String getResource(final String classname, final ClassLoader loader) {
		final String resourceName = getResourceName(classname);

		final ClassLoader classLoader = (loader == null ? ClassLoader.getSystemClassLoader() : loader);
		final URL url = classLoader.getResource(resourceName);
		return (url == null ? null : url.toString());
	}

	/**
	 * Invokes {@link Supplier#get()} on the passed <code>provider</code>.
	 *
	 * @throws RuntimeException
	 *             if the <code>provider</code> throws a {@link ProviderException}.
	 * @throws UnexpectedNullPointerException
	 *             if the <code>provider</code> returns <code>null</code>.
	 */
	public static <T> T provide(final Supplier<T> provider) throws RuntimeException, UnexpectedNullPointerException {
		try {
			final T result = provider.get();
			if (result == null) {
				throw new UnexpectedNullPointerException(
						"Provider unexpectedly returned null! " + CommonTools.getParametersString("provider", provider));
			}
			return result;
		} catch (final Exception e) {
			throw new RuntimeException("Unexpected provider error!", e);
		}
	}

	/**
	 * Invokes {@link Function#apply(Object)} on the passed <code>provider</code>.
	 *
	 * @throws RuntimeException
	 *             if the <code>provider</code> throws a {@link ProviderException}.
	 * @throws UnexpectedNullPointerException
	 *             if the <code>provider</code> returns <code>null</code>.
	 */
	public static <I, E> E provide(final Function<I, E> provider, final I index) throws RuntimeException, UnexpectedNullPointerException {
		try {
			final E result = provider.apply(index);
			if (result == null) {
				throw new UnexpectedNullPointerException(
						"Provider unexpectedly returned null! " + CommonTools.getParametersString("provider", provider, "index", index));
			}
			return result;
		} catch (final Exception e) {
			throw new RuntimeException("Unexpected provider error!", e);
		}
	}

	/**
	 * Returns the <code>Class</code> specified by <code>className</code>.
	 */
	public static Class<?> getClass(final String className) throws UncheckedClassNotFoundException {
		try {
			return Not.Null(Class.forName(className));
		} catch (final ClassNotFoundException e) {
			throw new UncheckedClassNotFoundException("Couldn't find class named " + className + "!", e);
		}
	}

	/**
	 * Returns the name of the method that called this method.
	 */
	public static String getMethodName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}

	/**
	 * Returns the name of the method that invoked the method that called this method.
	 */
	public static String getInvokingMethodName() {
		return Thread.currentThread().getStackTrace()[3].getMethodName();
	}

	/**
	 * Converts the passed array of bytes into a (lower case) hex string. This can be used to convert {@link MessageDigest} hashes (e.g. MD5 bytes to
	 * string).
	 */
	public static String asString(final byte[] bytes) {
		final String result = printHexBinary(bytes).toLowerCase();
		return result;
	}

	/**
	 * Converts a byte array into its hex-representation.
	 *
	 * @param byteArray
	 *            The byte array that should be converted to a String.
	 * @return The hex representation of the byte array.
	 */
	public static String printHexBinary(byte[] byteArray) {
		StringBuilder r = new StringBuilder(byteArray.length * 2);
		for (byte b : byteArray) {
			r.append(hexCode[(b >> 4 & 0xF)]);
			r.append(hexCode[(b & 0xF)]);
		}
		return r.toString();
	}

	/**
	 * Given a Class object, attempts to find its .class location [returns null if no such definition can be found]. Use for testing/debugging only.
	 *
	 * @return URL that points to the class definition [null if not found].
	 */
	public static URL getClassLocation(final Class<?> cls) {
		if (cls == null) {
			throw new IllegalArgumentException("null input: cls");
		}

		URL result = null;
		final String clsAsResource = cls.getName().replace('.', '/').concat(".class");

		final ProtectionDomain pd = cls.getProtectionDomain();
		// java.lang.Class contract does not specify if 'pd' can ever be null;
		// it is not the case for Sun's implementations, but guard against null
		// just in case:
		if (pd != null) {
			final CodeSource cs = pd.getCodeSource();
			// 'cs' can be null depending on the classloader behavior:
			if (cs != null) {
				result = cs.getLocation();
			}

			if (result != null) {
				// Convert a code source location into a full class file location
				// for some common cases:
				if ("file".equals(result.getProtocol())) {
					try {
						if (result.toExternalForm().endsWith(".jar") || result.toExternalForm().endsWith(".zip")) {
							result = new URL("jar:".concat(result.toExternalForm()).concat("!/").concat(clsAsResource));
						} else if (new File(result.getFile()).isDirectory()) {
							result = new URL(result, clsAsResource);
						}
					} catch (MalformedURLException ignore) {
						// shouldn't happen
					}
				}
			}
		}

		if (result == null) {
			// Try to find 'cls' definition as a resource; this is not
			// documented to be legal, but Sun's implementations seem to allow this:
			final ClassLoader clsLoader = cls.getClassLoader();

			result = clsLoader != null ? clsLoader.getResource(clsAsResource) : ClassLoader.getSystemResource(clsAsResource);
		}

		return result;
	}

	/**
	 * Returns the {@link System#getenv() environment variables} as a <code>Map</code>.
	 */
	public static Map<String, String> getEnvironmentVariables() {
		Map<String, String> result = new HashMap<>();
		for (String key : System.getenv().keySet()) {
			result.put(key, System.getenv(key));
		}
		return result;
	}

	/**
	 * Returns the {@link System#getProperties() system properties} as a <code>Map</code>.
	 */
	public static Map<String, String> getSystemProperties() {
		Map<String, String> result = new HashMap<>();
		for (Object keyObject : System.getProperties().keySet()) {
			String key = (String) keyObject;
			result.put(key, System.getProperty(key));
		}
		return result;
	}
}
