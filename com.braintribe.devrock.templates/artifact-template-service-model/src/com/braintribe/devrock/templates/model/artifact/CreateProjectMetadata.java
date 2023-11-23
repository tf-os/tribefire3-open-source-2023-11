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
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Description("Creates the artifact project metadata files.")
@PositionalArguments({"projectName", "sourceDirectory", "outputDirectory"})
public interface CreateProjectMetadata extends ArtifactTemplateRequest {

	EntityType<CreateProjectMetadata> T = EntityTypes.T(CreateProjectMetadata.class);
	
	@Description("The name of the IDE configuration files are created for. Currently only 'eclipse' is available.")
	@Initializer("'eclipse'")
	String getIde();
	void setIde(String ide);
	
	@Description("The build system name. If provided, the standard build system project metadata entries will be added. Currently available options are 'bt-ant' and 'maven'.")
	@Alias("bs")
	String getBuildSystem();
	void setBuildSystem(String buildSystem);
	
	@Description("The name of the artifact project.")
	@Alias("pn")
	@Mandatory
	String getProjectName();
	void setProjectName(String projectName);
	
	@Description("The name of the artifact source directory. 'null' means no source directory.")
	@Alias("sd")
	String getSourceDirectory();
	void setSourceDirectory(String sourceDirectory);
	
	@Description("The name of the java builder output directory. Ignored if sourceDirectory is not set.")
	@Alias("od")
	@Initializer("'classes'")
	String getOutputDirectory();
	void setOutputDirectory(String outputDirectory);
	
	@Description("The names of the extra builder classes/lib output directories. Ignored if sourceDirectory is not set.")
	@Alias("bol")
	List<String> getBuilderOutputLibs();
	void setBuilderOutputLibs(List<String> builderOutputLibs);
	
	@Description("The class-path entries of the artifact. Ignored if sourceDirectory is not set.")
	@Alias("cpes")
	List<String> getClassPathEntries();
	void setClassPathEntries(List<String> classPathEntries);
	
	@Description("The natures of the artifact. Ignored if sourceDirectory is not set.")
	List<String> getNatures();
	void setNatures(List<String> natures);
	
	@Description("The builders of the artifact. Ignored if sourceDirectory is not set.")
	List<String> getBuilders();
	void setBuilders(List<String> builders);
	
	@Override
	default String template() {
		return "com.braintribe.devrock.templates:project-metadata-template#2.0";
	}
	
}
