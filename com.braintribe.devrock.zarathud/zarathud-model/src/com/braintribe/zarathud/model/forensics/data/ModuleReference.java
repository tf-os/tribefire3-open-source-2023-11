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
package com.braintribe.zarathud.model.forensics.data;

import java.util.List;


import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents the 'împorter side' of the modules, i.e. the required packages of
 * the terminal retrieved from the dependency.
 * @author pit
 *
 */
public interface ModuleReference extends GenericEntity {
		
	EntityType<ModuleReference> T = EntityTypes.T(ModuleReference.class);
	
	String moduleName = "moduleName";
	String artifactName = "artifactName";
	String requiredPackages = "requiredPackages";
	
	/**
	 * @return
	 */
	String getArtifactName();
	void setArtifactName(String value);
	
	/**
	 * @return - a {@link List} of the packages required (the packages of the types required) 
	 */
	List<String> getRequiredPackages();
	void setRequiredPackages(List<String> value);
	
	
		
}
