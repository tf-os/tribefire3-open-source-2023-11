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
package com.braintribe.devrock.zarathud.model.module;

import java.util.List;

import com.braintribe.devrock.zarathud.model.common.Node;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ImportedModuleNode extends Node {
		
	EntityType<ImportedModuleNode> T = EntityTypes.T(ImportedModuleNode.class);
	
	String artifactName = "artifactName";
	String moduleName = "moduleName";
	String exportedPackages = "exportedPackages";
			
	String getArtifactName();
	void setArtifactName(String value);

	String getModuleName();
	void setModuleName(String value);
	
	List<PackageNode> getExportedPackages();
	void setExportedPackages(List<PackageNode> value);
	
}
