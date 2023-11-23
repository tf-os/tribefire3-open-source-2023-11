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
import java.util.Set;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

@Description("Download the given artifacts to a given path.")
@PositionalArguments({"path","artifacts"})
public interface DownloadArtifacts extends ArtifactManagementRequest { 

	EntityType<DownloadArtifacts> T = EntityTypes.T(DownloadArtifacts.class);
	
	String path = "path";
	String transitive = "transitive";
	String artifacts = "artifacts";
	String scopes = "scopes";
	String includeOptional = "includeOptional";
	String licenseInfo = "licenseInfo";

	@Alias("p")
	@Description("The path of the root folder where the downloaded artifacts should be placed.")
	@Mandatory
	String getPath();
	void setPath(String value);
	
	@Alias("t")
	@Description("If true the transitive dependencies of the given artifacts will be downloaded as well.")
	boolean getTransitive();
	void setTransitive(boolean transitive);
	
	@Alias("l")
	@Description("If true and license related output will be made.")
	boolean getLicenseInfo();
	void setLicenseInfo(boolean licenseInfo);
	
	@Alias("o")
	@Description("If true optional dependencies will be included as well in transitive download.")
	boolean getIncludeOptional();
	void setIncludeOptional(boolean includeOptional);
	
	@Alias("s")
	@Initializer("{runtime}")
	@Description("The resolution scopes used to filter dependencies in transitive download.")
	Set<ResolutionScope> getScopes();
	void setScopes(Set<ResolutionScope> scopes);
	
	@Alias("a")
	@Description("The terminal artifacts to be downloaded.")
	@Mandatory
	List<String> getArtifacts();
	void setArtifacts(List<String> artifacts);
	
	@Override
	EvalContext<Neutral> eval(Evaluator<ServiceRequest> evaluator);
			
}
