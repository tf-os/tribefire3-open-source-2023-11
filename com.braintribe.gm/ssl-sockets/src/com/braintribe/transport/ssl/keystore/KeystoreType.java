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
package com.braintribe.transport.ssl.keystore;

import java.io.File;

import com.braintribe.logging.Logger;

public enum KeystoreType {
	AUTO, JKS, PKCS12;
	
	static Logger logger = Logger.getLogger(KeystoreType.class);
	
	public static final KeystoreType DEFAULT_KEYSTORE_TYPE = KeystoreType.AUTO;
	public static final KeystoreType FALLBACK_KEYSTORE_TYPE = KeystoreType.JKS;
	
	public static KeystoreType determineKeyStoreType(File pKeystoreFile) throws IllegalArgumentException {
		if ((pKeystoreFile == null) || (!pKeystoreFile.exists())) {
			throw new IllegalArgumentException("The keystore file "+pKeystoreFile+" does not exist.");
		}
		String name = pKeystoreFile.getName().toLowerCase(); 
		if (name.endsWith(".jks")) {
			return KeystoreType.JKS;
		} else if ((name.endsWith(".p12")) || (name.endsWith(".pfx")) || (name.endsWith(".pkcs")) || (name.endsWith(".pkcs12"))) {
			return KeystoreType.PKCS12;
		}
		logger.debug("Cannot determine type of keystore file "+pKeystoreFile.getAbsolutePath()+" Using default Keystore type "+FALLBACK_KEYSTORE_TYPE);
		return FALLBACK_KEYSTORE_TYPE;
	}
}
