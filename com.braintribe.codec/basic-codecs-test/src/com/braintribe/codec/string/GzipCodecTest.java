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
package com.braintribe.codec.string;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class GzipCodecTest {

	@Test
	public void testNull() throws Exception {
		GzipCodec codec = new GzipCodec();
		Assert.assertNull(codec.encode(null));
		Assert.assertNull(codec.decode(null));
	}
	
	@Test
	public void testCodecSimple() throws Exception {
		
		GzipCodec codec = new GzipCodec();
		
		String test = "Hello, world.";
		byte[] testBytes = test.getBytes("UTF-8");
		
		byte[] encodedBytes = codec.encode(testBytes);
		
		byte[] decodedBytes = codec.decode(encodedBytes);
		
		String decodedString = new String(decodedBytes, "UTF-8");
		
		Assert.assertEquals(test, decodedString);
		
	}
	

	@Test
	public void testCodecEmpty() throws Exception {
		
		GzipCodec codec = new GzipCodec();
		
		String test = "";
		byte[] testBytes = test.getBytes("UTF-8");
		
		byte[] encodedBytes = codec.encode(testBytes);
		
		byte[] decodedBytes = codec.decode(encodedBytes);
		
		String decodedString = new String(decodedBytes, "UTF-8");
		
		Assert.assertEquals(test, decodedString);
		
	}
	

	@Test
	public void testCodecNull() throws Exception {
		
		GzipCodec codec = new GzipCodec();
		
		byte[] encodedBytes = codec.encode(null);
		Assert.assertNull(encodedBytes);
		
		byte[] decodedBytes = codec.decode(null);
		Assert.assertNull(decodedBytes);
				
	}
	
	@Test
	public void testCodecRandomBytes() throws Exception {
		
		GzipCodec codec = new GzipCodec();

		Random rnd = new Random();
		int count = 10000;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(count);
		for (int i=0; i<count; ++i) {
			baos.write(rnd.nextInt());
		}
		baos.close();
		
		byte[] testBytes = baos.toByteArray();
		
		byte[] encodedBytes = codec.encode(testBytes);
		
		byte[] decodedBytes = codec.decode(encodedBytes);
		
		Assert.assertArrayEquals(testBytes, decodedBytes);
				
	}
	
	@Test
	public void testCodecLargeText() throws Exception {
		
		GzipCodec codec = new GzipCodec();

		int count = 10000;
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<count; ++i) {
			sb.append("The quick brown fox jumps over the lazy dog. ");
		}
		String testString = sb.toString();
		
		byte[] testBytes = testString.getBytes("UTF-8");
		
		byte[] encodedBytes = codec.encode(testBytes);
		
		byte[] decodedBytes = codec.decode(encodedBytes);
		
		String decodedString = new String(decodedBytes, "UTF-8");
		
		Assert.assertEquals(testString, decodedString);
				
	}
	
	@Test
	public void testValueClass() throws Exception {
		GzipCodec codec = new GzipCodec();
		Class<byte[]> valueClass = codec.getValueClass();
		Assert.assertEquals(byte[].class, valueClass);
	}
}
