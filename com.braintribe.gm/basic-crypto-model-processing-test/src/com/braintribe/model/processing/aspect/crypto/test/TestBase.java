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
package com.braintribe.model.processing.aspect.crypto.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;

import com.braintribe.codec.Codec;
import com.braintribe.crypto.BidiCryptor;
import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.Decryptor;
import com.braintribe.crypto.Encryptor;
import com.braintribe.crypto.commons.Base64Codec;
import com.braintribe.crypto.commons.HexCodec;
import com.braintribe.model.crypto.configuration.CryptoConfiguration;
import com.braintribe.model.crypto.configuration.encryption.AsymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.EncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.SymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.configuration.hashing.HashingConfiguration;
import com.braintribe.model.crypto.key.KeyPair;
import com.braintribe.model.crypto.key.PrivateKey;
import com.braintribe.model.crypto.key.PublicKey;
import com.braintribe.model.crypto.key.SecretKey;
import com.braintribe.model.crypto.key.encoded.EncodedKeyPair;
import com.braintribe.model.crypto.key.encoded.EncodedPrivateKey;
import com.braintribe.model.crypto.key.encoded.EncodedPublicKey;
import com.braintribe.model.crypto.key.encoded.EncodedSecretKey;
import com.braintribe.model.crypto.key.encoded.KeyEncodingFormat;
import com.braintribe.model.crypto.key.encoded.KeyEncodingStringFormat;
import com.braintribe.model.crypto.key.keystore.HasKeyStoreEntry;
import com.braintribe.model.crypto.key.keystore.KeyStore;
import com.braintribe.model.crypto.key.keystore.KeyStoreCertificate;
import com.braintribe.model.crypto.key.keystore.KeyStoreKeyPair;
import com.braintribe.model.crypto.key.keystore.KeyStoreSecretKey;
import com.braintribe.model.crypto.token.AsymmetricEncryptionToken;
import com.braintribe.model.meta.data.crypto.PropertyCrypting;
import com.braintribe.model.processing.aspect.crypto.test.commons.TestDataGenerator;
import com.braintribe.model.processing.aspect.crypto.test.commons.TestDataGenerator.KeyStoreTestEntry;
import com.braintribe.model.processing.aspect.crypto.test.commons.TestDataProvider;
import com.braintribe.model.processing.core.expert.api.GmExpertDefinition;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertDefinition;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.crypto.factory.CryptorFactory;
import com.braintribe.model.processing.crypto.provider.CryptorProvider;
import com.braintribe.model.processing.crypto.token.KeyCodecProvider;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoader;
import com.braintribe.model.processing.cryptor.basic.cipher.BasicCipherCryptorFactory;
import com.braintribe.model.processing.cryptor.basic.cipher.key.codec.PrivateKeyCodecProvider;
import com.braintribe.model.processing.cryptor.basic.cipher.key.codec.PublicKeyCodecProvider;
import com.braintribe.model.processing.cryptor.basic.cipher.key.codec.SecretKeyCodecProvider;
import com.braintribe.model.processing.cryptor.basic.cipher.key.generator.EncodedKeyPairGenerator;
import com.braintribe.model.processing.cryptor.basic.cipher.key.generator.EncodedSecretKeyGenerator;
import com.braintribe.model.processing.cryptor.basic.cipher.key.generator.KeyStoreCertificateGenerator;
import com.braintribe.model.processing.cryptor.basic.cipher.key.generator.KeyStoreKeyPairGenerator;
import com.braintribe.model.processing.cryptor.basic.cipher.key.generator.KeyStoreSecretKeyGenerator;
import com.braintribe.model.processing.cryptor.basic.cipher.key.generator.StandardKeyPairGenerator;
import com.braintribe.model.processing.cryptor.basic.cipher.key.generator.StandardSecretKeyGenerator;
import com.braintribe.model.processing.cryptor.basic.cipher.key.loader.EncodedKeyLoader;
import com.braintribe.model.processing.cryptor.basic.cipher.key.loader.EncodedKeyPairLoader;
import com.braintribe.model.processing.cryptor.basic.cipher.key.loader.EncodedPrivateKeyLoader;
import com.braintribe.model.processing.cryptor.basic.cipher.key.loader.EncodedPublicKeyLoader;
import com.braintribe.model.processing.cryptor.basic.cipher.key.loader.EncodedSecretKeyLoader;
import com.braintribe.model.processing.cryptor.basic.cipher.key.loader.KeyStoreCertificateLoader;
import com.braintribe.model.processing.cryptor.basic.cipher.key.loader.KeyStoreKeyPairLoader;
import com.braintribe.model.processing.cryptor.basic.cipher.key.loader.KeyStoreSecretKeyLoader;
import com.braintribe.model.processing.cryptor.basic.hash.BasicHasherFactory;
import com.braintribe.model.processing.cryptor.basic.provider.BasicCryptorProvider;
import com.braintribe.utils.IOTools;

public class TestBase {

	public static final Base64Codec base64Codec = Base64Codec.INSTANCE;
	public static final HexCodec hexCodec = new HexCodec();
	public static final Map<Cryptor.Encoding, Codec<byte[], String>> stringCodecs = new HashMap<>();

	public static BasicCipherCryptorFactory cipherCryptorFactory;
	public static BasicHasherFactory hasherFactory;

	public static StandardKeyPairGenerator standardKeyPairGenerator;
	public static StandardSecretKeyGenerator standardSecretKeyGenerator;

