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
package com.braintribe.model.processing.cryptor.basic.hash;

import java.security.SecureRandom;
import java.util.Random;

import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.CryptorException;
import com.braintribe.crypto.hash.Hasher;
import com.braintribe.crypto.hash.Hasher.SaltProvider;
import com.braintribe.logging.Logger;
import com.braintribe.model.crypto.configuration.hashing.HashingConfiguration;
import com.braintribe.model.processing.crypto.factory.CryptorFactoryException;
import com.braintribe.model.processing.cryptor.basic.factory.AbstractCachingCryptorFactory;

/**
 * <p>
 * A {@link com.braintribe.model.processing.crypto.factory.CryptorFactory CryptorFactory} of {@link Hasher}(s).
 * 
 */
public class BasicHasherFactory extends AbstractCachingCryptorFactory<HashingConfiguration, Hasher> {

	private static final Logger log = Logger.getLogger(BasicHasherFactory.class);

	@Override
	public com.braintribe.model.processing.crypto.factory.CryptorFactory.CryptorBuilder<HashingConfiguration, Hasher> builder()
			throws CryptorFactoryException {

		return new com.braintribe.model.processing.crypto.factory.CryptorFactory.CryptorBuilder<HashingConfiguration, Hasher>() {

			private HashingConfiguration configuration;

			@Override
			public com.braintribe.model.processing.crypto.factory.CryptorFactory.CryptorBuilder<HashingConfiguration, Hasher> configuration(
					HashingConfiguration cryptoConfiguration) {
				this.configuration = cryptoConfiguration;
				return this;
			}

			@Override
			public Hasher build() throws CryptorFactoryException {
				return getCryptor(this.configuration);
			}

			@Override
			public <R extends Cryptor> R build(Class<R> requiredType) throws CryptorFactoryException {
				return getCryptor(requiredType, this.configuration);
			}

		};

	}

	@Override
	public Hasher newCryptor(HashingConfiguration configuration) throws CryptorFactoryException {

		Hasher hasher = null;
		try {
			hasher = new Hasher(configuration.getAlgorithm(), createSaltProvider(configuration), stringCodecs, stringCharset);
		} catch (Exception e) {
			throw CryptorFactoryException.wrap("Failed to create a hasher", e);
		}

		if (log.isDebugEnabled()) {
			if (log.isTraceEnabled()) {
				log.trace("Created " + hasher + " based on " + configuration);
			} else {
				log.debug("Created new " + configuration.getAlgorithm() + "-based " + hasher.getClass().getName());
			}
		}

		return hasher;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	private byte[] convertSalt(String salt) throws CryptorFactoryException {

		if (salt == null) {
			return null;
		}

		try {
			return salt.getBytes(stringCharset);
		} catch (Exception e) {
			throw CryptorFactoryException.wrap("Failed to convert salt to bytes", e);
		}

	}

	private SaltProvider createSaltProvider(HashingConfiguration configuration) throws CryptorFactoryException {
		if (configuration.getEnablePublicSalt() && configuration.getPublicSalt() != null) {
			return new PublicSaltProvider(convertSalt(configuration.getPublicSalt()));
		} else if (configuration.getEnableRandomSalt() && configuration.getRandomSaltSize() != null && configuration.getRandomSaltSize() > 0) {
			return new RandomSaltProvider(configuration.getRandomSaltSize().intValue(), configuration.getRandomSaltAlgorithm(),
					configuration.getRandomSaltAlgorithmProvider());
		} else {
			return null;
		}
	}

	protected static class PublicSaltProvider implements SaltProvider {

		byte[] salt;
		int saltSize;

		PublicSaltProvider(byte salt[]) {
			this.salt = salt;
			this.saltSize = salt.length;
		}

		@Override
		public byte[] getSalt() throws CryptorException {
			return salt;
		}

		@Override
		public int getSaltSize() {
			return saltSize;
		}

		@Override
		public boolean isRandom() {
			return false;
		}
	}

	protected static class RandomSaltProvider implements SaltProvider {

		int randomSaltSize;
		String randomSaltAlgorithm;
		String randomSaltAlgorithmProvider;

		public RandomSaltProvider(int randomSaltSize, String randomSaltAlgorithm, String randomSaltAlgorithmProvider) {
			super();
			this.randomSaltSize = randomSaltSize;
			this.randomSaltAlgorithm = randomSaltAlgorithm;
			this.randomSaltAlgorithmProvider = randomSaltAlgorithmProvider;
		}

		@Override
		public byte[] getSalt() throws CryptorException {
			try {
				final Random r;
				if (randomSaltAlgorithm != null && randomSaltAlgorithmProvider != null) {
					r = SecureRandom.getInstance(randomSaltAlgorithm, randomSaltAlgorithmProvider);
				} else if (randomSaltAlgorithm != null) {
					r = SecureRandom.getInstance(randomSaltAlgorithm);
				} else {
					r = new SecureRandom();
				}

				byte[] salt = new byte[randomSaltSize];
				r.nextBytes(salt);
				return salt;
			} catch (Exception e) {
				throw new CryptorException("Failed to generate salt", e);
			}
		}

		@Override
		public int getSaltSize() {
			return randomSaltSize;
		}

		@Override
		public boolean isRandom() {
			return true;
		}

	}

}
