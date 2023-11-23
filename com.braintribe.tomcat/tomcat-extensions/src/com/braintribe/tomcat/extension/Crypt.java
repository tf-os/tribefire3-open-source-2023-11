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

import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This is the class that does the heavy-lifting. It is purely responsible to encrypting and decrypting. <br>
 * The tool uses the AES encryption algorithm. The password or the actual encryption can be overridden by setting the system property
 * <code>com.braintribe.tomcat.extension.EncryptedPropertySource.password</code>. If this is not set, a default password is used.
 */
public class Crypt {

	/* This is a nod to the tool that is the reason for this class. Our dear not-to-be-named customer used this tool as an example of how
	 * "it should be done". Pointing out that the password is readable in the class file (found it in less than 5 minutes) did not help. As pointed
	 * out in https://wiki.apache.org/tomcat/FAQ/Password, it really does not make sense to do this. But what the hell... */
	private static String password = "crushftp";

	static {
		try {
			String pw = System.getProperty("com.braintribe.tomcat.extension.EncryptedPropertySource.password");
			if (pw != null && pw.trim().length() > 0) {
				password = pw;
			}
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}

	public static String encrypt(String word, int keyLength) throws Exception {

		byte[] ivBytes;

		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[20];
		random.nextBytes(bytes);
		byte[] saltBytes = bytes;

		// Derive the key
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

		int maxAllowedKeyLength = Cipher.getMaxAllowedKeyLength("AES");
		if (maxAllowedKeyLength > 0 && maxAllowedKeyLength < keyLength) {
			throw new Exception("The requested key length " + keyLength + " is greater than allowed: " + maxAllowedKeyLength);
		}

		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65556, keyLength);

		SecretKey secretKey = factory.generateSecret(spec);
		SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

		// encrypting the word

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secret);
		AlgorithmParameters params = cipher.getParameters();
		ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();

		byte[] encryptedTextBytes = cipher.doFinal(word.getBytes("UTF-8"));

		// prepend salt and vi

		byte[] buffer = new byte[saltBytes.length + ivBytes.length + encryptedTextBytes.length];

		System.arraycopy(saltBytes, 0, buffer, 0, saltBytes.length);
		System.arraycopy(ivBytes, 0, buffer, saltBytes.length, ivBytes.length);

		System.arraycopy(encryptedTextBytes, 0, buffer, saltBytes.length + ivBytes.length, encryptedTextBytes.length);

		String encoded = new String(Base64.getEncoder().encode(buffer));
		return encoded;

	}

	public static String decrypt(String passwordEncodedAndEncrypted, int keyLength) throws Exception {

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

		ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(passwordEncodedAndEncrypted));

		byte[] saltBytes = new byte[20];
		buffer.get(saltBytes, 0, saltBytes.length);
		byte[] ivBytes1 = new byte[cipher.getBlockSize()];
		buffer.get(ivBytes1, 0, ivBytes1.length);
		byte[] encryptedTextBytes = new byte[buffer.capacity() - saltBytes.length - ivBytes1.length];

		buffer.get(encryptedTextBytes);

		// Deriving the key

		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

		int maxAllowedKeyLength = Cipher.getMaxAllowedKeyLength("AES");
		if (maxAllowedKeyLength > 0 && maxAllowedKeyLength < keyLength) {
			throw new Exception("The requested key length " + keyLength + " is greater than allowed: " + maxAllowedKeyLength);
		}
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65556, keyLength);

		SecretKey secretKey = factory.generateSecret(spec);
		SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

		cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes1));

		byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);

		String text = new String(decryptedTextBytes);

		return text;

	}
}
