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
package tribefire.extension.demo.model.deployment;

import com.braintribe.model.extensiondeployment.access.AccessRequestProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * The denotation type representing the actual processor implementation. The
 * properties configured here will be transfered to the NewEmployeeProcessor
 * implementation during deployment.
 */

public interface NewEmployeeProcessor extends AccessRequestProcessor {
	
	EntityType<NewEmployeeProcessor> T = EntityTypes.T(NewEmployeeProcessor.class);

	/**
	 * The message that will be added as a comment to the new employee. 
	 */
	void setWelcomeMessage(String welcomeMessage);
	String getWelcomeMessage();
	
	/**
	 * The message that will be added to the manager of the department the employee just joined. 
	 */
	void setManagerMessage(String managerMessage);
	String getManagerMessage();
}
