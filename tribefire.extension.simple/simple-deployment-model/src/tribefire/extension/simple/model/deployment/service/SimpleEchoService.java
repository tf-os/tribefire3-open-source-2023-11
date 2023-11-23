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
package tribefire.extension.simple.model.deployment.service;

import com.braintribe.model.extensiondeployment.access.AccessRequestProcessor;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * The <code>SimpleEchoService</code> is a simple service that receives a message via a <code>SimpleEchoRequest</code> and echoes them by returning a
 * <code>SimpleEchoResponse</code>.<br>
 * This is just a very simple example for a (DDSA) service.
 * <p>
 * This is the denotation type that represents the respective <code>SimpleEchoService</code> implementation. For more information on denotation types
 * see {@link tribefire.extension.simple.model.deployment.access.SimpleInMemoryAccess}.
 *
 * @author michael.lafite
 */
public interface SimpleEchoService extends AccessRequestProcessor {

	EntityType<SimpleEchoService> T = EntityTypes.T(SimpleEchoService.class);

	/**
	 * An (optional) delay in milliseconds.
	 */
	Long getDelay();
	void setDelay(Long delay);

	/**
	 * Specifies how many times the message will be echoed. Default is <code>1</code>.
	 */
	@Initializer("1")
	Integer getEchoCount();
	void setEchoCount(Integer echoCount);
}
