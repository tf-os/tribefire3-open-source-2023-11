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
/**
 * This artifact provides two different types of Accesses that are able to connect to an LDAP-compatible
 * directory service (e.g., Active Directory).
 * <br><br>
 * <ul>
 *  <li><code>{@link com.braintribe.model.access.impl.LdapAccess}</code>: A general purpose Access that allows to access arbitrary objects in a directory service. 
 *     It requires a model and corresponding meta-data that allows to map entity types to LDAP classes.</li>
 *  <li><code>{@link com.braintribe.model.access.impl.LdapUserAccess}</code>: A specialized LDAP Access that can be used in conjunction with the <code>UserModel</code>. 
 *     It allows to query users and groups and also supports user authentication.</li>
 * </ul>
 */
package com.braintribe.model.access.impl;
