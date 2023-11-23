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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.utils.PropertyTools.SortProperty.Direction;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.NullSafe;

/**
 * This class provides utility methods related to properties (i.e. which can be accessed via <code>get[PropertyName]</code> and
 * <code>set[PropertyName]</code> methods).
 *
 * @author michael.lafite
 */
public class PropertyTools {

	/**
	 * See {@link CommonTools#getGetterMethodName(String)}.
	 */
	public static String getGetterMethodName(final String property) {
		return CommonTools.getGetterMethodName(property);
	}

	/**
	 * See {@link CommonTools#getSetterMethodName(String)}.
	 */
	public static String getSetterMethodName(final String property) {
		return CommonTools.getSetterMethodName(property);
	}

	/**
	 * Invokes the specified <code>getterMethod</code> on the passed <code>object</code>.
	 */
	private static Object invokeGetterMethod(final Method getterMethod, final Object object) {
		try {
			return getterMethod.invoke(object);
		} catch (final Exception e) {
			throw new RuntimeException(
					"Error while invoking getter method! " + CommonTools.getParametersString("method", getterMethod, "object", object), e);
		}
	}

	/**
	 * Gets the value of the specified property.
	 */
	public static Object getPropertyValue(final Object object, final String property) {
		Arguments.notNull(object, "The passed object must not be null!");
		Arguments.notNull(object, "The passed property must not be null!");
		final String getterName = getGetterMethodName(property);
		// TODO: there could be multiple getter methods for the same property
		final Method getterMethod = ReflectionTools.getMethod(getterName, object.getClass());
		if (getterMethod == null) {
			throw new IllegalArgumentException("No getter method for property '" + property + "' found!");
		}
		final Object value = invokeGetterMethod(getterMethod, object);
		return value;
	}

	/**
	 * Gets the value of the specified <code>property</code> for each element of the passed <code>collection</code> and returns the values in a list.
	 */
	public static <T> List<Object> getPropertyValues(final Collection<T> collection, final String property) {
		final List<Object> propertyValues = new ArrayList<>();
		for (final T object : NullSafe.collection(collection)) {
			final Object propertyValue = getPropertyValue(object, property);
			propertyValues.add(propertyValue);
		}
		return propertyValues;
	}

	/**
	 * Invokes {@link #getPropertyValueByPath(Object, String, boolean, PropertyGetter)} with a {@link DefaultPropertyGetter}.
	 */
	public static Object getPropertyValueByPath(final Object object, final String propertyPath, final boolean nullsInPathAllowed) {
		return getPropertyValueByPath(object, propertyPath, nullsInPathAllowed, new DefaultPropertyGetter());
	}

	/**
	 * Gets the value of the property specified by the <code>propertyPath</code>, e.g. <code>document.version.info</code> (--&gt;
	 * <code>getDocument().getVersion().getInfo()</code>).
	 *
	 * @param nullsInPathAllowed
	 *            whether or not a <code>null</code> in the path is allowed, e.g. if it's fine when in <code>getDocument().getVersion()</code>,
	 *            <code>getDocument()</code> returns <code>null</code>. Otherwise an exception is thrown.
	 * @param propertyGetter
	 *            used to get the values of the properties in the <code>propertyPath</code>. Default implementation is {@link DefaultPropertyGetter}.
	 * @return the value of the specified property.
	 */
	public static Object getPropertyValueByPath(final Object object, final String propertyPath, final boolean nullsInPathAllowed,
			final PropertyGetter propertyGetter) {
		Arguments.notNull(object, "The passed object must not be null!");
		Arguments.notNull(object, "The passed propertyPath must not be null!");

		final String currentProperty = getFirstProperty(propertyPath);
		final String restOfPropertyPath = getPropertyPathAfterFirstProperty(propertyPath);

		final Object currentPropertyValue = propertyGetter.getPropertyValue(object, currentProperty);
		if (restOfPropertyPath == null) {
			return currentPropertyValue;
		}

		if (currentPropertyValue == null) {
			if (nullsInPathAllowed) {
				return null;
			}
			throw new GenericRuntimeException("Cannot get property by path '" + propertyPath + "', because property '" + currentProperty
					+ "' is not set in the passed object " + object + "!");
		}
		return getPropertyValueByPath(currentPropertyValue, restOfPropertyPath, nullsInPathAllowed, propertyGetter);
	}

	/**
	 * Returns the first property from the property path, e.g. "property1" if path is "property1.property2".
	 */
	public static String getFirstProperty(final String propertyPath) {
		return StringTools.getSubstringBefore(propertyPath, ".");
	}

	/**
	 * Returns the property path after the {@link #getFirstProperty(String) first property}, e.g. "property2.property3" if path is
	 * "property1.property2.property3".
	 */
	public static String getPropertyPathAfterFirstProperty(final String propertyPath) {
		final String propertyPathAfterFirstProperty = StringTools.getSubstringAfter(propertyPath, ".");
		if (CommonTools.isEmpty(propertyPathAfterFirstProperty)) {
			return null;
		}
		return propertyPathAfterFirstProperty;
	}

