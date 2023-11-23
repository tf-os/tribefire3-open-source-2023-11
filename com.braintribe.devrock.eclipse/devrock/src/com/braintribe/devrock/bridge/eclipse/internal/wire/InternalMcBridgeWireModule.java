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
package com.braintribe.devrock.bridge.eclipse.internal.wire;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.braintribe.devrock.bridge.eclipse.internal.wire.contract.InternalMcBridgeContract;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectInfo;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.DevelopmentEnvironmentContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.workspace.WorkspaceRepositoryModule;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.WorkspaceRepository;
import com.braintribe.devrock.model.repository.filters.AllMatchingArtifactFilter;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.resource.FileResource;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

/**
 * @author dirk / pit
 *
 */
public class InternalMcBridgeWireModule implements WireTerminalModule< InternalMcBridgeContract>{

	private static final String WORKSPACE = "eclipse-projects";
	private WorkspaceRepository workspaceRepository;
	private List<WireModule> dependencies = new ArrayList<>();
	private File devEnvRootFolder;
	private RepositoryConfiguration repositoryConfiguration;

	
	
	public InternalMcBridgeWireModule(List<WorkspaceProjectInfo> projectinfos, RepositoryConfiguration repositoryConfiguration) {
		super();
		this.repositoryConfiguration = repositoryConfiguration;		
		
		// if passed repository configuration is null, we build up the standard for plugins  
		if (repositoryConfiguration == null) {
			workspaceRepository = WorkspaceRepository.T.create();
			workspaceRepository.setName(WORKSPACE);
			workspaceRepository.setCachable( false);
			workspaceRepository.setDominanceFilter( AllMatchingArtifactFilter.T.create());
			
			for (WorkspaceProjectInfo projectInfo : projectinfos) {		
				Artifact workspaceArtifact = buildWorkspaceArtifact(projectInfo);		
				workspaceRepository.getArtifacts().add( workspaceArtifact);						
			}
			
			dependencies.add(new EnvironmentSensitiveConfigurationWireModule( DevrockPlugin.instance().virtualEnviroment()));
			dependencies.add( new WorkspaceRepositoryModule( Collections.singletonList( workspaceRepository)));
			dependencies.add( ClasspathResolverWireModule.INSTANCE);
	
			Optional<File> optional = DevrockPlugin.instance().getDevEnvironmentRoot();
			if (optional.isPresent()) {
				devEnvRootFolder = optional.get();
			}
		}
		else {
			// this one has an external cfg, hence not to be modified by the injection of the workspace repository..
			dependencies.add(new EnvironmentSensitiveConfigurationWireModule( DevrockPlugin.instance().virtualEnviroment()));
			dependencies.add( ClasspathResolverWireModule.INSTANCE);
			
			Optional<File> optional = DevrockPlugin.instance().getDevEnvironmentRoot();
			if (optional.isPresent()) {
				devEnvRootFolder = optional.get();
			}
		}
	}

	
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		
		// if overriding repository configuration has been passed, use this
		if (repositoryConfiguration != null) {
			contextBuilder.bindContract( RepositoryConfigurationContract.class, () -> Maybe.complete( repositoryConfiguration));
		}
		else if (devEnvRootFolder != null) {
			contextBuilder.bindContract( DevelopmentEnvironmentContract.class, () -> devEnvRootFolder);
		}
				
	}



	@Override
	public List<WireModule> dependencies() {
		return dependencies;
	}
	
	
	private Artifact buildWorkspaceArtifact(WorkspaceProjectInfo wpi) {
		Artifact artifact = AnalysisArtifact.T.create();
		
		VersionedArtifactIdentification cai = wpi.getVersionedArtifactIdentification();
		artifact.setGroupId( cai.getGroupId());
		artifact.setArtifactId( cai.getArtifactId());
		artifact.setVersion( cai.getVersion());
		
		PartIdentification pomPartIdentification = PartIdentification.create("pom");
		Part pomPart = buildPart( wpi.getProjectFolder(), pomPartIdentification);
		
		artifact.getParts().put( pomPartIdentification.asString(), pomPart);
		
		PartIdentification sourcesPartIdentification = PartIdentification.create("sources");
		File sourceFolder = wpi.getSourceFolder();
		if (sourceFolder != null) {
			Part sourcesPart = buildPart( sourceFolder, sourcesPartIdentification);
			artifact.getParts().put( sourcesPartIdentification.asString(), sourcesPart);
		}
		
		PartIdentification jarPartIdentification = PartIdentification.create("jar");
		File binariesFolder = wpi.getBinariesFolder();
		if (binariesFolder != null) {
			Part jarPart = buildPart( binariesFolder, jarPartIdentification);
			artifact.getParts().put( jarPartIdentification.asString(), jarPart);
		}
		
		
		return artifact;
	}
	
	private Part buildPart(File folder, PartIdentification pi) {
		Part part = Part.T.create();
		part.setClassifier( pi.getClassifier());
		part.setType( pi.getType());
		
		FileResource resource = FileResource.T.create();
		if (pi.getType().equals("pom")) {
			resource.setName("pom.xml");
			resource.setPath( new File( folder, "pom.xml").getAbsolutePath());
		}
		else {
			resource.setName( folder.getAbsolutePath() + "/");
			resource.setPath( folder.getAbsolutePath());
		}
		part.setResource(resource);
		part.setRepositoryOrigin(WORKSPACE);
		
		return part;
		
	}
	
}
