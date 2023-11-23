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
package com.braintribe.zarathud.model.forensics;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.zarathud.model.forensics.data.ImportedModule;
import com.braintribe.zarathud.model.forensics.data.ModuleReference;

public interface ModuleForensicsResult extends ForensicsResult {
	EntityType<ModuleForensicsResult> T = EntityTypes.T(ModuleForensicsResult.class);
	
	String moduleImports = "moduleImports";
	String requiredImportModules = "requiredImportModules";

	/**
	 * @return - a list of what the terminal requires module-wise
	 */
	List<ModuleReference> getModuleImports();
	void setModuleImports(List<ModuleReference> value);
	
	/**
	 * @return - a {@link List} of modules with their exports (derived from above and 
	 * collated during the full walk
	 */
	List<ImportedModule> getRequiredImportModules();
	void setRequiredImportModules(List<ImportedModule> value);

}
