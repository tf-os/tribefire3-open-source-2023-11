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

import com.braintribe.crypto.Encryptor;
import com.braintribe.model.processing.rpc.commons.api.GmRpcException;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * Cryptography context for RPC servers.
 * 
 * @deprecated Cryptographic capabilities were removed from the RPC layer. This interface is now obsolete and will be
 *             removed in a future version.
 */
@Deprecated
public interface RpcServerCryptoContext extends RpcCryptoContext {

	/**
	 * <p>
	 * Determines whether the response the given request must be encrypted.
	 * 
	 * @param request
	 *            The {@link ServiceRequest} to be inspected
	 * @return Whether the response the given request must be encrypted.
	 */
	boolean encryptResponseFor(ServiceRequest request);

	/**
	 * <p>
	 * The context for encrypting a RPC response based on the requirements of the given client.
	 * 
	 * @param clientId
	 *            The client id
	 * @return The context for encrypting a RPC response for a given client
	 * @throws GmRpcException
	 *             If the context generation fails
	 */
	CryptoResponseContext createResponseContext(String clientId) throws GmRpcException;

	/**
	 * <p>
	 * Generates a key for encrypting the rpc response.
	 * 
	 * @return A {@link Key} for encrypting the rpc response
	 * @throws GmRpcException
	 *             If the key generation fails
	 */
	Key generateKey() throws GmRpcException;

	/**
	 * <p>
	 * Exports the {@code key} used for encrypting the rpc response.
	 * 
	 * <p>
	 * Implementations must ensure that the resulting exported key is safe to be imported only by the client represented
	 * by the given {@code clientId}.
	 * 
	 * @param clientId
	 *            Id of the key destination client
	 * @param key
	 *            Key to be exported
	 * @return String representation of the exported key
	 * @throws GmRpcException
	 *             If the key exporting fails
	 */
	String exportKey(String clientId, Key key) throws GmRpcException;

	/**
	 * <p>
	 * The context for encrypting a RPC response based on the requirements of a client.
	 * 
	 * <p>
	 * The key to be transported back to the client is given by {@link #getEncryptedResponseKey()} whereas the means for
	 * encrypting the response (an {@link Encryptor}) are given by {@link #getResponseEncryptor()}
	 * 
	 */
	interface CryptoResponseContext {

		/**
		 * <p>
		 * Returns the String representation of the encrypted response key
		 * 
		 * @return The String representation of the encrypted response key
		 */
		String getEncryptedResponseKey();

		/**
		 * <p>
		 * Returns the algorithm of the key given by {@link #getEncryptedResponseKey()}.
		 * 
		 * @return The algorithm of the key given by {@link #getEncryptedResponseKey()}.
		 */
		String getEncryptedResponseKeyAlgorithm();

		/**
		 * <p>
		 * Returns an {@link Encryptor} for the RPC response
		 * 
		 * @return An {@link Encryptor} for the RPC response
		 */
		Encryptor getResponseEncryptor();

	}

}
