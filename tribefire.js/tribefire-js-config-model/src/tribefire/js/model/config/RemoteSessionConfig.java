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
package tribefire.js.model.config;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.securityservice.credentials.Credentials;

public interface RemoteSessionConfig extends GenericEntity {
	EntityType<RemoteSessionConfig> T = EntityTypes.T(RemoteSessionConfig.class);

	/**
	 * The url to the tribefire services endpoint root or null if this should be automatically determined
	 */
	String getServicesUrl();
	void setServicesUrl(String servicesUrl);

	/**
	 * The credentials to authenticate
	 */
	Credentials getCredentials();
	void setCredentials(Credentials credentials);
}
