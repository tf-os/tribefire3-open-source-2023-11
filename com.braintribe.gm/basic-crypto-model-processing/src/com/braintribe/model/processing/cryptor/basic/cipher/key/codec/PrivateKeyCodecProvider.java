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
package com.braintribe.model.processing.cryptor.basic.cipher.key.codec;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoaderException;

/**
 * <p>
 * Provides {@link com.braintribe.codec.Codec}(s) capable of importing/exporting {@link PrivateKey}(s) from/to byte
 * arrays.
 * 
 * @see com.braintribe.model.processing.crypto.token.KeyCodecProvider
 *
 */
public class PrivateKeyCodecProvider extends AbstractKeyCodecProvider<PrivateKey> {

	@Override
	public Codec<PrivateKey, byte[]> getKeyCodec(String algorithm) throws EncryptionTokenLoaderException {
		return getKeyCodec(PrivateKey.class, algorithm);
	}

	@Override
	protected Codec<PrivateKey, byte[]> createKeyCodec(final String algorithm) {
		return new Codec<PrivateKey, byte[]>() {

			@Override
			public byte[] encode(PrivateKey value) {
				return value.getEncoded();
			}

			@Override
			public PrivateKey decode(byte[] encodedValue) throws CodecException {
				try {
					PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encodedValue);
					KeyFactory factory = KeyFactory.getInstance(algorithm);
					return factory.generatePrivate(spec);
				} catch (Exception e) {
					throw asCodecException("Failed to decode private key", e);
				}
			}

			@Override
			public Class<PrivateKey> getValueClass() {
				return PrivateKey.class;
			}

		};
	}

}
