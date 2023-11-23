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

import java.util.Set;

import com.braintribe.model.generic.annotation.meta.Unique;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author gunther.schenk
 *
 */
public interface Group extends Identity {

	final EntityType<Group> T = EntityTypes.T(Group.class);
	
	public static final String localizedName = "localizedName";
	public static final String users = "users";
	public static final String conflictPriority = "conflictPriority";
	
	@Override
	@Unique
	String getName();

	/**
	 * @param localizedName the localizedName to set
	 */
	public void setLocalizedName(LocalizedString localizedName);
	/**
	 * @return the localizedName
	 */
	public LocalizedString getLocalizedName();
	
	/**
	 * @param users the users to set
	 */
	public void setUsers(Set<User> users);
	/**
	 * @return the users
	 */
	public Set<User> getUsers();
	
	/**
	 * @param conflictPriority the conflictPriority to set
	 */
	public void setConflictPriority(double conflictPriority);
	/**
	 * @return the conflictPriority
	 */
	public double getConflictPriority();
	
	@Override
	default String roleName() {
		return "$group-"+getName();
	}


}
