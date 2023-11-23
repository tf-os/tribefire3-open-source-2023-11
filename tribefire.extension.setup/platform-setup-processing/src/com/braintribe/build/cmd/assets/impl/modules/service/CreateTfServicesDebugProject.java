package com.braintribe.build.cmd.assets.impl.modules.service;

import java.util.List;

import com.braintribe.devrock.templates.model.ArtifactTemplateRequest;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CreateTfServicesDebugProject extends ArtifactTemplateRequest {

	EntityType<CreateTfServicesDebugProject> T = EntityTypes.T(CreateTfServicesDebugProject.class);

	@Mandatory
	String getGroupId();
	void setGroupId(String groupId);

	@Mandatory
	String getArtifactId();
	void setArtifactId(String artifactId);

	@Mandatory
	String getVersion();
	void setVersion(String version);

	/**
	 * Dependencies to be written directly to the pom.xml Typically in this format:
	 * 
	 * {@code  <dependency><groupId>foo.bar</groupId><artifactId>xyz</artifactId><version>1.0.42</version><exclusions><exclusion/></exclusions></dependency>}
	 */
	@Mandatory
	List<String> getDependencies();
	void setDependencies(List<String> dependencies);

	@Override
	default String template() {
		return "tribefire.cortex.assets.templates:tf-services-debug-project-template#1.0";
	}

}
