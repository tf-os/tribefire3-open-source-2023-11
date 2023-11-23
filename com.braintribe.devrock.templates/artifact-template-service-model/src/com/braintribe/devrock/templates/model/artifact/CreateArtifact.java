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

import java.util.List;

import com.braintribe.devrock.templates.model.ArtifactTemplateRequest;
import com.braintribe.devrock.templates.model.Dependency;
import com.braintribe.devrock.templates.model.Property;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
@PositionalArguments({ "artifactId", "version", "buildSystem", "ide", "sourceControl" })
public interface CreateArtifact extends ArtifactTemplateRequest {

	EntityType<CreateArtifact> T = EntityTypes.T(CreateArtifact.class);

	@Initializer("'${request.artifactId}'")
	@Override
	String getDirectoryName();

	@Description("The group id of the projected artifact. If not specified the installation directory name will be used.")
	@Alias("gid")
	@Initializer("'${support.getFileName(request.installationPath)}'")
	String getGroupId();
	void setGroupId(String groupId);

	@Description("The artifact id of the projected artifact.")
	@Alias("aid")
	@Mandatory
	String getArtifactId();
	void setArtifactId(String artifactId);

	@Description("The version of the projected artifact. If no version is specified, we try to inference the version from the parent artifact, and if that fails, default '1.0' is used.")
	@Alias("v")
	@Initializer("'${support.getDefaultArtifactVersion(request.installationPath, request.buildSystem)}'")
	String getVersion();
	void setVersion(String version);

	@Description("Specifies whether or not the projected artifact has parent.")
	@Alias("hp")
	@Initializer("true")
	boolean getHasParent();
	void setHasParent(boolean hasParent);

	@Description("The parent artifact id of the projected artifact. Ignored if hasParent is set to 'false'.")
	@Alias("paid")
	@Initializer("'parent'")
	String getParentArtifactId();
	void setParentArtifactId(String parentArtifactId);

	@Description("The build system to create configuration for. Currently available options are 'bt-ant' and 'maven'.")
	@Alias("bs")
	@Initializer("'bt-ant'")
	String getBuildSystem();
	void setBuildSystem(String buildSystem);

	@Description("The IDE to create project metadata for. Currently only 'eclipse' is available.")
	@Initializer("'eclipse'")
	String getIde();
	void setIde(String ide);

	@Description("The source control to create configuration for. Currently only 'git' is available.")
	@Alias("sc")
	@Initializer("'git'")
	String getSourceControl();
	void setSourceControl(String sourceControl);

	@Description("The properties of the projected artifact.")
	@Alias("props")
	List<Property> getProperties();
	void setProperties(List<Property> properties);

	@Description("The dependencies of the projected artifact.")
	@Alias("deps")
	List<Dependency> getDependencies();
	void setDependencies(List<Dependency> dependencies);

}
