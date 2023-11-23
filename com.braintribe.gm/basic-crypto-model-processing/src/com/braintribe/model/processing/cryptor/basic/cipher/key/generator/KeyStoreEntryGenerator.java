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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.crypto.key.SecretKey;
import com.braintribe.model.crypto.key.keystore.HasKeyStoreEntry;
import com.braintribe.model.crypto.key.keystore.KeyStore;
import com.braintribe.model.crypto.key.keystore.KeyStoreCertificate;
import com.braintribe.model.crypto.key.keystore.KeyStoreKeyPair;
import com.braintribe.model.crypto.key.keystore.KeyStoreSecretKey;
import com.braintribe.model.processing.crypto.token.generator.EncryptionTokenGeneratorException;
import com.braintribe.model.processing.cryptor.basic.cipher.key.loader.KeyStoreCertificateLoader;
import com.braintribe.model.processing.cryptor.basic.cipher.key.loader.KeyStoreKeyPairLoader;
//import com.braintribe.model.processing.cryptor.basic.cipher.key.loader.KeyStoreSecretKeyLoader;
import com.braintribe.utils.IOTools;

/**
 * <p>
 * Base for {@link StandardKeyGenerator} generating {@link Key}(s) based on {@link com.braintribe.model.crypto.key.Key}
 * (s) denoted with {@link HasKeyStoreEntry}.
 * 
 * <p>
 * This generator creates the key store entries as defined by {@link HasKeyStoreEntry} specifications.
 * 
 */
public class KeyStoreEntryGenerator extends StandardKeyGenerator {

	private static final Logger log = Logger.getLogger(KeyStoreEntryGenerator.class);

	private KeyStoreKeyPairLoader keyStoreKeyPairLoader;
	private KeyStoreCertificateLoader keyStoreCertificateLoader;
	// private KeyStoreSecretKeyLoader keyStoreSecretKeyLoader;

	private String keyToolPath = System.getProperty("java.home") + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "keytool";

	private long keyToolExecTimeOut = 1;
	private TimeUnit keyToolExecTimeOutUnit = TimeUnit.MINUTES;

	private String selfSignedCertificateIssuerDistinguishedName = "cn=tribefire, ou=R&D, o=Braintribe, c=AT";
	private int selfSignedCertificateValidity = 36500;
	private String selfSignedCertificateSignatureAlgorithm = "MD5withRSA";

	@Configurable
	@Required
	public void setKeyStoreKeyPairLoader(KeyStoreKeyPairLoader keyStoreKeyPairLoader) {
		this.keyStoreKeyPairLoader = keyStoreKeyPairLoader;
	}

	@Configurable
	@Required
	public void setKeyStoreCertificateLoader(KeyStoreCertificateLoader keyStoreCertificateLoader) {
		this.keyStoreCertificateLoader = keyStoreCertificateLoader;
	}

	// @Configurable
	// @Required
	// public void setKeyStoreSecretKeyLoader(KeyStoreSecretKeyLoader keyStoreSecretKeyLoader) {
	// this.keyStoreSecretKeyLoader = keyStoreSecretKeyLoader;
	// }

	@Configurable
	public void setKeyToolPath(String keyToolPath) {
		if (keyToolPath == null || keyToolPath.trim().isEmpty()) {
			throw new IllegalArgumentException("Cannot set a null or empty keytool path");
		}
		this.keyToolPath = keyToolPath;
	}

	@Configurable
	public void setKeyToolExecTimeOut(long keyToolExecTimeOut) {
		this.keyToolExecTimeOut = keyToolExecTimeOut;
	}

	@Configurable
	public void setKeyToolExecTimeOutUnit(TimeUnit keyToolExecTimeOutUnit) {
		Objects.requireNonNull(keyToolExecTimeOutUnit, "Argument cannot be null");
		this.keyToolExecTimeOutUnit = keyToolExecTimeOutUnit;
	}

	@Configurable
	public void setSelfSignedCertificateIssuerDistinguishedName(String selfSignedCertificateIssuerDistinguishedName) {
		Objects.requireNonNull(selfSignedCertificateIssuerDistinguishedName, "selfSignedCertificateIssuerDistinguishedName cannot be set to null");
		this.selfSignedCertificateIssuerDistinguishedName = selfSignedCertificateIssuerDistinguishedName;
	}