	public static EncodedKeyPairGenerator encodedKeyPairGenerator;
	public static EncodedSecretKeyGenerator encodedSecretKeyGenerator;

	public static KeyStoreKeyPairGenerator keyStoreKeyPairGenerator;
	public static KeyStoreCertificateGenerator keyStoreCertificateGenerator;
	public static KeyStoreSecretKeyGenerator keyStoreSecretKeyGenerator;

	private static Map<String, BidiCryptor> nonDeterministicCryptors = new HashMap<>();

	protected static Map<String, HashingConfiguration> hashingConfigurations = new HashMap<>();
	protected static Map<String, EncryptionConfiguration> encryptionConfigurations = new HashMap<>();
	protected static Map<String, EncryptionConfiguration> encryptionConfigurationsStandard = new HashMap<>();

	public static CryptorProvider<Cryptor, PropertyCrypting> cryptorProvider;

	@BeforeClass
	public static void init() throws Exception {

		// ##############################
		// # .. String Codecs ......... #
		// ##############################

		stringCodecs.put(Cryptor.Encoding.base64, base64Codec);
		stringCodecs.put(Cryptor.Encoding.hex, hexCodec);

		// ##############################
		// # .. Configurations ........ #
		// ##############################

		hashingConfigurations.put("MD5|false", createHashingConfiguration("MD5", false));
		hashingConfigurations.put("MD5|true", createHashingConfiguration("MD5", true));
		hashingConfigurations.put("SHA-1|false", createHashingConfiguration("SHA-1", false));
		hashingConfigurations.put("SHA-1|true", createHashingConfiguration("SHA-1", true));
		hashingConfigurations.put("SHA-256|false", createHashingConfiguration("SHA-256", false));
		hashingConfigurations.put("SHA-256|true", createHashingConfiguration("SHA-256", true));

		// symmetric, standard keys

		encryptionConfigurationsStandard.put("AES", createSymmetricEncryptionConfigurationStandard("AES"));
		encryptionConfigurationsStandard.put("DES", createSymmetricEncryptionConfigurationStandard("DES"));
		encryptionConfigurationsStandard.put("DESede", createSymmetricEncryptionConfigurationStandard("DESede"));

		// symmetric, encoded keys

		encryptionConfigurations.put("AES|hex",
				createSymmetricEncryptionConfigurationEncoded("AES", KeyEncodingFormat.raw, KeyEncodingStringFormat.hex));
		encryptionConfigurations.put("DES|hex",
				createSymmetricEncryptionConfigurationEncoded("DES", KeyEncodingFormat.raw, KeyEncodingStringFormat.hex));
		encryptionConfigurations.put("DESede|hex",
				createSymmetricEncryptionConfigurationEncoded("DESede", KeyEncodingFormat.raw, KeyEncodingStringFormat.hex));
		encryptionConfigurations.put("AES|base64",
				createSymmetricEncryptionConfigurationEncoded("AES", KeyEncodingFormat.raw, KeyEncodingStringFormat.base64));
		encryptionConfigurations.put("DES|base64",
				createSymmetricEncryptionConfigurationEncoded("DES", KeyEncodingFormat.raw, KeyEncodingStringFormat.base64));
		encryptionConfigurations.put("DESede|base64",
				createSymmetricEncryptionConfigurationEncoded("DESede", KeyEncodingFormat.raw, KeyEncodingStringFormat.base64));

		// symmetric, keystore keys

		encryptionConfigurations.put("AES|keystore", createSymmetricEncryptionConfigurationKeyStore("AES"));
		encryptionConfigurations.put("DES|keystore", createSymmetricEncryptionConfigurationKeyStore("DES"));
		encryptionConfigurations.put("DESede|keystore", createSymmetricEncryptionConfigurationKeyStore("DESede"));

		// asymmetric, standard key

		encryptionConfigurationsStandard.put("RSA", createAsymmetricEncryptionConfigurationStandard("RSA", true, true));

		// asymmetric, encoded keys

		encryptionConfigurations.put("RSA|hex", createAsymmetricEncryptionConfigurationEncoded("RSA", KeyEncodingStringFormat.hex, true, true));
		encryptionConfigurations.put("RSA|base64", createAsymmetricEncryptionConfigurationEncoded("RSA", KeyEncodingStringFormat.base64, true, true));

		// asymmetric, keystore keys

		encryptionConfigurations.put("RSA|keystore", createAsymmetricEncryptionConfigurationKeyStore("RSA", false));

		// ##############################
		// # .. Factories ............. #
		// ##############################

		cipherCryptorFactory = createCipherCryptorFactory();
		hasherFactory = createHasherFactory();

		// ##############################
		// # .. Key Generators ........ #
		// ##############################

		standardKeyPairGenerator = new StandardKeyPairGenerator();
		standardSecretKeyGenerator = new StandardSecretKeyGenerator();
		encodedKeyPairGenerator = createEncodedKeyPairGenerator();
		encodedSecretKeyGenerator = createEncodedSecretKeyGenerator();
		keyStoreKeyPairGenerator = createKeyStoreKeyPairGenerator();
		keyStoreCertificateGenerator = createKeyStoreCertificateGenerator();
		keyStoreSecretKeyGenerator = createKeyStoreSecretKeyGenerator();

		// ##############################
		// # .. Bidi RSA Cryptor ...... #
		// ##############################

		// RSA is deterministic by spec, but padding makes it non-deterministic
		nonDeterministicCryptors.put("RSA", (BidiCryptor) cipherCryptorFactory.getCryptor(encryptionConfigurations.get("RSA|keystore")));
		// nonDeterministicCryptors.put("RSA", (BidiCryptor)
		// cipherCryptorFactory.newCryptor(encryptionConfigurations.get("RSA|base64")));

		// ##############################
		// # .. Provider .............. #
		// ##############################

		cryptorProvider = createCryptorProvider();

	}

