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
package com.braintribe.utils.encryption;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.braintribe.utils.StringTools;

/**
 * @author peter.gazdik
 */
public class Md5Tools {

	public static String getMd5(String string) {
		MessageDigest digest = getMessageDigest();
		digest.update(string.getBytes());

		return StringTools.toHex(digest.digest());
	}

	public static String getMd5(File file) {
		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			return getMd5(in);

		} catch (Exception e) {
			throw new RuntimeException("Error while computing MD5 for file: " + file.getAbsolutePath(), e);
		}
	}

	public static String getMd5(URL url) throws Exception {
		try (InputStream in = url.openStream()) {
			return getMd5(in);
		}
	}

	public static String getMd5(InputStream in) {
		MessageDigest digest = getMessageDigest();
		byte md[] = new byte[8192];

		try {
			for (int n = 0; (n = in.read(md)) > -1;) {
				digest.update(md, 0, n);
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Error while computing MD5", e);
		}

		return StringTools.toHex(digest.digest());
	}

	private static MessageDigest getMessageDigest() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("No MD5 Algorithm available.", e);
		}
	}

}
