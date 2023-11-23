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
package com.braintribe.utils.genericmodel.entity.lcd;

import java.math.BigDecimal;
import java.util.Date;

import com.braintribe.model.generic.GenericEntity;

/**
 * Provides {@link GenericEntity} related tools and convenience methods.
 *
 * @author michael.lafite
 */
public class GmEntityTools {

	/**
	 * Returns the GM simple type Java type for the specified <code>clazz</code>. In most cases this method just returns
	 * the passed <code>clazz</code>. However, {@link BigDecimal} and {@link Date} are not final. Therefore, if one
	 * passes a sub class of these classes, the respective super type is returned. Furthermore the method makes sure
	 * that the <code>clazz</code> is a valid Java representation of GM simple type. For example, {@link String} is
	 * valid, but {@link Character} is not.
	 *
	 * @throws IllegalArgumentException 
	 *             if the passed <code>clazz</code> is <code>null</code> or not a valid GM simple type class.
	 */
	public static Class<?> getGmSimpleTypeJavaType(Class<?> clazz) {

		if (clazz == null) {
			throw new IllegalArgumentException("The passed class must not be null!");
		}

		Class<?> gmSimpleTypeClass = getGmSimpleTypeClassOrNull(clazz);

		if (gmSimpleTypeClass == null) {
			throw new IllegalArgumentException("Class " + clazz.getName() + " is not a GM simple type class!");
		}

		return gmSimpleTypeClass;
	}

	/**
	 * Returns <code>true</code>, if there is a GM simple type Java type for the specified <code>clazz</code>. See
	 * {@link #getGmSimpleTypeJavaType(Class)} for more info.
	 *
	 * @throws IllegalArgumentException
	 *            if the passed <code>clazz</code> is <code>null</code> or not a valid GM simple type class.
	 */
	public static boolean hasGmSimpleTypeJavaType(Class<?> clazz) {

		if (clazz == null) {
			throw new IllegalArgumentException("The passed class must not be null!");
		}

		Class<?> gmSimpleTypeClass = getGmSimpleTypeClassOrNull(clazz);

		return gmSimpleTypeClass != null;
	}

	/**
	 * @see #getGmSimpleTypeJavaType(Class)
	 * @see #hasGmSimpleTypeJavaType(Class)
	 */
	private static Class<?> getGmSimpleTypeClassOrNull(Class<?> clazz) {

		if (clazz == null) {
			throw new IllegalArgumentException("The passed class must not be null!");
		}

		Class<?> gmSimpleTypeClass;

		if (Boolean.class.equals(clazz)) {
			gmSimpleTypeClass = clazz;
		} else if (Date.class.equals(clazz)) {
			gmSimpleTypeClass = Date.class;
		} else if (BigDecimal.class.equals(clazz)) {
			gmSimpleTypeClass = BigDecimal.class;
		} else if (Double.class.equals(clazz)) {
			gmSimpleTypeClass = clazz;
		} else if (Float.class.equals(clazz)) {
			gmSimpleTypeClass = clazz;
		} else if (Integer.class.equals(clazz)) {
			gmSimpleTypeClass = clazz;
		} else if (Long.class.equals(clazz)) {
			gmSimpleTypeClass = clazz;
		} else if (String.class.equals(clazz)) {
			gmSimpleTypeClass = clazz;
		} else {
			gmSimpleTypeClass = null;
		}

		return gmSimpleTypeClass;
	}
}
