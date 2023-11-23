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
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Description("Creates the artifact source control configuration files.")
public interface CreateSourceControlConfig extends ArtifactTemplateRequest {

	EntityType<CreateSourceControlConfig> T = EntityTypes.T(CreateSourceControlConfig.class);
	
	@Description("The source control name. Currently only 'git' is available.")
	@Alias("sc")
	@Initializer("'git'")
	String getSourceControl();
	void setSourceControl(String sourceControl);
	
	@Description("The build system name. If provided, the standard build system output files will be ignored. Currently available options are 'bt-ant' and 'maven'.")
	@Alias("bs")
	String getBuildSystem();
	void setBuildSystem(String buildSystem);
	
	@Description("The paths of the files to be ignored when checking artifact into source control.")
	@Alias("if")
	List<String> getIgnoredFiles();
	void setIgnoredFiles(List<String> ignoredFiles);
	
	@Override
	default String template() {
		return "com.braintribe.devrock.templates:source-control-configuration-template#2.0";
	}
	
}
