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

import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import com.braintribe.transport.ssl.SslSocketFactoryProvider;

public class StrictSslSocketFactoryProvider implements SslSocketFactoryProvider {
	protected String securityProtocol = Constants.DEFAULT_SECURITY_PROTOCOL;
	
	@Override
	public SSLSocketFactory provideSSLSocketFactory() throws Exception {
		SSLContext sslContext = this.provideSSLContext();
		SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();		
		return sslSocketFactory;
	}
	
	@Override
	public SSLContext provideSSLContext() throws Exception {
		SSLContext sslContext = SSLContext.getInstance(this.securityProtocol);
		sslContext.init(null, null, new SecureRandom());
		return sslContext;
	}

	public String getSecurityProtocol() {
		return securityProtocol;
	}
	public void setSecurityProtocol(String securityProtocol) {
		this.securityProtocol = securityProtocol;
	}
}
