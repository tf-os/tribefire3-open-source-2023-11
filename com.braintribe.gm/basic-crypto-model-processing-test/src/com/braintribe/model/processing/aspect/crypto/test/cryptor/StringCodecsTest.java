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
package com.braintribe.model.processing.aspect.crypto.test.cryptor;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.codec.Codec;
import com.braintribe.crypto.commons.Base64Codec;
import com.braintribe.crypto.commons.HexCodec;
import com.braintribe.model.processing.aspect.crypto.test.TestBase;

public class StringCodecsTest {

	@Test
	public void testCustomBase64Codec() throws Exception {
		testCodec(Base64Codec.INSTANCE);
	}

	@Test
	public void testCustomHexCodec() throws Exception {
		testCodec(new HexCodec());
	}

	@Test
	public void testHexCodecWithSeparator() throws Exception {
		testCodec(new HexCodec(':'));
	}

	// for manual execution
	@org.junit.Ignore
	@Test
	public void testPerformanceReusingValues() throws Exception {
		int iters = 100000;
		byte[] fixedValue = UUID.randomUUID().toString().getBytes("UTF-8");
		compareCodecs(TestBase.base64Codec, TestBase.hexCodec, iters, fixedValue);
		compareCodecs(TestBase.hexCodec, TestBase.base64Codec, iters, fixedValue);

		iters = iters * 10;
		compareCodecs(TestBase.base64Codec, TestBase.hexCodec, iters, fixedValue);
		compareCodecs(TestBase.hexCodec, TestBase.base64Codec, iters, fixedValue);

		iters = iters * 10;
		compareCodecs(TestBase.base64Codec, TestBase.hexCodec, iters, fixedValue);
		compareCodecs(TestBase.hexCodec, TestBase.base64Codec, iters, fixedValue);

	}

	// for manual execution
	@org.junit.Ignore
	@Test
	public void testPerformanceDifferentValues() throws Exception {
		int iters = 100000;
		compareCodecs(TestBase.base64Codec, TestBase.hexCodec, iters, null);
		compareCodecs(TestBase.hexCodec, TestBase.base64Codec, iters, null);

		iters = iters * 10;
		compareCodecs(TestBase.base64Codec, TestBase.hexCodec, iters, null);
		compareCodecs(TestBase.hexCodec, TestBase.base64Codec, iters, null);

		iters = iters * 10;
		compareCodecs(TestBase.base64Codec, TestBase.hexCodec, iters, null);
		compareCodecs(TestBase.hexCodec, TestBase.base64Codec, iters, null);

	}

	public void compareCodecs(Codec<byte[], String> codec1, Codec<byte[], String> codec2, int n, byte[] value) throws Exception {

		long r1 = testCodec(codec1, n, value);
		long r2 = testCodec(codec2, n, value);

		String winner = (r1 == r2) ? "tied" : (r1 < r2) ? codec1.getClass().getSimpleName() : codec2.getClass().getSimpleName();
		System.out.println("Faster codec: {" + winner + "}");
		System.out.println("Codec 1 {" + codec1.getClass().getSimpleName() + "} took {" + r1 + "} ms to encode {" + n + "} iterations");
		System.out.println("Codec 2 {" + codec2.getClass().getSimpleName() + "} took {" + r2 + "} ms to encode {" + n + "} iterations");
		System.out.println();

	}

	private static long testCodec(Codec<byte[], String> codec, int n, byte[] value) throws Exception {

		byte[][] values = null;
		if (value == null) {
			values = generateValues(n);
		}

		long s = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			codec.encode(value != null ? value : values[i]);
		}

		return System.currentTimeMillis() - s;

	}

	private static byte[][] generateValues(int n) throws Exception {
		byte[][] r = new byte[n][];
		for (int i = 0; i < n; i++) {
			r[i] = UUID.randomUUID().toString().getBytes("UTF-8");
		}
		return r;
	}

	private static void testCodec(Codec<byte[], String> codec) throws Exception {
		
		byte[] original = UUID.randomUUID().toString().getBytes("UTF-8");

		String encoded = codec.encode(original);

		byte[] decoded = codec.decode(encoded);
		
		Assert.assertArrayEquals(original, decoded);

	}

	// ######################
	// ## .. Java 8 code .. #
	// ######################

	// @Test
	// public void compareBase64CodecsPerformance() throws Exception {
	//
	// JdkBase64Codec jdkEncoder = new JdkBase64Codec();
	//
	// int iters = 100000;
	// compareCodecs(TestBase.base64Codec, jdkEncoder, iters, null);
	// compareCodecs(jdkEncoder, TestBase.base64Codec, iters, null);
	//
	// iters = iters * 10;
	// compareCodecs(TestBase.base64Codec, jdkEncoder, iters, null);
	// compareCodecs(jdkEncoder, TestBase.base64Codec, iters, null);
	//
	// iters = iters * 10;
	// compareCodecs(TestBase.base64Codec, jdkEncoder, iters, null);
	// compareCodecs(jdkEncoder, TestBase.base64Codec, iters, null);
	//
	// }
	//
	// @Test
	// public void compareBase64CodecsValues() throws Exception {
	//
	// int n = 100000 * 10 * 10;
	// JdkBase64Codec jdkEncoder = new JdkBase64Codec();
	// Codec<byte[], String> customEncoder = TestBase.base64Codec;
	//
	// byte[][] values = generateValues(n);
	// for (int i = 0; i < n; i++) {
	// String jdkValue = jdkEncoder.encode(values[i]);
	// String customValue = customEncoder.encode(values[i]);
	// org.junit.Assert.assertEquals(customValue, jdkValue);
	// }
	//
	// }
	//
	// public static class JdkBase64Codec implements Codec<byte[], String> {
	//
	// java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
	// java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
	//
	// @Override
	// public String encode(byte[] value) {
	// return encoder.encodeToString(value);
	// }
	//
	// @Override
	// public byte[] decode(String encodedValue) {
	// return decoder.decode(encodedValue);
	// }
	//
	// @Override
	// public Class<byte[]> getValueClass() {
	// return byte[].class;
	// }
	//
	// }

}
