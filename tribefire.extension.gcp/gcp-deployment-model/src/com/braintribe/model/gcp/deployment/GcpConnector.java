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
package com.braintribe.model.gcp.deployment;

import com.braintribe.model.deployment.connector.Connector;
import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.annotation.meta.MaxLength;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface GcpConnector extends Connector, HasName {

	final EntityType<GcpConnector> T = EntityTypes.T(GcpConnector.class);

	final static String jsonCredentials = "jsonCredentials";
	
	final static String privateKeyId = "privateKeyId";
	final static String privateKey = "privateKey";
	final static String clientId = "clientId";
	final static String clientEmail = "clientEmail";
	final static String tokenServerUri = "tokenServerUri";
	final static String projectId = "projectId";

	@MaxLength(4096)
	String getJsonCredentials();
	void setJsonCredentials(String jsonCredentials);

	@MaxLength(2048)
	String getPrivateKeyId();
	void setPrivateKeyId(String privateKeyId);

	@MaxLength(2048)
	String getPrivateKey();
	void setPrivateKey(String privateKey);

	String getClientId();
	void setClientId(String clientId);

	String getClientEmail();
	void setClientEmail(String clientEmail);

	String getTokenServerUri();
	void setTokenServerUri(String tokenServerUri);

	String getProjectId();
	void setProjectId(String projectId);

}
