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
package com.braintribe.codec.marshaller.api;

import java.security.Key;

import com.braintribe.crypto.Cryptor;

/**
 * <p>
 * Builder of {@link Marshaller} cryptography decorators.
 * 
 * <p>
 * Implementations must ensure that the returned decorated {@link Marshaller}(s) will encrypt data on
 * {@link Marshaller#marshall(java.io.OutputStream, Object)} and decrypt upon
 * {@link Marshaller#unmarshall(java.io.InputStream)}.
 * 
 *
 */
public interface CryptoMarshallerDecorator {

	/**
	 * <p>
	 * Decorates the given {@link Marshaller} to work with cryptography.
	 * 
	 * @param delegate
	 *            The {@link Marshaller} to be decorated
	 * @return A cryptography-enabled {@link Marshaller}
	 * @throws MarshallException
	 *             If the decoration fails.
	 */
	Marshaller decorate(Marshaller delegate) throws MarshallException;

	/**
	 * <p>
	 * Decorates the given {@link Marshaller} to work with cryptography. Using the given {@link Key}.
	 * 
	 * @param delegate
	 *            The {@link Marshaller} to be decorated
	 * @param key
	 *            The {@link Key} used for encrypting/decrypting data
	 * @return A cryptography-enabled {@link Marshaller}
	 * @throws MarshallException
	 *             If the decoration fails.
	 */
	Marshaller decorate(Marshaller delegate, Key key) throws MarshallException;

	/**
	 * <p>
	 * Decorates the given {@link Marshaller} to work with cryptography. Using the given {@link Key} and cipher
	 * transformation.
	 * 
	 * @param delegate
	 *            The {@link Marshaller} to be decorated
	 * @param key
	 *            The {@link Key} used for encrypting/decrypting data
	 * @param cipherTransformation
	 *            The transformation used for encrypting/decrypting data
	 * @return A cryptography-enabled {@link Marshaller}
	 * @throws MarshallException
	 *             If the decoration fails.
	 */
	Marshaller decorate(Marshaller delegate, Key key, String cipherTransformation) throws MarshallException;

	/**
	 * <p>
	 * Decorates the given {@link Marshaller} to work with cryptography. Using the given {@link Cryptor}.
	 * 
	 * @param delegate
	 *            The {@link Marshaller} to be decorated
	 * @param cryptor
	 *            The {@link Cryptor} used for encrypting/decrypting data
	 * @return A cryptography-enabled {@link Marshaller}
	 * @throws MarshallException
	 *             If the decoration fails.
	 */
	Marshaller decorate(Marshaller delegate, Cryptor cryptor) throws MarshallException;

}
