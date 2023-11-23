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

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.crypto.key.keystore.HasKeyStoreEntry;
import com.braintribe.model.crypto.key.keystore.KeyStore;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoaderException;
import com.braintribe.utils.IOTools;

/**
 * TODO: document.
 * 
 */
public abstract class KeyStoreEntryLoader {
	
	private static final Logger log = Logger.getLogger(KeyStoreEntryLoader.class);

	private boolean cacheKeyStores = true;

	private Map<CachedKeyStoreKey, java.security.KeyStore> cachedKeyStores = new HashMap<>();

	@Configurable
	public void setCacheKeyStores(boolean cacheKeyStores) {
		this.cacheKeyStores = cacheKeyStores;
	}

	public <O> O loadKey(Class<O> keyType, HasKeyStoreEntry keyDefinition) throws EncryptionTokenLoaderException {

		String entryAlias = keyDefinition.getKeyEntryAlias();
		char[] entryPassword = keyDefinition.getKeyEntryPassword() != null ? keyDefinition.getKeyEntryPassword().toCharArray() : new char[0];

		java.security.KeyStore keyStore = getKeyStore(keyDefinition.getKeyStore());

		java.security.KeyStore.ProtectionParameter protectionParam = new java.security.KeyStore.PasswordProtection(entryPassword);

		java.security.KeyStore.Entry keyStoreEntry = null;
		try {
			keyStoreEntry = keyStore.getEntry(entryAlias, protectionParam);
		} catch (Exception e) {
			throw EncryptionTokenLoaderException.wrap("Failed to retrieve keystore entry [" + entryAlias + "]", e);
		}

		if (keyStoreEntry == null) {
			throw new EncryptionTokenLoaderException("No entry found in the key store for [" + entryAlias + "]");
		}

		O key = extractFromKeyStoreEntry(keyType, keyStoreEntry);

		if (log.isTraceEnabled()) {
			log.trace("Loaded [" + key + "] from entry [" + entryAlias + "] stored in [" + keyStore + "]");
		}

		return key;

	}

	/**
	 * <p>
	 * Extracts an instance of a specific type from the provided
	 * {@link java.security.KeyStore.Entry}
	 * 
	 * @param extractableType
	 *            The type of the object expected to be extracted from the the entry
	 * @param keyStoreEntry
	 *            The {@link java.security.KeyStore.Entry} to be inspected for
	 *            the expected type.
	 * @return An instance of the specified type as retrieved from the given
	 *         {@link java.security.KeyStore.Entry}.
	 * @throws EncryptionTokenLoaderException
	 *             If no instance of the specified type can be retrieved from the
	 *             given {@link java.security.KeyStore.Entry}.
	 */
	protected <O> O extractFromKeyStoreEntry(Class<O> extractableType, java.security.KeyStore.Entry keyStoreEntry) throws EncryptionTokenLoaderException {

		O key = null;

		try {

			if (keyStoreEntry instanceof java.security.KeyStore.PrivateKeyEntry) {
				key = convertToExpected(extractableType, (java.security.KeyStore.PrivateKeyEntry) keyStoreEntry);
			}
			
			if (keyStoreEntry instanceof java.security.KeyStore.TrustedCertificateEntry) {
				key = convertToExpected(extractableType, (java.security.KeyStore.TrustedCertificateEntry) keyStoreEntry);
			}
			
			if (keyStoreEntry instanceof java.security.KeyStore.SecretKeyEntry) {
				key = convertToExpected(extractableType, (java.security.KeyStore.SecretKeyEntry) keyStoreEntry);
			}
			
		} catch (Exception e) {
			throw EncryptionTokenLoaderException.wrap("Unable to extract a [ " + extractableType.getName() + " ] from an entry of type [ " + keyStoreEntry.getClass().getName() + " ]", e);
		}

		if (key == null) {
			throw new EncryptionTokenLoaderException("Unable to extract a [ " + extractableType.getName() + " ] from an entry of type [ " + keyStoreEntry.getClass().getName() + " ]");
		} 

		if (log.isTraceEnabled()) {
			log.trace("Extracted key [ " + key + " ] from entry [ " + keyStoreEntry + " ]");
		}

		return key;

	}

	protected <O> O convertToExpected(Class<O> type, java.security.KeyStore.PrivateKeyEntry entry) {
		
		if (entry.getPrivateKey() == null) {
			return null;
		}
		
		if (java.security.PrivateKey.class.isAssignableFrom(type)) {
			
			return type.cast(entry.getPrivateKey());
			
		} else if (java.security.cert.Certificate.class.isAssignableFrom(type)) {
			
			return type.cast(entry.getCertificate());
			
		} else if (java.security.KeyPair.class.isAssignableFrom(type)) {
			
			return type.cast(new java.security.KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey()));
			
		}
		
