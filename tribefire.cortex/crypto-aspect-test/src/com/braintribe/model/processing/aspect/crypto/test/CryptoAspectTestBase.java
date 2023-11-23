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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;

import com.braintribe.codec.Codec;
import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.commons.Base64Codec;
import com.braintribe.crypto.commons.HexCodec;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.impl.aop.AopAccess;
import com.braintribe.model.crypto.configuration.encryption.EncryptionConfiguration;
import com.braintribe.model.crypto.configuration.hashing.HashingConfiguration;
import com.braintribe.model.crypto.key.PrivateKey;
import com.braintribe.model.crypto.key.PublicKey;
import com.braintribe.model.crypto.key.SecretKey;
import com.braintribe.model.crypto.key.encoded.EncodedKeyPair;
import com.braintribe.model.crypto.key.encoded.EncodedPrivateKey;
import com.braintribe.model.crypto.key.encoded.EncodedPublicKey;
import com.braintribe.model.crypto.key.encoded.EncodedSecretKey;
import com.braintribe.model.crypto.key.encoded.KeyEncodingStringFormat;
import com.braintribe.model.crypto.key.keystore.KeyStoreCertificate;
import com.braintribe.model.crypto.key.keystore.KeyStoreKeyPair;
import com.braintribe.model.crypto.key.keystore.KeyStoreSecretKey;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.aspect.crypto.CryptoAspect;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.TestMetaModelProvider;
import com.braintribe.model.processing.core.expert.api.GmExpertDefinition;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertDefinition;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.crypto.factory.CryptorFactory;
import com.braintribe.model.processing.crypto.token.KeyCodecProvider;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoader;
import com.braintribe.model.processing.cryptor.basic.cipher.BasicCipherCryptorFactory;
import com.braintribe.model.processing.cryptor.basic.cipher.key.codec.PrivateKeyCodecProvider;
import com.braintribe.model.processing.cryptor.basic.cipher.key.codec.PublicKeyCodecProvider;
import com.braintribe.model.processing.cryptor.basic.cipher.key.codec.SecretKeyCodecProvider;
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
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.ResolutionContextBuilder;
import com.braintribe.model.processing.meta.cmd.ResolutionContextInfo;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.auth.BasicSessionAuthorization;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.processing.test.impl.session.TestModelAccessory;
import com.braintribe.provider.Holder;
import com.braintribe.utils.lcd.SetTools;

public class CryptoAspectTestBase {

	private static final GmMetaModel metaModel = TestMetaModelProvider.provideEnrichedModel();
	private static final ModelOracle modelOracle = new BasicModelOracle(metaModel);
	protected static final Base64Codec base64Codec = Base64Codec.INSTANCE;
	protected static final HexCodec hexCodec = new HexCodec();
	protected static final Map<Cryptor.Encoding, Codec<byte[], String>> stringCodecs = new HashMap<>();

	protected AopAccess access;
	protected AopAccess accessWithoutAspect;
	protected Smood smood;

	protected BasicPersistenceGmSession session;
	protected BasicPersistenceGmSession aopSession;
	protected BasicPersistenceGmSession aopSessionWithoutAspect;

	protected CryptoAspect cryptoAspect;

	protected BasicHasherFactory defaultHasherFactory;
	protected BasicCipherCryptorFactory defaultEncryptorFactory;

	protected Set<String> userRoles = Collections.emptySet();

	@Before
	public void setUp() throws Exception {
		newSession();

		access = new AopAccess();
		access.setDelegate(smood);
		access.setAspects(Arrays.asList(getCryptoAspect()));
		access.setUserSessionFactory(new TestGmSessionFactory());
		access.setSystemSessionFactory(new TestGmSessionFactory());

		accessWithoutAspect = new AopAccess();
		accessWithoutAspect.setDelegate(smood);
		accessWithoutAspect.setUserSessionFactory(new TestGmSessionFactory());
		accessWithoutAspect.setSystemSessionFactory(new TestGmSessionFactory());

		session.commit();

		aopSession = newAopSession(access);
		aopSessionWithoutAspect = newAopSession(accessWithoutAspect);

	}

	private class TestGmSessionFactory implements PersistenceGmSessionFactory {
		@Override
		public PersistenceGmSession newSession(String accessId) {
			Holder<Object> sessionProvider = new Holder<Object>(new Object());

			ResolutionContextInfo rci = new ResolutionContextBuilder(modelOracle).setSessionProvider(sessionProvider)
					.addDynamicAspectProvider(RoleAspect.class, () -> userRoles).build();
			CmdResolver cmdResolver = new CmdResolverImpl(rci);

			BasicPersistenceGmSession newSession = new BasicPersistenceGmSession();
			newSession.setIncrementalAccess(smood);
			newSession.setModelAccessory(new TestModelAccessory(cmdResolver));
			newSession.setSessionAuthorization(sessionAuthorization());
			return newSession;
		}

		private SessionAuthorization sessionAuthorization() {
			BasicSessionAuthorization bsa = new BasicSessionAuthorization();
			bsa.setUserRoles(userRoles);

			return bsa;
		}
	}

	protected void setUserRoles(String... roles) {
		userRoles = SetTools.asSet(roles);
	}

	private AccessAspect getCryptoAspect() {

		if (cryptoAspect == null) {
			cryptoAspect = new CryptoAspect();
			cryptoAspect.setCryptorProvider(getCryptoProvider());
		}

		return cryptoAspect;

	}

