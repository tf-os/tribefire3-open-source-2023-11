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
package com.braintribe.model.processing.cryptor.basic.cipher.key.generator;

import java.security.Key;
import java.util.Map;
import java.util.Objects;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.logging.Logger;
import com.braintribe.model.crypto.key.HasKeySpecification;
import com.braintribe.model.crypto.key.encoded.EncodedKey;
import com.braintribe.model.crypto.key.encoded.KeyEncodingFormat;
import com.braintribe.model.crypto.key.encoded.KeyEncodingStringFormat;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.crypto.token.KeyCodecProvider;
import com.braintribe.model.processing.crypto.token.generator.EncryptionTokenGeneratorException;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoaderException;

/**
 * <p>
 * Base for {@link StandardKeyGenerator} generating {@link Key}(s) based on {@link EncodedKey}(s).
 * 
 * <p>
 * This generator allows the given {@link EncodedKey} instances to be enriched with the generated key's material.
 * 
 */
public abstract class EncodedKeyGenerator extends StandardKeyGenerator {

	private static final Logger log = Logger.getLogger(EncodedKeyGenerator.class);

	private Map<KeyEncodingStringFormat, Codec<byte[], String>> keyStringCodecs;

	private KeyEncodingStringFormat defaultKeyEncodingStringFormat = KeyEncodingStringFormat.base64;

	protected GmExpertRegistry expertRegistry;

	@Required
	@Configurable
	public void setKeyStringCodecs(Map<KeyEncodingStringFormat, Codec<byte[], String>> keyStringCodecs) {
		this.keyStringCodecs = keyStringCodecs;
	}

	/**
	 * <p>
	 * Determines the {@link KeyEncodingStringFormat} to be used if the given {@link HasKeySpecification} instance fail
	 * to provide this information.
	 * 
	 * @param defaultKeyEncodingStringFormat
	 *            Determines the default {@link KeyEncodingStringFormat} to be used when the string format is not given
	 *            in the {@link HasKeySpecification} instances.
	 */
	@Configurable
	public void setDefaultKeyEncodingStringFormat(KeyEncodingStringFormat defaultKeyEncodingStringFormat) {
		Objects.requireNonNull(defaultKeyEncodingStringFormat, "defaultKeyEncodingStringFormat cannot be set to null");
		this.defaultKeyEncodingStringFormat = defaultKeyEncodingStringFormat;
	}

	@Required
	@Configurable
	public void setExpertRegistry(GmExpertRegistry expertRegistry) {
		this.expertRegistry = expertRegistry;
	}

	public EncodedKey export(java.security.Key key, EncodedKey keySpecification) throws EncryptionTokenGeneratorException {

		if (key == null) {
			throw new IllegalArgumentException("key argument cannot be null");
		}

		if (keySpecification == null) {
			throw new IllegalArgumentException("keySpecification argument cannot be null");
		}

		if (key.getAlgorithm() != null && !key.getAlgorithm().equals(keySpecification.getKeyAlgorithm())) {
			keySpecification.setKeyAlgorithm(key.getAlgorithm());
		}

		Codec<java.security.Key, byte[]> encoder = getKeyEncoder(key, keySpecification);

		byte[] keyMaterial = null;
		try {
			keyMaterial = encoder.encode(key);
		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Failed to decode " + key.getClass().getName() + " instance", e);
		}

		keySpecification = enrichWithKeyMaterial(keySpecification, keyMaterial);

		String keyFormat = key.getFormat();

		keySpecification = enrichWithKeyFormat(keySpecification, keyFormat);

		return keySpecification;

	}

