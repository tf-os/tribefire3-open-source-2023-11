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
package com.braintribe.devrock.eclipse.model.storage;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Transient;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * simple container for the parameters of the transposition
 * @author pit
 *
 */
public interface TranspositionContext extends GenericEntity {
		
	EntityType<TranspositionContext> T = EntityTypes.T(TranspositionContext.class);
	
	String showDependencies = "showDependencies";
	String showDependers = "showDependers";
	String showParents = "showParents";
	String showParentDependers = "showParentDependers";
	String showImports = "showImports";
	String showImportDependers = "showImportDependers";
	String showParts = "showParts";
	String coalesce = "coalesce";
	String key = "key";
	String assignedKey = "assignedKey";
	String detectProjects = "detectProjects";

	/**
	 * @return - whether to transpose dependencies of artifacts
	 */
	boolean getShowDependencies();
	void setShowDependencies(boolean value);
	
	/**
	 * @return - whether to transpose dependers of artifacts / dependencies
	 */
	boolean getShowDependers();
	void setShowDependers(boolean value);
	
	/**
	 * @return - whether to show parents
	 */
	boolean getShowParents();
	void setShowParents(boolean value);
	
	/**
	 * @return - whether to show dependers of parents
	 */
	boolean getShowParentDependers();
	void setShowParentDependers(boolean value);
	
	/**
	 * @return - whether to show imports of parents
	 */
	boolean getShowImports();
	void setShowImports(boolean value);
	
	/**
	 * @return - whether to show dependers of imports (the parents)
	 */
	boolean getShowImportDependers();
	void setShowImportDependers(boolean value);

	
	/**
	 * @return - whether to show parts
	 */
	boolean getShowParts();
	void setShowParts(boolean value);
	
	/**
	 * @return - true if filtered dependencies should be coalesced
	 */
	boolean getCoalesce();
	void setCoalesce(boolean value);
	
	/**
	 * @return - the key as assigned/read 
	 */
	String getKey();
	void setKey(String value);

	@Transient
	String getAssignedKey();
	void setAssignedKey(String value);
	
	/**
	 * @return - true if projects should be shown
	 */
	boolean getDetectProjects();
	void setDetectProjects(boolean value);


	
}