	private BasicCryptorProvider getCryptoProvider() {
		BasicCryptorProvider cryptorProvider = new BasicCryptorProvider();
		cryptorProvider.setExpertRegistry(getCryptoProviderExpertRegistry());
		return cryptorProvider;
	}

	private GmExpertRegistry getCryptoProviderExpertRegistry() {

		// ####################
		// ## .. Factories .. #
		// ####################

		GmExpertDefinition cipherCryptorFactoryDef = createGmExpertDefinition(CryptorFactory.class, EncryptionConfiguration.class,
				getDefaultEncryptorFactory());
		GmExpertDefinition hasherFactoryDef = createGmExpertDefinition(CryptorFactory.class, HashingConfiguration.class, getDefaultHasherFactory());

		// ###################
		// ## .. Registry .. #
		// ###################

		GmExpertRegistry cryptorsRegistry = createGmExpertRegistry(cipherCryptorFactoryDef, hasherFactoryDef);
		return cryptorsRegistry;
	}

	private BasicHasherFactory getDefaultHasherFactory() {

		if (defaultHasherFactory == null) {
			defaultHasherFactory = new BasicHasherFactory();
			defaultHasherFactory.setStringCodecs(getStringCodecs());
		}

		return defaultHasherFactory;
	}

	private BasicCipherCryptorFactory getDefaultEncryptorFactory() {

		if (defaultEncryptorFactory == null) {
			defaultEncryptorFactory = createCipherCryptorFactory();
		}

		return defaultEncryptorFactory;
	}

	private static Map<Cryptor.Encoding, Codec<byte[], String>> getStringCodecs() {
		if (stringCodecs.isEmpty()) {
			stringCodecs.put(Cryptor.Encoding.base64, base64Codec);
			stringCodecs.put(Cryptor.Encoding.hex, hexCodec);
		}
		return stringCodecs;
	}

	private static BasicCipherCryptorFactory createCipherCryptorFactory() {

		Map<String, String> defaultTransformations = new HashMap<>();
		defaultTransformations.put("AES", "AES/ECB/PKCS5Padding");
		defaultTransformations.put("DES", "DES/ECB/PKCS5Padding");
		defaultTransformations.put("DESede", "DESede/ECB/PKCS5Padding");
		defaultTransformations.put("RSA", "RSA/ECB/PKCS1Padding");

		BasicCipherCryptorFactory factory = new BasicCipherCryptorFactory();

		factory.setDefaultTransformations(defaultTransformations);
		factory.setStringCodecs(getStringCodecs());
		factory.setValidateTransformationsUponCreation(false);

		factory.setExpertRegistry(
				createGmExpertRegistry(createGmExpertDefinition(EncryptionTokenLoader.class, EncodedKeyPair.class, createEncodedKeyPairLoader()),
						createGmExpertDefinition(EncryptionTokenLoader.class, EncodedPublicKey.class, createEncodedPublicKeyLoader()),
						createGmExpertDefinition(EncryptionTokenLoader.class, EncodedPrivateKey.class, createEncodedPrivateKeyLoader()),
						createGmExpertDefinition(EncryptionTokenLoader.class, EncodedSecretKey.class, createEncodedSecretKeyLoader()),
						createGmExpertDefinition(EncryptionTokenLoader.class, KeyStoreKeyPair.class, createKeyStoreKeyPairLoader()),
						createGmExpertDefinition(EncryptionTokenLoader.class, KeyStoreCertificate.class, createKeyStoreCertificateLoader()),
						createGmExpertDefinition(EncryptionTokenLoader.class, KeyStoreSecretKey.class, createKeyStoreSecretKeyLoader())));

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

	public static Map<KeyEncodingStringFormat, Codec<byte[], String>> createKeyStringCodecsMap() {
		Map<KeyEncodingStringFormat, Codec<byte[], String>> keyStringCodecs = new HashMap<>();
		keyStringCodecs.put(KeyEncodingStringFormat.hex, hexCodec);
		keyStringCodecs.put(KeyEncodingStringFormat.base64, base64Codec);
		return keyStringCodecs;
	}

	private static GmExpertRegistry createGmExpertRegistry(GmExpertDefinition... gmExpertDefinitions) {
		ConfigurableGmExpertRegistry expertRegistry = new ConfigurableGmExpertRegistry();
		expertRegistry.setExpertDefinitions(Arrays.asList(gmExpertDefinitions));
		return expertRegistry;
	}

	private static GmExpertDefinition createGmExpertDefinition(Class<?> expertType, Class<?> denotationType, Object expert) {
		ConfigurableGmExpertDefinition def = new ConfigurableGmExpertDefinition();
		def.setExpertType(expertType);
		def.setDenotationType(denotationType);
		def.setExpert(expert);
		return def;
	}

	protected PersistenceGmSession newSession() {
		smood = new Smood(EmptyReadWriteLock.INSTANCE);
		smood.setAccessId("testedAccess");
		smood.setMetaModel(metaModel);

		session = new BasicPersistenceGmSession();
		session.setIncrementalAccess(smood);

		return session;
	}

	protected BasicPersistenceGmSession newAopSession(IncrementalAccess incrementalAccess) {
		BasicPersistenceGmSession s = new BasicPersistenceGmSession();
		s.setIncrementalAccess(incrementalAccess);
		return s;
	}

}
