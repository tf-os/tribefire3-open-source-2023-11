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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.junit.Test;

@SuppressWarnings("unused")
public class ReflectionToolsTest {

	@Test
	public void testGenericsParameterResolution() {
		Type type = ReflectionTools.getGenericsParameter(E.class, A.class, "T");

		assertThat(type).isSameAs(String.class);
	}

	@Test
	public void testUnconcreteGenericsParameterResolution() {
		Type type = ReflectionTools.getGenericsParameter(P.class, O.class, "T");

		assertThat(type).isInstanceOf(TypeVariable.class);
		assertThat(((TypeVariable<?>) type).getName()).isEqualTo("S");
	}

	@Test(expected = IllegalStateException.class)
	public void testGenericsParameterResolutionWithoutGivenInheritance() {
		Type type = ReflectionTools.getGenericsParameter(P.class, A.class, "T");
	}

	// @formatter:off
	interface O<T> { /* empty */	}
	interface P<S> extends O<S> { /* empty */ }
	interface A<T> { /* empty */ }
	interface Z<T> { /* empty */ }
	interface B<Y, X extends CharSequence> extends A<X>, Z<Y> { /* empty */ }
	interface B1<X1 extends CharSequence> extends B<Integer, X1> { /* empty */ }
	interface C { /* empty */ }
	interface D { /* empty */ }
	interface E extends B1<String>, C, D { /* empty */ }
	// @formatter:on

}
