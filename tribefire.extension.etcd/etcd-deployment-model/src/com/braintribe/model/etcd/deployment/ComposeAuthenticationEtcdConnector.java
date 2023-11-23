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
package com.braintribe.model.etcd.deployment;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * 
 * {@link EtcdConnector} including capabilities for https://compose.io managed etcd
 * 
 */
@SelectiveInformation("Compose.io Authentication Etcd Connector: ${name}")
public interface ComposeAuthenticationEtcdConnector extends EtcdConnector {

	final EntityType<ComposeAuthenticationEtcdConnector> T = EntityTypes.T(ComposeAuthenticationEtcdConnector.class);

	public final static String authority = "authority";
	public final static String authorityPrefix = "authorityPrefix";
	public final static String certificate = "certificate";

	@Mandatory
	String getAuthority();
	void setAuthority(String authority);

	// prefix for authority - used because a wildcard is not working - should define the authority more in detail; in
	// general it can be anything - not sure how it will behave in the the future so let this configurable.
	// e.g. TCP portals 'portal2335-0.astute-etcd-58.2678148991.composedb.com:19714' become
	// 'xxx.astute-etcd-58.2678148991.composedb.com'
	@Mandatory
	@Initializer("'xxx.'")
	String getAuthorityPrefix();
	void setAuthorityPrefix(String authorityPrefix);

	@Mandatory
	String getCertificate();
	void setCertificate(String certificate);
}
