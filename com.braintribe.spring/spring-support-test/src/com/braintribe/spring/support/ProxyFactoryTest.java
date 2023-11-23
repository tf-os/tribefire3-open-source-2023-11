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

import java.util.Arrays;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.braintribe.cfg.Required;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * Simple test that demonstrates how a {@link org.springframework.aop.framework.ProxyFactoryBean ProxyFactoryBean} in
 * combination with an {@link MethodInterceptor} can be used to easily extend the functionality of a service.
 * 
 * @author michael.lafite
 */
public class ProxyFactoryTest {

	@Test
	public void test() {
		final ApplicationContext context = new FileSystemXmlApplicationContext("res/"
				+ ProxyFactoryTest.class.getSimpleName() + ".xml");

		final TestService testService = (TestService) context.getBean("proxyFactoryTest.testService");

		final String message = "test message";
		BtAssertions.assertThat(testService.echo(message)).endsWith(message);
	}

	public static class LoggingInterceptor implements org.aopalliance.intercept.MethodInterceptor {

		public Object invoke(final MethodInvocation invocation) throws Throwable {
			System.out.println("Invoking method " + invocation.getMethod().getName() + " with arguments "
					+ Arrays.asList(invocation.getArguments()) + " ...");
			final Object result = invocation.proceed();
			System.out.println("Invoked method " + invocation.getMethod().getName() + ". Result is '" + result + "'.");
			return result;
		}
	}

	/**
	 * A simple test service.
	 * 
	 * @author michael.lafite
	 */
	public static interface TestService {

		/**
		 * Returns the passed <code>message</code>.
		 */
		public String echo(String message);
	}

	/**
	 * {@link TestService} implementation.
	 * 
	 * @author michael.lafite
	 */
	public static class TestServiceImpl implements TestService {

		private String prefix;

		/**
		 * Returns the passed <code>message</code>, prepended by the configured {@link #setPrefix(String) prefix}.
		 */
		public String echo(final String message) {
			return getPrefix() + message;
		}

		public String getPrefix() {
			return this.prefix;
		}

		@Required
		public void setPrefix(final String prefix) {
			this.prefix = prefix;
		}

	}

}
