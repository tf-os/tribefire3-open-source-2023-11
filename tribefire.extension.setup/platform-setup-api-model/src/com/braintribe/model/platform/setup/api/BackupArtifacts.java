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
package com.braintribe.model.platform.setup.api;

import java.util.List;

import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

/**
 * Backup concrete versions of artifacts.
 */
@Description("Backup concrete versions of artifacts.")
public interface BackupArtifacts extends SetupRequest {

	String artifacts = "artifacts";
	String file = "file";
	
	EntityType<BackupArtifacts> T = EntityTypes.T(BackupArtifacts.class);

	@Description("The artifact(s) to backup.")
	@Alias("artifacts")
	List<String> getArtifacts();
	void setArtifacts(List<String> artifacts);

	@Description("The YAML file that contains the artifact(s) to backup (each line starts with a dash followed by a space).")
	String getYamlFile();
	void setYamlFile(String yamlFile);

	@Description("Whether to generate maven-metadata.xml")
	boolean getGenerateMavenMetadata();
	void setGenerateMavenMetadata(boolean generateMavenMetadata);

	@Override
	EvalContext<? extends Neutral> eval(Evaluator<ServiceRequest> evaluator);

}
