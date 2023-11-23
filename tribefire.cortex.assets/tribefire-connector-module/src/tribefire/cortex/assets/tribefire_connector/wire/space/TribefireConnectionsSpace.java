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
package tribefire.cortex.assets.tribefire_connector.wire.space;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.tribefire.connector.LocalTribefireConnection;
import com.braintribe.model.deployment.tribefire.connector.RemoteTribefireConnection;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.session.GmSessionFactories;
import com.braintribe.model.processing.session.GmSessionFactoryBuilderException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.RequestUserRelatedContract;
import tribefire.module.wire.contract.SystemUserRelatedContract;

@Managed
public class TribefireConnectionsSpace implements WireSpace {

	private static final Logger logger = Logger.getLogger(TribefireConnectionsSpace.class);

	@Import
	private SystemUserRelatedContract systemUserRelated;
	
	@Import
	private RequestUserRelatedContract requestUserRelated; 

	@Managed
	public PersistenceGmSessionFactory localTribefireConnection(ExpertContext<LocalTribefireConnection> context) {
		LocalTribefireConnection deployable = context.getDeployable();
		return localTribefireConnection(deployable);
	}
	
	public PersistenceGmSessionFactory localTribefireConnection(LocalTribefireConnection connection) {
		
		logger.debug(() -> "Creating local session factory for "+connection);

		boolean systemSessionFactory = connection.getSystemSession();
		
		PersistenceGmSessionFactory sessionFactory = systemSessionFactory ? systemUserRelated.sessionFactory() : requestUserRelated.sessionFactory();

		return sessionFactory;
	}


	@Managed
	public PersistenceGmSessionFactory remoteTribefireConnection(ExpertContext<RemoteTribefireConnection> context) {
		RemoteTribefireConnection deployable = context.getDeployable();
		return remoteTribefireConnection(deployable);
	}
	
	public PersistenceGmSessionFactory remoteTribefireConnection(RemoteTribefireConnection connection) {
		
		String url = connection.getServicesUrl();
		Credentials cred = connection.getCredentials();

		logger.debug(() -> "Creating remote session factory to "+url);

		PersistenceGmSessionFactory sessionFactory = null;

		try {
			sessionFactory = GmSessionFactories
					.remote(url)
					.authentication(cred)
					.done();
		} catch (GmSessionFactoryBuilderException e) {
			throw Exceptions.unchecked(e, "Error while trying to create a remote session factory to "+url);
		}

		return sessionFactory;
	}
	
}
