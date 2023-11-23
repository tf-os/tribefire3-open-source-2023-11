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

import com.braintribe.crypto.Cryptor;
import com.braintribe.model.crypto.configuration.CryptoConfiguration;

/**
 * <p>
 * Factory of {@link Cryptor} instances.
 * 
 *
 * @param <T>
 *            The type of {@link CryptoConfiguration} the factory uses for creating {@link Cryptor} instances.
 * @param <E>
 *            The type of {@link Cryptor} the factory creates.
 */
public interface CryptorFactory<T extends CryptoConfiguration, E extends Cryptor> {

	E getCryptor(T cryptoConfiguration) throws CryptorFactoryException;

	<R extends Cryptor> R getCryptor(Class<R> requiredType, T cryptoConfiguration) throws CryptorFactoryException;

	CryptorBuilder<T, E> builder() throws CryptorFactoryException;

	interface CryptorBuilder<T, E> {

		CryptorBuilder<T, E> configuration(T cryptoConfiguration) throws CryptorFactoryException;

		E build() throws CryptorFactoryException;

		<R extends Cryptor> R build(Class<R> requiredType) throws CryptorFactoryException;

	}
	
}
