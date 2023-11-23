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

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.braintribe.utils.IOTools;

public class TestResourceDataTools {

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

	public static TestResourceData createResourceData(InputStream in) throws IOException {
		byte[] data = IOTools.slurpBytes(in);
		return createResourceData(data);
	}

	public static TestResourceData createResourceData() {
		int dataSize = 100 * 1024;
		byte[] data = new byte[dataSize];
		new Random().nextBytes(data);
		return createResourceData(data);
	}

	public static TestResourceData createResourceData(byte[] data) {
		TestResourceData resourceData = new TestResourceData();
		resourceData.data = data;
		resourceData.md5 = hash(data);
		resourceData.name = "test-file-" + resourceData.md5 + ".bin";
		return resourceData;
	}

}
