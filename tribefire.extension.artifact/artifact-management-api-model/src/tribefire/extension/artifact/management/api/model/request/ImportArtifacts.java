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

import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

import tribefire.extension.artifact.management.api.model.data.ResolutionTransitivity;

@Description("Uploads artifacts from a given arbitrary folder structure which is recursively scanned for upload candidates. "
		+ "A candidate is detected by the existence of a file with the extension *.pom. "
		+ "Each file in such a candidate directory must be formatted like artifactId-classifier-version.type to be recognized and uploaded as an artifact part.")
@PositionalArguments({"path","repoId"})
public interface ImportArtifacts extends ArtifactManagementRequest { 

	EntityType<ImportArtifacts> T = EntityTypes.T(ImportArtifacts.class);
	
	String update = "update";
	String transitivity = "transitivity";

	@Alias("u")
	@Description("If set, already existing artifacts in the repository will be updated. New parts are added and already existing parts "
			+ "are updated if their hash differs.")
	boolean getUpdate();
	void setUpdate(boolean value);
	
	ResolutionTransitivity getTransitivity();
	void setTransitivity(ResolutionTransitivity transitivity);
	
	

	@Override
	EvalContext<Neutral> eval(Evaluator<ServiceRequest> evaluator);
			
}
