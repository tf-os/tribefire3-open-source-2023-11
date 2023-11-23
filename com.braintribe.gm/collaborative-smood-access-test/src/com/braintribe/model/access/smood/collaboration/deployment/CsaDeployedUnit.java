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
package com.braintribe.model.access.smood.collaboration.deployment;

import com.braintribe.model.access.collaboration.CollaborativeSmoodAccess;
import com.braintribe.model.access.collaboration.offline.CollaborativeAccessOfflineManager;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;

/**
 * @author peter.gazdik
 */
public class CsaDeployedUnit extends AbstractCsaDeployedUnit<CollaborativeSmoodAccess> {

	private CollaborativeAccessOfflineManager offlineManager;

	public void evalOffline(CollaborativePersistenceRequest request) {
		acquireOfflineManager().process(null, request);
	}

	public CollaborativeAccessOfflineManager acquireOfflineManager() {
		if (offlineManager == null)
			offlineManager = newOfflineManager();

		return offlineManager;
	}

	private CollaborativeAccessOfflineManager newOfflineManager() {
		CollaborativeAccessOfflineManager result = new CollaborativeAccessOfflineManager();
		result.setBaseFolder(baseFolder);
		result.setCsaStatePersistence(statePersistence);

		return result;
	}

}
