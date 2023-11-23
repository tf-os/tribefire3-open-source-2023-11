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
package com.braintribe.model.processing.bootstrapping;

public class TribefireRuntimeDeprecation {

	@Deprecated
	public static final String ENVIRONMENT_LOCAL_BASE_URL = "TRIBEFIRE_LOCAL_BASE_URL";
	@Deprecated
	public static final String ENVIRONMENT_SERVICES_PATH = "TRIBEFIRE_SERVICES_PATH";
	@Deprecated
	public static final String ENVIRONMENT_SERVICES_HOST = "TRIBEFIRE_SERVICES_HOST";
	@Deprecated
	public static final String ENVIRONMENT_DEFAULT_SCHEMA = "TRIBEFIRE_DEFAULT_SCHEMA";

	
	@Deprecated
	public static final String ENVIRONMENT_REPOSITORY_URL = "TRIBEFIRE_REPOSITORY_URL";
	@Deprecated
	public static final String ENVIRONMENT_JAVADOC_URL = "TRIBEFIRE_JAVADOC_URL";
	@Deprecated
	public static final String ENVIRONMENT_MANAGER_URL = "TRIBEFIRE_MANAGER_URL";
	@Deprecated
	public static final String ENVIRONMENT_MODELBROWSER_URL = "TRIBEFIRE_MODELBROWSER_URL";
	
	// Web-related settings
	@Deprecated
	public static final String ENVIRONMENT_WEB_LOGIN_URL = "TRIBEFIRE_WEB_LOGIN_URL";


	/**
	 * @deprecated This property was deprecated and will soon be removed. In tribefire 2.0, trusted credentials are only
	 *             accepted when trusted communication channels are used, whether the request is associated with a
	 *             localhost ip or not doesn't play a role.
	 */
	@Deprecated
	public static final String ENVIRONMENT_TRUST_LEVEL = "TRIBEFIRE_TRUST_LEVEL";
	
	
	
	
	/**
	 * @deprecated This property was deprecated and will soon be removed. In tribefire 2.0, trusted credentials are only
	 *             accepted when trusted communication channels are used, whether the request is associated with a
	 *             localhost ip or not doesn't play a role.
	 */
	@Deprecated
	public static String getTrustLevel() {
		return getProperty(ENVIRONMENT_TRUST_LEVEL);
	}

	
	@Deprecated
	public static String getWebLoginUrl() {
		return getProperty(ENVIRONMENT_WEB_LOGIN_URL);
	}

	@Deprecated
	public static String getLocalBaseUrl() {
		return getProperty(ENVIRONMENT_LOCAL_BASE_URL);
	}

	@Deprecated
	public static String getServicesPath() {
		return getProperty(ENVIRONMENT_SERVICES_PATH);
	}

	@Deprecated
	public static String getServicesHost() {
		return getProperty(ENVIRONMENT_SERVICES_HOST);
	}

	@Deprecated
	public static String getDefaultSchema() {
		return getProperty(ENVIRONMENT_DEFAULT_SCHEMA);
	}
	
	@Deprecated
	public static String getJavaDocUrl() {
		return getProperty(ENVIRONMENT_JAVADOC_URL);
	}

	@Deprecated
	public static String getRepositoryUrl() {
		return getProperty(ENVIRONMENT_REPOSITORY_URL);
	}
	
	@Deprecated
	public static String getManagerUrl() {
		return getProperty(ENVIRONMENT_MANAGER_URL);
	}
	
	@Deprecated
	public static String getModelBrowserUrl() {
		return getProperty(ENVIRONMENT_MODELBROWSER_URL);
	}

	private static String getProperty (String var) {
		return TribefireRuntime.getProperty(var);
	}
}
