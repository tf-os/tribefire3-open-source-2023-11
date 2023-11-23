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
package com.braintribe.model.processing.wopi;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.lcd.CommonTools;

/**
 * Utility class related to WOPI connector
 * 
 *
 */
public class WopiConnectorUtil {

	/**
	 * An extension of {@link TribefireRuntime#getPublicServicesUrl()} which returns a outside reachable IP address if
	 * 'localhost' or '127.0.0.1' is configured. Additionally in case {@link TribefireRuntime#getPublicServicesUrl()} is
	 * relative {@link TribefireRuntime#getServicesUrl()} will be used - this is for testing to have a reachable IP
	 * address.
	 * 
	 * @param customPublicServicesUrl
	 *            an optional custom PUBLIC_SERVICES_URL
	 * 
	 * @return Adapted version of {@link TribefireRuntime#ENVIRONMENT_PUBLIC_SERVICES_URL} if 'localhost' or '127.0.0.1'
	 */
	@SuppressWarnings("unused")
	public static String getPublicServicesUrl(String customPublicServicesUrl) {
		if (CommonTools.isEmpty(customPublicServicesUrl)) {
			String publicServicesUrl = TribefireRuntime.getPublicServicesUrl();

			try {
				new URL(publicServicesUrl);
			} catch (MalformedURLException e) {
				publicServicesUrl = TribefireRuntime.getServicesUrl();
			}

			if (StringUtils.containsIgnoreCase(publicServicesUrl, "localhost")) {
				publicServicesUrl = publicServicesUrl.replace("localhost", NetworkTools.getNetworkAddress().getHostAddress());
			}
			if (publicServicesUrl.contains("127.0.0.1")) {
				publicServicesUrl = publicServicesUrl.replace("127.0.0.1", NetworkTools.getNetworkAddress().getHostAddress());
			}

			return publicServicesUrl;
		} else {
			return customPublicServicesUrl;
		}

	}

}
