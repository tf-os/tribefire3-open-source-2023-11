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
package com.braintribe.testing.junit.assertions.assertj.core.api;

import org.assertj.core.api.ClassAssert;
import org.assertj.core.internal.Objects;

import com.braintribe.logging.Logger;

/**
 * Provides custom {@link Class} assertions.
 *
 * @author michael.lafite
 */
public class ExtendedClassAssert extends ClassAssert implements SharedAssert<ClassAssert, Class<?>> {

	@SuppressWarnings("unused") // may be used by SharedAssert methods via reflection
	private static final Logger logger = Logger.getLogger(ExtendedClassAssert.class);

	public ExtendedClassAssert(Class<?> actual) {
		super(actual);
	}

	/**
	 * Asserts that the actual {@link Class} {@link Class#getName() name} matches the <code>expectedName</code>.
	 */
	public ExtendedClassAssert hasName(String expectedName) {
		Objects.instance().assertNotNull(info, actual);
		if (!actual.getName().equals(expectedName)) {
			failWithMessage("Class name " + toString(actual.getName()) + " doesn't match expected classname " + toString(expectedName) + ".");
		}
		return this;
	}

	/**
	 * Asserts that the class is {@link Class#isPrimitive() primitive}.
	 */
	public ExtendedClassAssert isPrimitive() {
		Objects.instance().assertNotNull(info, actual);
		if (!actual.isPrimitive()) {
			failWithMessage("Class " + actual.getName() + " should be primitive.");
		}
		return this;
	}

	/**
	 * Asserts that the class is not {@link Class#isPrimitive() primitive}.
	 */
	public ExtendedClassAssert isNotPrimitive() {
		Objects.instance().assertNotNull(info, actual);
		if (actual.isPrimitive()) {
			failWithMessage("Class " + actual.getName() + " should not be primitive.");
		}
		return this;
	}

	/**
	 * Asserts that the class is an {@link Class#isPrimitive() enum}.
	 */
	public ExtendedClassAssert isEnum() {
		Objects.instance().assertNotNull(info, actual);
		if (!actual.isEnum()) {
			failWithMessage("Class " + actual.getName() + " should be an enum.");
		}
		return this;
	}

	/**
	 * Asserts that the class is not an {@link Class#isPrimitive() enum}.
	 */
	public ExtendedClassAssert isNotEnum() {
		Objects.instance().assertNotNull(info, actual);
		if (actual.isEnum()) {
			failWithMessage("Class " + actual.getName() + " should not be an enum.");
		}
		return this;
	}
}
