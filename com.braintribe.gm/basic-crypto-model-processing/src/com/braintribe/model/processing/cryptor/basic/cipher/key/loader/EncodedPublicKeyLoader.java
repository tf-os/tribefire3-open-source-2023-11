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
package com.braintribe.model.processing.cryptor.basic.cipher.key.loader;

import java.security.PublicKey;

import com.braintribe.model.crypto.key.encoded.EncodedPublicKey;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoaderException;
import com.braintribe.model.processing.crypto.token.loader.PublicKeyLoader;

/**
 * TODO: document.
 * 
 */
public class EncodedPublicKeyLoader extends EncodedKeyLoader implements PublicKeyLoader<EncodedPublicKey, PublicKey> {

	@Override
	public PublicKey load(EncodedPublicKey encodedPublicKey) throws EncryptionTokenLoaderException {
		
		PublicKey publicKey = loadKey(PublicKey.class, encodedPublicKey);
		
		return publicKey;
		
	}

}
