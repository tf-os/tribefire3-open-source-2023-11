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
package com.braintribe.model.deployment;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Represents a remote system, which provides the actual service (i.e. does the work we need) expected from a "remote deployable". A remote deployable
 * is any deployable whose expert does not implement the required service directly, but is just a remote proxy for some external system.
 * <p>
 * The other system might also be example be another tribefire.
 * <p>
 * TODO explain when to use such a heterogeneous architecture.
 * 
 * @author peter.gazdik
 */
@Abstract
public interface Server extends GenericEntity {

    EntityType<Server> T = EntityTypes.T(Server.class);

}