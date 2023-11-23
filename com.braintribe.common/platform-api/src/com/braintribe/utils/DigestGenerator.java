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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.zip.Adler32;

public class DigestGenerator {

	private static boolean forceLowerCasePrefix = false;

	public static byte[] streamDigest(final String algo, final InputStream in) throws Exception {
		final MessageDigest digest = MessageDigest.getInstance(algo);
		final byte md[] = new byte[8192];
		for (int n = 0; (n = in.read(md)) > -1;) {
			digest.update(md, 0, n);
		}
		return digest.digest();
	}

	public static String streamDigestAsString(final String algo, final InputStream in) throws Exception {
		final byte digest[] = streamDigest(algo, in);
		return getStringRepresentation(digest);
	}

	// SHA,MD5
	public static byte[] fileDigest(final String file, final String algo) throws Exception {
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			return streamDigest(algo, in);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public static byte[] stringDigest(final String text, final String algo) throws Exception {
		ByteArrayInputStream in = null;
		try {
			in = new ByteArrayInputStream(text.getBytes());
			return streamDigest(algo, in);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public static String inputStreamDigestAsString(final InputStream in, final String algo) throws Exception {
		final byte digest[] = streamDigest(algo, in);
		return getStringRepresentation(digest);
	}

	public static String fileDigestAsString(final String file, final String algo) throws Exception {
		final byte digest[] = fileDigest(file, algo);
		return getStringRepresentation(digest);
	}

	public static String stringDigestAsString(final String text, final String algo, final boolean includeAlgorithmInDigest) throws Exception {
		if (algo.startsWith("SSHA") || algo.startsWith("SMD")) {
			return stringDigestAsString(text, algo, new byte[] { 'b', 'r', 'a', 'i', 'n', 't', 'r', 'i', 'b', 'e' }, includeAlgorithmInDigest);
		} else {
			return stringDigestAsString(text, algo, null, includeAlgorithmInDigest);
		}
		// byte digest[] = stringDigest(text, algo);
		// return getStringRepresentation(digest);
	}

	public static String stringDigestAsString(final String text, final String algo) throws Exception {
		return stringDigestAsString(text, algo, false);
	}

	public static String stringDigestAsString(final String text, final String algo, final byte[] salt, final boolean includeAlgorithmInDigest)
			throws Exception {

		boolean base64 = false;
		int start = 0, end = 0;

		if (salt != null) {
			start = 1;
		}

		if (algo.contains("withBase64")) {
			end = "withBase64".length();
			base64 = true;
		}

		final String algoWithoutSalt = algo.substring(start, algo.length() - end);

		final byte digest[] = stringDigest(text, algoWithoutSalt);

		MessageDigest md;

		try {
			md = MessageDigest.getInstance(algoWithoutSalt);
		} catch (final java.security.NoSuchAlgorithmException e) {
			throw new Exception("No " + algoWithoutSalt + " implementation available!", e);
		}

		md.update(text.getBytes());

		if (salt != null) {
			md.update(salt);
		}

		final byte[] hash = combineHashAndSalt(md.digest(), salt);

		String prefix;

		if (salt == null) {
			prefix = forceLowerCasePrefix ? ("{" + algoWithoutSalt + "}").toLowerCase() : "{" + algoWithoutSalt + "}";
		} else {
			prefix = forceLowerCasePrefix ? ("{S" + algoWithoutSalt + "}").toLowerCase() : "{S" + algoWithoutSalt + "}";
		}

		final String suffix = base64 ? Base64.encodeBytes(hash) : getStringRepresentation(hash);

		if (includeAlgorithmInDigest) {
			if (salt == null) {
				return prefix + getStringRepresentation(digest);
			} else {
				return prefix + suffix;
			}
		} else {
			if (salt == null) {
				return getStringRepresentation(digest);
			} else {
				return suffix;
			}

		}
	}

	private static byte[] combineHashAndSalt(final byte[] hash, final byte[] salt) {
		if (salt == null) {
			return hash;
		}

		final byte[] hashAndSalt = new byte[hash.length + salt.length];
		System.arraycopy(hash, 0, hashAndSalt, 0, hash.length);
		System.arraycopy(salt, 0, hashAndSalt, hash.length, salt.length);

		return hashAndSalt;
	}

	public void setForceLowerCasePrefix(final boolean forceLowerCasePrefix) {
		DigestGenerator.forceLowerCasePrefix = forceLowerCasePrefix;
	}

	public static String getStringRepresentation(final byte[] digest) {
		// System.out.println( "Schl�ssell�nge " + algo + ": " + digest.length*8 + " Bits" );
		final StringBuffer res = new StringBuffer();
		for (byte element : digest) {
			final String s = Integer.toHexString(element & 0xFF);
			res.append((s.length() == 1) ? "0" + s : s);
		}
		return res.toString();
	}

	public static String encodeStringToHex(final String text) throws Exception {
		return DigestGenerator.getStringRepresentation(text.getBytes("UTF-8"));
	}

	public static String decodeHexString(final String hexString) throws Exception {
		if (hexString == null) {
			return null;
		}
		if ((hexString.length() % 2) != 0) {
			throw new Exception("The string " + hexString + " is not a hex encoded String.");
		}

		final StringBuffer sb = new StringBuffer();

		for (int i = 0; i < hexString.length(); ++i) {
			String str = "0x";
			str += hexString.charAt(i);
			str += hexString.charAt(i + 1);
			final char decodedCharacter = (char) Integer.decode(str).intValue();
			sb.append(decodedCharacter);
			i++;
		}
		return sb.toString();
	}

	public static long getAdler32(final Class<?> c) {
		if (c == null) {
			return -1;
		}
		try {

			final String res = c.getName().replace('.', '/') + ".class";
			InputStream is = ClassLoader.getSystemResourceAsStream(res);
			if (is == null) {
				is = c.getClassLoader().getResourceAsStream(res);
			}

			int d;
			final Adler32 a = new Adler32();
			while ((d = is.read()) != -1) {
				a.update(d);
			}
			return a.getValue();
		} catch (final Exception e) { /* add custom logging here */
		}
		return -1;
	}

	public static String getBuildVersion() {
		return "$Build_Version$ $Id: DigestGenerator.java 96901 2017-02-19 10:37:36Z michael.lafite $";
	}
}