	public static HashingConfiguration getHashingConfiguration(String algorithm, boolean salted) {
		return hashingConfigurations.get(algorithm + "|" + salted);
	}

	public static <T extends EncryptionConfiguration> T getEncryptionConfiguration(String algorithm) {
		return getEncryptionConfiguration(algorithm, null);
	}

	@SuppressWarnings("unchecked")
	public static <T extends EncryptionConfiguration> T getEncryptionConfiguration(String algorithm, String keyRepresentation) {
		if (keyRepresentation == null) {
			return (T) encryptionConfigurationsStandard.get(algorithm);
		} else {
			return (T) encryptionConfigurations.get(algorithm + "|" + keyRepresentation);
		}
	}

	public static String getExpectedEncryptedValue(String algorithm, boolean salted, String valueFormat, String cleanValue) throws Exception {
		return (String) TestDataProvider.testValues.get(algorithm + "|" + salted + "|" + valueFormat + "|" + cleanValue);
	}

	public static byte[] getExpectedEncryptedValue(String algorithm, boolean salted, byte[] cleanValue) throws Exception {
		return (byte[]) TestDataProvider.testValues.get(algorithm + "|" + salted + "|bytes|" + new String(cleanValue, "UTF-8"));
	}

	public static BasicCipherCryptorFactory createCipherCryptorFactory() {

		Map<String, String> defaultTransformations = new HashMap<>();
		defaultTransformations.put("AES", "AES/ECB/PKCS5Padding");
		defaultTransformations.put("DES", "DES/ECB/PKCS5Padding");
		defaultTransformations.put("DESede", "DESede/ECB/PKCS5Padding");
		defaultTransformations.put("RSA", "RSA/ECB/PKCS1Padding");

		BasicCipherCryptorFactory factory = new BasicCipherCryptorFactory();

		factory.setDefaultTransformations(defaultTransformations);
		factory.setStringCodecs(stringCodecs);

		// @formatter:off
		factory.setExpertRegistry(
			createGmExpertRegistry(
				createGmExpertDefinition(EncryptionTokenLoader.class, EncodedKeyPair.class		, createEncodedKeyPairLoader()), 
				createGmExpertDefinition(EncryptionTokenLoader.class, EncodedPublicKey.class	, createEncodedPublicKeyLoader()), 
				createGmExpertDefinition(EncryptionTokenLoader.class, EncodedPrivateKey.class	, createEncodedPrivateKeyLoader()),
				createGmExpertDefinition(EncryptionTokenLoader.class, EncodedSecretKey.class	, createEncodedSecretKeyLoader()), 
				createGmExpertDefinition(EncryptionTokenLoader.class, KeyStoreKeyPair.class		, createKeyStoreKeyPairLoader()), 
				createGmExpertDefinition(EncryptionTokenLoader.class, KeyStoreCertificate.class	, createKeyStoreCertificateLoader()),
				createGmExpertDefinition(EncryptionTokenLoader.class, KeyStoreSecretKey.class	, createKeyStoreSecretKeyLoader())
			)
		);
		// @formatter:on

		factory.setValidateTransformationsUponCreation(true);

		return factory;
	}

	public static EncodedKeyLoader createEncodedKeyPairLoader() {

		EncodedKeyLoader encodedKeyLoader = new EncodedKeyPairLoader();
		encodedKeyLoader.setKeyStringCodecs(createKeyStringCodecsMap());
		encodedKeyLoader.setExpertRegistry(createGmExpertRegistry(createKeyCodecProviderGmExpertDefinitions()));

		return encodedKeyLoader;

	}

	public static EncodedKeyLoader createEncodedPublicKeyLoader() {

		EncodedKeyLoader encodedKeyLoader = new EncodedPublicKeyLoader();
		encodedKeyLoader.setKeyStringCodecs(createKeyStringCodecsMap());
		encodedKeyLoader.setExpertRegistry(createGmExpertRegistry(createKeyCodecProviderGmExpertDefinitions()));

		return encodedKeyLoader;

	}

	public static EncodedKeyLoader createEncodedPrivateKeyLoader() {

		EncodedKeyLoader encodedKeyLoader = new EncodedPrivateKeyLoader();
		encodedKeyLoader.setKeyStringCodecs(createKeyStringCodecsMap());
		encodedKeyLoader.setExpertRegistry(createGmExpertRegistry(createKeyCodecProviderGmExpertDefinitions()));

		return encodedKeyLoader;

	}

	public static EncodedKeyLoader createEncodedSecretKeyLoader() {

		EncodedKeyLoader encodedKeyLoader = new EncodedSecretKeyLoader();
		encodedKeyLoader.setKeyStringCodecs(createKeyStringCodecsMap());
		encodedKeyLoader.setExpertRegistry(createGmExpertRegistry(createKeyCodecProviderGmExpertDefinitions()));

		return encodedKeyLoader;

	}

	public static KeyStoreKeyPairLoader createKeyStoreKeyPairLoader() {
		KeyStoreKeyPairLoader loader = new KeyStoreKeyPairLoader();
		return loader;
	}

	public static KeyStoreCertificateLoader createKeyStoreCertificateLoader() {
		KeyStoreCertificateLoader loader = new KeyStoreCertificateLoader();
		return loader;
	}

