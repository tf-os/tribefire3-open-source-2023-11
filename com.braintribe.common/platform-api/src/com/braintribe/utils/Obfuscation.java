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

import java.nio.charset.StandardCharsets;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

/**
 * This utility can be used to visually hide secret text like passwords, codes, etc... The obfuscation algorithm is not meant to be secure but create
 * results that protects secret text from casual viewing. <br />
 * This class contains static methods while acting as {@link Codec} at the same time.
 */
public class Obfuscation implements Codec<String, String> {

	public static final String OBFUSCATION_PREFIX = "OBF:";

	/**
	 * Returns true if the passed string starts with the default obfuscation prefix ({@link #OBFUSCATION_PREFIX}).
	 */
	public static boolean isObfuscated(String s) {
		return isObfuscated(s, OBFUSCATION_PREFIX);
	}

	/**
	 * Returns true if the passed string starts with the passed prefix. If a null string is passed this method returns false.
	 */
	public static boolean isObfuscated(String s, String obfuscationPrefix) {
		return s != null && s.startsWith(obfuscationPrefix);
	}

	/**
	 * Obfuscates passed string and adds passed the default prefix ( {@link #OBFUSCATION_PREFIX} at the beginning of the string.
	 */
	public static String obfuscate(String s) {
		return obfuscate(s, OBFUSCATION_PREFIX);
	}

	/**
	 * Obfuscates passed string and adds passed prefix at the beginning of the string. If a null prefix is passed the prefix will be ignored.
	 */
	public static String obfuscate(String s, String obfuscationPrefix) {
		StringBuilder buf = new StringBuilder();
		byte b[] = s.getBytes(StandardCharsets.UTF_8);
		if (obfuscationPrefix != null) {
			buf.append(obfuscationPrefix);
		}
		for (int i = 0; i < b.length; i++) {
			byte b1 = b[i];
			byte b2 = b[b.length - (i + 1)];
			if (b1 < 0 || b2 < 0) {
				int i0 = (255 & b1) * 256 + (255 & b2);
				String x = Integer.toString(i0, 36).toLowerCase();
				buf.append("U0000", 0, 5 - x.length());
				buf.append(x);
			} else {
				int i1 = 127 + b1 + b2;
				int i2 = (127 + b1) - b2;
				int i0 = i1 * 256 + i2;
				String x = Integer.toString(i0, 36).toLowerCase();
				buf.append("000", 0, 4 - x.length());
				buf.append(x);
			}
		}
		return buf.toString();
	}

	/**
	 * Deobfuscates the passed string. A may existing default obfuscation prefix ({@link #OBFUSCATION_PREFIX}) will be ignored.
	 */
	public static String deobfuscate(String s) {
		return deobfuscate(s, OBFUSCATION_PREFIX);
	}

	/**
	 * Deobfuscates the passed string. The passed obfuscation prefix will be ignored.
	 */
	public static String deobfuscate(String s, String obfuscationPrefix) {
		if (isObfuscated(s, obfuscationPrefix)) {
			s = s.substring(obfuscationPrefix.length());
		}
		byte b[] = new byte[s.length() / 2];
		int l = 0;
		for (int i = 0; i < s.length(); i += 4) {
			if (s.charAt(i) == 'U') {
				i++;
				String x = s.substring(i, i + 4);
				int i0 = Integer.parseInt(x, 36);
				byte bx = (byte) (i0 >> 8);
				b[l++] = bx;
			} else {
				String x = s.substring(i, i + 4);
				int i0 = Integer.parseInt(x, 36);
				int i1 = i0 / 256;
				int i2 = i0 % 256;
				byte bx = (byte) (((i1 + i2) - 254) / 2);
				b[l++] = bx;
			}
		}

		return new String(b, 0, l, StandardCharsets.UTF_8);
	}

	// **************************************
	// Methods to support the Codec interface
	// **************************************

	/**
	 * @see #obfuscate(String)
	 */
	@Override
	public String encode(String value) throws CodecException {
		return obfuscate(value);
	}

	/**
	 * @see #deobfuscate(String)
	 */
	@Override
	public String decode(String encodedValue) throws CodecException {
		return deobfuscate(encodedValue);
	}

	@Override
	public Class<String> getValueClass() {
		return String.class;
	}

	// **************************************
	// Simple Test method
	// **************************************

	public static void main(String[] args) {

		String[] plains = null;
		if (args == null || args.length == 0) {
			plains = new String[] { "operating", "access4U" };
		} else {
			plains = args;
		}

		for (String plain : plains) {

			String obfuscated = obfuscate(plain);
			System.out.println("Password: " + plain + " obfuscated to : " + obfuscated + " deobfuscated to : " + deobfuscate(obfuscated));
		}

	}

}
