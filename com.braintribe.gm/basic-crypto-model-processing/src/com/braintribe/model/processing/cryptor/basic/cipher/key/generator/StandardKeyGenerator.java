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

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.crypto.key.HasKeySpecification;
import com.braintribe.model.crypto.key.KeyPair;
import com.braintribe.model.crypto.key.SecretKey;
import com.braintribe.model.processing.crypto.token.generator.EncryptionTokenGeneratorException;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoaderException;

/**
 * TODO: document.
 * 
 */
public abstract class StandardKeyGenerator {

	protected String randomAlgorithm;
	protected String randomAlgorithmProvider;

	protected Map<String, Integer> defaultKeySizes = new HashMap<>();

	public StandardKeyGenerator() {
		defaultKeySizes.put("RSA", 2048);
		defaultKeySizes.put("AES", 128);
		defaultKeySizes.put("DES", 56);
		defaultKeySizes.put("DESede", 168);
	}
	
	@Configurable
	public void setRandomNumberGeneratorAlgorithm(String randomAlgorithm) {
		this.randomAlgorithm = randomAlgorithm;

	}

	@Configurable
	public void setRandomNumberGeneratorAlgorithmProvider(String randomAlgorithmProvider) {
		this.randomAlgorithmProvider = randomAlgorithmProvider;
	}

	/**
	 * <p>
	 * Determines the default key sizes to be used when the provided
	 * {@link HasKeySpecification} instances lack of this
	 * information.
	 * 
	 * @param defaultKeySizes
	 *            Determines the default key sizes to be used when the size is not given in the
	 *            {@link HasKeySpecification} instances.
	 */
	@Configurable
	public void setDefaultKeySizes(Map<String, Integer> defaultKeySizes) {
		this.defaultKeySizes = defaultKeySizes;
	}

	protected javax.crypto.SecretKey generateSecretKey(String algorithm, String provider, SecretKey secretKeySpecification) throws EncryptionTokenGeneratorException {

		try {

			javax.crypto.KeyGenerator keyGenerator = getKeyGenerator(algorithm, provider);

			int keySize = getKeySize(algorithm, secretKeySpecification);

			keyGenerator.init(keySize, getSecureRandom());

			javax.crypto.SecretKey secretKey = keyGenerator.generateKey();

			return secretKey;

		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Failed to generate a secret key", e);
		}

	}

	protected java.security.KeyPair generateKeyPair(String algorithm, String provider, KeyPair keyPair) throws EncryptionTokenGeneratorException {

		try {

			KeyPairGenerator keyPairGenerator = getKeyPairGenerator(algorithm, provider);

			int keySize = getKeySize(algorithm, keyPair);

			keyPairGenerator.initialize(keySize, getSecureRandom());

			java.security.KeyPair pair = keyPairGenerator.generateKeyPair();

			return pair;

		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Failed to generate a key pair", e);
		}

	}

	protected int getKeySize(String algorithm, HasKeySpecification tokenWithSize) throws EncryptionTokenGeneratorException {
		
		int keySize = -1;
		
		if (tokenWithSize.getKeySize() != null) {
			keySize = tokenWithSize.getKeySize();
		}
		
		if (keySize <= 0) {

			Integer defaultKeySize = defaultKeySizes.get(algorithm);

			if (defaultKeySize == null) {
				throw new EncryptionTokenGeneratorException("Unable to retrieve the key size from the given token and no default key size is configured for " + algorithm);
			}

			keySize = defaultKeySize.intValue();

		}
		
		if (tokenWithSize.getKeySize() == null || tokenWithSize.getKeySize().intValue() != keySize) {
			tokenWithSize.setKeySize(keySize);
		}

		return keySize;

	}

	protected KeyPairGenerator getKeyPairGenerator(String algorithm, String provider) throws EncryptionTokenLoaderException {

		if (algorithm == null) {
			throw new EncryptionTokenLoaderException("Algorithm cannot be null");
		}

		KeyPairGenerator keyPairGenerator = null;

		try {
			if (provider == null) {
				keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
			} else {
				keyPairGenerator = KeyPairGenerator.getInstance(algorithm, provider);
			}
		} catch (Exception e) {
			throw EncryptionTokenLoaderException.wrap("Failed to obtain a key pair generator instance", e);
		}

		return keyPairGenerator;

	}

	protected javax.crypto.KeyGenerator getKeyGenerator(String algorithm, String provider) throws EncryptionTokenLoaderException {

		if (algorithm == null) {
			throw new EncryptionTokenLoaderException("Algorithm cannot be null");
		}

		javax.crypto.KeyGenerator keyGenerator = null;

		try {
			if (provider == null) {
				keyGenerator = javax.crypto.KeyGenerator.getInstance(algorithm);
			} else {
				keyGenerator = javax.crypto.KeyGenerator.getInstance(algorithm, provider);
			}
		} catch (Exception e) {
			throw EncryptionTokenLoaderException.wrap("Failed to obtain a key generator instance", e);
		}

		return keyGenerator;

	}

	/**
	 * <p>
	 * Returns a {@link SecureRandom} implementation.
	 * 
	 * @return A *strong* {@link SecureRandom} implementation for initializing key generators.
	 * @throws EncryptionTokenLoaderException
	 *             If unable to obtain a {@link SecureRandom} implementation
	 */
	protected SecureRandom getSecureRandom() throws EncryptionTokenLoaderException {

		SecureRandom random = null;

		try {
			if (randomAlgorithm != null && randomAlgorithmProvider != null) {
				random = SecureRandom.getInstance(randomAlgorithm, randomAlgorithmProvider);
			} else if (randomAlgorithm != null) {
				random = SecureRandom.getInstance(randomAlgorithm);
			} else {
				random = new SecureRandom();
			}
		} catch (NoSuchAlgorithmException e) {
			throw EncryptionTokenLoaderException.wrap("Failed to obtain a Random Number Generator (RNG) with the [ " + randomAlgorithm + " ] algorithm", e);
		} catch (NoSuchProviderException e) {
			throw EncryptionTokenLoaderException.wrap("Failed to obtain a Random Number Generator (RNG) with the [ " + randomAlgorithmProvider + " ] provider", e);
		} catch (Exception e) {
			throw EncryptionTokenLoaderException.wrap("Failed to obtain a Random Number Generator (RNG)", e);
		}

		return random;
	}


	
}
