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
package com.braintribe.model.processing.webrpc.client;

import java.util.function.Consumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.processing.rpc.commons.api.GmRpcException;
import com.braintribe.model.processing.rpc.commons.api.authorization.RpcClientAuthorizationContext;

/**
 * <p>A basic {@link RpcClientAuthorizationContext} implementation which 
 *    notifies authorization failures ({@code Throwable}(s)) to the receiver configured via
 *    {@link #setAuthorizationFailureListener(Consumer)}.
 * 
 */
public class GmWebRpcClientAuthorizationContext implements RpcClientAuthorizationContext<Throwable> {
	
	private int maxRetries;
	private Consumer<Throwable> authorizationFailureListener;

	@Configurable
	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Configurable
	public void setAuthorizationFailureListener(Consumer<Throwable> authorizationFailureListener) {
		this.authorizationFailureListener = authorizationFailureListener;
	}

	@Override
	public int getMaxRetries() {
		return maxRetries;
	}

	@Override
	public void onAuthorizationFailure(Throwable failureContext) {
		if (authorizationFailureListener != null) {
			try {
				authorizationFailureListener.accept(failureContext);
			} catch (Exception e) {
				throw new GmRpcException("Failed to notify configured listener about authorization failure. "
						+ "Listener: [ "+authorizationFailureListener+" ]. Failure: [ "+failureContext+" ]", e);
			}
		}
	}

}
