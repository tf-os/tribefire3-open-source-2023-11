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
package com.braintribe.model.processing.rpc.commons.impl.crypto;

import java.security.Key;

import com.braintribe.model.processing.rpc.commons.api.GmRpcException;
import com.braintribe.model.processing.rpc.commons.api.crypto.RpcServerCryptoContext;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * @deprecated Cryptographic capabilities were removed from the RPC layer. This interface is now obsolete and will be
 *             removed in a future version.
 */
@Deprecated
public class NoOpRpcServerCryptoContext implements RpcServerCryptoContext {

	public static final RpcServerCryptoContext INSTANCE = new NoOpRpcServerCryptoContext();

	private NoOpRpcServerCryptoContext() {
	}

	@Override
	public boolean encryptResponseFor(ServiceRequest request) {
		return false;
	}

	@Override
	public CryptoResponseContext createResponseContext(String clientId) throws GmRpcException {
		throw new UnsupportedOperationException("Method not supported");
	}

	@Override
	public Key generateKey() throws GmRpcException {
		throw new UnsupportedOperationException("Method not supported");
	}

	@Override
	public String exportKey(String clientId, Key key) throws GmRpcException {
		throw new UnsupportedOperationException("Method not supported");
	}

}
