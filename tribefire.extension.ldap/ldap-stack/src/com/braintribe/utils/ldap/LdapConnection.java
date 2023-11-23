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
package com.braintribe.utils.ldap;

import javax.naming.ldap.LdapContext;

/**
 * <p>Interface describing the methods necessary for an LDAP connection stack.
 * Whenevery a piece of software needs an LDAP connection, the {@link #pop()} can
 * be used to get an {@link LdapContext} object. When the context is no longer needed,
 * it should be returned to the stack by calling the {@link #push(LdapContext)} method.</p>
 * <p>In case the code has to use different code when dealing with an Active Directory server 
 * (the most prominent use case when dealing with LDAP), it can detect the presence of an AD server
 * by calling the {@link #isActiveDirectory()} method.
 */
public interface LdapConnection {
	
	void push(LdapContext item);
	LdapContext pop() throws Exception;

	boolean isActiveDirectory();
}
