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
package com.braintribe.web.multipart.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.braintribe.testing.test.AbstractTest;
import com.braintribe.web.multipart.impl.FormDataMultipartConstants;
import com.braintribe.web.multipart.streams.ContentFilteringInputStream;

public class ContentFilteringStreamsTest extends AbstractTest implements FormDataMultipartConstants {
	private final static String DEFAULT_STRING = "Hallo Du!";
	private final static int DEFAULT_STRING_LENGTH = DEFAULT_STRING.length();
	
	@Test
	public void testSimpleRead() throws Exception{
		testSingleRead(DEFAULT_STRING);
	}
	
	@Test
	public void testSimplePrefix() throws Exception{
		ContentFilteringInputStream in = createStreamWith("--" + DEFAULT_STRING);
		in.expect("--", DEFAULT_STRING_LENGTH, "");
		
		assertRead(in, DEFAULT_STRING);
	}
	
	@Test
	public void testMultiRead() throws Exception {
		ContentFilteringInputStream in = createStreamWith("--" + DEFAULT_STRING +  "--");
		in.expect("--", DEFAULT_STRING_LENGTH, "--");
		
		assertRead(in, "Hallo Du");
		assertRead(in, "!");
	}
	
	@Test
	public void testMultipleExpect() throws Exception {
		ContentFilteringInputStream in = createStreamWith("--" + DEFAULT_STRING +  "--!" + DEFAULT_STRING + "???->");

		in.expect("--", DEFAULT_STRING_LENGTH, "--");
		assertRead(in, DEFAULT_STRING);
		
		in.expect("!", DEFAULT_STRING_LENGTH, "???");
		assertRead(in, DEFAULT_STRING);
		
		in.expect("->", 0, "");
		assertRead(in, "");
	}
	
	@Test
	public void testLargeXFixes() throws Exception {
		byte[] largePrefix = new byte[0xffff];
		byte[] largeSuffix = new byte[0xffff];
		
		largePrefix[0xf0ff] = 'c';
		largeSuffix[0xfffe] = '.';
		
		ContentFilteringInputStream in = createStreamWith(new String(largePrefix) + DEFAULT_STRING + new String(largeSuffix));
		in.expectRaw(largePrefix, DEFAULT_STRING_LENGTH, largeSuffix);
		
		assertRead(in, DEFAULT_STRING);
		
		////////////////////////
		
		String largeData = new String(new byte[0xffff]);
		
		for (int i=0; i<10; i++) {
			largeData += "\n-and more data\n" + largeData;
		}
		
		System.out.println(largeData.length() > Integer.MAX_VALUE);
		
		in = createStreamWith(new String(largePrefix) + largeData + new String(largeSuffix));
		in.expectRaw(largePrefix, largeData.length(), largeSuffix);
		
		assertRead(in, largeData);
		
	}
	
	@Test
	public void testXfixNotFound() throws Exception {
		ContentFilteringInputStream in = createStreamWith("--" + DEFAULT_STRING + "--");

		in.expect("-!", DEFAULT_STRING_LENGTH, "--");
		assertThatThrownBy(() -> in.read(new byte[100])).isInstanceOf(IllegalStateException.class);
		
		ContentFilteringInputStream in2 = createStreamWith("--" + DEFAULT_STRING + "--");
		
		in2.expect("!-", DEFAULT_STRING_LENGTH, "--");
		assertThatThrownBy(() -> in2.read(new byte[100])).isInstanceOf(IllegalStateException.class);
		
		ContentFilteringInputStream in3 = createStreamWith("--" + DEFAULT_STRING + "--");
		
		in3.expect("--", DEFAULT_STRING_LENGTH, "!-");
		assertThatThrownBy(() -> in3.read(new byte[100])).isInstanceOf(IllegalStateException.class);
		
		ContentFilteringInputStream in4 = createStreamWith("--" + DEFAULT_STRING + "--");
		
		in4.expect("--", DEFAULT_STRING_LENGTH, "-!");
		assertThatThrownBy(() -> in4.read(new byte[100])).isInstanceOf(IllegalStateException.class);
		
	}
	
	private ContentFilteringInputStream createStreamWith(String streamContent) {
		InputStream in = new ByteArrayInputStream(streamContent.getBytes(StandardCharsets.ISO_8859_1));
		return new ContentFilteringInputStream(in);
	}
	
	private void testSingleRead(String streamContent) throws IOException {
		ContentFilteringInputStream in = createStreamWith(streamContent);
		in.expect("", streamContent.length(), "");
		
		assertRead(in, streamContent);
	}
	
	private void assertRead(InputStream in, String expectedResult) throws IOException {
			byte[] resultBuffer = new byte[expectedResult.length()];
			
			in.read(resultBuffer);
			
			String result = new String(resultBuffer);
			
			assertThat(result).isEqualTo(expectedResult);
	}

}
