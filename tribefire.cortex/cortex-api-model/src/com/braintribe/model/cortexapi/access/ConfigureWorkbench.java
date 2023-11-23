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
package com.braintribe.model.cortexapi.access;

import java.util.List;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.workbench.instruction.WorkbenchInstruction;

public interface ConfigureWorkbench extends SetupAccessRequest {

	EntityType<ConfigureWorkbench> T = EntityTypes.T(ConfigureWorkbench.class);

	@Initializer("true")
	boolean getEnsureStandardFolders();

	void setEnsureStandardFolders(boolean ensureStandardFolders);

	ExplorerStyle getExplorerStyle();

	void setExplorerStyle(ExplorerStyle explorerStyle);

	List<WorkbenchInstruction> getInstructions();

	void setInstructions(List<WorkbenchInstruction> instructions);

	// TODO visibility based on whether the workbench had been setup already
	// Extension point for when a core decision is made to optionally combine
	// setup and configure workbench.
	
	// boolean getResetExistingAccess();
	// void setResetExistingAccess(boolean resetExistingAccess);
	//
	// @Initializer("true")
	// boolean getResetExistingModel();
	// void setResetExistingModel(boolean resetExistingModel);

}
