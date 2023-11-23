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

import java.util.Map;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

/**
 * Restores backed up artifacts. See {@link BackupArtifacts} command.
 */
@Description("Restores backed up artifacts. See backup-artifacts command.")
public interface RestoreArtifacts extends SetupRequest {

	String folder = "folder";
	String artifactoryUrl = "artifactoryUrl";
	String user = "user";
	String password = "password";
	String changedRepositoryIds = "changedRepositoryIds";

	EntityType<RestoreArtifacts> T = EntityTypes.T(RestoreArtifacts.class);

	@Description("The folder that contains the artifacts to restore.")
	@Mandatory
	String getFolder();
	void setFolder(String folder);

	@Description("The user with access to target repositories.")
	@Mandatory
	String getUser();
	void setUser(String user);

	@Description("The password of the user with access to target repositories.")
	@Mandatory
	String getPassword();
	void setPassword(String password);

	@Description("The base url of the repository manager for all target repositories.")
	String getUrl();
	void setUrl(String url);

	@Description("The map of repository ids based on which artifacts can be restored to the mapped target repositories.")
	Map<String, String> getChangedRepositoryIds();
	void setChangedRepositoryIds(Map<String, String> changedRepositoryIds);

	@Override
	EvalContext<? extends Neutral> eval(Evaluator<ServiceRequest> evaluator);

}
