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
package com.braintribe.template.processing.wire.space;

import java.io.File;

import com.braintribe.devrock.templates.config.model.ArtifactTemplatesConfiguration;
import com.braintribe.gm.config.wire.contract.ModeledConfigurationContract;
import com.braintribe.template.processing.ArtifactTemplateProcessor;
import com.braintribe.template.processing.projection.ArtifactTemplateFreeMarkerProjector;
import com.braintribe.template.processing.projection.ArtifactTemplateRequestFreeMarkerProjector;
import com.braintribe.template.processing.wire.contract.ArtifactTemplateProcessingContract;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import freemarker.template.Configuration;
import freemarker.template.Version;

@Managed
public class ArtifactTemplateProcessingSpace implements ArtifactTemplateProcessingContract {

	private static final Version FREEMARKER_VERSION = Configuration.VERSION_2_3_28;

	@Import
	private ModeledConfigurationContract modelledConfiguration;

	@Managed
	@Override
	public ArtifactTemplateProcessor artifactTemplateProcessor() {
		ArtifactTemplatesConfiguration config = modelledConfiguration.config(ArtifactTemplatesConfiguration.T);
		String repositoryConfigurationLocation = config.getRepositoryConfigurationLocation();
		
		ArtifactTemplateProcessor bean = new ArtifactTemplateProcessor();
		bean.setVirtualEnvironment(virtualEnvironment());
		bean.setRequestProjector(requestProjector());
		bean.setTemplateProjector(templateProjector());
		bean.setModeledConfiguration(modelledConfiguration.config());
		
		if (repositoryConfigurationLocation != null)
			bean.setUseCaseRepositoryConfigurationLocation(new File(repositoryConfigurationLocation));
		return bean;
	}

	@Managed
	private ArtifactTemplateRequestFreeMarkerProjector requestProjector() {
		ArtifactTemplateRequestFreeMarkerProjector bean = new ArtifactTemplateRequestFreeMarkerProjector(FREEMARKER_VERSION, modelledConfiguration.config());
		return bean;
	}

	@Managed
	private ArtifactTemplateFreeMarkerProjector templateProjector() {
		ArtifactTemplateFreeMarkerProjector bean = new ArtifactTemplateFreeMarkerProjector(FREEMARKER_VERSION, modelledConfiguration.config());
		return bean;
	}

	@Managed
	private VirtualEnvironment virtualEnvironment() {
		OverridingEnvironment bean = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		bean.setEnv("PROFILE_USECASE", "CORE");
		return bean;
	}

}