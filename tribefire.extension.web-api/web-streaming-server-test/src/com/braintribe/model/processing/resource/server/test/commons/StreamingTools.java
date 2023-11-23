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
package com.braintribe.model.processing.resource.server.test.commons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;

public class StreamingTools {

	public static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public static String hash(byte[] data) {
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("MD5");
			byte[] md5hash = null;
			md.update(data, 0, data.length);
			md5hash = md.digest();
			return convertToHex(md5hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String hash(InputStream in) throws IOException {
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("MD5");
			byte[] md5hash = null;

			byte[] buffer = new byte[8192];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				md.update(buffer, 0, bytesRead);
			}

			md5hash = md.digest();
			return convertToHex(md5hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static RandomData createRandomData() {
		int dataSize = 100 * 1024;
		byte[] data = new byte[dataSize];
		new Random().nextBytes(data);

		RandomData randomData = new RandomData();
		randomData.data = data;
		randomData.md5 = hash(data);

		return randomData;
	}

	public static RandomData createShortRandomData() {

		byte[] data = ("short random data: " + UUID.randomUUID().toString()).getBytes();

		RandomData randomData = new RandomData();
		randomData.data = data;
		randomData.md5 = hash(data);

		return randomData;
	}

	public static class RandomData implements InputStreamProvider {
		public byte[] data;
		public String md5;

		@Override
		public InputStream openInputStream() throws IOException {
			return new ByteArrayInputStream(data);
		}
	}

	public static void checkResource(Resource resource) throws Exception {
		if (resource != null) {
			try (InputStream in = resource.openStream()) {
				String md5 = hash(in);

				if (!md5.equals(resource.getMd5())) {
					throw new IllegalStateException("md5 not correct");
				}
			}
		}
	}

	public static String serveCapture(CallStreamCapture capture) throws Exception {
		if (capture != null) {
			RandomData randomData = createRandomData();
			try (OutputStream out = capture.openStream(); InputStream in = randomData.openInputStream()) {
				IOTools.pump(in, out);
			}
			return randomData.md5;
		} else
			return null;
	}

	public static void checkOutput(ByteArrayOutputStream out, String md5) {
		byte[] byteArray = out.toByteArray();
		String actualMd5 = hash(byteArray);

		if (!actualMd5.equals(md5)) {
			throw new IllegalStateException("invalid md5 on captured data");
		}
	}

}
