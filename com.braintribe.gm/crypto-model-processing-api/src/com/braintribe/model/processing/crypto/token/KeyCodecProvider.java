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
package com.braintribe.model.processing.crypto.token;

import java.security.Key;

import com.braintribe.codec.Codec;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoaderException;

/**
 * <p>
 * Provides {@link com.braintribe.codec.Codec}(s) capable of importing/exporting {@link Key}(s) from/to byte arrays.
 * 
 * <p>
 * The provided codec shall import and export keys in accordance with the key algorithm passed as argument to the
 * {@link #getKeyCodec(String)} method.
 * 
 */
public interface KeyCodecProvider<T extends Key> {

	/**
	 * <p>
	 * Returns a {@link com.braintribe.codec.Codec} capable of converting {@link Key}(s) of the given
	 * {@code keyAlgorithm} to and from byte arrays.
	 * 
	 * @param keyAlgorithm
	 *            The algorithm of the {@link Key} type to be encoded/decoded.
	 * @return A {@link com.braintribe.codec.Codec} capable of converting {@link Key}(s) of the given
	 *         {@code keyAlgorithm} to and from byte arrays.
	 * @throws EncryptionTokenLoaderException
	 *             If any error occur while obtaining the codec.
	 */
	Codec<T, byte[]> getKeyCodec(String keyAlgorithm) throws EncryptionTokenLoaderException;

}
