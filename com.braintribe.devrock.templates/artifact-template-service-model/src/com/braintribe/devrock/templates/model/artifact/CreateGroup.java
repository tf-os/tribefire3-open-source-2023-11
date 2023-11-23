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
package com.braintribe.devrock.templates.model.artifact;

import com.braintribe.devrock.templates.model.ArtifactTemplateRequest;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Description("Creates a group consisting of a group build script and the parent artifact.")
@PositionalArguments({"version", "buildSystem", "ide", "sourceControl"})
public interface CreateGroup extends ArtifactTemplateRequest {

	EntityType<CreateGroup> T = EntityTypes.T(CreateGroup.class);
	
	@Description("The group id of the group.")
	@Alias("gid")
	@Initializer("'${support.getFileName(request.installationPath)}'")
	String getGroupId();
	void setGroupId(String groupId);

	@Description("The initial version of the artifacts in the group.")
	@Alias("v")
	@Initializer("'1.0'")
	String getVersion();
	void setVersion(String version);
	
	@Description("The build system used in the group. Currently available options are 'bt-ant' and 'maven'.")
	@Alias("bs")
	@Initializer("'bt-ant'")
	String getBuildSystem();
	void setBuildSystem(String buildSystem);
	
	@Description("The IDE to create project metadata for. Currently only 'eclipse' is available.")
	@Initializer("'eclipse'")
	String getIde();
	void setIde(String ide);
	
	@Description("The source control to create group configuration for. Currently only 'git' is available.")
	@Alias("sc")
	@Initializer("'git'")
	String getSourceControl();
	void setSourceControl(String sourceControl);
	
	@Override
	default String template() {
		return "com.braintribe.devrock.templates:group-template#2.0";
	}

}
