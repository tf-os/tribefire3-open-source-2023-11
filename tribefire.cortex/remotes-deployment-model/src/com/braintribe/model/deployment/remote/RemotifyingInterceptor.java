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
package com.braintribe.model.deployment.remote;

import com.braintribe.model.extensiondeployment.ServiceAroundProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.securityservice.credentials.Credentials;

/**
 * Denotation type for a {@link ServiceAroundProcessor} which handles the authentication against the remote server (when relevant) and sets the
 * correct remote domain id based on the configured {@link RemoteDomainIdMapping}.
 * <p>
 * The remote authentication is only done in case {@link #getCredentials() credentials} are set, otherwise we assume the remote server shares the
 * session with the local one (i.e. the one where this interceptor is running).
 * 
 * @author peter.gazdik
 */
public interface RemotifyingInterceptor extends ServiceAroundProcessor {

	EntityType<RemotifyingInterceptor> T = EntityTypes.T(RemotifyingInterceptor.class);

	boolean getDecryptCredentials();
	void setDecryptCredentials(boolean decryptCredentials);

	Credentials getCredentials();
	void setCredentials(Credentials credentials);

}
