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
package tribefire.extension.ldap.initializer.wire.contract;

import com.braintribe.wire.api.annotation.Decrypt;
import com.braintribe.wire.api.annotation.Default;

import tribefire.cortex.initializer.support.wire.contract.PropertyLookupContract;

public interface RuntimePropertiesContract extends PropertyLookupContract {

	@Default("false")
	boolean LDAP_INITIALIZE_DEFAULTS();

	String LDAP_CONN_URL(String defaultValue);
	String LDAP_CONN_USERNAME();
	@Decrypt
	String LDAP_CONN_PASSWORD_ENCRYPTED();

	@Default("OU=Groups,OU=<Organization>,DC=<Company>")
	String LDAP_BASE_GROUPS();
	@Default("OU=Accounts,OU=<Organization>,DC=<Company>")
	String LDAP_BASE_USERS();

	@Default("distinguishedName")
	String LDAP_GROUP_ID();
	@Default("member")
	String LDAP_GROUP_MEMBER();
	@Default("name")
	String LDAP_GROUP_NAME();
	@Default("true")
	boolean LDAP_GROUPS_ARE_ROLES();
	@Default("memberOf")
	String LDAP_MEMBER_ATTRIBUTE();
	@Default("group")
	String LDAP_GROUP_OBJECT_CLASSES();

	@Default("distinguishedName")
	String LDAP_ROLE_ID();
	@Default("name")
	String LDAP_ROLE_NAME();

	@Default("distinguishedName")
	String LDAP_USER_ID();
	@Default("givenName")
	String LDAP_USER_FIRSTNAME();
	@Default("sn")
	String LDAP_USER_LASTNAME();
	@Default("sAMAccountName")
	String LDAP_USER_NAME();
	@Default("displayName")
	String LDAP_USER_DESCRIPTION();
	@Default("mail")
	String LDAP_USER_MAIL();
	@Default("(sAMAccountName=%s)")
	String LDAP_USER_FILTER();
	@Default("lastLogon")
	String LDAP_USER_LASTLOGON();
	@Default("memberOf")
	String LDAP_USER_MEMBER_OF();
	@Default("user")
	String LDAP_USER_OBJECT_CLASSES();

	@Default("20")
	int LDAP_SEARCH_PAGESIZE();

	@Default("false")
	boolean LDAP_REFERRAL_FOLLOW();
	@Default("30000")
	long LDAP_CONNECT_TIMEOUT();
	@Default("10000")
	long LDAP_DNS_TIMEOUT_INITIAL();
	@Default("3")
	int LDAP_DNS_TIMEOUT_RETRIES();

	@Default("OU=<Organization>,DC=<Company>")
	String LDAP_BASE();

	@Default("false")
	boolean LDAP_USE_EMPTY_ASPECTS();

	@Default("false")
	boolean LDAP_ATTACH_TO_CORTEXCONFIGURATION();

}