	public static KeyStoreSecretKeyLoader createKeyStoreSecretKeyLoader() {
		KeyStoreSecretKeyLoader loader = new KeyStoreSecretKeyLoader();
		return loader;
	}

	public static GmExpertDefinition[] createKeyCodecProviderGmExpertDefinitions() {

		GmExpertDefinition pubDef = createGmExpertDefinition(KeyCodecProvider.class, PublicKey.class, new PublicKeyCodecProvider());
		GmExpertDefinition privDef = createGmExpertDefinition(KeyCodecProvider.class, PrivateKey.class, new PrivateKeyCodecProvider());
		GmExpertDefinition secretDef = createGmExpertDefinition(KeyCodecProvider.class, SecretKey.class, new SecretKeyCodecProvider());

		return new GmExpertDefinition[] { pubDef, privDef, secretDef };

	}

	public static EncodedKeyPairGenerator createEncodedKeyPairGenerator() {

		EncodedKeyPairGenerator gen = new EncodedKeyPairGenerator();

		gen.setKeyStringCodecs(createKeyStringCodecsMap());
		gen.setExpertRegistry(createGmExpertRegistry(createKeyCodecProviderGmExpertDefinitions()));

		return gen;

	}

	public static EncodedSecretKeyGenerator createEncodedSecretKeyGenerator() {

		EncodedSecretKeyGenerator gen = new EncodedSecretKeyGenerator();

		gen.setKeyStringCodecs(createKeyStringCodecsMap());
		gen.setExpertRegistry(createGmExpertRegistry(createKeyCodecProviderGmExpertDefinitions()));

		return gen;

	}

	public static KeyStoreKeyPairGenerator createKeyStoreKeyPairGenerator() {
		KeyStoreKeyPairGenerator gen = new KeyStoreKeyPairGenerator();
		gen.setKeyStoreKeyPairLoader(createKeyStoreKeyPairLoader());
		return gen;
	}

	public static KeyStoreCertificateGenerator createKeyStoreCertificateGenerator() {
		KeyStoreCertificateGenerator gen = new KeyStoreCertificateGenerator();
		gen.setKeyStoreCertificateLoader(createKeyStoreCertificateLoader());
		return gen;
	}

	public static KeyStoreSecretKeyGenerator createKeyStoreSecretKeyGenerator() {
		KeyStoreSecretKeyGenerator gen = new KeyStoreSecretKeyGenerator();
		return gen;
	}

	public static Map<KeyEncodingStringFormat, Codec<byte[], String>> createKeyStringCodecsMap() {
		Map<KeyEncodingStringFormat, Codec<byte[], String>> keyStringCodecs = new HashMap<>();
		keyStringCodecs.put(KeyEncodingStringFormat.hex, hexCodec);
		keyStringCodecs.put(KeyEncodingStringFormat.base64, base64Codec);
		return keyStringCodecs;
	}

	public static GmExpertRegistry createGmExpertRegistry(GmExpertDefinition... gmExpertDefinitions) {
		ConfigurableGmExpertRegistry expertRegistry = new ConfigurableGmExpertRegistry();
		expertRegistry.setExpertDefinitions(Arrays.asList(gmExpertDefinitions));
		return expertRegistry;
	}

	public static GmExpertDefinition createGmExpertDefinition(Class<?> expertType, Class<?> denotationType, Object expert) {
		ConfigurableGmExpertDefinition def = new ConfigurableGmExpertDefinition();
		def.setExpertType(expertType);
		def.setDenotationType(denotationType);
		def.setExpert(expert);
		return def;
	}

	public static BasicHasherFactory createHasherFactory() {
		BasicHasherFactory factory = new BasicHasherFactory();
		factory.setStringCodecs(stringCodecs);
		return factory;
	}

	public static CryptorProvider<Cryptor, PropertyCrypting> createCryptorProvider() {
		BasicCryptorProvider basicCryptorProvider = new BasicCryptorProvider();

		// @formatter:off
		basicCryptorProvider.setExpertRegistry(
			createGmExpertRegistry(
				createGmExpertDefinition(CryptorFactory.class, HashingConfiguration.class	, hasherFactory), 
				createGmExpertDefinition(CryptorFactory.class, EncryptionConfiguration.class, cipherCryptorFactory)
			)
		);
		// @formatter:on

		return basicCryptorProvider;
	}

	protected static HashingConfiguration createHashingConfiguration(String algorithm, boolean salt) {
		HashingConfiguration config = HashingConfiguration.T.create();
		config.setAlgorithm(algorithm);
		if (salt) {
			config.setPublicSalt("SALT-FOR-" + algorithm);
		}
		return config;
	}

	protected static SymmetricEncryptionConfiguration createSymmetricEncryptionConfigurationStandard(String algorithm) throws Exception {

		SecretKey secretKey = SecretKey.T.create();
		secretKey.setKeyAlgorithm(algorithm);

		SymmetricEncryptionConfiguration config = SymmetricEncryptionConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setSymmetricEncryptionToken(secretKey);

		return config;
	}

	protected static SymmetricEncryptionConfiguration createSymmetricEncryptionConfigurationEncoded(String algorithm, KeyEncodingFormat format,
			KeyEncodingStringFormat stringFormat) {

		String keyEncoded = TestDataProvider.symmetricKeys.get(algorithm + "|" + stringFormat);

		EncodedSecretKey secretKey = EncodedSecretKey.T.create();
		secretKey.setKeyAlgorithm(algorithm);
		secretKey.setEncodedKey(keyEncoded);
		secretKey.setEncodingFormat(format);
		secretKey.setEncodingStringFormat(stringFormat);

		SymmetricEncryptionConfiguration config = SymmetricEncryptionConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setSymmetricEncryptionToken(secretKey);

		return config;

	}

