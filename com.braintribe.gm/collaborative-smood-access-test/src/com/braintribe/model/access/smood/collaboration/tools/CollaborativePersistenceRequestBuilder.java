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
package com.braintribe.model.access.smood.collaboration.tools;

import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeInitializers;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeStageStats;
import com.braintribe.model.cortexapi.access.collaboration.MergeCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.PushCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.RenameCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.ResetCollaborativePersistence;

/**
 * @author peter.gazdik
 */
public class CollaborativePersistenceRequestBuilder {

	public static RenameCollaborativeStage renameStageRequest(String oldName, String newName) {
		RenameCollaborativeStage result = RenameCollaborativeStage.T.create();
		result.setOldName(oldName);
		result.setNewName(newName);

		return result;
	}

	public static PushCollaborativeStage pushStageRequest(String name) {
		PushCollaborativeStage result = PushCollaborativeStage.T.create();
		result.setName(name);

		return result;
	}

	public static MergeCollaborativeStage mergeStageRequest(String source, String target) {
		MergeCollaborativeStage result = MergeCollaborativeStage.T.create();
		result.setSource(source);
		result.setTarget(target);
	
		return result;
	}

	public static ResetCollaborativePersistence resetCollaborativePersistence() {
		return ResetCollaborativePersistence.T.create();
	}

	public static GetCollaborativeStageStats getStageStatsRequest(String name) {
		GetCollaborativeStageStats result = GetCollaborativeStageStats.T.create();
		result.setName(name);
	
		return result;
	}

	public static GetCollaborativeInitializers getInitializersRequest() {
		return GetCollaborativeInitializers.T.create();
	}


}
