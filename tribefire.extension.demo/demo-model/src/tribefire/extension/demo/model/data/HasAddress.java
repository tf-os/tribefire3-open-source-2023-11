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
package tribefire.extension.demo.model.data;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Abstract type that can be sub-classed by entities having an {@link Address}
 * assigned.
 */
@Abstract
public interface HasAddress extends GenericEntity {

	
	final EntityType<HasAddress> T = EntityTypes.T(HasAddress.class);
	
	/*
	 * Constants for each property name.
	 */
	public static final String address = "address";

	/**
	 * The address property enabling references to an instance of
	 * {@link Address}
	 */
	void setAddress(Address address);

	Address getAddress();

}