	protected static SymmetricEncryptionConfiguration createSymmetricEncryptionConfigurationKeyStore(String algorithm) throws Exception {

		KeyStoreTestEntry keyStoreEntry = TestDataGenerator.symmetricKeys.get(algorithm);

		KeyStoreSecretKey secretKey = KeyStoreSecretKey.T.create();
		secretKey.setKeyAlgorithm(algorithm);
		secretKey.setKeyEntryAlias(keyStoreEntry.alias);
		secretKey.setKeyEntryPassword(keyStoreEntry.keypass);
		secretKey.setKeyStore(createKeyStoreDefinition(keyStoreEntry));

		SymmetricEncryptionConfiguration config = SymmetricEncryptionConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setSymmetricEncryptionToken(secretKey);

		return config;

	}

	protected static SymmetricEncryptionConfiguration createSymmetricEncryptionConfigurationPrototype(String algorithm,
			KeyEncodingStringFormat stringFormat) {

		EncodedSecretKey secretKey = EncodedSecretKey.T.create();
		secretKey.setKeyAlgorithm(algorithm);
		secretKey.setEncodingStringFormat(stringFormat);

		SymmetricEncryptionConfiguration config = SymmetricEncryptionConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setSymmetricEncryptionToken(secretKey);

		return config;

	}

	protected static SymmetricEncryptionConfiguration createSymmetricEncryptionConfigurationPrototype(String algorithm, KeyStoreSecretKey spec)
			throws Exception {

		SymmetricEncryptionConfiguration config = SymmetricEncryptionConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setSymmetricEncryptionToken(spec);

		return config;

	}

	protected static AsymmetricEncryptionConfiguration createAsymmetricEncryptionConfigurationStandard(String algorithm, boolean loadPublic,
			boolean loadPrivate) {

		if (!loadPublic && !loadPrivate) {
			throw new RuntimeException("Must load at least one");
		}

		AsymmetricEncryptionToken token = null;

		if (loadPublic && loadPrivate) {
			KeyPair keyPair = KeyPair.T.create();
			keyPair.setKeyAlgorithm(algorithm);
			token = keyPair;
		} else if (loadPublic) {
			PublicKey publicKey = PublicKey.T.create();
			publicKey.setKeyAlgorithm(algorithm);
			token = publicKey;
		} else if (loadPrivate) {
			PrivateKey privateKey = PrivateKey.T.create();
			privateKey.setKeyAlgorithm(algorithm);
			token = privateKey;
		}

		AsymmetricEncryptionConfiguration config = AsymmetricEncryptionConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setAsymmetricEncryptionToken(token);

		return config;

	}

	protected static AsymmetricEncryptionConfiguration createAsymmetricEncryptionConfigurationEncoded(String algorithm,
			KeyEncodingStringFormat stringFormat, boolean loadPublic, boolean loadPrivate) {

		if (!loadPublic && !loadPrivate) {
			throw new RuntimeException("Must load at least one");
		}

		EncodedPublicKey publicKey = null;
		EncodedPrivateKey privateKey = null;

		if (loadPublic) {
			String keyEncoded = TestDataProvider.asymmetricKeys.get(algorithm + "|publ|" + stringFormat);
			publicKey = EncodedPublicKey.T.create();
			publicKey.setKeyAlgorithm(algorithm);
			publicKey.setEncodedKey(keyEncoded);
			publicKey.setEncodingFormat(KeyEncodingFormat.x509);
			publicKey.setEncodingStringFormat(stringFormat);
		}

		if (loadPrivate) {
			String keyEncoded = TestDataProvider.asymmetricKeys.get(algorithm + "|priv|" + stringFormat);
			privateKey = EncodedPrivateKey.T.create();
			privateKey.setKeyAlgorithm(algorithm);
			privateKey.setEncodedKey(keyEncoded);
			privateKey.setEncodingFormat(KeyEncodingFormat.pkcs8);
			privateKey.setEncodingStringFormat(stringFormat);
		}

		AsymmetricEncryptionToken token = null;

		if (publicKey != null && privateKey != null) {
			EncodedKeyPair encodedKeyPair = EncodedKeyPair.T.create();
			encodedKeyPair.setPublicKey(publicKey);
			encodedKeyPair.setPrivateKey(privateKey);
			token = encodedKeyPair;
		} else if (publicKey != null) {
			token = publicKey;
		} else if (privateKey != null) {
			token = privateKey;
		}

		AsymmetricEncryptionConfiguration config = AsymmetricEncryptionConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setAsymmetricEncryptionToken(token);

		return config;

	}

	protected static AsymmetricEncryptionConfiguration createAsymmetricEncryptionConfigurationKeyStore(String algorithm, boolean fromCertificate)
			throws Exception {

		KeyStoreTestEntry keyStoreEntry = TestDataGenerator.asymmetricKeys.get(algorithm);
		KeyStore keyStoreDef = createKeyStoreDefinition(keyStoreEntry);

		AsymmetricEncryptionToken token = null;

		if (fromCertificate) {
			KeyStoreCertificate certificate = KeyStoreCertificate.T.create();
			certificate.setKeyAlgorithm(algorithm);
			token = certificate;
		} else {
			KeyStoreKeyPair pair = KeyStoreKeyPair.T.create();
			pair.setKeyAlgorithm(algorithm);
			token = pair;
		}

		((HasKeyStoreEntry) token).setKeyEntryAlias(keyStoreEntry.alias);
		((HasKeyStoreEntry) token).setKeyEntryPassword(keyStoreEntry.keypass);
		((HasKeyStoreEntry) token).setKeyStore(keyStoreDef);

		AsymmetricEncryptionConfiguration config = AsymmetricEncryptionConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setAsymmetricEncryptionToken(token);

		return config;

	}

