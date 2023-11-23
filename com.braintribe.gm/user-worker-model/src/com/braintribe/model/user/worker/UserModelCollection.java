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
package com.braintribe.model.user.worker;

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.user.Identity;
import com.braintribe.model.user.Role;

public interface UserModelCollection extends GenericEntity {

	final EntityType<UserModelCollection> T = EntityTypes.T(UserModelCollection.class);

	public static final String identities = "identities";
	public static final String roles = "roles";

	// **************************************************************************
	// Getter/Setter
	// **************************************************************************

	public List<Identity> getIdentities();
	public void setIdentities(List<Identity> identities);

	public Set<Role> getRoles();
	public void setRoles(Set<Role> roles);

}
