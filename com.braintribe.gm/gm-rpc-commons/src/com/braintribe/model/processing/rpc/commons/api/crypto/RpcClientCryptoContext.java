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
package com.braintribe.model.processing.rpc.commons.api.crypto;

import java.security.Key;

import com.braintribe.crypto.Decryptor;
import com.braintribe.model.processing.rpc.commons.api.GmRpcException;

/**
 * <p>
 * Cryptography context for RPC clients.
 * 
 * @deprecated Cryptographic capabilities were removed from the RPC layer. This interface is now obsolete and will be
 *             removed in a future version.
 */
@Deprecated
public interface RpcClientCryptoContext extends RpcCryptoContext {

	/**
	 * <p>
	 * Returns a unique identifier for the RPC caller.
	 * 
	 * @return A unique identifier for the RPC caller.
	 */
	String getClientId();

	/**
	 * <p>
	 * Returns a {@link Decryptor} for for decrypting the RPC response based on the given key data.
	 * 
	 * @param encodedKey
	 *            The encoded key
	 * @param keyAlgorithm
	 *            The algorithm of the encoded key
	 * @return A {@link Decryptor} for for decrypting the RPC response
	 * @throws GmRpcException
	 *             In case a {@link Decryptor} fails to be created
	 */
	Decryptor responseDecryptor(String encodedKey, String keyAlgorithm) throws GmRpcException;

	/**
	 * <p>
	 * Returns a {@link Key} for decrypting the RPC response based on the given key data.
	 * 
	 * @param encodedKey
	 *            The encoded key
	 * @param keyAlgorithm
	 *            The algorithm of the encoded key
	 * @return A {@link Key} for decrypting the RPC response
	 * @throws GmRpcException
	 *             In case a {@link Key} fails to be imported
	 */
	Key importKey(String encodedKey, String keyAlgorithm) throws GmRpcException;

}
