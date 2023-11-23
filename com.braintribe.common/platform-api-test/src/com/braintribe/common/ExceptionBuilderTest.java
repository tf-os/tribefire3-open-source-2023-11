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
package com.braintribe.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link ExceptionBuilder}.
 *
 */
public class ExceptionBuilderTest {

	private static Throwable cause = new IllegalArgumentException("Cause message " + System.currentTimeMillis());
	private static String message = "Exception message " + System.currentTimeMillis();

	@Test
	public void testStandardConstructor() {

		Throwable result = ExceptionBuilder.createException(StandardConstructor.class.getName(), message);

		Assert.assertEquals(StandardConstructor.class, result.getClass());
		Assert.assertEquals(message, result.getMessage());

	}

	@Test
	public void testStandardConstructorWithCause() {

		Throwable result = ExceptionBuilder.createException(StandardConstructor.class.getName(), message, cause);

		Assert.assertEquals(StandardConstructor.class, result.getClass());
		Assert.assertEquals(message, result.getMessage());
		Assert.assertNotNull(result.getCause());
		Assert.assertEquals(cause.getClass(), result.getCause().getClass());

	}

	@Test
	public void testCauseConstructor() {

		Throwable result = ExceptionBuilder.createException(CauseConstructor.class.getName(), message);

		Assert.assertEquals(CauseConstructor.class, result.getClass());
		Assert.assertNull(result.getMessage());

	}

	@Test
	public void testCauseConstructorWithCause() {

		Throwable result = ExceptionBuilder.createException(CauseConstructor.class.getName(), message, cause);

		Assert.assertEquals(CauseConstructor.class, result.getClass());
		Assert.assertEquals(cause.getClass().getName() + ": " + cause.getMessage(), result.getMessage());
		Assert.assertNotNull(result.getCause());
		Assert.assertEquals(cause.getClass(), result.getCause().getClass());

	}

	@Test
	public void testMessageConstructor() {

		Throwable result = ExceptionBuilder.createException(MessageConstructor.class.getName(), message);

		Assert.assertEquals(MessageConstructor.class, result.getClass());
		Assert.assertEquals(message, result.getMessage());

	}

	@Test
	public void testMessageConstructorWithCause() {

		Throwable result = ExceptionBuilder.createException(MessageConstructor.class.getName(), message, cause);

		Assert.assertEquals(MessageConstructor.class, result.getClass());
		Assert.assertEquals(message, result.getMessage());
		Assert.assertNotNull(result.getCause());
		Assert.assertEquals(cause.getClass(), result.getCause().getClass());

	}

	@Test
	public void testNoArgsConstructor() {

		Throwable result = ExceptionBuilder.createException(NoArgsConstructor.class.getName(), message);

		Assert.assertEquals(NoArgsConstructor.class, result.getClass());
		Assert.assertNull(result.getMessage());

	}

	@Test
	public void testNoArgsConstructorWithCause() {

		Throwable result = ExceptionBuilder.createException(NoArgsConstructor.class.getName(), message, cause);

		Assert.assertEquals(NoArgsConstructor.class, result.getClass());
		Assert.assertNull(result.getMessage());
		Assert.assertNotNull(result.getCause());
		Assert.assertEquals(cause.getClass(), result.getCause().getClass());

	}

	@Test
	public void testNoEligibleConstructor() {

		Throwable result = ExceptionBuilder.createException(NoEligibleConstructor.class.getName(), message);

		Assert.assertEquals(Exception.class, result.getClass());
		Assert.assertEquals(NoEligibleConstructor.class.getName() + ": " + message, result.getMessage());

	}

	@Test
	public void testNoEligibleConstructorWithCause() {

		Throwable result = ExceptionBuilder.createException(NoEligibleConstructor.class.getName(), message, cause);

		Assert.assertEquals(Exception.class, result.getClass());
		Assert.assertEquals(NoEligibleConstructor.class.getName() + ": " + message, result.getMessage());
		Assert.assertNotNull(result.getCause());
		Assert.assertEquals(cause.getClass(), result.getCause().getClass());

	}

	public static class StandardConstructor extends Exception {

		private static final long serialVersionUID = 1L;

		public StandardConstructor(String message, Throwable cause) {
			super(message, cause);
		}

	}

	public static class MessageConstructor extends Exception {

		private static final long serialVersionUID = 1L;

		public MessageConstructor(String message) {
			super(message);
		}

	}

	public static class CauseConstructor extends Exception {

		private static final long serialVersionUID = 1L;

		public CauseConstructor(Throwable cause) {
			super(cause);
		}

	}

	public static class NoArgsConstructor extends Exception {

		private static final long serialVersionUID = 1L;

	}

	public static class NoEligibleConstructor extends Exception {

		private static final long serialVersionUID = 1L;

		public NoEligibleConstructor(@SuppressWarnings("unused") Boolean b) {
			Assert.fail("Shouldn't be choosen");
		}

	}

}