	private EncodedKey enrichWithKeyMaterial(EncodedKey keySpecification, byte[] keyMaterial) throws EncryptionTokenGeneratorException {

		KeyEncodingStringFormat stringEncoding = keySpecification.getEncodingStringFormat();

		if (keyMaterial == null) {
			throw new EncryptionTokenGeneratorException("No key material provided");
		}

		if (stringEncoding == null) {
			stringEncoding = defaultKeyEncodingStringFormat;
		}

		long t = 0;
		if (log.isTraceEnabled()) {
			t = System.currentTimeMillis();
		}

		Codec<byte[], String> stringCodec = keyStringCodecs.get(stringEncoding);

		if (stringCodec == null) {
			throw new EncryptionTokenGeneratorException("Key loader is not configured to decode " + stringEncoding + " strings");
		}

		try {
			String encodedKey = stringCodec.encode(keyMaterial);

			if (log.isTraceEnabled()) {
				if (encodedKey == null) {
					log.trace("No String resulted from encoding the provided key material using " + stringCodec);
				} else {
					t = System.currentTimeMillis() - t;
					log.trace("Encoding " + keyMaterial.length + " bytes long key took " + t + " ms. Encoded as  [" + stringEncoding + "]  using " + stringCodec);
				}
			}

			if (!stringEncoding.equals(keySpecification.getEncodingStringFormat())) {
				keySpecification.setEncodingStringFormat(stringEncoding);
			}

			keySpecification.setEncodedKey(encodedKey);

			return keySpecification;

		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Failed to encode key material as " + stringEncoding, e);
		}
	}

	private static EncodedKey enrichWithKeyFormat(EncodedKey keySpecification, String format) {

		KeyEncodingFormat formatTranslated = null;

		if (format == null) {
			formatTranslated = KeyEncodingFormat.raw;
		} else {
			switch (format.toUpperCase()) {
			case "X.509":
			case "X 509":
			case "X509":
				formatTranslated = KeyEncodingFormat.x509;
				break;
			case "PKCS#8":
			case "PKCS 8":
			case "PKCS8":
				formatTranslated = KeyEncodingFormat.pkcs8;
				break;
			default:
				formatTranslated = KeyEncodingFormat.raw;
			}
		}

		if (!formatTranslated.equals(keySpecification.getEncodingFormat())) {
			keySpecification.setEncodingFormat(formatTranslated);
		}

		return keySpecification;

	}

	protected <O extends Key> Codec<O, byte[]> getKeyEncoder(O key, HasKeySpecification keySpecification) throws EncryptionTokenGeneratorException {

		String keyAlgo = keySpecification.getKeyAlgorithm();
		KeyCodecProvider<O> keyDecoderProvider = null;
		Codec<O, byte[]> keyDecoder = null;

		long t = 0;
		if (log.isTraceEnabled()) {
			t = System.currentTimeMillis();
		}

		try {
			keyDecoderProvider = expertRegistry.getExpert(KeyCodecProvider.class).forInstance(keySpecification);
			if (keyDecoderProvider == null) {
				throw new EncryptionTokenLoaderException("null was returned from the expert registry for input: " + keySpecification);
			}
		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Failed to obtain an encoder provider for " + keySpecification, e);
		}

		try {
			keyDecoder = keyDecoderProvider.getKeyCodec(keyAlgo);
			if (keyDecoder == null) {
				throw new EncryptionTokenLoaderException(keyDecoderProvider + " provided null for input: " + keyAlgo);
			}
		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Failed to obtain an encoder for " + keyAlgo + " " + key.getClass().getSimpleName() + "(s)", e);
		}

		if (keyDecoder.getValueClass().isAssignableFrom(key.getClass())) {

			Codec<O, byte[]> finalKeyDecoder = keyDecoder;

			if (log.isTraceEnabled()) {
				t = System.currentTimeMillis() - t;
				log.trace("Fetching " + finalKeyDecoder + " took " + t + " ms ");
			}

			return finalKeyDecoder;

		} else {
			throw new EncryptionTokenGeneratorException(keyDecoder.getClass().getName() + " value type " + keyDecoder.getValueClass() + " is not assignable from the expected key type " + key.getClass().getSimpleName());
		}

	}

}
