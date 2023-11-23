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

import java.util.HashMap;
import java.util.Map;

import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.ve.impl.ContextualizedVirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * <p>
 * VirtualEnvironment allows to access {@link System#getProperty(String) system properties} and {@link System#getenv() environment variables} via this interface instead of direct access
 * to static methods offered by the JDK. This is neccessary to make the properties and environment variable more agile and different in various parts of a running program.
 * 
 * <p>
 * The following fundamental implementations offer important features that found on the virtualization of the primary static access
 * 
 * <ul>
 * 	<li>direct access to the static methods: {@link StandardEnvironment}
 *  <li>overriding a parent virtual environment: {@link OverridingEnvironment}
 *  <li>accessibility via virtual environment via {@link AttributeContexts} and the type safe attribute {@link VirtualEnvironmentAttribute} and the convenience proxy {@link ContextualizedVirtualEnvironment}
 * </ul>
 * @author Dirk Scheffler
 *
 */
public interface VirtualEnvironment {
	/**
	 * Accesses a virtualized system property by its name
	 * @param name the name of the system property
	 * @return the value of the system property
	 */
	String getProperty(String name);
	
	/**
	 * Accesses a virtualized environment variable by its name
	 * @param name the name of the environment variable
	 * @return the value of the environment variable
	 */
	String getEnv(String name);
	
	
	/**
	 * @return - a {@link Map} of all currently overriding environment variables and their values
	 */
	default Map<String,String> getEnvironmentOverrrides() { return new HashMap<>();}
	
	/**
	 * @return - a {@link Map} of all currently overriding system properties and their values
	 */
	default Map<String,String> getPropertyOverrrides() { return new HashMap<>();}
}
