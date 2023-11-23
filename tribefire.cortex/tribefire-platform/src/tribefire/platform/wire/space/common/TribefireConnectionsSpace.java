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
package tribefire.platform.wire.space.common;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.tribefire.connector.LocalTribefireConnection;
import com.braintribe.model.deployment.tribefire.connector.RemoteTribefireConnection;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.session.GmSessionFactories;
import com.braintribe.model.processing.session.GmSessionFactoryBuilderException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.TribefireConnectionsContract;
import tribefire.platform.impl.binding.TribefireConnectionBinder;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;

@Managed
public class TribefireConnectionsSpace implements TribefireConnectionsContract {

	private static final Logger logger = Logger.getLogger(TribefireConnectionsSpace.class);

	@Import
	private GmSessionsSpace gmSessions;

	@Import
	private ResourceProcessingSpace resourceProcessing;

	public void bindAll(DenotationBindingBuilder bindings) {
		bindings.bind(LocalTribefireConnection.T).component(TribefireConnectionBinder.INSTANCE)
				.expertFactory(this::localTribefireConnection);
		bindings.bind(RemoteTribefireConnection.T).component(TribefireConnectionBinder.INSTANCE)
				.expertFactory(this::remoteTribefireConnection);
	}

	@Override
	@Managed
	public PersistenceGmSessionFactory localTribefireConnection(ExpertContext<LocalTribefireConnection> context) {
		LocalTribefireConnection deployable = context.getDeployable();
		return localTribefireConnection(deployable);
	}

	@Override
	public PersistenceGmSessionFactory localTribefireConnection(LocalTribefireConnection connection) {

		logger.debug(() -> "Creating local session factory for " + connection);

		boolean systemSessionFactory = connection.getSystemSession();

		PersistenceGmSessionFactory sessionFactory = systemSessionFactory ? gmSessions.systemSessionFactory() : gmSessions.sessionFactory();

		return sessionFactory;
	}

	@Override
	@Managed
	public PersistenceGmSessionFactory remoteTribefireConnection(ExpertContext<RemoteTribefireConnection> context) {
		RemoteTribefireConnection deployable = context.getDeployable();
		return remoteTribefireConnection(deployable);
	}

	@Override
	public PersistenceGmSessionFactory remoteTribefireConnection(RemoteTribefireConnection connection) {

		String url = connection.getServicesUrl();
		Credentials cred = connection.getCredentials();

		logger.debug(() -> "Creating remote session factory to " + url);

		PersistenceGmSessionFactory sessionFactory = null;

		Integer socketTimeoutMs = connection.getSocketTimeoutMs();
		Integer poolTtlMs = connection.getPoolTtlMs();

		DefaultHttpClientProvider clientProvider = null;
		if (socketTimeoutMs != null || poolTtlMs != null) {
			clientProvider = new DefaultHttpClientProvider();
			if (socketTimeoutMs != null) {
				clientProvider.setSocketTimeout(socketTimeoutMs);
			}
			if (poolTtlMs != null) {
				clientProvider.setPoolTimeToLive(poolTtlMs.longValue());
			}
		}

		try {
			sessionFactory = GmSessionFactories.remote(url).authentication(cred).streamPipeFactory(resourceProcessing.streamPipeFactory())
					.httpClientProvider(clientProvider).done();
		} catch (GmSessionFactoryBuilderException e) {
			throw Exceptions.unchecked(e, "Error while trying to create a remote session factory to " + url);
		}

		return sessionFactory;
	}

}
