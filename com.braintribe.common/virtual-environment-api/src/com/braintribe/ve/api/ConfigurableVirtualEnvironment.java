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
package com.braintribe.ve.api;

import java.util.Map;

/**
 * An interface that allows to configure a {@link VirtualEnvironment}
 * @author Dirk Scheffler
 */
public interface ConfigurableVirtualEnvironment extends VirtualEnvironment {

	/**
	 * Configures a single virtual environment variable.
	 * @param name the name of the environment variable
	 * @param value the value of the environment variable
	 */
	void setEnv(String name, String value);

	/**
	 * Configures a number of virtual environment variables
	 * @param env a map mapping from name to value of the environment variables
	 */
	void setEnvs(Map<String, String> env);

	
	/**
	 * Configures a single virtual system property.
	 * @param name the name of the system property
	 * @param value the value of the system property
	 */
	void setProperty(String name, String value);

	/**
	 * Configures a number of virtual system properties
	 * @param env a map mapping from name to value of the system properties
	 */
	void setProperties(Map<String, String> properties);
}
