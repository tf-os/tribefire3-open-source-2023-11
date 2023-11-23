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

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

import com.braintribe.cfg.Configurable;


public class FilteredKeyManager implements X509KeyManager {

	protected String clientAlias = null;
	protected String serverAlias = null;

	protected final X509KeyManager delegate;

	public FilteredKeyManager(X509KeyManager originatingKeyManager) {
		this.delegate = originatingKeyManager;
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		return this.delegate.getCertificateChain(alias);
	}

	@Override
	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		return this.delegate.chooseClientAlias(keyType, issuers, socket);
	}

	@Override
	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		return this.delegate.chooseServerAlias(keyType, issuers, socket);
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		if (this.clientAlias == null) {
			return this.delegate.getClientAliases(keyType, issuers);
		} else {
			return new String[] {this.clientAlias};
		}
	}

	@Override
	public PrivateKey getPrivateKey(String alias) {
		return this.delegate.getPrivateKey(alias);
	}

	@Override
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		if (this.serverAlias == null) {
			return this.delegate.getServerAliases(keyType, issuers);
		} else {
			return new String[] {this.serverAlias};
		}
	}

	@Configurable
	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	@Configurable
	public void setServerAlias(String serverAlias) {
		this.serverAlias = serverAlias;
	}


}