	@Configurable
	public void setSelfSignedCertificateValidity(int selfSignedCertificateValidity) {
		this.selfSignedCertificateValidity = selfSignedCertificateValidity;
	}

	@Configurable
	public void setSelfSignedCertificateSignatureAlgorithm(String selfSignedCertificateSignatureAlgorithm) {
		Objects.requireNonNull(selfSignedCertificateSignatureAlgorithm, "selfSignedCertificateSignatureAlgorithm cannot be set to null");
		this.selfSignedCertificateSignatureAlgorithm = selfSignedCertificateSignatureAlgorithm;
	}

	protected javax.crypto.SecretKey generateSecretKey(String algorithm, String provider, KeyStoreSecretKey keyStoreSecretKey) throws EncryptionTokenGeneratorException {

		validateHasKeyStoreEntry(keyStoreSecretKey);

		try {

			javax.crypto.KeyGenerator keyGenerator = getKeyGenerator(algorithm, provider);

			int keySize = getKeySize(algorithm, keyStoreSecretKey);

			keyGenerator.init(keySize, getSecureRandom());

			javax.crypto.SecretKey secretKey = keyGenerator.generateKey();

			saveToKeyStore(secretKey, keyStoreSecretKey);

			return secretKey;

		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Failed to generate a secret key", e);
		}

	}

	protected java.security.KeyPair generateKeyPair(String algorithm, @SuppressWarnings("unused") String provider, KeyStoreKeyPair keyStoreKeyPair) throws EncryptionTokenGeneratorException {

		validateHasKeyStoreEntry(keyStoreKeyPair);

		try {

			int keySize = getKeySize(algorithm, keyStoreKeyPair);

			generateKeyPairWithKeyTool(keyStoreKeyPair.getKeyAlgorithm(), keySize, keyStoreKeyPair);

			java.security.KeyPair keyPair = keyStoreKeyPairLoader.load(keyStoreKeyPair);

			return keyPair;

		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Failed to generate a key pair", e);
		}

	}

	protected java.security.cert.Certificate generateCertificate(String algorithm, @SuppressWarnings("unused") String provider, KeyStoreCertificate keyStoreCertificate) throws EncryptionTokenGeneratorException {

		validateHasKeyStoreEntry(keyStoreCertificate);

		try {

			int keySize = getKeySize(algorithm, keyStoreCertificate);

			generateKeyPairWithKeyTool(keyStoreCertificate.getKeyAlgorithm(), keySize, keyStoreCertificate);

			java.security.cert.Certificate certificate = keyStoreCertificateLoader.load(keyStoreCertificate);

			return certificate;

		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Failed to generate a key pair", e);
		}

	}

	protected void validateHasKeyStoreEntry(HasKeyStoreEntry keyStoreEntry) throws EncryptionTokenGeneratorException {

		if (keyStoreEntry == null) {
			throw new IllegalArgumentException("keySpecification argument cannot be null");
		}

		String entryAlias = keyStoreEntry.getKeyEntryAlias();
		KeyStore keyStoreDefinition = keyStoreEntry.getKeyStore();

		if (entryAlias == null || entryAlias.isEmpty()) {
			throw new EncryptionTokenGeneratorException("The given " + EncryptionTokenGeneratorException.class.getName() + " has no entry alias set");
		}

		if (keyStoreDefinition == null) {
			throw new EncryptionTokenGeneratorException("The given " + keyStoreEntry.getClass().getSimpleName() + " has no " + KeyStore.class.getName() + " set");
		}

	}

	protected void generateKeyPairWithKeyTool(String algorithm, int keySize, HasKeyStoreEntry keyStoreKeyPair) throws EncryptionTokenGeneratorException {

		String[] args = createKeyPairGenerationArguments(algorithm, keySize, keyStoreKeyPair);

		runKeyTools(args);

	}

	private static String emptyIfNull(String string) {
		return (string == null) ? "" : string;
	}

