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
package com.braintribe.gwt.gme.constellation.client.expert;

import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.LoaderChainImpl;
import com.braintribe.gwt.async.client.TextLoader;
import com.braintribe.gwt.genericmodel.client.codec.dom4.GmXmlCodec;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.packaging.Packaging;


/**
 * Provider responsible for providing the {@link Packaging} instance from the xml.
 * @author michel.docouto
 *
 */
public class PackagingProvider implements Supplier<Future<Packaging>> {
	
	private Future<Packaging> packagingFuture;
	private String packagingXmlUrl;
	private String defaultPackagingXmlUrl = "packaging.xml";
	private String clientUrl;
	
	/**
	 * Configures the URL of the packaging.xml.
	 * Defaults to "./packaging.xml".
	 */
	@Configurable
	public void setPackagingXmlUrl(String packagingXmlUrl) {
		this.packagingXmlUrl = packagingXmlUrl;
	}

	/**
	 * Configures the client Url to be used in the default Url formation, when no URL is set via {@link #setPackagingXmlUrl(String)}.
	 */
	@Configurable
	public void setClientUrl(String clientUrl) {
		if (!clientUrl.endsWith("/"))
			clientUrl += "/";
		this.clientUrl = clientUrl;
	}

	@Override
	public Future<Packaging> get() throws RuntimeException {
		if (packagingFuture == null) {
			if (packagingXmlUrl == null)
				packagingXmlUrl = clientUrl == null ? defaultPackagingXmlUrl : clientUrl + defaultPackagingXmlUrl;
			
			packagingFuture = LoaderChainImpl
					.begin(new TextLoader(packagingXmlUrl))
					.decode(new GmXmlCodec<Packaging>())
					.load();
		}
		
		return packagingFuture;
	}

}
