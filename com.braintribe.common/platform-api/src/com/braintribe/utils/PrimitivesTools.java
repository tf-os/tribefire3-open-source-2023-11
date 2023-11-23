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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class may contain Java-only (i.e. GWT incompatible) code. For further information please see {@link com.braintribe.utils.lcd.PrimitivesTools}.
 *
 * @author michael.lafite
 */
public final class PrimitivesTools extends com.braintribe.utils.lcd.PrimitivesTools {

	private static final Set<Class<?>> WRAPPER_CLASSES = new HashSet<>();
	private static final Map<Class<?>, Object> PRIMITIVE_CLASSES = new HashMap<>();
	private static final Map<Class<?>, Class<?>> PRIMITIVECLASS_TO_WRAPPERCLASS_MAP = new HashMap<>();
	private static final Map<Class<?>, Class<?>> WRAPPERCLASS_TO_PRIMITIVECLASS_MAP = new HashMap<>();
	private static final Map<String, Class<?>> WRAPPERCLASSNAME_TO_WRAPPERCLASS_MAP = new HashMap<>();
	private static final Map<String, Class<?>> WRAPPERSIMPLECLASSNAME_TO_WRAPPERCLASS_MAP = new HashMap<>();
	private static final Map<String, Class<?>> PRIMITIVECLASSNAME_TO_PRIMITIVECLASS_MAP = new HashMap<>();

	public static class Defaults {
		public static boolean BOOLEAN;
		public static byte BYTE;
		public static char CHAR;
		public static short SHORT;
		public static int INT;
		public static long LONG;
		public static float FLOAT;
		public static double DOUBLE;
	}

	static {
		try {
			for (Field field : Defaults.class.getFields()) {
				Object defaultValue = field.get(null);
				Class<?> wrapperClass = defaultValue.getClass();
				Class<?> primitiveClass = field.getType();
				register(wrapperClass, primitiveClass, defaultValue);
			}
		} catch (Exception e) {
			throw new IllegalStateException("unexpected error occured when generically registering primitive:wrapper:default relations", e);
		}
	}

	private static void register(Class<?> wrapperClass, Class<?> primitiveClass, Object defaultValue) {
		PRIMITIVE_CLASSES.put(primitiveClass, defaultValue);
		WRAPPERCLASS_TO_PRIMITIVECLASS_MAP.put(wrapperClass, primitiveClass);
		PRIMITIVECLASS_TO_WRAPPERCLASS_MAP.put(primitiveClass, wrapperClass);
		WRAPPERCLASSNAME_TO_WRAPPERCLASS_MAP.put(wrapperClass.getName(), wrapperClass);
		WRAPPERSIMPLECLASSNAME_TO_WRAPPERCLASS_MAP.put(wrapperClass.getSimpleName(), wrapperClass);
		PRIMITIVECLASSNAME_TO_PRIMITIVECLASS_MAP.put(primitiveClass.getName(), primitiveClass);
	}

	private PrimitivesTools() {
		// no instantiation required
	}

	/**
	 * Returns <code>true</code>, if the passed class is a wrapper class.
	 */
	public static boolean isWrapper(final Class<?> clazz) {
		return WRAPPER_CLASSES.contains(clazz);
	}

	/**
	 * Returns <code>true</code>, if the passed class is primitive type class.
	 *
	 * @deprecated don't use as {@link Class#isPrimitive()} is faster and always there
	 */
	@Deprecated
	public static boolean isPrimitive(final Class<?> clazz) {
		return clazz.isPrimitive();
	}

	/**
	 * Returns <code>true</code>, if a wrapper with the specified <code>name</code> exists.
	 *
	 * @see #getWrapper(String)
	 */
	public static boolean isWrapper(final String name) {
		return getWrapper(name) != null;
	}

	/**
	 * Returns <code>true</code>, if a wrapper class with the specified <code>className</code> exists.
	 *
	 * @see #getWrapperByClassName(String)
	 */
	public static boolean isWrapperClassName(final String className) {
		return getWrapperByClassName(className) != null;
	}

	/**
	 * Returns <code>true</code>, if a primitive type with the specified <code>name</code> exists.
	 *
	 * @see #getPrimitive(String)
	 */
	public static boolean isPrimitive(final String name) {
		return getPrimitive(name) != null;
	}

	/**
	 * Gets a primitive type class by name. For example, for "int" the method returns <code>int.class</code>.
	 */

	public static Class<?> getPrimitive(final String name) {
		return PRIMITIVECLASSNAME_TO_PRIMITIVECLASS_MAP.get(name);
	}

	/**
	 * Gets a wrapper class by simple class name. For example, for "Integer" the method returns <code>Integer.class</code>.
	 *
	 * @see #getWrapperByClassName(String)
	 */

	public static Class<?> getWrapper(final String simpleClassName) {
		return WRAPPERSIMPLECLASSNAME_TO_WRAPPERCLASS_MAP.get(simpleClassName);
	}

	/**
	 * Gets a wrapper class by class name. For example, for "java.lang.Integer" the method returns <code>Integer.class</code>.
	 *
	 * @see #getWrapper(String)
	 */

	public static Class<?> getWrapperByClassName(final String className) {
		return WRAPPERCLASSNAME_TO_WRAPPERCLASS_MAP.get(className);
	}

	/**
	 * Returns the wrapper class for passed primitive type class.
	 */

	public static Class<?> getWrapper(final Class<?> primitive) {
		return PRIMITIVECLASS_TO_WRAPPERCLASS_MAP.get(primitive);
	}

	/**
	 * Returns the primitive type class for the passed wrapper class.
	 */

	public static Class<?> getPrimitive(final Class<?> wrapper) {
		return WRAPPERCLASS_TO_PRIMITIVECLASS_MAP.get(wrapper);
	}

	/**
	 * Returns <code>true</code>, if <code>wrapper</code> wraps <code>primitive</code>, otherwise <code>false</code>.
	 *
	 * @see #assertIsWrapperOf(Class, Class)
	 */
	public static boolean isWrapperOf(final Class<?> wrapper, final Class<?> primitive) {
		final Class<?> realWrapperClass = getWrapper(primitive);
		if (wrapper.equals(realWrapperClass)) {
			return true;
		}
		return false;
	}

	/**
	 * Throws an {@link IllegalArgumentException}, if <code>wrapper</code> doesn't wrap <code>primitive</code>.
	 */
	public static void assertIsWrapperOf(final Class<?> wrapper, final Class<?> primitive) {
		if (!isWrapper(wrapper)) {
			throw new IllegalArgumentException("Class " + wrapper.getName() + " is not a wrapper type!");
		}
		if (!primitive.isPrimitive()) {
			throw new IllegalArgumentException("Class " + primitive.getName() + " is not a primitive type!");
		}

		if (!isWrapperOf(wrapper, primitive)) {
			throw new IllegalArgumentException("Class " + wrapper.getName() + " is not the wrapper type for " + primitive.getName());
		}
	}

	/**
	 * Returns the default value for the specified <code>primitive</code> type.
	 */
	public static Object getDefaultValue(final Class<?> primitive) {
		return PRIMITIVE_CLASSES.get(primitive);
	}

}
