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
package com.braintribe.model.processing.cryptor.basic.cipher;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.cipher.BidiCipherCryptor;
import com.braintribe.crypto.cipher.CipherCryptor;
import com.braintribe.crypto.cipher.CipherDecryptor;
import com.braintribe.crypto.cipher.CipherEncryptor;
import com.braintribe.logging.Logger;
import com.braintribe.model.crypto.certificate.Certificate;
import com.braintribe.model.crypto.configuration.encryption.AsymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.EncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.SymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.key.HasKeySpecification;
import com.braintribe.model.crypto.key.KeyPair;
import com.braintribe.model.crypto.key.PrivateKey;
import com.braintribe.model.crypto.key.PublicKey;
import com.braintribe.model.crypto.key.SecretKey;
import com.braintribe.model.crypto.token.AsymmetricEncryptionToken;
import com.braintribe.model.crypto.token.EncryptionToken;
import com.braintribe.model.crypto.token.SymmetricEncryptionToken;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.crypto.factory.CipherCryptorFactory;
import com.braintribe.model.processing.crypto.factory.CryptorFactoryException;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoader;
import com.braintribe.model.processing.cryptor.basic.factory.AbstractCachingCryptorFactory;

/**
 * <p>
 * A {@link com.braintribe.model.processing.crypto.factory.CryptorFactory} of {@link CipherCryptor}(s).
 * 
 */
public class BasicCipherCryptorFactory extends AbstractCachingCryptorFactory<EncryptionConfiguration, CipherCryptor> implements CipherCryptorFactory<EncryptionConfiguration, CipherCryptor> {

	private static final Logger log = Logger.getLogger(BasicCipherCryptorFactory.class);

	private GmExpertRegistry expertRegistry;

	private Map<String, String> defaultTransformations = new HashMap<>();

	private boolean validateCryptorUponCreation = false;

	@Required
	@Configurable
	public void setExpertRegistry(GmExpertRegistry expertRegistry) {
		this.expertRegistry = expertRegistry;
	}

	/**
	 * <p>
	 * Determines the default transformation to be used per algorithm in case given {@link EncryptionConfiguration} does
	 * not provide the block cipher mode of operation and padding.
	 * 
	 * <p>
	 * Example:
	 * <ul>
	 * <li><b>AES:</b> AES/ECB/PKCS5Padding</li>
	 * <li><b>DES:</b> DES/ECB/PKCS5Padding</li>
	 * <li><b>DESede:</b> DESede/ECB/PKCS5Padding</li>
	 * <li><b>RSA:</b> RSA/ECB/PKCS1Padding</li>
	 * </ul>
	 * 
	 * @param defaultTransformations
	 *            the default transformation to be used per algorithm in case {@link EncryptionConfiguration}(s) lack of
	 *            mode and padding.
	 */
	@Configurable
	public void setDefaultTransformations(Map<String, String> defaultTransformations) {
		this.defaultTransformations = defaultTransformations;
	}

	/**
	 * <p>
	 * Determines whether transformations will be validated upon {@link #newCryptor(EncryptionConfiguration)} calls.
	 * 
	 * @param validateCryptorUponCreation
	 *            Whether transformations will be validated upon {@link #newCryptor(EncryptionConfiguration)} calls.
	 */
	@Configurable
	public void setValidateTransformationsUponCreation(boolean validateCryptorUponCreation) {
		this.validateCryptorUponCreation = validateCryptorUponCreation;
	}

