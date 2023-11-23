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

import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.logging.Logger;
import com.braintribe.model.crypto.key.encoded.EncodedKey;
import com.braintribe.model.crypto.key.encoded.KeyEncodingStringFormat;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.crypto.token.KeyCodecProvider;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoaderException;

/**
 * TODO: document.
 * 
 */
public abstract class EncodedKeyLoader {
	
	private static final Logger log = Logger.getLogger(EncodedKeyLoader.class);

	private Map<KeyEncodingStringFormat, Codec<byte[], String>> keyStringCodecs;

	private GmExpertRegistry expertRegistry;

	@Required
	@Configurable
	public void setKeyStringCodecs(Map<KeyEncodingStringFormat, Codec<byte[], String>> keyStringCodecs) {
		this.keyStringCodecs = keyStringCodecs;
	}

	@Required
	@Configurable
	public void setExpertRegistry(GmExpertRegistry expertRegistry) {
		this.expertRegistry = expertRegistry;
	}

	public <O extends java.security.Key> O loadKey(Class<O> keyType, EncodedKey keySpecification) throws EncryptionTokenLoaderException {

		if (keyType == null) {
			throw new IllegalArgumentException("Key type argument cannot be null");
		}

		if (keySpecification == null) {
			throw new IllegalArgumentException("Key specification argument cannot be null");
		}

		Codec<O, byte[]> keyDecoder = getKeyDecoder(keyType, keySpecification);
		byte[] keyMaterial = decodeKeyString(keySpecification);

		java.security.Key key = null;
		try {
			key = keyDecoder.decode(keyMaterial);
			if (key == null) {
				throw new EncryptionTokenLoaderException("null was returned from the underlying decoder: " + keyDecoder);
			}
		} catch (Exception e) {
			throw EncryptionTokenLoaderException.wrap("Failed to decode key material", e);
		}

		if (keyType.isInstance(key)) {

			if (log.isDebugEnabled()) {
				log.debug("Loaded a [ " + key.getClass().getName() + " ] based on a specification of type ["
						+ keySpecification.getClass().getName() + "]");
				if (log.isTraceEnabled()) {
					log.trace("Loaded [ " + key + " ] from [" + keySpecification + "]");
				}
			}

			return keyType.cast(key);

		} else {
			throw new EncryptionTokenLoaderException("Loaded key [ " + key.getClass().getName()
					+ " ] is not compatible with the expected key type: [ " + keyType.getClass().getName() + " ]");
		}

	}

	private byte[] decodeKeyString(EncodedKey keySpecification) throws EncryptionTokenLoaderException {

		String encodedKey = keySpecification.getEncodedKey();
		KeyEncodingStringFormat format = keySpecification.getEncodingStringFormat();

		if (encodedKey == null) {
			throw new EncryptionTokenLoaderException(
					"There is no encoded key set in the given specification: " + keySpecification);
		}

		if (format == null) {
			throw new EncryptionTokenLoaderException(
					"Unable to decode string representation of key as the given encoded key specification does not provide the string format");
		}

		long t = 0;
		if (log.isTraceEnabled()) {
			t = System.currentTimeMillis();
		}

		Codec<byte[], String> stringCodec = keyStringCodecs.get(format);

		if (stringCodec == null) {
			throw new EncryptionTokenLoaderException("Key loader is not configured to decode " + format + " strings");
		}

		try {
			byte[] decodedKey = stringCodec.decode(encodedKey);

			if (log.isTraceEnabled()) {
				if (decodedKey == null) {
					log.trace("No bytes resulted from decoding [" + encodedKey + "] from: " + keySpecification
							+ " using " + stringCodec);
				} else {
					t = System.currentTimeMillis() - t;
					log.trace("Decoding " + decodedKey.length + " bytes long key took " + t
							+ " ms. Decoded based on encoded value [" + encodedKey + "] from: " + keySpecification
							+ " using " + stringCodec);
				}
			}

			return decodedKey;

		} catch (Exception e) {
			throw EncryptionTokenLoaderException.wrap("Failed to decode " + format + " encoded string key from [ " + keySpecification + "]", e);
		}
	}

	private <O extends java.security.Key> Codec<O, byte[]> getKeyDecoder(Class<O> keyType, EncodedKey encodedKey) throws EncryptionTokenLoaderException {

		String keyAlgorithm = encodedKey.getKeyAlgorithm();
		KeyCodecProvider<O> keyDecoderProvider = null;
		Codec<O, byte[]> keyDecoder = null;
		
		if (keyAlgorithm == null || keyAlgorithm.isEmpty()) {
			throw new EncryptionTokenLoaderException("Key specification has no key algorithm");
		}
		
		long t = 0;
		if (log.isTraceEnabled()) {
			t = System.currentTimeMillis();
		}

		try {
			keyDecoderProvider = expertRegistry.getExpert(KeyCodecProvider.class).forInstance(encodedKey);
			if (keyDecoderProvider == null) {
				throw new EncryptionTokenLoaderException("null was returned from the expert registry for input: " + encodedKey);
			}
		} catch (Exception e) {
			throw EncryptionTokenLoaderException.wrap("Failed to obtain a decoder provider for " + keyAlgorithm + " " + keyType.getSimpleName() + "(s)", e);
		}

		try {
			keyDecoder = keyDecoderProvider.getKeyCodec(keyAlgorithm);
			if (keyDecoder == null) {
				throw new EncryptionTokenLoaderException(keyDecoderProvider + " provided null for input: " + keyAlgorithm);
			}
		} catch (Exception e) {
			throw EncryptionTokenLoaderException.wrap("Failed to obtain a decoder for " + keyAlgorithm + " " + keyType.getSimpleName() + "(s)", e);
		}

		if (keyDecoder.getValueClass().isAssignableFrom(keyType)) {

			Codec<O, byte[]> finalKeyDecoder = keyDecoder;

			if (log.isTraceEnabled()) {
				t = System.currentTimeMillis() - t;
				log.trace("Fetching " + finalKeyDecoder + " took " + t + " ms ");
			}

			return finalKeyDecoder;

		} else {
			throw new EncryptionTokenLoaderException(keyDecoder.getClass().getName() + " value type " + keyDecoder.getValueClass() + 
					" is not assignable from the expected key type " + keyType.getSimpleName());
		}

	}
}
