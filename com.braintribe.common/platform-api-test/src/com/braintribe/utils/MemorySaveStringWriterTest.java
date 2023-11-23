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

import org.junit.Assert;
import org.junit.Test;

public class MemorySaveStringWriterTest {

	@Test
	public void testBufferLimit() throws Exception {
		MemorySaveStringWriter writer = new MemorySaveStringWriter(20);
		writer.write("Hello world!");
		writer.write("This is a test to check whether the buffer limit works.");
		writer.close();
		String result = writer.toString();
		Assert.assertEquals("Hello world!This is a test to check whether the buffer limit works.", result);
		writer.destroy();
	}

	@Test
	public void testBufferLimit2() throws Exception {
		MemorySaveStringWriter writer = new MemorySaveStringWriter(20);
		writer.write("Hello world!");
		writer.write("This is a test to check whether the buffer limit works.");
		writer.write("Hello world, again!");
		writer.close();
		String result = writer.toString();
		Assert.assertEquals("Hello world!This is a test to check whether the buffer limit works.Hello world, again!", result);
		writer.destroy();
	}

	@Test
	public void testBufferLimit3() throws Exception {
		MemorySaveStringWriter writer = new MemorySaveStringWriter(20);
		writer.write("Hello world!");
		writer.close();
		String result = writer.toString();
		Assert.assertEquals("Hello world!", result);
		writer.destroy();
	}
}