	@Override
	public CipherCryptorFactory.CipherCryptorBuilder<EncryptionConfiguration, CipherCryptor> builder() throws CryptorFactoryException {

		return new CipherCryptorFactory.CipherCryptorBuilder<EncryptionConfiguration, CipherCryptor>() {

			private EncryptionConfiguration configuration;
			private String algorithm;
			private Key encryptingKey;
			private Key decryptingKey;
			private String mode;
			private String padding;
			private String provider;

			@Override
			public CipherCryptorFactory.CipherCryptorBuilder<EncryptionConfiguration, CipherCryptor> configuration(EncryptionConfiguration cryptoConfiguration) throws CryptorFactoryException {
				this.configuration = cryptoConfiguration;
				return this;
			}

			@Override
			public CipherCryptorFactory.CipherCryptorBuilder<EncryptionConfiguration, CipherCryptor> key(Key key) throws CryptorFactoryException {

				if (key instanceof javax.crypto.SecretKey) {
					encryptingKey = key;
					decryptingKey = key;
				} else if (key instanceof java.security.PrivateKey) {
					decryptingKey = key;
				} else if (key instanceof java.security.PublicKey) {
					encryptingKey = key;
				}
				
				evaluateAlgorithm();

				return this;
			}

			@Override
			public CipherCryptorFactory.CipherCryptorBuilder<EncryptionConfiguration, CipherCryptor> keyPair(java.security.KeyPair keyPair) throws CryptorFactoryException {

				if (keyPair != null) {
					decryptingKey = keyPair.getPrivate();
					encryptingKey = keyPair.getPublic();
					evaluateAlgorithm();
				}

				return this;
			}

			@Override
			public CipherCryptorFactory.CipherCryptorBuilder<EncryptionConfiguration, CipherCryptor> mode(String operationMode) throws CryptorFactoryException {
				this.mode = operationMode;
				return this;
			}

			@Override
			public CipherCryptorFactory.CipherCryptorBuilder<EncryptionConfiguration, CipherCryptor> padding(String blockPadding) throws CryptorFactoryException {
				this.padding = blockPadding;
				return this;
			}

			@Override
			public CipherCryptorFactory.CipherCryptorBuilder<EncryptionConfiguration, CipherCryptor> provider(String jcaProvider) throws CryptorFactoryException {
				this.provider = jcaProvider;
				return this;
			}

			@Override
			public CipherCryptor build() throws CryptorFactoryException {
				return build(CipherCryptor.class);
			}
			
			private void evaluateAlgorithm() throws CryptorFactoryException {
				
				if (encryptingKey == null && decryptingKey == null) {
					return;
				}
				
				if (encryptingKey != null) {
					algorithm = encryptingKey.getAlgorithm();
				}
				
				if (decryptingKey != null) {
					if (algorithm != null) {
						if (!algorithm.equals(decryptingKey.getAlgorithm())) {
							throw new CryptorFactoryException("Encrypting key algorithm [ "+algorithm+" ] does not match the decrypting key algorithm  [ "+decryptingKey.getAlgorithm()+" ].");
						}
					} else {
						algorithm = decryptingKey.getAlgorithm();
					}
				}
				
				if (algorithm == null) {
					throw new CryptorFactoryException("Encrypting key algorithm [ "+algorithm+" ] does not match the decrypting key algorithm  [ "+decryptingKey.getAlgorithm()+" ].");
				}
			
			}

			@Override
			public <R extends Cryptor> R build(Class<R> requiredType) throws CryptorFactoryException {
				
				if (encryptingKey == null && decryptingKey == null && configuration == null) {
					throw new CryptorFactoryException("Insufficient parameters were given to enable the creation of a Cryptor. Missing key, key pair and configuration.");
				}
				
				R result = null;
				
				if (configuration != null) {
					result = getCryptor(requiredType, configuration);
				} else {
					CipherCryptor cipherCryptor = newCryptor(algorithm, mode, padding, encryptingKey, decryptingKey, provider);
					try {
						result = requiredType.cast(cipherCryptor);
					} catch (Exception e) {
						throw CryptorFactoryException.wrap("Obtained Cryptor is not compatible with the required type", e);
					}
				}

				return result;

			}

		};

	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected CipherCryptor newCryptor(EncryptionConfiguration encryptionConfiguration) throws CryptorFactoryException {

		validateConfiguration(encryptionConfiguration);

		java.security.Key encryptingKey = null;
		java.security.Key decryptingKey = null;

		if (encryptionConfiguration instanceof SymmetricEncryptionConfiguration) {
			java.security.Key secretKey = loadSecretKey((SymmetricEncryptionConfiguration) encryptionConfiguration);
			encryptingKey = secretKey;
			decryptingKey = secretKey;
		} else if (encryptionConfiguration instanceof AsymmetricEncryptionConfiguration) {
			java.security.KeyPair keyPair = loadKeyPair((AsymmetricEncryptionConfiguration) encryptionConfiguration);
			encryptingKey = keyPair.getPublic();
			decryptingKey = keyPair.getPrivate();
		}

		if (encryptingKey == null && decryptingKey == null) {
			throw new CryptorFactoryException("Unable to obtain keys with the given " + encryptionConfiguration);
		}

		return newCryptor(encryptionConfiguration, encryptingKey, decryptingKey);

	}

	protected CipherCryptor newCryptor(EncryptionConfiguration encryptionConfiguration, java.security.Key encryptingKey, java.security.Key decryptingKey) throws CryptorFactoryException {

		String transformation = getTransformation(encryptionConfiguration);
		String provider = encryptionConfiguration.getProvider();
		
		CipherCryptor cipherCryptor = newCryptor(transformation, encryptingKey, decryptingKey, provider);

		if (log.isTraceEnabled()) {
			log.trace("Created " + cipherCryptor + " based on " + encryptionConfiguration);
		}

		return cipherCryptor;

	}
	
	protected CipherCryptor newCryptor(String algorithm, String mode, String padding, java.security.Key encryptingKey, java.security.Key decryptingKey, String provider) throws CryptorFactoryException {
		
		String transformation = getTransformation(algorithm, mode, padding);
		
		CipherCryptor cipherCryptor = newCryptor(transformation, encryptingKey, decryptingKey, provider);
		
		return cipherCryptor;

	}
	
	protected CipherCryptor newCryptor(String transformation, java.security.Key encryptingKey, java.security.Key decryptingKey, String provider) throws CryptorFactoryException {
		
		CipherEncryptor cipherEncryptor = (encryptingKey != null) ? createCipherEncryptor(encryptingKey, transformation, provider) : null;
		CipherDecryptor cipherDecryptor = (decryptingKey != null) ? createCipherDecryptor(decryptingKey, transformation, provider) : null;

		CipherCryptor finalCipherCryptor = toFinalCipherCryptor(cipherEncryptor, cipherDecryptor);

		if (log.isDebugEnabled()) {
			String msg = "Created new " + transformation + "-based " + finalCipherCryptor.getClass().getName();
			if (log.isTraceEnabled()) {
				log.trace(msg+": "+finalCipherCryptor);
			} 
			log.debug(msg);
		}

		return finalCipherCryptor;
		
	}
	
	/**
	 * <p>
	 * Forms a transformation string as defined by JCA: "algorithm/mode/padding".
	 * 
	 * <p>
	 * This method takes into account the configuration from the given {@link EncryptionConfiguration}.
	 * 
	 * <p>
	 * If mode and padding are not defined in {@link EncryptionConfiguration}, default values for the algorithm are
	 * retrieved from the map configured via {@link #setDefaultTransformations(Map)}.
	 * 
	 * @param encryptionConfiguration
	 *            {@link EncryptionConfiguration} providing the algorithm. Mode and padding from the configuration will
	 *            be prioritized while obtaining a transformation.
	 * @return The transformation string as defined by JCA: "algorithm/mode/padding"
	 */
	protected String getTransformation(EncryptionConfiguration encryptionConfiguration) {

		String transformation = null;
		String algorithm = encryptionConfiguration.getAlgorithm();
		String mode = encryptionConfiguration.getMode();
		String padding = encryptionConfiguration.getPadding();
		
		transformation = getTransformation(algorithm, mode, padding);
		
		return transformation;

	}

	/**
	 * <p>
	 * Forms a transformation string as defined by JCA: "algorithm/mode/padding".
	 * 
	 * <p>
	 * If mode and padding are not defined in {@link EncryptionConfiguration}, default values for the algorithm are
	 * retrieved from the map configured via {@link #setDefaultTransformations(Map)}.
	 * 
	 * @return The transformation string as defined by JCA: "algorithm/mode/padding"
	 */
	protected String getTransformation(String algorithm, String mode, String padding) {

		String transformation = null;

		if (mode != null && padding != null) {
			transformation = algorithm + "/" + mode + "/" + padding;
			if (log.isDebugEnabled()) {
				log.debug("Transformation formed from provided settings: [" + transformation + "]");
			}
		} else {
			transformation = defaultTransformations.get(algorithm);
			if (transformation == null) {
				transformation = algorithm;
				if (log.isDebugEnabled()) {
					log.debug("No default transformation string found for [" + algorithm + "]. Mode and padding will be set to JCA implementation defaults.");
				}
			} else if (log.isDebugEnabled()) {
				log.debug("Using default transformation for [" + algorithm + "]: [" + transformation + "]");
			}
		}

		return transformation;
	}

	protected CipherEncryptor createCipherEncryptor(java.security.Key encryptingKey, String transformation, String provider) throws CryptorFactoryException {
		try {
			CipherEncryptor cipherEncryptor = new CipherEncryptor(encryptingKey, transformation, provider, stringCodecs, stringCharset, validateCryptorUponCreation);
			return cipherEncryptor;
		} catch (Exception e) {
			throw new CryptorFactoryException("Failed to create an encryptor: " + e.getMessage(), e);
		}
	}

	protected CipherDecryptor createCipherDecryptor(java.security.Key decryptingKey, String transformation, String provider) throws CryptorFactoryException {
		try {
			CipherDecryptor cipherDecryptor = new CipherDecryptor(decryptingKey, transformation, provider, stringCodecs, stringCharset, validateCryptorUponCreation);
			return cipherDecryptor;
		} catch (Exception e) {
			throw new CryptorFactoryException("Failed to create a decryptor: " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	protected javax.crypto.SecretKey loadSecretKey(SymmetricEncryptionConfiguration encryptionConfiguration) throws CryptorFactoryException {

		SymmetricEncryptionToken symmetricEncryptionToken = encryptionConfiguration.getSymmetricEncryptionToken();

		if (symmetricEncryptionToken == null) {
			throw new CryptorFactoryException("The given " + SymmetricEncryptionConfiguration.class.getSimpleName() + " has no symmetric encryption token set");
		}

		if (!(symmetricEncryptionToken instanceof SecretKey)) {
			throw new CryptorFactoryException("Unexpected type of " + SymmetricEncryptionToken.class.getName() + ": " + symmetricEncryptionToken.getClass().getName());
		}

		SecretKey secretKeyToken = (SecretKey) symmetricEncryptionToken;

		EncryptionTokenLoader<SecretKey, javax.crypto.SecretKey> keyLoader = null;

		try {
			keyLoader = (EncryptionTokenLoader<SecretKey, javax.crypto.SecretKey>) requireKeyLoaderFor(secretKeyToken);
		} catch (Exception e) {
			throw CryptorFactoryException.wrap("Unexpected type of loader was returned for the given configuration", e);
		}

		if (secretKeyToken.getKeyAlgorithm() == null || secretKeyToken.getKeyAlgorithm().trim().isEmpty()) {
			if (log.isWarnEnabled()) {
				log.warn("The " + symmetricEncryptionToken.getClass().getSimpleName() + " instance associated with the " + SymmetricEncryptionConfiguration.class.getSimpleName() + " contains no key algorithm, using the algorithm of the wrapping configuration: [ " + encryptionConfiguration.getAlgorithm() + " ]");
			}
			secretKeyToken.setKeyAlgorithm(encryptionConfiguration.getAlgorithm());
		}

		javax.crypto.SecretKey secretKey = null;

		try {
			secretKey = keyLoader.load(secretKeyToken);
		} catch (Exception e) {
			throw CryptorFactoryException.wrap("Failed to load the secret key", e);
		}

		return secretKey;

	}

	protected java.security.KeyPair loadKeyPair(AsymmetricEncryptionConfiguration encryptionConfiguration) throws CryptorFactoryException {

		AsymmetricEncryptionToken asymmetricEncryptionToken = encryptionConfiguration.getAsymmetricEncryptionToken();

		if (asymmetricEncryptionToken == null) {
			throw new CryptorFactoryException("The given " + AsymmetricEncryptionConfiguration.class.getSimpleName() + " has no asymmetric encryption token set");
		}

		java.security.KeyPair keyPair = null;

		if (asymmetricEncryptionToken instanceof KeyPair) {

			keyPair = requireEncryptionTokenFor(java.security.KeyPair.class, (KeyPair) asymmetricEncryptionToken);

		} else if (asymmetricEncryptionToken instanceof Certificate) {

			java.security.cert.Certificate certificate = requireEncryptionTokenFor(java.security.cert.Certificate.class, (Certificate) asymmetricEncryptionToken);

			keyPair = new java.security.KeyPair(certificate.getPublicKey(), null);

		} else if (asymmetricEncryptionToken instanceof PublicKey) {

			java.security.PublicKey publicKey = requireEncryptionTokenFor(java.security.PublicKey.class, (PublicKey) asymmetricEncryptionToken);

			keyPair = new java.security.KeyPair(publicKey, null);

		} else if (asymmetricEncryptionToken instanceof PrivateKey) {

			java.security.PrivateKey privateKey = requireEncryptionTokenFor(java.security.PrivateKey.class, (PrivateKey) asymmetricEncryptionToken);

			keyPair = new java.security.KeyPair(null, privateKey);

		} else {
			throw new CryptorFactoryException("Unexpected type of " + SymmetricEncryptionToken.class.getName() + ": " + asymmetricEncryptionToken.getClass().getName());
		}

		// transfers algorithm from configuration to key spec, if necessary

		if (asymmetricEncryptionToken instanceof HasKeySpecification) {
			HasKeySpecification keySpecToken = (HasKeySpecification) asymmetricEncryptionToken;
			if (keySpecToken.getKeyAlgorithm() == null) {

				if (log.isWarnEnabled()) {
					log.warn("The " + keySpecToken.getClass().getSimpleName() + " instance associated with the " + AsymmetricEncryptionConfiguration.class.getSimpleName() + " contains no key algorithm, using the algorithm of the wrapping configuration: [ " + encryptionConfiguration.getAlgorithm() + " ]");
				}

				keySpecToken.setKeyAlgorithm(encryptionConfiguration.getAlgorithm());
			}
		}

		if (log.isTraceEnabled()) {
			log.trace("Loaded key pair: " + keyPair);
		}

		return keyPair;

	}

	@SuppressWarnings("unchecked")
	private <O, T extends EncryptionToken> O requireEncryptionTokenFor(@SuppressWarnings("unused") Class<O> encryptionTokenExpert, T encryptionToken) throws CryptorFactoryException {

		EncryptionTokenLoader<?, ?> fetchedLoader = requireKeyLoaderFor(encryptionToken);

		EncryptionTokenLoader<T, O> loader = null;
		try {
			loader = (EncryptionTokenLoader<T, O>) fetchedLoader;
		} catch (Exception e) {
			throw CryptorFactoryException.wrap("Unexpected loader returned", e);
		}

		O loadedToken = null;

		try {
			loadedToken = loader.load(encryptionToken);
		} catch (Exception e) {
			throw CryptorFactoryException.wrap("Failed to load encryption token", e);
		}

		return loadedToken;

	}

	private <T extends EncryptionToken> EncryptionTokenLoader<T, ?> requireKeyLoaderFor(T encryptionToken) throws CryptorFactoryException {

		EncryptionTokenLoader<T, ?> encryptionTokenLoader = null;

		try {
			encryptionTokenLoader = expertRegistry.getExpert(EncryptionTokenLoader.class).forInstance(encryptionToken);
		} catch (Exception e) {
			throw CryptorFactoryException.wrap("Failed to obtain a " + EncryptionTokenLoader.class.getName() + " for loading " + encryptionToken, e);
		}

		return encryptionTokenLoader;

	}

	private static CipherCryptor toFinalCipherCryptor(CipherEncryptor cipherEncryptor, CipherDecryptor cipherDecryptor) {
		if (cipherEncryptor != null && cipherDecryptor != null) {
			return new BidiCipherCryptor(cipherEncryptor, cipherDecryptor);
		} else if (cipherDecryptor == null) {
			return cipherEncryptor;
		} else if (cipherEncryptor == null) {
			return cipherDecryptor;
		} else {
			throw new IllegalStateException("Unexpected state. Neither encryptor nor decryptor were built.");
		}
	}

}
