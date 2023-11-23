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
package com.braintribe.util.velocity;


import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.braintribe.testing.junit.rules.ThrowableChain;
import com.braintribe.testing.junit.rules.ThrowableChainRule;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.velocity.SimpleVelocityTemplateRenderer;

/**
 * Provides tests for {@link SimpleVelocityTemplateRenderer}.
 * 
 * @author michael.lafite
 */

public class SimpleVelocityTemplateRendererTest {

	@Rule
	public ThrowableChainRule exceptionChainRule = new ThrowableChainRule();

	@Test
	public void testSimpleTemplates() {

		assertThat(
				SimpleVelocityTemplateRenderer.quickEvaluate("--- $sampleString ---", "sampleString", "SUCCESS"))
				.isEqualTo("--- SUCCESS ---");

		final String loopTestTemplate = "#foreach( $person in $personList )\r\n"
				+ "   Person $person.name is $person.age years old.\r\n" + "#end";
		final String result = SimpleVelocityTemplateRenderer.quickEvaluate(loopTestTemplate, "personList",
				CommonTools.getList(new Person("john", 32), new Person("jane", 29)));
		assertThat(result).matches("(?s).*john.*32.*jane.*29.*");
	}

	@ThrowableChain(org.apache.velocity.exception.MathException.class)
	@Test
	public void testDivisionByZeroCausesMathException() {
		SimpleVelocityTemplateRenderer.quickEvaluate("#set($result = 1/0)");
	}

	/**
	 * A simple test class.
	 * 
	 * @author michael.lafite
	 */
	public static class Person {
		private String name;
		private int age;

		public Person(final String name, final int age) {
			super();
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return this.name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public int getAge() {
			return this.age;
		}

		public void setAge(final int age) {
			this.age = age;
		}
	}

}
