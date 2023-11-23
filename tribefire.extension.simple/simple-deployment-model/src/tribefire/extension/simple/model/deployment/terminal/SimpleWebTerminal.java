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
package tribefire.extension.simple.model.deployment.terminal;

import com.braintribe.model.extensiondeployment.WebTerminal;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * The <code>SimpleWebTerminal</code> is a simple servlet which processes {@code GET} requests and returns a basic HTML page with information about
 * the request.
 * <p>
 * This is the denotation type that represents the respective <code>SimpleWebTerminal</code> implementation. For more information on denotation types
 * see {@link tribefire.extension.simple.model.deployment.access.SimpleInMemoryAccess}.
 *
 * @author michael.lafite
 */
public interface SimpleWebTerminal extends WebTerminal {

	EntityType<SimpleWebTerminal> T = EntityTypes.T(SimpleWebTerminal.class);

	/**
	 * Specifies whether or not to print out the HTTP request headers.
	 */
	boolean getPrintHeaders();
	void setPrintHeaders(boolean value);

	/**
	 * Specifies whether or not to print out the HTTP request parameters.
	 */
	boolean getPrintParameters();
	void setPrintParameters(boolean value);

}
