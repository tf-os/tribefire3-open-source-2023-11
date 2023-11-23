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

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

/**
 * Retrieves all transitive dependencies of the specified {@link #getTerminalArtifacts() terminal artifacts} and creates
 * a <code>LockArtifactFilter</code> which locks the respective versions. By default, this will be added to a
 * <code>RepositoryView</code> via an <code>ArtifactFilterEnrichment</code>. The result will be written to a Yaml file
 * based on which one create a release view asset.
 * 
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
@Description("Retrieves all transitive dependencies of the specified terminal artifacts and creates"
		+ "LockArtifactFilter which locks the respective versions. By default, this will be added to a"
		+ " RepositoryView via an ArtifactFilterEnrichment. The result will be written to a Yaml file"
		+ " based on which one create a release view asset.")
public interface LockVersions extends SetupRequest {

	EntityType<LockVersions> T = EntityTypes.T(LockVersions.class);

	@Override
	EvalContext<? extends Neutral> eval(Evaluator<ServiceRequest> evaluator);

	String invalidVersionRegex = "invalidVersionRegex";
	String includeAlreadyLockedVersions = "includeAlreadyLockedVersions";
	String markAsRelease = "markAsRelease";
	String targetFile = "targetFile";
	String terminalArtifacts = "terminalArtifacts";
	String writeLocksOnly = "writeLocksOnly";

	@Description("The terminal artifact(s) for which to lock the versions of all transitive dependencies.")
	@Mandatory
	@Alias("terminals")
	List<String> getTerminalArtifacts();
	void setTerminalArtifacts(List<String> terminalArtifacts);

	@Description("The path to the target file the locks will be written to.")
	@Initializer("'repositoryview.yaml'")
	@Alias("target")
	String getTargetFile();
	void setTargetFile(String targetFile);

	@Description("Whether or not to include already locked artifact versions, i.e. versions which are already locked"
			+ " in the currently used repository view (using LockArtifactFilter).")
	boolean getIncludeAlreadyLockedVersions();
	void setIncludeAlreadyLockedVersions(boolean includeAlreadyLockedVersions);

	@Description("If enabled, only the locks will be written, i.e. not a full RepositoryView.")
	boolean getWriteLocksOnly();
	void setWriteLocksOnly(boolean writeLocksOnly);

	@Description("The regex that is used to check the invalidity of the lock versions. By default, versions with '-pc' are referring to publishing candidates and usually are not expected to be locked.")
	@Initializer("'\\d+\\.\\d+\\.\\d+-pc'")
	String getInvalidVersionRegex();
	void setInvalidVersionRegex(String invalidVersionRegex);
	
	@Description("Whether or not the repository view to be created represents a release."
			+ " If it does, the respective Release entity will be attached to the view to mark it as release view."
			+ " Furthermore, the view will be marked immutable.")
	boolean getMarkAsRelease();
	void setMarkAsRelease(boolean markAsRelease);
}
