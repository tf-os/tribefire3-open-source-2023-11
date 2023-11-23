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
package com.braintribe.spring.support;

import java.util.function.Supplier;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;


import com.braintribe.utils.junit.assertions.BtAssertions;

public class StringToProviderConverterTest {

	@Test
	public void test() throws RuntimeException {
		final ApplicationContext context = new FileSystemXmlApplicationContext("res/"
				+ StringToProviderConverterTest.class.getSimpleName() + ".xml");

		final TestBean testBean = (TestBean) context.getBean("stringToProviderConverterTest.testBean");
		BtAssertions.assertThat(testBean.getStringProvider().get()).isEqualTo("provided string value");
	}

	public static class TestBean {

		private Supplier<String> stringProvider;

		public Supplier<String> getStringProvider() {
			return this.stringProvider;
		}

		public void setStringProvider(final Supplier<String> stringProvider) {
			this.stringProvider = stringProvider;
		}
	}
}
