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
package tribefire.platform.wire.space.common;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.ssl.SslSocketFactoryProvider;
import com.braintribe.transport.ssl.impl.EasySslSocketFactoryProvider;
import com.braintribe.transport.ssl.impl.StrictSslSocketFactoryProvider;
import com.braintribe.web.servlet.auth.cookie.DefaultCookieHandler;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.HttpContract;

@Managed
public class HttpSpace implements HttpContract {

	@Managed
	public HttpClientProvider clientProvider() {
		DefaultHttpClientProvider bean = new DefaultHttpClientProvider();
		bean.setSslSocketFactoryProvider(sslSocketFactoryProvider());
		return bean;
	}

	@Managed
	public HttpClientProvider nonPoolingClientProvider() {
		DefaultHttpClientProvider bean = new DefaultHttpClientProvider();
		bean.setSslSocketFactoryProvider(sslSocketFactoryProvider());
		bean.setPoolTimeToLive(1L);
		return bean;
	}

	@Managed
	public SslSocketFactoryProvider sslSocketFactoryProvider() {
		SslSocketFactoryProvider bean = TribefireRuntime.getAcceptSslCertificates()?
				new EasySslSocketFactoryProvider():
				new StrictSslSocketFactoryProvider();
				
		return bean;
	}

	@Override
	@Managed
	public DefaultCookieHandler cookieHandler() {
		DefaultCookieHandler bean = new DefaultCookieHandler();
		bean.setCookieDomain(TribefireRuntime.getCookieDomain());
		bean.setCookiePath(TribefireRuntime.getCookiePath());
		bean.setCookieHttpOnly(TribefireRuntime.getCookieHttpOnly()); // DEVCX-208: The Control Center cannot access the cookie anymore if this is true
		bean.setAddCookie(TribefireRuntime.getCookieEnabled());
		return bean;
	}
}
