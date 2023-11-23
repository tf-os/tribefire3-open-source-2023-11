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
package com.braintribe.crypto.key.provider;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;


/**
 * <p>Provides {@link com.braintribe.codec.Codec}(s) capable of importing/exporting 
 *    {@link PrivateKey}(s) from/to byte arrays.
 * 
 * @see KeyCodecProvider
 *
 */
public class PrivateKeyCodecProvider extends KeyCodecProvider<PrivateKey> {

	@Override
	public Codec<PrivateKey, byte[]> apply(String algorithm) throws RuntimeException {
		return provide(PrivateKey.class, algorithm);
	}

	@Override
	protected Codec<PrivateKey, byte[]> createExporter(final String algorithm) {
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
					 throw new CodecException("Failed to decode private key: "+e.getMessage(), e);
				}
			}

			@Override
			public Class<PrivateKey> getValueClass() {
				return PrivateKey.class;
			}
			
		};
	}
	
}
