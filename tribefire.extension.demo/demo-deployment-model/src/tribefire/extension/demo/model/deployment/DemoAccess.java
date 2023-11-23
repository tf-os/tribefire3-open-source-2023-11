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

import com.braintribe.model.accessdeployment.IncrementalAccess;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * The denotation type for the actual DemoAccess implementation which can be
 * configured and deployed in the ControlCenter. The properties configured here
 * will be transfered to the DemoAccess implementation during deployment.
 */
public interface DemoAccess extends IncrementalAccess {
	
	EntityType<DemoAccess> T = EntityTypes.T(DemoAccess.class);

	/**
	 * If set false the DemoAccess will start with an empty population.
	 */
	@Initializer("true")
	boolean getInitDefaultPopulation();
	void setInitDefaultPopulation(boolean initDefaultPopulation);

}
