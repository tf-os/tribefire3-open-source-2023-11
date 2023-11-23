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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.assertj.core.api.CollectionAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.MapAssert;

import com.braintribe.testing.junit.assertions.assertj.core.api.MethodInvocationAssert.ExecutableWithReturnValue;
import com.braintribe.testing.junit.assertions.assertj.core.api.MethodInvocationAssert.ExecutableWithoutReturnValue;
import com.braintribe.testing.junit.assertions.assertj.core.api.MethodInvocationAssert.MethodInvocationSettings;

/**
 * Entry point for all custom assertions as well as {@link org.assertj.core.api.Assertions standard AssertJ assertions}. Just add a static import ...
 *
 * <pre>
 * <code class=
 * 'java'>import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;</code>
 * </pre>
 *
 * ... and then in your test methods write assertions starting with <code>assertThat</code>:
 *
 * <pre>
 * assertThat("").isEmpty();</code>
 * </pre>
 * <p>
 * In some special cases with generics the Java compiler may require a cast to be able to infer the type, although the type may be obvious. For
 * example, if a method returns <code>T extends Exception</code> and the result of this method is passed to <code>assertThat</code>, the compiler
 * needs a cast, because T could also implement an interface (e.g. {@link CharSequence}. AssertJ therefore splits into {@link AssertionsForClassTypes}
 * and {@link AssertionsForInterfaceTypes}. By statically importing <code>assertThat</code> from those classes (instead of a single static import from
 * this class), casts are no longer required. However, this doesn't work with the Oracle Java compiler (version 1.8.0_112). It also doesn't seem to be
 * best solution. Casting when required seems to be reasonable. Therefore this class serves as a single entry point for interface and class based
 * assertions.
 *
 * @author michael.lafite
 *
 * @see AssertionsForClassTypes
 * @see AssertionsForInterfaceTypes
 */
public abstract class Assertions extends org.assertj.core.api.Assertions {

	/**
	 * Creates a {@link CharSequence} assertion.
	 */
	public static ExtendedCharSequenceAssert assertThat(CharSequence actual) {
		return new ExtendedCharSequenceAssert(actual);
	}

	/**
	 * Creates a {@link Class} assertion.
	 */
	public static ExtendedClassAssert assertThat(Class<?> actual) {
		return new ExtendedClassAssert(actual);
	}

	/**
	 * Creates a {@link File} assertion.
	 */
	public static ExtendedFileAssert assertThat(File actual) {
		return new ExtendedFileAssert(actual);
	}

	/**
	 * Creates a {@link CollectionAssert} where generics are ignored. This can be useful to avoid certain generics related warnings, e.g. when working
	 * with colelctions where element type is not specified, such as <code>Collection<?>.</code>.
	 */
	public static CollectionAssert<Object> assertThatIgnoringGenerics(Collection<?> actual) {
		@SuppressWarnings("unchecked")
		Collection<Object> actualHelper = (Collection<Object>) actual;
		return new CollectionAssert<>(actualHelper);
	}

	/**
	 * Creates a {@link ListAssert} where generics are ignored. This can be useful to avoid certain generics related warnings, e.g. when working with
	 * lists where element type is not specified, such as <code>List<?>.</code>.
	 */
	public static ListAssert<Object> assertThatIgnoringGenerics(List<?> actual) {
		@SuppressWarnings("unchecked")
		List<Object> actualHelper = (List<Object>) actual;
		return new ListAssert<>(actualHelper);
	}

	/**
	 * Creates a {@link MapAssert} where generics are ignored. This can be useful to avoid certain generics related warnings, e.g. when working with
	 * maps where key and/or value types are not specified, such as <code>Map<String,?>.</code>.
	 */
	public static MapAssert<Object, Object> assertThatIgnoringGenerics(Map<?, ?> actual) {
		@SuppressWarnings("unchecked")
		Map<Object, Object> actualHelper = (Map<Object, Object>) actual;
		return new MapAssert<>(actualHelper);
	}

	/**
	 * Creates a {@link String} assertion.
	 */
	public static ExtendedStringAssert assertThat(String actual) {
		return new ExtendedStringAssert(actual);
	}

	/**
	 * Creates an assertion for a method execution which is usually specified as lambda expression. For examples, see {@link MethodInvocationAssert}.
	 */
	public static <T> MethodInvocationSettings<T> assertThatExecuting(ExecutableWithReturnValue<T> executable) {
		MethodInvocationAssert<T> methodInvocationAssert = new MethodInvocationAssert<>(executable);
		return methodInvocationAssert.configure();
	}

	/**
	 * Creates an assertion for a method execution which is usually specified as lambda expression. For examples, see {@link MethodInvocationAssert}.
	 */
	public static MethodInvocationSettings<Void> assertThatExecuting(ExecutableWithoutReturnValue executable) {
		MethodInvocationAssert<Void> methodInvocationAssert = new MethodInvocationAssert<>(executable);
		return methodInvocationAssert.configure();
	}
}
