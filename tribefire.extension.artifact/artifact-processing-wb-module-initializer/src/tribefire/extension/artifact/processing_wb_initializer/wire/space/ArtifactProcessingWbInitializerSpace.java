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
package tribefire.extension.artifact.processing_wb_initializer.wire.space;

import static com.braintribe.wire.api.util.Lists.list;
import static com.braintribe.wire.api.util.Sets.set;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.folder.FolderContent;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.artifact.processing_wb_initializer.wire.contract.ArtifactProcessingWbInitializerContract;
import tribefire.extension.artifact.processing_wb_initializer.wire.contract.ArtifactProcessingWbResourceContract;

@Managed
public class ArtifactProcessingWbInitializerSpace extends AbstractInitializerSpace implements ArtifactProcessingWbInitializerContract {
	@Import
	ArtifactProcessingWbResourceContract resources;
	
	@Managed
	@Override
	public Folder artifactProcessingFolder() {
		Folder bean = create(Folder.T).initFolder("artifactProcessing", "Artifact Processing");
		
		bean.getSubFolders().addAll(list(
				repositoryFolder(),
				resolutionFolder(),
				assetsFolder(),
				overridesFolder()
				));
		
		return bean;
	}

	@Managed
	@Override
	public Folder repositoryFolder() {
		Folder bean = create(Folder.T).initFolder("Repository", "Repository");
		
		bean.getSubFolders().addAll(list(
				mavenConfigurationFolder(),
				simplifiedConfigurationFolder(),
				repositoryPolicyFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public Folder overridesFolder() {
		Folder bean = create(Folder.T).initFolder("overrides", "Overrides");
		
		bean.getSubFolders().addAll(list(
				environmentVariablesOverridesFolder(),
				systemPropertyOverridesFolder()));
		
		return bean;
	}
	
	@Managed
	@Override
	public Folder environmentVariablesOverridesFolder() {
		Folder bean = create(Folder.T).initFolder("environmentVariablesOverrides", "Environment Variables");
		
		bean.setIcon(configIcon());
		bean.setContent(environmentVariablesOverridesQueryAction());
		
		return bean;
	}
	
	private FolderContent environmentVariablesOverridesQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		
		bean.setTypeSignature("com.braintribe.model.artifact.processing.cfg.env.OverridingEnvironmentVariable");
		
		return bean;
	}

	@Managed
	@Override
	public Folder systemPropertyOverridesFolder() {
		Folder bean = create(Folder.T).initFolder("systemPropertyOverridesOverrides", "System Properties");
		
		bean.setIcon(configIcon());
		bean.setContent(systemPropertyOverridesOverridesQueryAction());
		
		return bean;
	}
	
	private FolderContent systemPropertyOverridesOverridesQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		
		bean.setTypeSignature("com.braintribe.model.artifact.processing.cfg.env.OverridingSystemProperty");
		
		return bean;
	}

	@Managed
	@Override
	public Folder assetsFolder() {
		Folder bean = create(Folder.T).initFolder("assets", "Assets");
		
		bean.getSubFolders().add(assetContextsFolder());
		
		return bean;
	}
	
	@Managed
	@Override
	public Folder assetContextsFolder() {
		Folder bean = create(Folder.T).initFolder("assetContexts", "Asset Contexts");
		
		bean.setIcon(configIcon());
		bean.setContent(assetContextConfigurationQueryAction());
		
		return bean;
	}
	
	@Managed
	private SimpleQueryAction assetContextConfigurationQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		
		bean.setTypeSignature("com.braintribe.model.artifact.processing.cfg.asset.AssetContextConfiguration");
		
		return bean;
	}

	@Override
	@Managed
	public Folder mavenConfigurationFolder() {
		Folder bean = create(Folder.T).initFolder("mavenConfiguration", "Maven Configuration");
		
		bean.setIcon(configIcon());
		bean.setContent(mavenRepositoryConfigurationQueryAction());
		
		return bean;
	}
	
	@Managed
	private SimpleQueryAction mavenRepositoryConfigurationQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		
		bean.setTypeSignature("com.braintribe.model.artifact.processing.cfg.repository.MavenRepositoryConfiguration");
		
		return bean;
	}

	@Override
	@Managed
	public Folder simplifiedConfigurationFolder() {
		Folder bean = create(Folder.T).initFolder("simplifiedConfiguration", "Simplified Configuration");
		
		bean.setIcon(configIcon());
		bean.setContent(simplifiedRepositoryConfigurationQueryAction());
		
		return bean;
	}
	
	@Managed
	private SimpleQueryAction simplifiedRepositoryConfigurationQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		
		bean.setTypeSignature("com.braintribe.model.artifact.processing.cfg.repository.SimplifiedRepositoryConfiguration");
		
		return bean;
	}

	@Override
	@Managed
	public Folder repositoryPolicyFolder() {
		Folder bean = create(Folder.T).initFolder("policies", "Policies");
		
		bean.setIcon(configIcon());
		bean.setContent(repositoryPolicyQueryAction());
		
		return bean;
	}
	
	@Managed
	private SimpleQueryAction repositoryPolicyQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		
		bean.setTypeSignature("com.braintribe.model.artifact.processing.cfg.repository.details.RepositoryPolicy");
		
		return bean;
	}
	
	@Managed
	@Override
	public Folder resolutionFolder() {
		Folder bean = create(Folder.T).initFolder("resolution", "Resolution");
		
		bean.getSubFolders().add(resolutionConfigurationFolder());
		
		return bean;
	}

	@Override
	@Managed
	public Folder resolutionConfigurationFolder() {
		Folder bean = create(Folder.T).initFolder("resolutionConfiguration", "Resolution Configuration");
		
		bean.setIcon(configIcon());
		bean.setContent(resolutionConfigurationQueryAction());
		
		return bean;
	}
	
	@Managed
	private SimpleQueryAction resolutionConfigurationQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		
		bean.setTypeSignature("com.braintribe.model.artifact.processing.cfg.resolution.ResolutionConfiguration");
		
		return bean;
	}
	
	@Managed
	private AdaptiveIcon configIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("Config Icon");
		bean.setRepresentations(set(
				resources.config24Png(),
				resources.config32Png(),
				resources.config64Png()
				));
		
		return bean;
	}
}
