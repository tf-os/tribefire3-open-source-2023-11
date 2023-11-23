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
package com.braintribe.transport.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

public class SocksProxyAwareSslConnectionFactory extends SSLConnectionSocketFactory {

	private InetSocketAddress socksAddress = null;

	public SocksProxyAwareSslConnectionFactory(final SSLContext sslContext, final HostnameVerifier hostnameVerifier) {
		super(sslContext, hostnameVerifier);

		String socksHost = DefaultHttpClientProvider.getProperty("tf_socksProxyHost");
		String socksPort = DefaultHttpClientProvider.getProperty("tf_socksProxyPort");
		if (socksHost != null && socksPort != null) {
			socksAddress = new InetSocketAddress(socksHost, Integer.parseInt(socksPort));
		}

	}

	@Override
	public Socket createSocket(final HttpContext context) throws IOException {
		if (socksAddress == null) {
			return super.createSocket(context);
		}
		Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksAddress);
		return new Socket(proxy);
	}
}
