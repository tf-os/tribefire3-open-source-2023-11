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
package com.braintribe.model.accessapi;

import com.braintribe.model.generic.annotation.Abstract;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import java.util.List;
import java.util.Set;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.workbench.WorkbenchConfiguration;
import com.braintribe.model.workbench.WorkbenchPerspective;

@Abstract
public interface WorkbenchEnvironment extends GenericEntity {

	final EntityType<WorkbenchEnvironment> T = EntityTypes.T(WorkbenchEnvironment.class);

	// @formatter:off
	void setWorkbenchRootFolders(Set<Folder> rootFolders);
	Set<Folder> getWorkbenchRootFolders();
	
	List<WorkbenchPerspective> getPerspectives();
	void setPerspectives(List<WorkbenchPerspective> perspectives);
	
	void setWorkbenchConfiguration(WorkbenchConfiguration workbenchConfiguration);
	WorkbenchConfiguration getWorkbenchConfiguration();
	// @formatter:on

}
