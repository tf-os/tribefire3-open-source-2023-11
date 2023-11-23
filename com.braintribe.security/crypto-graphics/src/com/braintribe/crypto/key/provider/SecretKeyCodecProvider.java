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

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;


/**
 * <p>Provides {@link com.braintribe.codec.Codec}(s) capable of importing/exporting 
 *    {@link SecretKey}(s) from/to byte arrays.
 *    
 * @see KeyCodecProvider
 *
 */
public class SecretKeyCodecProvider extends KeyCodecProvider<SecretKey> {

	@Override
	public Codec<SecretKey, byte[]> apply(String algorithm) throws RuntimeException {
		return provide(SecretKey.class, algorithm);
	}

	@Override
	protected Codec<SecretKey, byte[]> createExporter(String algorithm) throws RuntimeException {
		if (AESKeyCodec.algorithm.equals(algorithm)) {
			return new AESKeyCodec();
		} else if (DESedeKeyCodec.algorithm.equals(algorithm)) {
			return new DESedeKeyCodec();
		} else if (DESKeyCodec.algorithm.equals(algorithm)) {
			return new DESKeyCodec();
		} else {
			throw new RuntimeException("Unsupported key algorithm: [ "+algorithm+" ]");
		}
	}
	
	protected static class AESKeyCodec implements Codec<SecretKey, byte[]> {

		protected static final String algorithm = "AES";
		
		@Override
		public byte[] encode(SecretKey value) {
			return value.getEncoded();
		}

		@Override
		public SecretKey decode(byte[] encodedValue) throws CodecException {
			try {
				return new SecretKeySpec(encodedValue, algorithm);
			} catch (Exception e) {
				throw new CodecException(e);
			}
		}

		@Override
		public Class<SecretKey> getValueClass() {
			return SecretKey.class;
		}
		
	}

	protected static class DESedeKeyCodec implements Codec<SecretKey, byte[]> {
		
		protected static final String algorithm = "DESede";
		
		@Override
		public byte[] encode(SecretKey value) throws CodecException {
			try {
				SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
				DESedeKeySpec spec = (DESedeKeySpec)factory.getKeySpec(value, javax.crypto.spec.DESedeKeySpec.class);
				return spec.getKey();	
			} catch (Exception e) {
				 throw new CodecException("Failed to encode "+algorithm+" key: "+e.getMessage(), e);
			}
		}

		@Override
		public SecretKey decode(byte[] encodedValue) throws CodecException {
			try {
				DESedeKeySpec spec = new DESedeKeySpec(encodedValue);
				SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
				return factory.generateSecret(spec);
			} catch (Exception e) {
				 throw new CodecException("Failed to decode "+algorithm+" key: "+e.getMessage(), e);
			}
		}

		@Override
		public Class<SecretKey> getValueClass() {
			return SecretKey.class;
		}
		
	}
	
	protected static class DESKeyCodec implements Codec<SecretKey, byte[]> {
		
		protected static final String algorithm = "DES";
		
		@Override
		public byte[] encode(SecretKey value) throws CodecException {
			try {
				SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
				DESKeySpec spec = (DESKeySpec)factory.getKeySpec(value, javax.crypto.spec.DESKeySpec.class);
				return spec.getKey();	
			} catch (Exception e) {
				 throw new CodecException("Failed to encode "+algorithm+" key: "+e.getMessage(), e);
			}
		}

		@Override
		public SecretKey decode(byte[] encodedValue) throws CodecException {
			try {
				DESKeySpec spec = new DESKeySpec(encodedValue);
				SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
				return factory.generateSecret(spec);
			} catch (Exception e) {
				 throw new CodecException("Failed to decode "+algorithm+" key: "+e.getMessage(), e);
			}
		}

		@Override
		public Class<SecretKey> getValueClass() {
			return SecretKey.class;
		}
		
	}
	
}
