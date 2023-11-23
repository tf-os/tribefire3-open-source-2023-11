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
package com.braintribe.model.processing.misc.model;

import java.util.List;

import com.braintribe.model.common.IdentifiableEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.misc.NamableEntity;

/**
 * A Dummy class used for all MPC testing
 * 
 */

public interface Person extends IdentifiableEntity, NamableEntity, Comparable<Person> {

	EntityType<Person> T = EntityTypes.T(Person.class);
	
	// @formatter:off
	List<String> getFavouriteColours();
	void setFavouriteColours(List<String> favouriteColours);

	Person getDescendant();
	void setDescendant(Person descendant);
	// @formatter:on

	public static class DefaultMethods {
		public static int compareTo(Person me, Object o) {
			return me.getName().compareTo(((Person)o).getName());
		}
	}
}
