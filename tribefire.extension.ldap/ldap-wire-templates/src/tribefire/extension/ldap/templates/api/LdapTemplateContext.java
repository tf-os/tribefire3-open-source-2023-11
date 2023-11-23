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

import tribefire.extension.templates.api.TemplateContext;

public interface LdapTemplateContext extends TemplateContext {

	static LdapTemplateContextBuilder builder() {
		return new LdapTemplateContextImpl();
	}

	String getConnectionUrl();
	String getUsername();
	String getPassword();

	String getGroupBase();
	String getUserBase();

	String getGroupIdAttribute();
	String getGroupMemberAttribute();
	String getGroupNameAttribute();
	boolean getGroupsAreRoles();

	String getMemberAttribute();
	Set<String> getGroupObjectClasses();

	String getRoleIdAttribute();
	String getRoleNameAttribute();

	String getUserIdAttribute();
	String getUserFirstNameAttribute();
	String getUserLastNameAttribute();
	String getUserUsernameAttribute();
	String getUserDisplayNameAttribute();
	String getEmailAttribute();

	String getUserFilter();

	String getLastLogonAttribute();
	String getMemberOfAttribute();
	Set<String> getUserObjectClasses();

	int getSearchPageSize();

	boolean getReferralFollow();
	long getConnectTimeout();
	long getDnsTimeout();
	int getDnsRetries();

	String getBase();
	boolean getUseEmptyAspects();

	boolean getUseTlsExtension();

}