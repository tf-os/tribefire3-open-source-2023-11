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
package com.braintribe.model.processing.rpc.test.commons;

import java.security.KeyPair;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.crypto.BidiCryptor;
import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.Decryptor;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.crypto.configuration.encryption.EncryptionConfiguration;
import com.braintribe.model.processing.crypto.factory.CipherCryptorFactory;
import com.braintribe.model.processing.crypto.token.generator.KeyPairGenerator;


public class TestClientKeyProvider implements Supplier<KeyPair>, Function<String, Cryptor>, InitializationAware {

	private CipherCryptorFactory<EncryptionConfiguration, ? extends Cryptor> cryptorFactory;
	private KeyPairGenerator<com.braintribe.model.crypto.key.KeyPair> keyPairGenerator;
	private com.braintribe.model.crypto.key.KeyPair keyPairSpec;

	private Cryptor serverSideCryptor;
	private BidiCryptor clientSideCryptor;
	private KeyPair keyPair;

	@Required
	@Configurable
	public void setCryptorFactory(CipherCryptorFactory<EncryptionConfiguration, ? extends Cryptor> cryptorFactory) {
		this.cryptorFactory = cryptorFactory;
	}

	@Required
	@Configurable
	public void setKeyPairGenerator(KeyPairGenerator<com.braintribe.model.crypto.key.KeyPair> keyPairGenerator) {
		this.keyPairGenerator = keyPairGenerator;
	}

	@Required
	@Configurable
	public void setKeyPairSpec(com.braintribe.model.crypto.key.KeyPair keyPairSpec) {
		this.keyPairSpec = keyPairSpec;
	}

	@Override
	public void postConstruct() {

		try {
			keyPair = keyPairGenerator.generate(keyPairSpec, null);

			clientSideCryptor = cryptorFactory.builder().keyPair(keyPair).build(BidiCryptor.class);

			KeyPair publicOnly = new KeyPair(keyPair.getPublic(), null);

			serverSideCryptor = cryptorFactory.builder().keyPair(publicOnly).build();
		} catch(Exception e) {
			Exceptions.unchecked(e, "Error while initializing the TestClientKeyProvider");
		}
	}

	@Override
	public Cryptor apply(String clientId) throws RuntimeException {
		return serverSideCryptor;
	}

	@Override
	public KeyPair get() throws RuntimeException {
		return keyPair;
	}
	
	public Decryptor getDecryptor() {
		return clientSideCryptor;
	}

}
