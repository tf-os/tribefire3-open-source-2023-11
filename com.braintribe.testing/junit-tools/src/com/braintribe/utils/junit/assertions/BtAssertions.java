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
package com.braintribe.utils.junit.assertions;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import org.fest.assertions.Assertions;

/**
 * Extension of {@link Assertions} class.
 *
 * @author peter.gazdik
 */
public class BtAssertions extends Assertions {

	/**
	 * Creates a new instance of {@link ClassAssert}.
	 *
	 * @param actual
	 *            the value to be the target of the assertions methods.
	 * @return the created assertion object
	 */
	public static ClassAssert assertThat(final Class<?> actual) {
		return new ClassAssert(actual);
	}

	/**
	 * Creates a new instance of {@link ExtendedFileAssert}.
	 *
	 * @param actual
	 *            the value to be the target of the assertions methods.
	 * @return the created assertion object
	 */
	public static ExtendedFileAssert assertThat(final File actual) {
		return new ExtendedFileAssert(actual);
	}

	/**
	 * Creates a new instance of {@link ExtendedLongAssert}.
	 *
	 * @param actual
	 *            the value to be the target of the assertions methods.
	 * @return the created assertion object
	 */
	public static ExtendedLongAssert assertThat(final long actual) {
		return new ExtendedLongAssert(actual);
	}

	/**
	 * Creates a new instance of {@link ExtendedLongAssert}.
	 *
	 * @param actual
	 *            the value to be the target of the assertions methods.
	 * @return the created assertion object
	 */
	public static ExtendedLongAssert assertThat(final Long actual) {
		return new ExtendedLongAssert(actual);
	}

	/** Returns a new instance of {@link ExtendedMapAssert}. */
	public static ExtendedMapAssert assertThat(final Map<?, ?> actual) {
		return new ExtendedMapAssert(actual);
	}

	/**
	 * Creates a new instance of {@link ExtendedStringAssert}.
	 */
	public static ExtendedStringAssert assertThat(final String actual) {
		return new ExtendedStringAssert(actual);
	}

	/**
	 * Creates a new instance of {@link InputStreamAssert}.
	 *
	 * @param actual
	 *            the value to be the target of the assertions methods.
	 * @return the created assertion object
	 */
	public static InputStreamAssert assertThat(final InputStream actual) {
		return new InputStreamAssert(actual);
	}

}
