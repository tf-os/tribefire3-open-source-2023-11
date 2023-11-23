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
package com.braintribe.zarathud.model.storage;

import java.util.Map;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.ClasspathForensicsResult;
import com.braintribe.zarathud.model.forensics.DependencyForensicsResult;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;
import com.braintribe.zarathud.model.forensics.ModelForensicsResult;
import com.braintribe.zarathud.model.forensics.ModuleForensicsResult;

public interface ViewingContextStorageContainer extends GenericEntity{
	
	EntityType<ViewingContextStorageContainer> T = EntityTypes.T(ViewingContextStorageContainer.class);
	
	
	Artifact getArtifact();
	void setArtifact(Artifact value);
	
	ModelForensicsResult getModelForensicsResult();
	void setModelForensicsResult(ModelForensicsResult value);

	DependencyForensicsResult getDependencyForensicsResult();
	void setDependencyForensicsResult(DependencyForensicsResult value);
	
	ClasspathForensicsResult getClasspathForensicsResult();
	void setClasspathForensicsResult(ClasspathForensicsResult value);
	
	ModuleForensicsResult getModuleForensicsResult();
	void setModuleForensicsResult(ModuleForensicsResult value);

	ForensicsRating getWorstRating();
	void setWorstRating(ForensicsRating value);

	Map<FingerPrint, ForensicsRating> getIssues();
	void setIssues(Map<FingerPrint, ForensicsRating> value);

	Reason getAnalyzerProcessingReturnReason();
	void setAnalyzerProcessingReturnReason(Reason value);
	
	Map<FingerPrint, ForensicsRating> getActiveRatings();
	void setActiveRatings(Map<FingerPrint, ForensicsRating> value);
	
	
}
	