	protected static AsymmetricEncryptionConfiguration createAsymmetricEncryptionConfigurationPrototype(String algorithm,
			KeyEncodingStringFormat stringFormat, boolean loadPublic, boolean loadPrivate) {

		if (!loadPublic && !loadPrivate) {
			throw new RuntimeException("Must load at least one");
		}

		EncodedPublicKey publicKey = null;
		EncodedPrivateKey privateKey = null;

		AsymmetricEncryptionToken token = null;

		if (loadPublic) {
			publicKey = EncodedPublicKey.T.create();
			publicKey.setKeyAlgorithm(algorithm);
			publicKey.setEncodingFormat(KeyEncodingFormat.x509);
			publicKey.setEncodingStringFormat(stringFormat);
		}

		if (loadPrivate) {
			privateKey = EncodedPrivateKey.T.create();
			privateKey.setKeyAlgorithm(algorithm);
			privateKey.setEncodingFormat(KeyEncodingFormat.pkcs8);
			privateKey.setEncodingStringFormat(stringFormat);
		}

		// if (publicKey != null && privateKey != null) {
		EncodedKeyPair keyPair = EncodedKeyPair.T.create();
		keyPair.setKeyAlgorithm(algorithm);
		keyPair.setPublicKey(publicKey);
		keyPair.setPrivateKey(privateKey);
		token = keyPair;
		// } else if (publicKey != null) {
		// token = publicKey;
		// } else if (privateKey != null) {
		// token = privateKey;
		// }

		AsymmetricEncryptionConfiguration config = AsymmetricEncryptionConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setAsymmetricEncryptionToken(token);

		return config;

	}

	protected static AsymmetricEncryptionConfiguration createAsymmetricEncryptionConfigurationPrototype(String algorithm,
			KeyStoreKeyPair keyStoreKeyPair) throws Exception {

		AsymmetricEncryptionConfiguration config = AsymmetricEncryptionConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setAsymmetricEncryptionToken(keyStoreKeyPair);

		return config;

	}

	protected static AsymmetricEncryptionConfiguration createAsymmetricEncryptionConfigurationPrototype(String algorithm,
			KeyStoreCertificate keyStoreCertificate) throws Exception {

		AsymmetricEncryptionConfiguration config = AsymmetricEncryptionConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setAsymmetricEncryptionToken(keyStoreCertificate);

		return config;

	}

	protected static KeyStore createKeyStoreDefinition(KeyStoreTestEntry entry) throws Exception {
		KeyStore ks = KeyStore.T.create();

		URL keystoreFilePath = TestBase.class.getClassLoader().getResource(entry.keystore);
		if (keystoreFilePath == null) {
			throw new RuntimeException("Unable to load required path: " + entry.keystore);
		}

		String filePath = Paths.get(keystoreFilePath.toURI()).toString();

		ks.setFilePath(filePath);
		ks.setPassword(entry.storepass);
		ks.setType(entry.storetype);

		return ks;

	}

	protected static PropertyCrypting createPropertyCrypting(CryptoConfiguration config) {
		PropertyCrypting pc = PropertyCrypting.T.create();
		pc.setCryptoConfiguration(config);
		return pc;

	}

	protected static String encode(byte[] value, String stringFormat) throws Exception {
		switch (stringFormat) {
			case "hex":
				return hexCodec.encode(value);
			case "base64":
				return base64Codec.encode(value);
			default:
				throw new RuntimeException("unsupported  format " + stringFormat);
		}
	}

