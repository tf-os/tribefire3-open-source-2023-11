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
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Demonstrates the problem with generics which is explained in documentation of {@link org.assertj.core.api.Assertions}.
 *
 * @author michael.lafite
 */
public class ClassVsInterfaceTest {

	@Test
	public void testClassVsInterface() {
		// casting required for type that matches two asserThat (where none is more concrete than the other)
		ThrowableAndCharSequence throwableAndCharSequence = new ThrowableAndCharSequence();
		assertThat((Throwable) throwableAndCharSequence).hasNoCause();
		assertThat((CharSequence) throwableAndCharSequence).isEmpty();

		// same with generics (casting required - java version 1.8.0_112)
		// (documented out because Eclipse cleanup removed the cast)
		// assertThat((Throwable)newException()).hasNoCause();

		// here it works without casting, since no interface asserts are included
		// commented out, because it fails with Java compiler (see below)
		// org.assertj.core.api.AssertionsForClassTypes.assertThat(newException()).hasNoCause();

		/* 2017-02-07: one could statically import AssertionsForClassTypes and AssertionsForInterfaceTypes instead of Assertions. This works in
		 * Eclipse, but still fails in java version 1.8.0_112. */
	}

	@SuppressWarnings("unused")
	private static <T extends Exception> T newException() {
		return (T) new Exception();
	}

	private static class ThrowableAndCharSequence extends Exception implements CharSequence {

		private static final long serialVersionUID = -8797648422313105775L;

		@Override
		public int length() {
			return 0;
		}

		@Override
		public char charAt(int index) {
			return 0;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return null;
		}

	}

}
