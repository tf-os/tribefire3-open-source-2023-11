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
package com.braintribe.model.processing.rpc.test.commons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;

public class StreamingTools {

	private static final Logger log = Logger.getLogger(StreamingTools.class);

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

	public static String hashCompare(InputStream in, Resource resource) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			int totalBytes = hash(in, md);
			byte[] md5hash = md.digest();
			String md5 = convertToHex(md5hash);
			if (!md5.equals(resource.getMd5())) {
				throw new IOException(in + " provided " + totalBytes + " bytes and the calculated md5 " + md5 + " differs from expected "
						+ resource.getMd5() + " for resource " + resource.getGlobalId());
			}
			return md5;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static int hash(InputStream in, MessageDigest md) throws IOException {
		byte[] buffer = new byte[8192];
		int bytesRead = -1;
		int totalBytes = 0;
		while ((bytesRead = in.read(buffer)) != -1) {
			md.update(buffer, 0, bytesRead);
			totalBytes += bytesRead;
		}
		return totalBytes;
	}

	public static String hash(InputStream in) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			hash(in, md);
			byte[] md5hash = md.digest();
			return convertToHex(md5hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static RandomData createRandomData() {
		int dataSize = 100 * 1024;
		return createRandomData(dataSize);
	}

	public static RandomData createRandomData(int dataSize) {
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

	public static Resource createResource() throws Exception {
		RandomData randomData = createRandomData();
		Resource resource = Resource.createTransient(randomData);
		resource.setMd5(randomData.md5);
		return resource;
	}

	public static Resource createResource(int dataSize) throws Exception {
		RandomData randomData = createRandomData(dataSize);
		Resource resource = Resource.createTransient(randomData);
		resource.setMd5(randomData.md5);
		return resource;
	}

	public static void checkResource(Resource resource) throws Exception {
		if (resource != null) {
			try (InputStream in = resource.openStream()) {
				hashCompare(in, resource);
			}
		}
	}

	public static String serveCapture(CallStreamCapture capture) throws Exception {
		if (capture != null) {
			RandomData randomData = createRandomData();
			try (OutputStream out = capture.openStream(); InputStream in = randomData.openInputStream()) {
				long bytes = IOTools.pump(in, out);
				log.debug("Wrote " + bytes + " bytes (" + randomData.md5 + ") to capture " + capture);
			}
			return randomData.md5;
		} else
			return null;
	}

	public static String serveCapture(RandomData randomData, CallStreamCapture capture) throws Exception {
		if (capture != null) {
			try (OutputStream out = capture.openStream(); InputStream in = randomData.openInputStream()) {
				long bytes = IOTools.pump(in, out);
				log.debug("Wrote " + bytes + " bytes (" + randomData.md5 + ") to capture " + capture);
			}
			return randomData.md5;
		} else
			return null;
	}

	public static void checkOutput(ByteArrayOutputStream out, String md5) {
		byte[] byteArray = out.toByteArray();
		String actualMd5 = hash(byteArray);

		if (!actualMd5.equals(md5)) {
			throw new IllegalStateException(
					"invalid md5 on captured data (" + byteArray.length + " bytes). Actual: [ " + actualMd5 + " ]. Expected: [ " + md5 + " ]");
		}

		log.debug("Captured data (" + byteArray.length + " bytes) from stream " + Integer.toHexString(out.hashCode()) + " matches expected md5: "
				+ md5);

	}

	public static class RandomDataStore {

		private static Map<String, RandomData> database = new HashMap<>();

		static {
			RandomData randomData1 = createRandomData();
			RandomData randomData2 = createRandomData();
			RandomData randomData3 = createRandomData();
			database.put(randomData1.md5, randomData1);
			database.put(randomData2.md5, randomData2);
			database.put(randomData3.md5, randomData3);
		}

		public static String upload(Resource uploadedResource) throws Exception {

			byte[] uploadedData = null;

			try (InputStream in = uploadedResource.openStream()) {
				uploadedData = IOTools.slurpBytes(in);
			}

			RandomData randomData = new RandomData();
			randomData.data = uploadedData;
			randomData.md5 = hash(uploadedData);

			database.put(randomData.md5, randomData);

			return randomData.md5;

		}

		public static Resource download(String id) throws Exception {
			RandomData randomData = database.get(id);
			Resource resource = Resource.createTransient(randomData);
			resource.setMd5(randomData.md5);
			return resource;
		}

		public static void download(CallStreamCapture capture, String id) throws Exception {
			RandomData randomData = database.get(id);

			try (OutputStream out = capture.openStream(); InputStream in = randomData.openInputStream()) {
				long bytes = IOTools.pump(in, out);
				log.debug("Wrote " + bytes + " bytes (" + randomData.md5 + ") to capture " + capture);
			}
		}

		public static Set<String> getExistingIds() {
			return database.keySet();
		}

		public static String getExistingId() {
			return database.keySet().iterator().next();
		}

	}

}