	/* <p> Tests Cryptors for which expected values are not available (i.e.: Cryptor based on dynamically generated key
	 * and enriched configs) */
	protected static void testCryptor(Encryptor encryptor, Decryptor decryptor) throws Exception {

		if (encryptor != null) {

			byte[] bytesFromStreaming = encryptStreaming(encryptor, TestDataProvider.inputA);
			byte[] bytesFromBytes = encryptor.encrypt(TestDataProvider.inputA).result().asBytes();
			byte[] bytesFromString = encryptor.encrypt(TestDataProvider.inputAString).result().asBytes();
			String stringFromBytes = encryptor.encrypt(TestDataProvider.inputA).result().asString();
			String stringFromString = encryptor.encrypt(TestDataProvider.inputAString).result().asString();
			String base64StringFromBytes = encryptor.encrypt(TestDataProvider.inputA).result().asString(Cryptor.Encoding.base64);
			String base64StringFromString = encryptor.encrypt(TestDataProvider.inputAString).result().asString(Cryptor.Encoding.base64);
			String hexStringFromBytes = encryptor.encrypt(TestDataProvider.inputA).result().asString(Cryptor.Encoding.hex);
			String hexStringFromString = encryptor.encrypt(TestDataProvider.inputAString).result().asString(Cryptor.Encoding.hex);

			if (decryptor != null) {

				Assert.assertArrayEquals(TestDataProvider.inputA, decryptStreaming(decryptor, bytesFromStreaming));
				Assert.assertArrayEquals(TestDataProvider.inputA, decryptor.decrypt(bytesFromBytes).result().asBytes());
				Assert.assertArrayEquals(TestDataProvider.inputA, decryptor.decrypt(bytesFromString).result().asBytes());
				Assert.assertArrayEquals(TestDataProvider.inputA, decryptor.decrypt(stringFromBytes).result().asBytes());
				Assert.assertArrayEquals(TestDataProvider.inputA, decryptor.decrypt(stringFromString).result().asBytes());
				Assert.assertArrayEquals(TestDataProvider.inputA,
						decryptor.decrypt(base64StringFromBytes).encodedAs(Cryptor.Encoding.base64).result().asBytes());
				Assert.assertArrayEquals(TestDataProvider.inputA,
						decryptor.decrypt(base64StringFromString).encodedAs(Cryptor.Encoding.base64).result().asBytes());
				Assert.assertArrayEquals(TestDataProvider.inputA,
						decryptor.decrypt(hexStringFromBytes).encodedAs(Cryptor.Encoding.hex).result().asBytes());
				Assert.assertArrayEquals(TestDataProvider.inputA,
						decryptor.decrypt(hexStringFromString).encodedAs(Cryptor.Encoding.hex).result().asBytes());

				Assert.assertEquals(TestDataProvider.inputAString, decryptor.decrypt(bytesFromBytes).result().asString());
				Assert.assertEquals(TestDataProvider.inputAString, decryptor.decrypt(bytesFromString).result().asString());
				Assert.assertEquals(TestDataProvider.inputAString, decryptor.decrypt(stringFromBytes).result().asString());
				Assert.assertEquals(TestDataProvider.inputAString, decryptor.decrypt(stringFromString).result().asString());
				Assert.assertEquals(TestDataProvider.inputAString,
						decryptor.decrypt(base64StringFromBytes).encodedAs(Cryptor.Encoding.base64).result().asString());
				Assert.assertEquals(TestDataProvider.inputAString,
						decryptor.decrypt(base64StringFromString).encodedAs(Cryptor.Encoding.base64).result().asString());
				Assert.assertEquals(TestDataProvider.inputAString,
						decryptor.decrypt(hexStringFromBytes).encodedAs(Cryptor.Encoding.hex).result().asString());
				Assert.assertEquals(TestDataProvider.inputAString,
						decryptor.decrypt(hexStringFromString).encodedAs(Cryptor.Encoding.hex).result().asString());

			}

		}

	}

	/* <p> Tests a Cryptor for which expected values are not available (i.e.: Cryptor based on dynamically generated key
	 * and enriched configs) */
	protected static void testCryptor(Cryptor cryptor) throws Exception {

		Encryptor encryptor = (cryptor instanceof Encryptor) ? (Encryptor) cryptor : null;
		Decryptor decryptor = (cryptor instanceof Decryptor) ? (Decryptor) cryptor : null;

		testCryptor(encryptor, decryptor);

	}

	/* <p> Tests Cryptors for which expected values are not available (i.e.: Cryptor based on dynamically generated key
	 * and enriched configs). Instances can differ but both must be based on the same configuration. Every Encryptor
	 * will be tested against every Decryptor, including themselves. */
	protected static void testCryptors(Cryptor... cryptors) throws Exception {
		for (Cryptor encryptor : cryptors) {
			if (encryptor instanceof Encryptor) {
				for (Cryptor decryptor : cryptors) {
					if (decryptor instanceof Decryptor) {
						testCryptor((Encryptor) encryptor, (Decryptor) decryptor);
					}
				}
			}
		}
	}

	protected static void testStringCrypting(Cryptor cryptor, String algorithm, Cryptor.Encoding stringEncoding, boolean salted, String cleanValue)
			throws Exception {

		String encryptedValue = null;
		String expectedEncryptedValue = getExpectedEncryptedValue(algorithm, salted, stringEncoding.toString(), cleanValue);

		if (cryptor instanceof Encryptor) {

			Encryptor encryptor = (Encryptor) cryptor;

			encryptedValue = encryptor.encrypt(cleanValue).result().asString(stringEncoding);

			if (encryptor.isDeterministic()) {

				Assert.assertEquals(
						"The value [" + cleanValue + "] when encrypted with [" + algorithm + "] resulted in [" + encryptedValue
								+ "], which differs from its expected [" + stringEncoding + "] formatted value: [" + expectedEncryptedValue + "]",
						expectedEncryptedValue, encryptedValue);

			}

			try {
				// clean value must match value encrypted just now
				boolean matched = encryptor.is(cleanValue).equals(encryptedValue, stringEncoding);

				Assert.assertTrue("Clean value [ " + cleanValue + " ] should have matched encrypted value [ " + encryptedValue + " ]", matched);

				// clean value must match value encrypted in previous run
				matched = encryptor.is(cleanValue).equals(expectedEncryptedValue, stringEncoding);

				Assert.assertTrue(
						"Clean value [ " + cleanValue + " ] should have matched value encrypted in previous run [ " + expectedEncryptedValue + " ]",
						matched);

			} catch (UnsupportedOperationException e) {
				// it's ok to not support matching
			}

		}

		if (cryptor instanceof Decryptor) {

			Decryptor decryptor = (Decryptor) cryptor;

			String valueToDecrypt = encryptedValue != null ? encryptedValue : expectedEncryptedValue;

			String decryptedValue = ((Decryptor) cryptor).decrypt(valueToDecrypt).encodedAs(stringEncoding).result().asString();

			Assert.assertEquals(
					"The value [" + valueToDecrypt + "] when decrypted with [" + algorithm + "] resulted in [" + decryptedValue
							+ "], which differs from its expected [" + stringEncoding + "] formatted value: [" + cleanValue + "]",
					cleanValue, decryptedValue);

			try {
				// clean value must match value encrypted just now
				boolean matched = decryptor.is(cleanValue).equals(valueToDecrypt, stringEncoding);

				Assert.assertTrue("Clean value [ " + cleanValue + " ] should have matched encrypted value [ " + encryptedValue + " ]", matched);

				// clean value must match value encrypted in previous run
				matched = decryptor.is(cleanValue).equals(expectedEncryptedValue, stringEncoding);

				Assert.assertTrue(
						"Clean value [ " + cleanValue + " ] should have matched value encrypted in previous run [ " + expectedEncryptedValue + " ]",
						matched);

			} catch (UnsupportedOperationException e) {
				// it's ok to not support matching
			}

		}

	}

