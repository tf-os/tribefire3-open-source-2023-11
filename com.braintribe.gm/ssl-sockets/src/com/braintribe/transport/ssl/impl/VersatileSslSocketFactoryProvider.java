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
package com.braintribe.transport.ssl.impl;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.transport.ssl.SslSocketFactoryProvider;
import com.braintribe.transport.ssl.keystore.KeystoreType;

public class VersatileSslSocketFactoryProvider implements SslSocketFactoryProvider {

	protected static Logger logger = Logger.getLogger(VersatileSslSocketFactoryProvider.class);

	protected String clientAlias = null;
	protected String serverAlias = null;
	protected File keystoreFile = null;
	protected String keystorePassword = null;
	protected String keyPassword = null;
	protected KeystoreType keyStoreType = KeystoreType.DEFAULT_KEYSTORE_TYPE;
	protected String keyStoreKeyManagerFactoryAlgorithm = Constants.DEFAULT_KEY_MANAGER_FACTORY_ALGORITHM;

	protected File truststoreFile = null;
	protected String truststorePassword = null;
	protected KeystoreType trustStoreType = KeystoreType.DEFAULT_KEYSTORE_TYPE;
	protected String trustStoreKeyManagerFactoryAlgorithm = Constants.DEFAULT_KEY_MANAGER_FACTORY_ALGORITHM;

	protected boolean trustAll = false;

	protected String securityProtocol = Constants.DEFAULT_SECURITY_PROTOCOL;

	@Override
	public SSLSocketFactory provideSSLSocketFactory() throws Exception {
		SSLContext sc = this.provideSSLContext();
		SSLSocketFactory sslSocketFactory = sc.getSocketFactory();
		return sslSocketFactory;
	}

	@Override
	public SSLContext provideSSLContext() throws Exception {

		boolean trace = logger.isTraceEnabled();

		KeyManager[] keyManagers = null;
		TrustManager[] trustManagers = null;

		if (this.keystoreFile != null) {

			if (trace) {
				logger.trace("Using keystore file "+this.keystoreFile);
			}

			KeystoreType type = this.keyStoreType;
			if (type == KeystoreType.AUTO) {
				type = KeystoreType.determineKeyStoreType(this.keystoreFile);
				if (logger.isTraceEnabled()) {
					logger.trace("Auto-detected keystore type "+type);
				}
			}

			KeyStore keyStore = KeyStore.getInstance(type.name());

			char[] keyStorePasswordChars = null;
			if (this.keystorePassword != null) {
				keyStorePasswordChars = this.keystorePassword.toCharArray();
			}
			keyStore.load(new FileInputStream(this.keystoreFile), keyStorePasswordChars);

			char[] keyPasswordChars = null;
			if (this.keyPassword != null) {
				keyPasswordChars = this.keyPassword.toCharArray();
			}

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(this.keyStoreKeyManagerFactoryAlgorithm);
			kmf.init(keyStore, keyPasswordChars);

			keyManagers = kmf.getKeyManagers();

			keyManagers = this.addAliasFilter(keyManagers);

		} else {
			if (trace) {
				logger.trace("No keystore file configured.");
			}

		}

		if (trustAll) {

			trustManagers = new TrustManager[] { new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
					//Intentionally left empty
				}

				@Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
					//Intentionally left empty
				}
			} };

		} else {
			if (this.truststoreFile != null) {

				if (trace) {
					logger.trace("Using truststore file "+this.truststoreFile);
				}

				KeystoreType type = this.trustStoreType;
				if (type == KeystoreType.AUTO) {
					type = KeystoreType.determineKeyStoreType(this.truststoreFile);
					if (logger.isTraceEnabled()) {
						logger.trace("Auto-detected truststore type "+type);
					}
				}

				KeyStore trustStore = KeyStore.getInstance(type.name());

				char[] trustStorePasswordChars = null;
				if (this.truststorePassword != null) {
					trustStorePasswordChars = this.truststorePassword.toCharArray();
				}
				trustStore.load(new FileInputStream(this.truststoreFile), trustStorePasswordChars);

				TrustManagerFactory tmf = TrustManagerFactory.getInstance(this.trustStoreKeyManagerFactoryAlgorithm);
				tmf.init(trustStore);

				trustManagers = tmf.getTrustManagers();
			} else {
				if (trace) {
					logger.trace("No truststore file configured.");
				}
			}
		}

		if ((keyManagers == null) && (trustManagers == null)) {
			throw new Exception("Neither a keystore ("+this.keystoreFile+") nor a truststore ("+this.truststoreFile+") can be used to initialize the SSLContext.");
		}

		SSLContext sc = SSLContext.getInstance(this.securityProtocol);
		sc.init(keyManagers, trustManagers, null);
		return sc;
	}

	protected KeyManager[] addAliasFilter(KeyManager[] keyManagers) {
		if ((this.clientAlias != null) || (this.serverAlias != null)) {
			KeyManager[] tmpKeyManagers = new KeyManager[keyManagers.length]; 
			for (int i=0; i<keyManagers.length; ++i) {
				if (keyManagers[i] instanceof X509KeyManager) {
					X509KeyManager x508KeyManager = (X509KeyManager) keyManagers[i];
					FilteredKeyManager filteredKeyManager = new FilteredKeyManager(x508KeyManager);
					if (this.clientAlias != null) {
						filteredKeyManager.setClientAlias(this.clientAlias);
					}
					if (this.serverAlias != null) {
						filteredKeyManager.setServerAlias(this.serverAlias);
					}
					tmpKeyManagers[i] = filteredKeyManager;
				} else {
					tmpKeyManagers[i] = keyManagers[i];
				}
			}
			keyManagers = tmpKeyManagers;
		}
		return keyManagers;
	}

	@Configurable
	public void setTruststoreFile(File truststoreFile) {
		this.truststoreFile = truststoreFile;
	}

	@Configurable
	public void setTruststorePassword(String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}


	public String getSecurityProtocol() {
		return securityProtocol;
	}
	@Configurable
	public void setSecurityProtocol(String securityProtocol) {
		this.securityProtocol = securityProtocol;
	}

	@Configurable
	public void setTrustStoreType(KeystoreType trustStoreType) {
		this.trustStoreType = trustStoreType;
	}

	@Configurable
	public void setTrustStoreKeyManagerFactoryAlgorithm(String trustStoreKeyManagerFactoryAlgorithm) {
		this.trustStoreKeyManagerFactoryAlgorithm = trustStoreKeyManagerFactoryAlgorithm;
	}

	@Configurable
	public void setKeystoreFile(File keystoreFile) {
		this.keystoreFile = keystoreFile;
	}

	@Configurable
	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	@Configurable
	public void setKeyStoreType(KeystoreType keyStoreType) {
		this.keyStoreType = keyStoreType;
	}

	@Configurable
	public void setKeyStoreKeyManagerFactoryAlgorithm(String keyStoreKeyManagerFactoryAlgorithm) {
		this.keyStoreKeyManagerFactoryAlgorithm = keyStoreKeyManagerFactoryAlgorithm;
	}

	@Configurable
	public void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword;
	}

	@Configurable
	public void setTrustAll(boolean trustAll) {
		this.trustAll = trustAll;
	}

	@Configurable
	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}
	@Configurable
	public void setServerAlias(String serverAlias) {
		this.serverAlias = serverAlias;
	}

	@Override
	public String toString() {
		return "VersatileSslSocketFactoryProvider with keystore file: "+keystoreFile+", truststore file: "+truststoreFile;
	}
}
