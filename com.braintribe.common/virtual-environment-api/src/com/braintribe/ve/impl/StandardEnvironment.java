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
package com.braintribe.ve.impl;

import com.braintribe.ve.api.VirtualEnvironment;

/**
 * This {@link VirtualEnvironment} implementation directly passes the access to system properties to {@link System#getProperty(String)} and the access to environment variables to {@link System#getenv(String)}.
 * @author Dirk Scheffler
 *
 */
public class StandardEnvironment implements VirtualEnvironment {
	public static final StandardEnvironment INSTANCE = new StandardEnvironment();
	@Override
	public String getEnv(String name) {
		return System.getenv(name);
	}
	
	@Override
	public String getProperty(String name) {
		return System.getProperty(name);
	}

}
