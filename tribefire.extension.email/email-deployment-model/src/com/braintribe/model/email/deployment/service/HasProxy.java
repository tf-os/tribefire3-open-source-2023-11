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
package com.braintribe.model.email.deployment.service;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Confidential;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
public interface HasProxy extends GenericEntity {

	final EntityType<HasProxy> T = EntityTypes.T(HasProxy.class);

	@Name("Proxy Host")
	@Description("Hostname or IP address of the proxy server.")
	String getProxyHost();
	void setProxyHost(String proxyHost);

	@Name("Proxy Port")
	@Description("The listening port of the proxy server.")
	Integer getProxyPort();
	void setProxyPort(Integer proxyPort);

	@Name("Proxy User")
	@Description("The username that should be used for authentication with the proxy server.")
	String getProxyUsername();
	void setProxyUsername(String proxyUsername);

	@Name("Proxy Password")
	@Description("The password that should be used for authentication at the proxy server.")
	@Confidential
	String getProxyPassword();
	void setProxyPassword(String proxyPassword);
}
