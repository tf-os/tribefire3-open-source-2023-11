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
package tribefire.extension.ldap.templates.api;

import java.util.Set;

import tribefire.extension.templates.api.TemplateContextBuilder;

public interface LdapTemplateContextBuilder extends TemplateContextBuilder<LdapTemplateContext> {

	LdapTemplateContextBuilder setConnectionUrl(String connectionUrl);
	LdapTemplateContextBuilder setUsername(String username);
	LdapTemplateContextBuilder setPassword(String password);

	LdapTemplateContextBuilder setGroupBase(String groupBase);
	LdapTemplateContextBuilder setUserBase(String userBase);

	LdapTemplateContextBuilder setGroupIdAttribute(String groupIdAttribute);
	LdapTemplateContextBuilder setGroupMemberAttribute(String groupMemberAttribute);
	LdapTemplateContextBuilder setGroupNameAttribute(String groupNameAttribute);
	LdapTemplateContextBuilder setGroupsAreRoles(boolean groupsAreRoles);

	LdapTemplateContextBuilder setMemberAttribute(String memberAttribute);
	LdapTemplateContextBuilder setGroupObjectClasses(Set<String> groupObjectClasses);

	LdapTemplateContextBuilder setRoleIdAttribute(String roleIdAttribute);
	LdapTemplateContextBuilder setRoleNameAttribute(String roleNameAttribute);

	LdapTemplateContextBuilder setUserIdAttribute(String userIdAttribute);
	LdapTemplateContextBuilder setUserFirstNameAttribute(String userFirstNameAttribute);
	LdapTemplateContextBuilder setUserLastNameAttribute(String userLastNameAttribute);
	LdapTemplateContextBuilder setUserUsernameAttribute(String userUsernameAttribute);
	LdapTemplateContextBuilder setUserDisplayNameAttribute(String userDisplayNameAttribute);
	LdapTemplateContextBuilder setEmailAttribute(String emailAttribute);

	LdapTemplateContextBuilder setUserFilter(String userFilter);

	LdapTemplateContextBuilder setLastLogonAttribute(String lastLogonAttribute);
	LdapTemplateContextBuilder setMemberOfAttribute(String memberOfAttribute);
	LdapTemplateContextBuilder setUserObjectClasses(Set<String> userObjectClasses);

	LdapTemplateContextBuilder setSearchPageSize(int searchPageSize);

	LdapTemplateContextBuilder setReferralFollow(boolean referralFollow);
	LdapTemplateContextBuilder setConnectTimeout(long connectTimeout);
	LdapTemplateContextBuilder setDnsTimeout(long dnsTimeout);
	LdapTemplateContextBuilder setDnsRetries(int dnsRetries);

	LdapTemplateContextBuilder setBase(String base);
	LdapTemplateContextBuilder setUseEmptyAspects(boolean useEmptyAspects);

	LdapTemplateContextBuilder setUseTlsExtension(boolean useTlsExtension);

}