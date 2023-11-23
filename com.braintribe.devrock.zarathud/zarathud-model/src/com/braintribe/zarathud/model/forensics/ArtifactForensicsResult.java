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
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.data.ArtifactReference;
import com.braintribe.zarathud.model.forensics.data.ClasspathDuplicate;

/**
 * represents what Zed knows about an artifact - ! NOT A TERMINAL ! 
 * @author pit
 *
 */
public interface ArtifactForensicsResult extends ForensicsResult {
	
	EntityType<ArtifactForensicsResult> T = EntityTypes.T(ArtifactForensicsResult.class);
		
	String numberOfReferences = "numberOfReferences";
	String references = "references";
	String duplicates = "duplicates";
	

	/**
	 * @return - how many types of the terminal references this artifact
	 */
	int getNumberOfReferences();
	void setNumberOfReferences(int numberOfReferences);
	
	/**
	 * @return - the {@link ArtifactReference}s of the terminal into this {@link Artifact}
	 */
	List<ArtifactReference> getReferences();
	void setReferences(List<ArtifactReference>  references);
	
	List<ClasspathDuplicate> getDuplicates();
	void setDuplicates( List<ClasspathDuplicate> duplicates);
	
}
