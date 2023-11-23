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
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.braintribe.transport.ssl.SslSocketFactoryProvider;

public class EasySslSocketFactoryProvider implements SslSocketFactoryProvider {
	protected String securityProtocol = Constants.DEFAULT_SECURITY_PROTOCOL;

	private SecureRandom secureRandom = new SecureRandom();
	
	@Override
	public SSLSocketFactory provideSSLSocketFactory() throws Exception {
		SSLContext sslContext = this.provideSSLContext();
		SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();		
		return sslSocketFactory;
	}
	
	@Override
	public SSLContext provideSSLContext() throws Exception {
		SSLContext sslContext = SSLContext.getInstance(this.securityProtocol);

		// set up a TrustManager that trusts everything
		sslContext.init(null, new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
				return;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
				return;
			}
		} }, secureRandom);
		
		return sslContext;
	}

	public String getSecurityProtocol() {
		return securityProtocol;
	}
	public void setSecurityProtocol(String securityProtocol) {
		this.securityProtocol = securityProtocol;
	}
}
