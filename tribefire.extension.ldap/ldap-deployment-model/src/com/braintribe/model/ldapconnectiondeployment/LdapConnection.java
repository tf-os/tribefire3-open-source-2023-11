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
package com.braintribe.model.ldapconnectiondeployment;

import java.util.Map;

import com.braintribe.model.deployment.connector.Connector;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface LdapConnection extends Connector {

	final EntityType<LdapConnection> T = EntityTypes.T(LdapConnection.class);
	
	String connectionUrl = "connectionUrl";
	String username = "username";
	String password = "password";
	String initialContextFactory = "initialContextFactory";
	String referralFollow = "referralFollow";
	String useTLSExtension = "useTLSExtension";
	String connectTimeout = "connectTimeout";
	String dnsTimeoutInitial = "dnsTimeoutInitial";
	String dnsTimeoutRetries = "dnsTimeoutRetries";
	String environmentSettings = "environmentSettings";
	
	void setConnectionUrl(String connectionUrl);
	@Initializer("'ldap://<host>:389'")
	String getConnectionUrl();

	void setUsername(String username);
	String getUsername();

	void setPassword(String password);
	String getPassword();

	void setInitialContextFactory(String initialContextFactory);
	@Initializer("'com.sun.jndi.ldap.LdapCtxFactory'")
	String getInitialContextFactory();

	void setReferralFollow(boolean referralFollow);
	@Initializer("false")
	boolean getReferralFollow();

	void setUseTLSExtension(boolean useTLSExtension);
	boolean getUseTLSExtension();

	void setConnectTimeout(long connectTimeout);
	@Initializer("30000L")
	long getConnectTimeout();

	void setDnsTimeoutInitial(long dnsTimeoutInitial);
	@Initializer("10000L")
	long getDnsTimeoutInitial();

	void setDnsTimeoutRetries(int dnsTimeoutRetries);
	@Initializer("3")
	int getDnsTimeoutRetries();

	void setEnvironmentSettings(Map<String,String> environmentSettings);
	Map<String,String> getEnvironmentSettings();

}
