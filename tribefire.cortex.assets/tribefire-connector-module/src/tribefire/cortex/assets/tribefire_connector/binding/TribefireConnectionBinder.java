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
package tribefire.cortex.assets.tribefire_connector.binding;

import com.braintribe.model.deployment.tribefire.connector.TribefireConnection;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.DirectComponentBinder;
import com.braintribe.model.processing.deployment.api.MutableDeploymentContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;

public class TribefireConnectionBinder implements DirectComponentBinder<TribefireConnection, PersistenceGmSessionFactory> {

	public static final TribefireConnectionBinder INSTANCE = new TribefireConnectionBinder();

	private TribefireConnectionBinder() {
	}

	@Override
	public PersistenceGmSessionFactory bind(MutableDeploymentContext<TribefireConnection, PersistenceGmSessionFactory> context) throws DeploymentException {
		return context.getInstanceToBeBound();
	}

	@Override
	public EntityType<TribefireConnection> componentType() {
		return TribefireConnection.T;
	}

	@Override
	public Class<?>[] componentInterfaces() {
		return new Class<?>[] { PersistenceGmSessionFactory.class };
	}

}