	protected String[] createKeyPairGenerationArguments(String algorithm, int keySize, HasKeyStoreEntry keyStoreKeyPair) throws EncryptionTokenGeneratorException {

		String keyStorePath = extractKeyStoreFilePath(keyStoreKeyPair.getKeyStore()).toString();

		List<String> args = new ArrayList<>();

		args.add(keyToolPath);
		args.add("-genkeypair");

		args.add("-alias");
		args.add(emptyIfNull(keyStoreKeyPair.getKeyEntryAlias()));

		args.add("-keyalg");
		args.add(emptyIfNull(algorithm));

		args.add("-keysize");
		args.add(String.valueOf(keySize));

		args.add("-sigalg");
		args.add(emptyIfNull(selfSignedCertificateSignatureAlgorithm)); // TODO: use configurable default

		args.add("-dname");
		args.add(emptyIfNull(selfSignedCertificateIssuerDistinguishedName)); // TODO: use configurable default

		args.add("-keypass");
		args.add(emptyIfNull(keyStoreKeyPair.getKeyEntryPassword()));

		args.add("-validity");
		args.add(String.valueOf(selfSignedCertificateValidity));

		args.add("-storetype");
		args.add(emptyIfNull(keyStoreKeyPair.getKeyStore().getType()));

		args.add("-keystore");
		args.add(emptyIfNull(keyStorePath));

		args.add("-storepass");
		args.add(emptyIfNull(keyStoreKeyPair.getKeyStore().getPassword()));

		if (keyStoreKeyPair.getKeyStore().getProvider() != null) {
			args.add("-providerName");
			args.add(keyStoreKeyPair.getKeyStore().getProvider());
		}

		args.add("-v");

		return args.toArray(new String[args.size()]);

	}

	protected void runKeyTools(String[] args) throws EncryptionTokenGeneratorException {

		Process pr = null;
		InputStream outputOs = null;
		try {
			pr = new ProcessBuilder(args).redirectErrorStream(true).start();
			outputOs = pr.getInputStream();
		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Process execution failed", e);
		}

		boolean success = false;

		try {

			try {
				success = pr.waitFor(keyToolExecTimeOut, keyToolExecTimeOutUnit);
			} catch (InterruptedException e) {
				if (log.isDebugEnabled()) {
					log.debug("Process execution thread was interrupted while waiting.");
				} else if (log.isTraceEnabled()) {
					log.trace("Process execution thread was interrupted while waiting.", e);
				}
			} catch (Exception e) {
				throw EncryptionTokenGeneratorException.wrap("Unexpected error during process execution", e);
			}

			if (success) {

				if (log.isDebugEnabled()) {
					String processOutput = getProcessOutput(outputOs);
					log.debug("Process execution was successful" + (processOutput != null && !processOutput.isEmpty() ? ": " + processOutput : ""));
				}

			} else {
				String processOutput = getProcessOutput(outputOs);
				throw new EncryptionTokenGeneratorException("Process failed" + (processOutput != null && !processOutput.isEmpty() ? ": " + processOutput : ""));
			}

		} finally {
			IOTools.closeCloseable(outputOs, log);
		}

	}

	private static String getProcessOutput(InputStream outputOs) {

		if (outputOs == null) {
			return null;
		}

		try {
			BufferedReader outputReader = new BufferedReader(new InputStreamReader(outputOs));
			return IOTools.slurp(outputReader);
		} catch (Exception e) {
			if (log.isWarnEnabled()) {
				log.warn("Failed to read process output", e);
			}
		}

		return null;

	}

	private static Path extractKeyStoreFilePath(KeyStore keyStore) throws EncryptionTokenGeneratorException {

		String filePath = null;

		if (keyStore.getFilePath() != null && !keyStore.getFilePath().isEmpty()) {

			filePath = keyStore.getFilePath();

		} else if (keyStore.getSystemProperty() != null && !keyStore.getSystemProperty().isEmpty()) {

			try {
				filePath = System.getProperty(keyStore.getSystemProperty());
			} catch (Exception e) {
				throw EncryptionTokenGeneratorException.wrap("Failed to read system property with the name [" + keyStore.getSystemProperty() + "]", e);
			}

			if (filePath == null) {
				throw new EncryptionTokenGeneratorException("No system property was found with the name [" + keyStore.getSystemProperty() + "]");
			}

		}

		if (filePath == null) {
			throw new EncryptionTokenGeneratorException("Unable to reach a key store file path");
		}

		Path path = Paths.get(filePath);

		if (Files.isDirectory(path)) {
			throw new EncryptionTokenGeneratorException("The provided path points to a directory rather than a file: " + filePath);
		}

		if (!Files.isWritable(path.getParent())) {
			throw new EncryptionTokenGeneratorException("The provided path's parent directory is non-writable: " + filePath);
		}

		return path;

	}

