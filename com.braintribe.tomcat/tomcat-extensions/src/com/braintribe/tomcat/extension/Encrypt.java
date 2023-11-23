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
package com.braintribe.tomcat.extension;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

/**
 * This class provides a main method for encrypting a password in the command line. <br>
 * The result is a string in the form <code>${AES/CBC/PKCS5Padding:keylength:....}</code>. <br>
 * Implementation note: this has been separated from the EncryptedPropertySource class so that it can be used independently of any other library.
 */
public class Encrypt {

	public static void main(String[] args) {

		if (args == null || args.length < 1) {
			System.err.println("Please provide a password that should be encrypted.");
			System.exit(1);
		}
		int keyLength = -1;
		if (args.length == 2) {
			keyLength = Integer.parseInt(args[1]);
		}
		if (keyLength <= 0) {
			keyLength = 256;
		}
		int maxAllowedKeyLength = 0;
		try {
			maxAllowedKeyLength = Cipher.getMaxAllowedKeyLength("AES");
		} catch (NoSuchAlgorithmException e1) {
			System.err.println("The required cipher AES is not available.");
			System.exit(2);
		}
		if (maxAllowedKeyLength > 0 && maxAllowedKeyLength < keyLength) {
			keyLength = maxAllowedKeyLength;
		}

		try {
			System.out.println("${" + EncryptedPropertySource.PREFIX + ":" + keyLength + ":" + Crypt.encrypt(args[0], keyLength) + "}");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
