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
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Updates the (major.minor) version of an artifact group. For further information see type and properties descriptions.
 * 
 * @author michael.lafite
 */
@Description("Updates the (major.minor) version of the specified artifact group, e.g. from 1.0 to 1.1 or 2.0."
		+ " This is done by iterating through all the artifacts in the specified artifact group folder and adapting the respective POM files."
		+ " The revisions of all artifacts will be reset, i.e. with the switch to version '2.0' the next published version of all artifacts in the group will be '2.0.1'."
		+ " The version to update to can either be specified directly, see 'version', or one can increment the major or minor version, see 'incrementMajor' and 'incrementMinor'."
		+ " These options cannot be combined.")
public interface UpdateGroupVersion extends SetupRequest {
	EntityType<UpdateGroupVersion> T = EntityTypes.T(UpdateGroupVersion.class);

	public static final String groupFolder = "groupFolder";
	
	@Override
	EvalContext<List<String>> eval(Evaluator<ServiceRequest> evaluator);

	@Description("The root folder of the artifact group to update. By default, the version update will be performed in the current working directory.")
	@Initializer("'.'")
	String getGroupFolder();
	void setGroupFolder(String groupFolder);

	@Description("The version to update to. Alternatively one may also specify to increment the major or minor version, see 'incrementMajor' and 'incrementMinor'.")
	String getVersion();
	void setVersion(String version);

	@Description("Enabling this increments the major version, e.g. from 2.3 to 3.0. Alternatively one may also specify a concrete version, see 'version' setting.")
	boolean getIncrementMajor();
	void setIncrementMajor(boolean incrementMajor);

	@Description("Enabling this increments the minor version, e.g. from 2.3 to 2.4. Alternatively one may also specify a concrete version, see 'version' setting.")
	boolean getIncrementMinor();
	void setIncrementMinor(boolean incrementMinor);
}
