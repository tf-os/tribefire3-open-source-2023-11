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
package com.braintribe.devrock.eclipse.model.workspace;

import java.util.List;

import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a GE representing a IProject of Eclipse
 * @author pit
 *
 */
public interface Project extends GenericEntity{
	
	final EntityType<Project> T = EntityTypes.T(Project.class);

	String projectName = "projectName";
	String natures= "natures";
	String identification = "identification";
	
	String getProjectName();
	void setProjectName(String  projectName);
	
	EnhancedCompiledArtifactIdentification getIdentification();
	void setIdentification(EnhancedCompiledArtifactIdentification  ecai);

	List<String> getNatures();
	void setNatures(List<String>  name);
	
}
