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
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Description("Creates the artifact build system configuration files.")
@PositionalArguments({"groupId", "artifactId", "version", "artifactType", "packaging"})
public interface CreateBuildSystemConfig extends ArtifactTemplateRequest {

	EntityType<CreateBuildSystemConfig> T = EntityTypes.T(CreateBuildSystemConfig.class);
	
	@Description("The build system name. Currently available options are 'bt-ant' and 'maven'.")
	@Alias("bs")
	@Initializer("'bt-ant'")
	String getBuildSystem();
	void setBuildSystem(String buildSystem);
	
	@Description("The group id of the artifact.")
	@Alias("gid")
	@Mandatory
	String getGroupId();
	void setGroupId(String groupId);

	@Description("The artifact id of the artifact.")
	@Alias("aid")
	@Mandatory
	String getArtifactId();
	void setArtifactId(String artifactId);
	
	@Description("The version of the artifact.")
	@Alias("v")
	@Initializer("'1.0'")
	String getVersion();
	void setVersion(String version);
	
	@Description("Specifies whether or not the artifact has parent.")
	@Alias("hp")
	@Initializer("true")
	boolean getHasParent();
	void setHasParent(boolean hasParent);
	
	@Description("The parent artifact id of the artifact. Ignored if hasParent is set to 'false'.")
	@Alias("paid")
	@Initializer("'parent'")
	String getParentArtifactId();
	void setParentArtifactId(String parentArtifactId);
	
	@Description("The artifact type, e.g. 'common', 'library', 'model', ... If not specified, the build system default is used.")
	@Alias("at")
	@Mandatory
	String getArtifactType();
	void setArtifactType(String artifactType);
	
	@Description("The packaging of the artifact, e.g. 'jar', 'war', ... If not specified, the build system default is used.")
	@Alias("p")
	String getPackaging();
	void setPackaging(String packaging);
	
	@Description("The properties of the artifact.")
	@Alias("props")
	List<Property> getProperties();
	void setProperties(List<Property> properties);
	
	@Description("The dependencies of the artifact.")
	@Alias("deps")
	List<Dependency> getDependencies();
	void setDependencies(List<Dependency> dependencies);
	
	@Description("The managed dependencies of the artifact.")
	@Alias("mdeps")
	List<Dependency> getManagedDependencies();
	void setManagedDependencies(List<Dependency> managedDependencies);
	
	@Description("The paths of the artifact resources to be included in the build.")
	List<String> getResources();
	void setResources(List<String> resources);
	
	@Override
	default String template() {
		return "com.braintribe.devrock.templates:build-system-template#2.0";
	}
	
}
