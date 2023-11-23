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

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

public class GzipBase64CodecTest {

	@Test
	public void testCodecSimple() throws Exception {
		
		GzipBase64Codec codec = new GzipBase64Codec();
		
		String test = "Hello, world.";
		
		String encodedString = codec.encode(test);
		
		String decodedString = codec.decode(encodedString);
		
		Assert.assertEquals(test, decodedString);
		
	}
	

	@Test
	public void testCodecEmpty() throws Exception {
		
		GzipBase64Codec codec = new GzipBase64Codec();
		
		String test = "";

		String encodedString = codec.encode(test);
		
		String decodedString = codec.decode(encodedString);
		
		Assert.assertEquals(test, decodedString);
		
	}
	

	@Test
	public void testCodecNull() throws Exception {
		
		GzipBase64Codec codec = new GzipBase64Codec();
		
		String encodedString = codec.encode(null);
		Assert.assertNull(encodedString);
		
		String decodedString = codec.decode(null);
		Assert.assertNull(decodedString);
				
	}
	
	@Test
	public void testCodecRandomString() throws Exception {
		
		GzipBase64Codec codec = new GzipBase64Codec();

		int count = 10000;
		
		String test = RandomStringUtils.randomAlphanumeric(count);
		
		String encodedString = codec.encode(test);
		
		String decodedString = codec.decode(encodedString);
		
		Assert.assertEquals(test, decodedString);
				
	}
	
	@Test
	public void testCodecLargeText() throws Exception {
		
		GzipBase64Codec codec = new GzipBase64Codec();

		int count = 10000;
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<count; ++i) {
			sb.append("The quick brown fox jumps over the lazy dog. ");
		}
		String test = sb.toString();
		
		String encodedString = codec.encode(test);
		
		int testLength = test.length();
		int encodedStringLength = encodedString.length();
		Assert.assertTrue(encodedStringLength < testLength);
		
		String decodedString = codec.decode(encodedString);
		
		Assert.assertEquals(test, decodedString);
				
	}
	
	@Test
	public void testValueClass() throws Exception {
		GzipBase64Codec codec = new GzipBase64Codec();
		Class<String> valueClass = codec.getValueClass();
		Assert.assertEquals(String.class, valueClass);
	}

}
