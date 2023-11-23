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
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;


/**
 * <p>Provides {@link com.braintribe.codec.Codec}(s) capable of importing/exporting 
 *    {@link PublicKey}(s) from/to byte arrays.
 *    
 * @see KeyCodecProvider
 *
 */
public class PublicKeyCodecProvider extends KeyCodecProvider<PublicKey> {

	@Override
	public Codec<PublicKey, byte[]> apply(String algorithm) throws RuntimeException {
		return provide(PublicKey.class, algorithm);
	}

	@Override
	protected Codec<PublicKey, byte[]> createExporter(final String algorithm) {
		return new Codec<PublicKey, byte[]>() {

			@Override
			public byte[] encode(PublicKey value) {
				return value.getEncoded();
			}

			@Override
			public PublicKey decode(byte[] encodedValue) throws CodecException {
				try {
					X509EncodedKeySpec spec = new X509EncodedKeySpec(encodedValue);
					KeyFactory factory = KeyFactory.getInstance(algorithm);
					return factory.generatePublic(spec);
				} catch (Exception e) {
					 throw new CodecException("Failed to decode public key: "+e.getMessage(), e);
				}
			}

			@Override
			public Class<PublicKey> getValueClass() {
				return PublicKey.class;
			}
			
		};
	}

}
