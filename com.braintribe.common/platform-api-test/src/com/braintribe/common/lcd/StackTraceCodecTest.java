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
package com.braintribe.common.lcd;

import static com.braintribe.common.lcd.StackTraceCodec.INSTANCE;

import org.junit.Assert;
import org.junit.Test;

public class StackTraceCodecTest {

	@Test
	public void test() {

		StackTraceElement[] original = Thread.currentThread().getStackTrace();

		String encoded = INSTANCE.encode(original);

		StackTraceElement[] decoded = INSTANCE.decode(encoded);

		Assert.assertEquals(original.length, decoded.length);

		for (int i = 0; i < original.length; i++) {

			StackTraceElement originalElement = original[i];

			StackTraceElement decodedElement = decoded[i];

			Assert.assertEquals(originalElement.getClassName(), decodedElement.getClassName());
			Assert.assertEquals(originalElement.getMethodName(), decodedElement.getMethodName());
			Assert.assertEquals(originalElement.getFileName(), decodedElement.getFileName());
			Assert.assertEquals(originalElement.getLineNumber(), decodedElement.getLineNumber());
			Assert.assertEquals(originalElement.isNativeMethod(), decodedElement.isNativeMethod());

		}

	}

	@Test
	public void testNullArray() {

		StackTraceElement[] original = null;

		String encoded = INSTANCE.encode(original);

		Assert.assertNull(encoded);

		StackTraceElement[] decoded = INSTANCE.decode(encoded);

		Assert.assertNull(decoded);

	}

	@Test
	public void testEmptyArray() {

		StackTraceElement[] original = {};

		String encoded = INSTANCE.encode(original);

		Assert.assertNull(encoded);

		StackTraceElement[] decoded = INSTANCE.decode(encoded);

		Assert.assertNull(decoded);

		decoded = INSTANCE.decode("");

		Assert.assertNull(decoded);

	}

	@Test
	public void testEmptyString() {

		StackTraceElement[] decoded = INSTANCE.decode("");

		Assert.assertNull(decoded);

	}

	@Test
	public void testIllegalStrings() {

		StackTraceElement[] decoded = INSTANCE.decode("Class:method\nClass:method:file");

		Assert.assertNull(decoded);

		decoded = INSTANCE.decode("\n\n\n\n\n\n");

		Assert.assertNull(decoded);

		decoded = INSTANCE.decode("\n\n\nClass:method\nClass:method:file\n\n\n");

		Assert.assertNull(decoded);

	}

}
