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
package com.braintribe.gwt.logging.ui.gxt.client.experts;

import java.util.function.Predicate;

import com.braintribe.gwt.ioc.client.Configurable;

/**
 * This filter will match against server not running exceptions. 
 * @author michel.docouto
 *
 */
public class ServerNotRunningExceptionFilter implements Predicate<Throwable> {

	private String serverNotRunningExceptionString = "java.net.ConnectException";
	
	/**
	 * Configures the String name of the server not running exception. Defaults to "java.net.ConnectException".
	 */
	@Configurable
	public void setServerNotRunningExceptionString(String serverNotRunningExceptionString) {
		this.serverNotRunningExceptionString = serverNotRunningExceptionString;
	}

	@Override
	public boolean test(Throwable exception) {
		if (exception != null && exception.getMessage() != null && exception.getMessage().contains(serverNotRunningExceptionString)) {
			return true;
		}
		return false;
	}

}
