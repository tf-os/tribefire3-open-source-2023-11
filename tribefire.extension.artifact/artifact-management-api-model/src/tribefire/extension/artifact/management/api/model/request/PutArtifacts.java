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
package tribefire.extension.artifact.management.api.model.request;

import java.util.List;

import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.ArtifactResolution;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

@Description("Uploads artifacts from a given set of artifacts to the target repo of the current configuration.")
public interface PutArtifacts extends ArtifactManagementRequest { 

	EntityType<PutArtifacts> T = EntityTypes.T(PutArtifacts.class);
	
	String artifacts = "artifacts";
	String update = "update";

	List<Artifact> getArtifacts();
	void setArtifacts(List<Artifact> artifacts); 
	
	@Alias("u")
	@Description("If set, already existing artifacts in the repository will be updated. New parts are added and already existing parts "
			+ "are updated if their hash differs.")
	boolean getUpdate();
	void setUpdate(boolean value);
	
	@Override
	EvalContext<ArtifactResolution> eval(Evaluator<ServiceRequest> evaluator);
}
