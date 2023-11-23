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
package com.braintribe.model.uicommand;

import com.braintribe.model.command.Command;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Command which has a {@link Manipulation} to be applied.
 * @author michel.docouto
 *
 */
public interface ApplyManipulation extends Command {
	
	final EntityType<ApplyManipulation> T = EntityTypes.T(ApplyManipulation.class);
	
	/**
	 * Configures the manipulation which will be applied in the place which receives this command.
	 */
	void setManipulation(Manipulation manipulation);
	
	/**
	 * @return - the manipulation to be applied.
	 */
	Manipulation getManipulation();

}
