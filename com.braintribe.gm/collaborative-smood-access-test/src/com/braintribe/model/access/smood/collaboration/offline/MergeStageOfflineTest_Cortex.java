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
package com.braintribe.model.access.smood.collaboration.offline;

import com.braintribe.model.access.collaboration.CollaborativeAccessManager;
import com.braintribe.model.access.smood.collaboration.manager.MergeStageCsaTest_Cortex;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;
import com.braintribe.model.cortexapi.access.collaboration.MergeCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.ReadOnlyCollaborativePersistenceRequest;

/**
 * @see MergeStageCsaTest_Cortex
 * @see MergeCollaborativeStage
 * @see CollaborativeAccessManager
 *
 * @author peter.gazdik
 */
public class MergeStageOfflineTest_Cortex extends MergeStageCsaTest_Cortex {

	@Override
	protected <T> T eval(CollaborativePersistenceRequest request) {
		if (request instanceof ReadOnlyCollaborativePersistenceRequest) {
			return csaUnit.eval(request);

		} else {
			csaUnit.evalOffline(request);
			redeploy();
			return null;
		}
	}

}
