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
package com.braintribe.model.ldapaccessdeployment;

import java.util.List;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.ldapconnectiondeployment.LdapConnection;


public interface LdapUserAccess extends IncrementalAccess {

	final EntityType<LdapUserAccess> T = EntityTypes.T(LdapUserAccess.class);
	
	void setGroupBase(String groupBase);
	@Initializer("'OU=Groups,OU=<Organization>,DC=<Company>'")
	String getGroupBase();
	
	void setGroupIdAttribute(String groupIdAttribute);
	@Initializer("'distinguishedName'")
	String getGroupIdAttribute();
	
	void setGroupMemberAttribute(String groupMemberAttribute);
	@Initializer("'member'")
	String getGroupMemberAttribute();
	
	void setGroupNameAttribute(String groupNameAttribute);
	@Initializer("'name'")
	String getGroupNameAttribute();
	
	void setGroupObjectClasses(List<String> groupObjectClasses);
	@Initializer("['group']")
	List<String> getGroupObjectClasses();
	
	void setGroupsAreRoles(boolean groupsAreRoles);
	@Initializer("true")
	boolean getGroupsAreRoles();
	
	void setLdapConnection(LdapConnection ldapConnection);
	LdapConnection getLdapConnection();
	
	void setMemberAttribute(String memberAttribute);
	@Initializer("'memberOf'")
	String getMemberAttribute();
	
	void setRoleIdAttribute(String roleIdAttribute);
	@Initializer("'distinguishedName'")
	String getRoleIdAttribute();
	
	void setRoleNameAttribute(String roleNameAttribute);
	@Initializer("'name'")
	String getRoleNameAttribute();
	
	void setUserBase(String userBase);
	@Initializer("'OU=Accounts,OU=<Organization>,DC=<Company>'")
	String getUserBase();
	
	void setUserDescriptionAttribute(String userDescriptionAttribute);
	@Initializer("'displayName'")
	String getUserDescriptionAttribute();
	
	void setUserEmailAttribute(String userEmailAttribute);
	@Initializer("'mail'")
	String getUserEmailAttribute();
	
	void setUserFilter(String userFilter);
	@Initializer("'(sAMAccountName=%s)'")
	String getUserFilter();
	
	void setUserFirstNameAttribute(String userFirstNameAttribute);
	@Initializer("'givenName'")
	String getUserFirstNameAttribute();
	
	void setUserIdAttribute(String userIdAttribute);
	@Initializer("'distinguishedName'")
	String getUserIdAttribute();
	
	void setUserLastLoginAttribute(String userLastLoginAttribute);
	@Initializer("'lastLogon'")
	String getUserLastLoginAttribute();
	
	void setUserLastNameAttribute(String userLastNameAttribute);
	@Initializer("'sn'")
	String getUserLastNameAttribute();
	
	void setUserMemberOfAttribute(String userMemberOfAttribute);
	@Initializer("'memberOf'")
	String getUserMemberOfAttribute();
	
	void setUserNameAttribute(String userNameAttribute);
	@Initializer("'sAMAccountName'")
	String getUserNameAttribute();
	
	void setUserObjectClasses(List<String> userObjectClasses);
	@Initializer("['user']")
	List<String> getUserObjectClasses();

	void setSearchPageSize(int searchPageSize);
	@Initializer("20")
	int getSearchPageSize();
}