		return null;
		
	}

	protected <O> O convertToExpected(Class<O> type, java.security.KeyStore.TrustedCertificateEntry entry) {
		
		if (entry.getTrustedCertificate() == null) {
			return null;
		}
		
		if (java.security.PublicKey.class.isAssignableFrom(type)) {
			
			return type.cast(entry.getTrustedCertificate().getPublicKey());
			
		} else if (java.security.cert.Certificate.class.isAssignableFrom(type)) {
			
			return type.cast(entry.getTrustedCertificate());
			
		} else if (java.security.KeyPair.class.isAssignableFrom(type) && entry.getTrustedCertificate().getPublicKey() != null) {
			
			return type.cast(new java.security.KeyPair(entry.getTrustedCertificate().getPublicKey(), null));
			
		}
		
		return null;
		
	}

	protected <O> O convertToExpected(Class<O> type, java.security.KeyStore.SecretKeyEntry entry) {
		
		if (entry.getSecretKey() == null) {
			return null;
		}
		
		if (javax.crypto.SecretKey.class.isAssignableFrom(type)) {
			return type.cast(entry.getSecretKey());
		}
		
		return null;
		
	}

	protected java.security.KeyStore getKeyStore(KeyStore keyStoreDefinition) throws EncryptionTokenLoaderException {

		java.security.KeyStore keyStore = null;

		if (!cacheKeyStores) {
			keyStore = loadKeyStore(keyStoreDefinition);
		} else {

			CachedKeyStoreKey keyStoreKey = new CachedKeyStoreKey(keyStoreDefinition);

			keyStore = cachedKeyStores.get(keyStoreKey);

			if (keyStore == null) {
				keyStore = loadKeyStore(keyStoreDefinition);
				cachedKeyStores.put(keyStoreKey, keyStore);
			}

		}

		return keyStore;

	}

	protected java.security.KeyStore loadKeyStore(KeyStore keyStoreDefinition) throws EncryptionTokenLoaderException {

		String keyStorePath = keyStoreDefinition.getFilePath();
		String type = keyStoreDefinition.getType() != null ? keyStoreDefinition.getType() : java.security.KeyStore.getDefaultType();
		char[] password = keyStoreDefinition.getPassword() != null ? keyStoreDefinition.getPassword().toCharArray() : new char[0];
		
		
		if (keyStorePath == null) {
			keyStorePath = System.getProperty(keyStoreDefinition.getSystemProperty());
			if (keyStorePath == null || keyStorePath.trim().isEmpty()) {
				throw new EncryptionTokenLoaderException(
						"Unabled to a key store as the given definition defines neither a file path nor a reachable system property");
			}
		}

		java.security.KeyStore ks = null;
		try {
			ks = java.security.KeyStore.getInstance(type);
		} catch (Exception e) {
			throw EncryptionTokenLoaderException.wrap("Failed to get an instance of key store of the specified type: [" + type + "]", e);
		}

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(keyStorePath);
			ks.load(fis, password);
		} catch (Exception e) {
			throw EncryptionTokenLoaderException.wrap("Failed to load key store from location [" + keyStorePath + "]", e);
		} finally {
			IOTools.closeQuietly(fis);
		}

		return ks;

	}

	private static class CachedKeyStoreKey {

		private int hashCode = -1;

		CachedKeyStoreKey(KeyStore keyStoreDefinition) {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((keyStoreDefinition.getFilePath() == null) ? 0 : keyStoreDefinition.getFilePath().hashCode());
			result = prime * result
					+ ((keyStoreDefinition.getSystemProperty() == null) ? 0 : keyStoreDefinition.getSystemProperty().hashCode());
			result = prime * result
					+ ((keyStoreDefinition.getPassword() == null) ? 0 : keyStoreDefinition.getPassword().hashCode());
			result = prime * result
					+ ((keyStoreDefinition.getProvider() == null) ? 0 : keyStoreDefinition.getProvider().hashCode());
			result = prime * result
					+ ((keyStoreDefinition.getType() == null) ? 0 : keyStoreDefinition.getType().hashCode());
			this.hashCode = result;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CachedKeyStoreKey other = (CachedKeyStoreKey) obj;
			if (hashCode != other.hashCode)
				return false;
			return true;
		}

	}
}
