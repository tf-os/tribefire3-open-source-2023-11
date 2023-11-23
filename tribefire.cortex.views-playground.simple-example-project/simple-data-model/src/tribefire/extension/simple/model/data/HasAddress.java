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
package tribefire.extension.simple.model.data;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Super type for entities that have an {@link Address}.<br>
 * Note that this type is {@link Abstract}, which means it cannot be instantiated.
 */
@Abstract
public interface HasAddress extends GenericEntity {

	// Constant to conveniently access the entity type.
	EntityType<HasAddress> T = EntityTypes.T(HasAddress.class);

	/* Constants which provide convenient access to all property names, which is e.g. useful for queries. */
	String address = "address";

	/**
	 * A reference to the {@link Address} instance.
	 */
	Address getAddress();
	void setAddress(Address address);

}
