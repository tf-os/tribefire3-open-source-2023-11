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

import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.utils.lcd.CollectionTools2;

import tribefire.extension.templates.api.TemplateContextImpl;

public class LdapTemplateContextImpl extends TemplateContextImpl<LdapTemplateContext> implements LdapTemplateContext, LdapTemplateContextBuilder {

	private static final Logger logger = Logger.getLogger(LdapTemplateContextImpl.class);

	private String connectionUrl = "ldap://<host>:389";
	private String username;
	private String password;
	private String groupBase = "OU=Groups,OU=<Organization>,DC=<Company>";
	private String userBase = "OU=Accounts,OU=<Organization>,DC=<Company>";
	private String groupIdAttribute = "distinguishedName";
	private String groupMemberAttribute = "member";
	private String groupNameAttribute = "name";
	private boolean groupsAreRoles = true;
	private String memberAttribute = "memberOf";
	private Set<String> groupObjectClasses = CollectionTools2.asSet("group");
	private String roleIdAttribute = "distinguishedName";
	private String roleNameAttribute = "name";
	private String userIdAttribute = "distinguishedName";
	private String userFirstNameAttribute = "givenName";
	private String userLastNameAttribute = "sn";
	private String userUsernameAttribute = "sAMAccountName";
	private String userDisplayNameAttribute = "displayName";
	private String emailAttribute = "mail";
	private String userFilter = "(sAMAccountName=%s)";
	private String lastLogonAttribute = "lastLogon";
	private String memberOfAttribute = "memberOf";
	private Set<String> userObjectClasses = CollectionTools2.asSet("user");
	private int searchPageSize = 20;
	private boolean referralFollow = false;
	private long connectTimeout = Numbers.MILLISECONDS_PER_SECOND * 30;
	private long dnsTimeout = Numbers.MILLISECONDS_PER_SECOND * 10;
	private int dnsRetries = 3;
	private String base = "OU=<Organization>,DC=<Company>";
	private boolean useEmptyAspects = false;
	private boolean useTlsExtension = false;

	@Override
	public LdapTemplateContextBuilder setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setUsername(String username) {
		this.username = username;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setPassword(String password) {
		this.password = password;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setGroupBase(String groupBase) {
		this.groupBase = groupBase;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setUserBase(String userBase) {
		this.userBase = userBase;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setGroupIdAttribute(String groupIdAttribute) {
		this.groupIdAttribute = groupIdAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setGroupMemberAttribute(String groupMemberAttribute) {
		this.groupMemberAttribute = groupMemberAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setGroupNameAttribute(String groupNameAttribute) {
		this.groupNameAttribute = groupNameAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setGroupsAreRoles(boolean groupsAreRoles) {
		this.groupsAreRoles = groupsAreRoles;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setMemberAttribute(String memberAttribute) {
		this.memberAttribute = memberAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setGroupObjectClasses(Set<String> groupObjectClasses) {
		this.groupObjectClasses = groupObjectClasses;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setRoleIdAttribute(String roleIdAttribute) {
		this.roleIdAttribute = roleIdAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setRoleNameAttribute(String roleNameAttribute) {
		this.roleNameAttribute = roleNameAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setUserIdAttribute(String userIdAttribute) {
		this.userIdAttribute = userIdAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setUserFirstNameAttribute(String userFirstNameAttribute) {
		this.userFirstNameAttribute = userFirstNameAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setUserLastNameAttribute(String userLastNameAttribute) {
		this.userLastNameAttribute = userLastNameAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setUserUsernameAttribute(String userUsernameAttribute) {
		this.userUsernameAttribute = userUsernameAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setUserDisplayNameAttribute(String userDisplayNameAttribute) {
		this.userDisplayNameAttribute = userDisplayNameAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setEmailAttribute(String emailAttribute) {
		this.emailAttribute = emailAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setUserFilter(String userFilter) {
		this.userFilter = userFilter;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setLastLogonAttribute(String lastLogonAttribute) {
		this.lastLogonAttribute = lastLogonAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setMemberOfAttribute(String memberOfAttribute) {
		this.memberOfAttribute = memberOfAttribute;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setUserObjectClasses(Set<String> userObjectClasses) {
		this.userObjectClasses = userObjectClasses;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setSearchPageSize(int searchPageSize) {
		this.searchPageSize = searchPageSize;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setReferralFollow(boolean referralFollow) {
		this.referralFollow = referralFollow;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setConnectTimeout(long connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setDnsTimeout(long dnsTimeout) {
		this.dnsTimeout = dnsTimeout;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setDnsRetries(int dnsRetries) {
		this.dnsRetries = dnsRetries;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setBase(String base) {
		this.base = base;
		return this;
	}

	@Override
	public LdapTemplateContextBuilder setUseEmptyAspects(boolean useEmptyAspects) {
		this.useEmptyAspects = useEmptyAspects;
		return this;
	}

	@Override
	public String getConnectionUrl() {
		return connectionUrl;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getGroupBase() {
		return groupBase;
	}

	@Override
	public String getUserBase() {
		return userBase;
	}

	@Override
	public String getGroupIdAttribute() {
		return groupIdAttribute;
	}

	@Override
	public String getGroupMemberAttribute() {
		return groupMemberAttribute;
	}

	@Override
	public String getGroupNameAttribute() {
		return groupNameAttribute;
	}

	@Override
	public boolean getGroupsAreRoles() {
		return groupsAreRoles;
	}

	@Override
	public String getMemberAttribute() {
		return memberAttribute;
	}

	@Override
	public Set<String> getGroupObjectClasses() {
		return groupObjectClasses;
	}

	@Override
	public String getRoleIdAttribute() {
		return roleIdAttribute;
	}

	@Override
	public String getRoleNameAttribute() {
		return roleNameAttribute;
	}

	@Override
	public String getUserIdAttribute() {
		return userIdAttribute;
	}

	@Override
	public String getUserFirstNameAttribute() {
		return userFirstNameAttribute;
	}

	@Override
	public String getUserLastNameAttribute() {
		return userLastNameAttribute;
	}

	@Override
	public String getUserUsernameAttribute() {
		return userUsernameAttribute;
	}

	@Override
	public String getUserDisplayNameAttribute() {
		return userDisplayNameAttribute;
	}

	@Override
	public String getEmailAttribute() {
		return emailAttribute;
	}

	@Override
	public String getUserFilter() {
		return userFilter;
	}

	@Override
	public String getLastLogonAttribute() {
		return lastLogonAttribute;
	}

	@Override
	public String getMemberOfAttribute() {
		return memberOfAttribute;
	}

	@Override
	public Set<String> getUserObjectClasses() {
		return userObjectClasses;
	}

	@Override
	public int getSearchPageSize() {
		return searchPageSize;
	}

	@Override
	public boolean getReferralFollow() {
		return referralFollow;
	}

	@Override
	public long getConnectTimeout() {
		return connectTimeout;
	}

	@Override
	public long getDnsTimeout() {
		return dnsTimeout;
	}

	@Override
	public int getDnsRetries() {
		return dnsRetries;
	}

	@Override
	public String getBase() {
		return base;
	}

	@Override
	public boolean getUseEmptyAspects() {
		return useEmptyAspects;
	}

	@Override
	public LdapTemplateContextBuilder setUseTlsExtension(boolean useTlsExtension) {
		this.useTlsExtension = useTlsExtension;
		return this;
	}

	@Override
	public boolean getUseTlsExtension() {
		return useTlsExtension;
	}

}
