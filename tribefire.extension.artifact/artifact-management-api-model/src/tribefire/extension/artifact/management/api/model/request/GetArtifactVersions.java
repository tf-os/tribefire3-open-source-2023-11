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
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.artifact.management.api.model.data.ArtifactVersions;

@Description("Determines the versions of an artifact given as a formatted string such as: org.fox:fix, org.fox:fix#2.0, org.fox:fix#[1.0,1.1). "
		+ "If no range is specified, it will be interpreted as an unbounded interval.")
@PositionalArguments("artifact")
public interface GetArtifactVersions extends ArtifactManagementRequest {
	
	EntityType<GetArtifactVersions> T = EntityTypes.T(GetArtifactVersions.class);
	
	String artifact = "artifact";

	@Mandatory
	@Alias("a")
	@Description("Artifact qualification with optional version range to determine the available versions within the range (full range if not given explicitly). Examples: org.fox:fix, org.fox:fix#2.0, org.fox:fix#[1.0,1.1)")
	String getArtifact();
	void setArtifact(String artifact);

	@Override
	EvalContext<ArtifactVersions> eval(Evaluator<ServiceRequest> evaluator);
	
}
