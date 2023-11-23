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
package com.braintribe.model.processing.crypto.factory;

import java.security.Key;
import java.security.KeyPair;

import com.braintribe.crypto.Cryptor;
import com.braintribe.model.crypto.configuration.encryption.EncryptionConfiguration;

public interface CipherCryptorFactory<T extends EncryptionConfiguration, E extends Cryptor> extends CryptorFactory<T, E> {

	@Override
	CipherCryptorBuilder<T, E> builder() throws CryptorFactoryException;

	interface CipherCryptorBuilder<T, E> extends CryptorFactory.CryptorBuilder<T, E> {

		@Override
		CipherCryptorBuilder<T, E> configuration(T cryptoConfiguration) throws CryptorFactoryException;

		CipherCryptorBuilder<T, E> key(Key key) throws CryptorFactoryException;

		CipherCryptorBuilder<T, E> keyPair(KeyPair keyPair) throws CryptorFactoryException;
		
		CipherCryptorBuilder<T, E> mode(String mode) throws CryptorFactoryException;
		
		CipherCryptorBuilder<T, E> padding(String padding) throws CryptorFactoryException;
		
		CipherCryptorBuilder<T, E> provider(String provider) throws CryptorFactoryException;

	}

}
