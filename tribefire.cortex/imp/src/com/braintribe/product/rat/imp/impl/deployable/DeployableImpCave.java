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
package com.braintribe.product.rat.imp.impl.deployable;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.database.pool.ConfiguredDatabaseConnectionPool;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImpCave;

/**
 * An {@link AbstractImpCave} specialized in {@link Deployable}. Additionally this cave serves as an entry point to the
 * "Deployables" part of the ImpApi
 */
public class DeployableImpCave extends AbstractImpCave<Deployable, BasicDeployableImp<Deployable>> {

	public DeployableImpCave(PersistenceGmSession session) {
		super(session, "externalId", Deployable.T);
	}

	public <T extends ConfiguredDatabaseConnectionPool> ConnectionImpCave<T> connection(EntityType<T> connectionType) {
		return new ConnectionImpCave<>(session(), connectionType);
	}

	public AccessImpCave access() {
		return new AccessImpCave(session());
	}

	public WebTerminalImpCave webTerminal() {
		return new WebTerminalImpCave(session());
	}

	// public WorkerImp worker() {
	// return new WorkerImp(session);
	// }

	public ServiceProcessorImpCave serviceProcessor() {
		return new ServiceProcessorImpCave(session());
	}

	@Override
	protected BasicDeployableImp<Deployable> buildImp(Deployable instance) {
		return new BasicDeployableImp<Deployable>(session(), instance);
	}

	public AccessImp<IncrementalAccess> access(String externalId) {
		return new AccessImpCave(session()).with(externalId);
	}

}