	private static void saveToKeyStore(Key key, HasKeyStoreEntry keySpecification) throws EncryptionTokenGeneratorException {

		if (key == null) {
			throw new IllegalArgumentException("key argument cannot be null");
		}

		if (keySpecification == null) {
			throw new IllegalArgumentException("key store entry argument cannot be null");
		}

		if (keySpecification.getKeyStore() == null) {
			throw new IllegalArgumentException("key store entry key store cannot be null");
		}

		KeyStore keyStoreDefinition = keySpecification.getKeyStore();

		// asymmetric and symetric keys are stored in different key store types
		java.security.KeyStore keyStore = null;
		try {
			keyStore = java.security.KeyStore.getInstance(keyStoreDefinition.getType() != null ? keyStoreDefinition.getType() : java.security.KeyStore.getDefaultType());
		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Failed to read system property with the name [" + keyStoreDefinition.getSystemProperty() + "]", e);
		}

		// passwords cannot be null, but can be empty (only when created programmatically)
		char[] keyStorePassword = keyStoreDefinition.getPassword() != null ? keyStoreDefinition.getPassword().toCharArray() : new char[0];

		Path filePath = extractKeyStoreFilePath(keyStoreDefinition);

		InputStream is = null;
		OutputStream os = null;
		try {

			if (Files.exists(filePath)) {
				is = Files.newInputStream(filePath, StandardOpenOption.CREATE);
				keyStore.load(is, keyStorePassword);
			} else {
				Files.createFile(filePath);
				keyStore.load(null, null);
			}

			saveToKeyStore(keyStore, key, keySpecification);

			os = Files.newOutputStream(filePath, StandardOpenOption.CREATE);
			keyStore.store(os, keyStorePassword);
			os.flush();

		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Failed to manipulate key store", e);
		} finally {
			IOTools.closeCloseable(os, log);
			IOTools.closeCloseable(is, log);
		}

	}

	private static void saveToKeyStore(java.security.KeyStore keyStore, Key key, HasKeyStoreEntry keySpec) throws EncryptionTokenGeneratorException {

		String entryAlias = keySpec.getKeyEntryAlias();

		// passwords cannot be null, but can be empty (only when created programmatically)
		char[] entryPass = keySpec.getKeyEntryPassword() != null ? keySpec.getKeyEntryPassword().toCharArray() : new char[0];

		if (key instanceof java.security.PublicKey) {
			throw new EncryptionTokenGeneratorException(java.security.PublicKey.class.getName() + " instances cannot be added programmatically to key stores");
		} else if (key instanceof java.security.PrivateKey) {
			throw new EncryptionTokenGeneratorException(java.security.PrivateKey.class.getName() + " instances cannot be added programmatically to key stores");
		} else if (key instanceof javax.crypto.SecretKey) {
			saveSecretKeyToKeyStore(keyStore, (javax.crypto.SecretKey) key, entryAlias, entryPass);
		} else {
			throw new EncryptionTokenGeneratorException("Unexpected type of key: " + key);
		}

	}

	private static void saveSecretKeyToKeyStore(java.security.KeyStore keyStore, javax.crypto.SecretKey key, String entryAlias, char[] entryPass) throws EncryptionTokenGeneratorException {
		try {
			keyStore.setKeyEntry(entryAlias, key, entryPass, null);
		} catch (Exception e) {
			throw EncryptionTokenGeneratorException.wrap("Failed to set " + SecretKey.class.getName() + " to key store entry [" + entryAlias + "]", e);
		}
	}

}
