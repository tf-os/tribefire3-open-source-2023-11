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
import com.braintribe.zarathud.model.forensics.data.ModelEntityReference;
import com.braintribe.zarathud.model.forensics.data.ModelEnumReference;

/**
 * the result on the forensics run on a model artifact 
 * @author pit
 *
 */
public interface ModelForensicsResult extends ForensicsResult {
		
	EntityType<ModelForensicsResult> T = EntityTypes.T(ModelForensicsResult.class);
		
	String modelEntityReferences = "modelEntityReferences";
	String modelEnumEntities = "modelEnumEntities";
	String declarationResult = "declarationResult";

	/**
	 * @return - a {@link List} of the {@link ModelEntityReference} - the 'model'-specific view on an artifact 
	 */
	List<ModelEntityReference> getModelEntityReferences();
	void setModelEntityReferences(List<ModelEntityReference> value);
	
	List<ModelEnumReference> getModelEnumEntities();
	void setModelEnumEntities(List<ModelEnumReference> value);

	
	/**
	 * @return - the result of the forensics run on the model-declaration.xml file 
	 */
	ModelDeclarationForensicsResult getDeclarationResult();
	void setDeclarationResult( ModelDeclarationForensicsResult result);
	 
	
}
