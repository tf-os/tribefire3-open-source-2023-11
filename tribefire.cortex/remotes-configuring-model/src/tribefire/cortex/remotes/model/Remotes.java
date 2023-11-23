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
package tribefire.cortex.remotes.model;

import java.util.List;

import com.braintribe.model.deployment.HttpServer;
import com.braintribe.model.deployment.remote.GmWebRpcRemoteServiceProcessor;
import com.braintribe.model.deployment.remote.RemotifyingInterceptor;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.securityservice.credentials.Credentials;

/**
 * Based on this entity a new {@link GmWebRpcRemoteServiceProcessor} and {@link RemotifyingInterceptor} is configured, and for each
 * {@link RemoteServiceDomain} (contained in the {@link #getDomains() domains} property) a single domain is configured, where each request is
 * configured to be processed with this service processor and i.
 * 
 * @author peter.gazdik
 */
public interface Remotes extends GenericEntity {

	EntityType<Remotes> T = EntityTypes.T(Remotes.class);

	/**
	 * Remote server URL. This value is set as the {@link HttpServer#getBaseUrl() base URL} of the {@link GmWebRpcRemoteServiceProcessor}'s
	 * {@link GmWebRpcRemoteServiceProcessor#getServer() HTTP server}.
	 */
	@Mandatory
	String getServerUrl();
	void setServerUrl(String serverUrl);

	/**
	 * Optional remote server URI, which is appended to the {@link #getServerUrl() server's URL} to get the actual URL to call the remote service.
	 * Typically this value would be 'rpc' for a remote tribefire server.
	 * <p>
	 * This is set as {@link GmWebRpcRemoteServiceProcessor#getUri() uri} property of our remote service processor.
	 */
	@Mandatory
	String getServerUri();
	void setServerUri(String serverUri);

	// whether or not credentials are
	// TODO explain
	boolean getDecryptCredentials();
	void setDecryptCredentials(boolean decryptCredentials);

	/** Credentials for the remote server. If no credentials are configured, we assume the remote server shares sessions with ours. */
	// relevant globalId better be set
	// the credentials are expected not to contain the sensitive values like passwords...
	Credentials getServerCredentials();
	void setServerCredentials(Credentials serverCredentials);

	List<RemoteServiceDomain> getDomains();
	void setDomains(List<RemoteServiceDomain> domains);

}
