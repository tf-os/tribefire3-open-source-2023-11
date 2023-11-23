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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class provides {@link Enum} related utility methods.
 *
 * @author michael.lafite
 */
public class EnumTools {

	/**
	 * Works like {@link #findByName(String, Class, boolean)} with case sensitivity enabled.
	 */
	public static <E extends Enum<?>> E findByName(final String name, final Class<E> enumType) {
		return findByName(name, enumType, true);
	}

	/**
	 * Returns the <code>Enum</code> constant for the given <code>name</code>. The method at first compares the names using the <code>==</code>
	 * operator. If the <code>Enum</code> constant is not found that way, the comparison is performed again using {@link String#equals(Object) equals}
	 * (and afterwards optionally also {@link String#equalsIgnoreCase(String)}). <br>
	 * Note that this algorithm is a bit slower than using {@link Enum#valueOf(Class, String)} if the <code>Enum</code> name is (usually) specified
	 * correctly (i.e. case sensitively), but it's faster otherwise. Furthermore the <code>valueOf</code> method would enforce a stronger generic
	 * restriction.
	 *
	 * @param enumType
	 *            the <code>Enum</code> class. Must not be <code>null</code>.
	 * @param name
	 *            The name of the searched <code>Enum</code> constant. Must not be <code>null</code> or empty.
	 * @return the enum value.
	 * @throws IllegalArgumentException
	 *             if the specified <code>Enum</code> constant doesn't exist.
	 */
	public static <E extends Enum<?>> E findByName(final String name, final Class<E> enumType, boolean caseSensitivityEnabled) {

		Arguments.notNullWithNames("enumType", enumType, "enumName", name);

		final E[] enumConstants = enumType.getEnumConstants();

		for (final E enumConstant : enumConstants) {
			if (enumConstant.name() == name) {
				return enumConstant;
			}
		}

		for (final E enumConstant : enumConstants) {
			if (enumConstant.name().equals(name)) {
				return enumConstant;
			}
		}

		if (!caseSensitivityEnabled) {
			for (final E enumConstant : enumConstants) {
				if (enumConstant.name().equalsIgnoreCase(name)) {
					return enumConstant;
				}
			}
		}
		return null;

	}

	/**
	 * Returns <code>true</code>, if the given constant can be {@link #findByName(String, Class) found}, otherwise <code>false</code>.
	 */
	public static <E extends Enum<?>> boolean hasConstant(final String name, final Class<E> enumType) {
		return findByName(name, enumType) != null;
	}

	/**
	 * Works like {@link #getByName(String, Class, boolean)} with case sensitivity enabled.
	 */
	public static <E extends Enum<?>> E getByName(final String name, final Class<E> enumType) {
		return getByName(name, enumType, true);
	}

	/**
	 * Works like {@link #findByName(String, Class, boolean)}, but throws an exception if there is no <code>Enum</code> constant with the given
	 * <code>name</code>.
	 *
	 * @throws IllegalArgumentException
	 *             if the specified enum constant doesn't exist.
	 */
	public static <E extends Enum<?>> E getByName(final String name, final Class<E> enumType, boolean caseSensitivityEnabled) {
		E result = findByName(name, enumType, caseSensitivityEnabled);
		if (result == null) {
			throw new IllegalArgumentException("No enum constant named " + CommonTools.getStringRepresentation(name) + " in enum class "
					+ enumType.getName() + ". Valid names: " + getNames(enumType));
		}
		return result;
	}

	/**
	 * Returns the <code>Enum</code> {@link Class#getEnumConstants() constants} of the given <code>enumType</code> as list.
	 */
	public static <E extends Enum<?>> List<E> getConstants(final Class<E> enumType) {
		Arguments.notNullWithName("enumType", enumType);
		return Arrays.asList(enumType.getEnumConstants());
	}

	/**
	 * Returns the {@link Enum#name() names} of the <code>Enum</code> {@link #getConstants(Class) constants} of the given <code>enumType</code>.
	 */
	public static <E extends Enum<?>> List<String> getNames(final Class<E> enumType) {
		List<String> result = new ArrayList<>();
		for (E enumConstant : getConstants(enumType)) {
			result.add(enumConstant.name());
		}
		return result;
	}

	/**
	 * Similar to {@link #convertIfPossible(Enum, Class)}, but throws an exception if the <code>constant</code> can't be converted.
	 *
	 * @throws IllegalArgumentException
	 *             if the <code>constant</code> can't be converted.
	 */
	public static <E extends Enum<?>, E2 extends Enum<E2>> E2 convert(E constant, Class<E2> targetEnumType) {
		E2 result = convertIfPossible(constant, targetEnumType);
		if (result == null) {
			throw new IllegalArgumentException("Can't convert enum constant named '" + constant.name() + "' of type " + constant.getClass().getName()
					+ " to constant of target type " + targetEnumType.getName() + ", because the given constant doesn't exist in the target type!");
		}
		return result;
	}

	/**
	 * Returns the <code>Enum</code> constant from <code>targetEnumType</code> with the same {@link Enum#name() name} as the given
	 * <code>constant</code> or <code>null</code>, if no such constant exists in the target.
	 */
	public static <E extends Enum<?>, E2 extends Enum<E2>> E2 convertIfPossible(E constant, Class<E2> targetEnumType) {
		return findByName(constant.name(), targetEnumType);
	}
}