	/**
	 * Invokes {@link #getListSortedByProperties(PropertyGetter, Collection, SortProperty...)} with a {@link DefaultPropertyGetter}.
	 */
	public static <T> List<T> getListSortedByProperties(final Collection<T> collection, final SortProperty... properties) {
		return getListSortedByProperties(new DefaultPropertyGetter(), collection, properties);
	}

	/**
	 * Works {@link #sortByProperties(PropertyGetter, List, SortProperty...)} except that the elements from the <code>collection</code> are first put
	 * into a new <code>List</code>. The passed <code>collection</code> will not be modified.
	 */
	public static <T> List<T> getListSortedByProperties(final PropertyGetter propertyGetter, final Collection<T> collection,
			final SortProperty... properties) {
		final ArrayList<T> list = new ArrayList<>(collection);
		sortByProperties(propertyGetter, list, properties);
		return list;
	}

	/**
	 * Invokes {@link #sortByProperties(PropertyGetter, List, SortProperty...)} with a {@link DefaultPropertyGetter}.
	 */
	public static <T> void sortByProperties(final List<T> list, final SortProperty... properties) {
		sortByProperties(new DefaultPropertyGetter(), list, properties);
	}

	/**
	 * Sorts the elements in the passed <code>list</code> by the specified <code>sortProperties</code>.
	 *
	 * @param propertyGetter
	 *            the {@link PropertyGetter} used to retrieve the property values
	 * @param list
	 *            the list to sort.
	 * @param properties
	 *            the properties by which the elements in the list shall be sorted.
	 */
	public static <T> void sortByProperties(final PropertyGetter propertyGetter, final List<T> list, final SortProperty... properties) {
		Collections.sort(list, new SortPropertyBasedComparator<T>(propertyGetter, properties));
	}

	/**
	 * Helper class used to specify a property by which elements can be sorted.
	 *
	 * @see PropertyTools#sortByProperties(PropertyGetter, List, SortProperty...)
	 * @author michael.lafite
	 */
	public static class SortProperty {
		private String path;
		private Direction direction;

		public enum Direction {
			ASCENDING,
			DESCENDING
		}

		public SortProperty(final String path) {
			this(path, Direction.ASCENDING);
		}

		public SortProperty(final String path, final Direction direction) {
			this.path = path;
			this.direction = direction;
		}

		public String getPath() {
			return this.path;
		}

		public void setPath(final String path) {
			this.path = path;
		}

		public Direction getDirection() {
			return this.direction;
		}

		public void setDirection(final Direction direction) {
			this.direction = direction;
		}
	}

	/**
	 * A <code>PropertyGetter</code> retrieves the value of a property.
	 *
	 * @see PropertyTools#getPropertyValueByPath(Object, String, boolean, PropertyGetter)
	 * @author michael.lafite
	 */
	public static interface PropertyGetter {
		public Object getPropertyValue(Object object, String propertyName);
	}

	/**
	 * Default implementation of {@link PropertyGetter}. Uses {@link PropertyTools#getPropertyValue(Object, String)} to retrieve property values.
	 *
	 * @author michael.lafite
	 */
	public static class DefaultPropertyGetter implements PropertyGetter {
		@Override
		public Object getPropertyValue(final Object object, final String propertyName) {
			return PropertyTools.getPropertyValue(object, propertyName);
		}
	}

	/**
	 * A {@link Comparator} that compares elements based on the configured {@link SortProperty}s.
	 *
	 * @author michael.lafite
	 */
	private static class SortPropertyBasedComparator<T> implements Comparator<T> {
		private final SortProperty[] sortProperties;
		private final PropertyGetter propertyGetter;

		public SortPropertyBasedComparator(final PropertyGetter propertyGetter, final SortProperty[] sortProperties) {
			this.propertyGetter = propertyGetter;
			this.sortProperties = sortProperties;

		}

		private PropertyGetter getPropertyGetter() {
			return this.propertyGetter;
		}

		private SortProperty[] getSortProperties() {
			return this.sortProperties;
		}

		@Override
		public int compare(final T o1, final T o2) {
			Arguments.notNullWithNames("object1", o1, "object2", o2);

			if (getSortProperties() != null) {
				for (final SortProperty sortProperty : getSortProperties()) {
					final String propertyPath = sortProperty.getPath();
					int comparisonResult = comparePropertyValues(o1, o2, propertyPath);

					if (comparisonResult != Numbers.ZERO) {
						if (sortProperty.getDirection().equals(Direction.DESCENDING)) {
							comparisonResult = comparisonResult * Numbers.NEGATIVE_ONE;
						}
						return comparisonResult;
					}
				}
			}

			return Numbers.ZERO;
		}

		private <C extends Comparable<C>> int comparePropertyValues(final Object o1, final Object o2, final String propertyPath) {
			@SuppressWarnings("unchecked")
			final C value1 = (C) PropertyTools.getPropertyValueByPath(o1, propertyPath, true, getPropertyGetter());
			@SuppressWarnings("unchecked")
			final C value2 = (C) PropertyTools.getPropertyValueByPath(o2, propertyPath, true, getPropertyGetter());

			int comparisonResult = NullSafe.compare(value1, value2);
			return comparisonResult;
		}
	}
}
