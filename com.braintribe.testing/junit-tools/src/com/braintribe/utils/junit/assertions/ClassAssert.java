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

import org.fest.assertions.GenericAssert;

/**
 * Assertion for {@code Class<?>}.
 */
public class ClassAssert extends GenericAssert<ClassAssert, Class<?>> {

	public ClassAssert(final Class<?> clazz) {
		super(ClassAssert.class, clazz);
	}

	public ClassAssert isClass(final Class<?> clazz) {
		return isEqualTo(clazz);
	}

	public ClassAssert isInterface() {
		if (!actual.isInterface()) {
			fail("Class " + actual.getName() + " is not an interface.");
		}
		return myself;
	}

	public ClassAssert isPrimitive() {
		if (!actual.isPrimitive()) {
			fail("Class " + actual.getName() + " is not primitive.");
		}
		return myself;
	}

	public ClassAssert isAssignableFrom(final Class<?> clazz) {
		failIfFirstNotAssignableFromSecond(this.actual, clazz);
		return myself;
	}

	public ClassAssert isAssignableTo(final Class<?> clazz) {
		failIfFirstNotAssignableFromSecond(clazz, this.actual);
		return myself;
	}

	private void failIfFirstNotAssignableFromSecond(final Class<?> c1, final Class<?> c2) {
		if (!c1.isAssignableFrom(c2)) {
			fail("Class " + c1.getName() + " is not assignable from: " + c2.getName());
		}
	}
}
