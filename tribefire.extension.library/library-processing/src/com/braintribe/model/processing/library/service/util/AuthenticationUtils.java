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
package com.braintribe.model.processing.library.service.util;

import com.braintribe.crypto.key.SymmetricKeyGenerator;
import com.braintribe.crypto.key.codec.CryptoStringCodec;

public class AuthenticationUtils {

	private static CryptoStringCodec codec = null;

	// Used SymmetricKeyGenerator of CryptoUtils to generate the key 
	// SymmetricKeyGenerator skg = new SymmetricKeyGenerator();
	// String newKey = skg.getKeyAsString();

	private final static String KEY = "GYxMWEPBkmvWnfGFax8CxLBwpOP4m/HL";

	// Used CryptoStringCodec to create this password
	// CryptoStringCodec codec = new CryptoStringCodec();
	// codec.setCipher("DESede/ECB/PKCS5Padding");
	// codec.setEncoding("UTF-8");
	// codec.setKey(SymmetricKeyGenerator.importKeyFromString(newKey));
	// String encodedPassword = codec.encode("...originalPassword...");

	private static void initialize() throws Exception {
		if (codec != null) {
			return;
		}
		codec = new CryptoStringCodec();
		codec.setCipher("DESede/ECB/PKCS5Padding");
		codec.setEncoding("UTF-8");
		codec.setKey(SymmetricKeyGenerator.importKeyFromString(KEY));
	}

	public static String decrypt(String encryptedText) throws Exception {
		initialize();
		return codec.decode(encryptedText);
	}

}
