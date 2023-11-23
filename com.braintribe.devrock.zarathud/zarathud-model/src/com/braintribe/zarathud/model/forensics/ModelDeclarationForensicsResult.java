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

/**
 * the result of the forensics running on the model-declaration.xml file.
 * 
 * @author pit
 *
 */
public interface ModelDeclarationForensicsResult extends ForensicsResult {
	
	EntityType<ModelDeclarationForensicsResult> T = EntityTypes.T(ModelDeclarationForensicsResult.class);
	
	String modelDeclarationContents = "modelDeclarationContents";
	String missingDependencyDeclarations = "missingDependencyDeclarations";
	String excessDependencyDeclarations = "excessDependencyDeclarations";
	String missingTypeDeclarations = "missingTypeDeclarations";
	String excessTypeDeclarations = "excessTypeDeclarations";
	String fileHash = "fileHash";
	String computedHash = "computedHash";
	
	/**
	 * @return - a {@link String} representation of the declaration file 
	 */
	String getModelDeclarationContents();
	void setModelDeclarationContents(String value);
	
	
	/**
	 * @return - a {@link List} of {@link String} with the condensed names of the missing artifacts {@code (<groupId>:<artifactId>)}
	 */
	List<String> getMissingDependencyDeclarations();
	void setMissingDependencyDeclarations(List<String> value);
	
	/**
	 * @return - a {@link List} of {@link String} with the condensed names 
	 */
	List<String> getExcessDependencyDeclarations();
	void setExcessDependencyDeclarations(List<String> value);
	
	
	/**
	 * @return - a {@link List} of {@link String} of the missing types (just name)
	 */
	List<String> getMissingTypeDeclarations();
	void setMissingTypeDeclarations(List<String> value);
	
	/**
	 * @return - a {@link List} of {@link String} of the excess types (just name)
	 */
	List<String> getExcessTypeDeclarations();
	void setExcessTypeDeclarations(List<String> value);
	
	
	
	

}
