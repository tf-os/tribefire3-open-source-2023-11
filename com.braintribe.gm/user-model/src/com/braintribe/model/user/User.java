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
package com.braintribe.model.user;

import java.util.Date;
import java.util.Set;

import com.braintribe.model.descriptive.HasPassword;
import com.braintribe.model.generic.annotation.meta.Unique;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author gunther.schenk
 */

public interface User extends Identity, HasPassword {

	final EntityType<User> T = EntityTypes.T(User.class);

	public static final String firstName = "firstName";
	public static final String lastName = "lastName";
	public static final String groups = "groups";
	public static final String lastLogin = "lastLogin";
	public static final String password = "password";

	// **************************************************************************
	// Getter/Setter
	// **************************************************************************
	
	@Override
	@Unique
	String getName();

	public String getFirstName();
	public void setFirstName(String firstName);

	public String getLastName();
	public void setLastName(String lastName);
	
	public void setGroups(Set<Group> groups);
	public Set<Group> getGroups();
	
	public Date getLastLogin();
	public void setLastLogin(Date lastLogin);

	@Override
	default String roleName() {
		return "$user-"+getName();
	}
}
