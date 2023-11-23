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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.ClassAssert;
import org.junit.Test;

import com.braintribe.logging.Logger.LogLevel;

/**
 * Provides {@link ClassAssert} related tests.
 *
 * @author michael.lafite
 */
public class ExtendedClassAssertTest {

	@Test
	public void test() {
		assertThat(Object.class).hasName("java.lang.Object");

		assertThat(Object.class).isNotInterface();
		assertThat(Comparable.class).isInterface();

		assertThat(Object.class).isAssignableFrom(String.class);

		assertThat(LogLevel.class).isEnum();
		assertThat(Integer.class).isNotEnum();

		assertThat(Integer.class).isNotPrimitive();
		assertThat(int.class).isPrimitive();

		assertThat(Void.class).isNotPrimitive();
		assertThat(void.class).isPrimitive();
	}

}
