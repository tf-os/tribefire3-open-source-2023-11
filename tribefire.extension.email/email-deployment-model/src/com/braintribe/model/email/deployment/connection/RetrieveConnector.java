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
package com.braintribe.model.email.deployment.connection;

import com.braintribe.model.descriptive.HasCredentials;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
@SelectiveInformation("Retrieval Connector ${host}")
public interface RetrieveConnector extends EmailConnector, HasCredentials {

	final EntityType<RetrieveConnector> T = EntityTypes.T(RetrieveConnector.class);

	@Name("Host")
	@Description("Hostname or IP address of the mail server.")
	@Mandatory
	String getHost();
	void setHost(String host);

	@Name("Port")
	@Description("The listening port of the mail server.")
	@Mandatory
	Integer getPort();
	void setPort(Integer port);

	@Name("User")
	@Description("The username that should be used for authentication with the mail server.")
	@Override
	@Mandatory
	String getUser();

	@Name("Password")
	@Description("The password that should be used for authentication.")
	@Override
	@Mandatory
	String getPassword();

}
