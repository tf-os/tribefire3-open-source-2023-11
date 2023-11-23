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
package tribefire.module.wire.contract;

import java.util.Objects;

import com.braintribe.model.deployment.tribefire.connector.LocalTribefireConnection;
import com.braintribe.model.deployment.tribefire.connector.RemoteTribefireConnection;
import com.braintribe.model.deployment.tribefire.connector.TribefireConnection;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.wire.api.space.WireSpace;

public interface TribefireConnectionsContract extends WireSpace {

	PersistenceGmSessionFactory localTribefireConnection(ExpertContext<LocalTribefireConnection> context);

	PersistenceGmSessionFactory remoteTribefireConnection(ExpertContext<RemoteTribefireConnection> context);

	PersistenceGmSessionFactory localTribefireConnection(LocalTribefireConnection connection);

	PersistenceGmSessionFactory remoteTribefireConnection(RemoteTribefireConnection context);

	default PersistenceGmSessionFactory tribefireConnection(TribefireConnection connection) {
		Objects.requireNonNull(connection, "The connection must not be null.");

		if (connection instanceof LocalTribefireConnection)
			return localTribefireConnection((LocalTribefireConnection) connection);

		else if (connection instanceof RemoteTribefireConnection)
			return remoteTribefireConnection((RemoteTribefireConnection) connection);

		else
			throw new IllegalStateException("Unsupported tribefire connection type: " + connection);
	}

}