	protected static void testBytesCrypting(Cryptor cryptor, String algorithm, boolean salted, byte[] cleanValue, boolean streamed) throws Exception {

		byte[] encryptedValue = null;
		byte[] expectedEncryptedValue = getExpectedEncryptedValue(algorithm, salted, cleanValue);

		if (cryptor instanceof Encryptor) {

			Encryptor encryptor = (Encryptor) cryptor;

			if (streamed) {
				encryptedValue = encryptStreaming(encryptor, cleanValue);
			} else {
				encryptedValue = ((Encryptor) cryptor).encrypt(cleanValue).result().asBytes();
			}

			if (encryptor.isDeterministic()) {

				Assert.assertArrayEquals(
						"The value [" + cleanValue + "] when encrypted with [" + algorithm + "] resulted in [" + encryptedValue
								+ "], which differs from its expected value: [" + expectedEncryptedValue + "]",
						expectedEncryptedValue, encryptedValue);

			}

			try {
				// clean value must match value encrypted just now
				boolean matched = encryptor.is(cleanValue).equals(encryptedValue);

				Assert.assertTrue("Clean value [ " + cleanValue + " ] should have matched encrypted value [ " + encryptedValue + " ]", matched);

				// clean value must match value encrypted in previous run
				matched = encryptor.is(cleanValue).equals(expectedEncryptedValue);

				Assert.assertTrue(
						"Clean value [ " + cleanValue + " ] should have matched value encrypted in previous run [ " + expectedEncryptedValue + " ]",
						matched);

			} catch (UnsupportedOperationException e) {
				// it's ok to not support matching
			}

		}

		if (cryptor instanceof Decryptor) {

			Decryptor decryptor = (Decryptor) cryptor;

			byte[] valueToDecrypt = encryptedValue != null ? encryptedValue : expectedEncryptedValue;

			byte[] decryptedValue = null;

			if (streamed) {
				decryptedValue = decryptStreaming(decryptor, valueToDecrypt);
			} else {
				decryptedValue = decryptor.decrypt(valueToDecrypt).result().asBytes();
			}

			Assert.assertArrayEquals("The value [" + valueToDecrypt + "] when decrypted with [" + algorithm + "] resulted in [" + decryptedValue
					+ "], which differs from its expected value: [" + cleanValue + "]", cleanValue, decryptedValue);

			try {
				// clean value must match value encrypted just now
				boolean matched = decryptor.is(cleanValue).equals(encryptedValue);

				Assert.assertTrue("Clean value [ " + cleanValue + " ] should have matched encrypted value [ " + encryptedValue + " ]", matched);

				// clean value must match value encrypted in previous run
				matched = decryptor.is(cleanValue).equals(expectedEncryptedValue);

				Assert.assertTrue(
						"Clean value [ " + cleanValue + " ] should have matched value encrypted in previous run [ " + expectedEncryptedValue + " ]",
						matched);

			} catch (UnsupportedOperationException e) {
				// it's ok to not support matching
			}

		}

	}

	protected static void testBytesCrypting(Cryptor cryptor, String algorithm, boolean salted, byte[] cleanValue) throws Exception {
		testBytesCrypting(cryptor, algorithm, salted, cleanValue, false);
	}

	protected static void testStreamedCrypting(Cryptor cryptor, String algorithm, boolean salted, byte[] cleanValue) throws Exception {
		testBytesCrypting(cryptor, algorithm, salted, cleanValue, true);
	}

	protected static void testNullCrypting(Cryptor cryptor) throws Exception {

		byte[] nullInput = null;
		byte[] nullOutput = null;

		if (cryptor instanceof Encryptor) {
			nullOutput = ((Encryptor) cryptor).encrypt(nullInput).result().asBytes();
			Assert.assertNull("Encryption of null should have returned null", nullOutput);
		}

		if (cryptor instanceof Decryptor) {
			nullOutput = ((Decryptor) cryptor).decrypt(nullInput).result().asBytes();
			Assert.assertNull("Decryption of null should have returned null", nullOutput);
		}

	}

	protected static byte[] encryptStreaming(Encryptor encryptor, byte[] cleanValue) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = encryptor.wrap(baos);
		os.write(cleanValue);
		os.close();
		byte[] encryptedValue = baos.toByteArray();
		return encryptedValue;
	}

	protected static byte[] decryptStreaming(Decryptor decryptor, byte[] encryptedValue) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(encryptedValue);
		InputStream is = decryptor.wrap(bais);
		byte[] decryptedValue = IOTools.slurpBytes(is, true);
		return decryptedValue;
	}

}
