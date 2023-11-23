// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.wire.artifact;

import static java.util.Collections.singletonList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.build.cmd.assets.wire.artifact.contract.ArtifactResolutionContract;
import com.braintribe.devrock.env.api.DevEnvironment;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocator;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.DevelopmentEnvironmentContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationLocatorContract;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.js.JsResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.workspace.WorkspaceRepositoryModule;
import com.braintribe.devrock.model.repository.WorkspaceRepository;
import com.braintribe.devrock.model.repository.filters.AllMatchingArtifactFilter;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.FileResource;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.asset.resolving.ng.wire.AssetResolverWireModule;

/**
 * @author peter.gazdik
 */
public class ArtifactResolutionWireModule implements WireTerminalModule<ArtifactResolutionContract>, DevelopmentEnvironmentContract {

	private final static PartIdentification PART_IDENTIFICATION_ASSET = PartIdentification.create("asset", "man");
	private final File developmentEnvironmentRoot;
	private final List<WireModule> dependencies = new ArrayList<>();
	private final RepositoryConfigurationLocator repositoryConfigurationLocator;

	public ArtifactResolutionWireModule(VirtualEnvironment virtualEnvironment, boolean jsDebug) {
		this(null, virtualEnvironment, jsDebug);
	}
	
	public ArtifactResolutionWireModule(RepositoryConfigurationLocator repositoryConfigurationLocator, VirtualEnvironment virtualEnvironment, boolean jsDebug) {
		this.repositoryConfigurationLocator = repositoryConfigurationLocator;
		this.developmentEnvironmentRoot = resolveDevEnvironmentRoot();
		
		dependencies.add(ClasspathResolverWireModule.INSTANCE);
		dependencies.add(AssetResolverWireModule.INSTANCE);
		dependencies.add(JsResolverWireModule.INSTANCE);
		dependencies.add(new EnvironmentSensitiveConfigurationWireModule(virtualEnvironment));
		
		if (jsDebug)
			maybeAddJsWorkspaceRepoModuleDependency();
	}

	private File resolveDevEnvironmentRoot() {
		return AttributeContexts.peek().findAttribute(DevEnvironment.class)//
				.map(DevEnvironment::getRootPath) //
				.orElse(null);
	}
	
	private void maybeAddJsWorkspaceRepoModuleDependency() {
		if (developmentEnvironmentRoot == null)
			return;

		File gitFolder = new File(developmentEnvironmentRoot, "git");
		if (!gitFolder.isDirectory())
			return;

		Set<Artifact> jsCodebaseArtifacts = scanCodebaseArtifacts(gitFolder);
		if (jsCodebaseArtifacts.isEmpty())
			return;

		WorkspaceRepository workspaceRepository = WorkspaceRepository.T.create();
		workspaceRepository.setArtifacts(jsCodebaseArtifacts);
		workspaceRepository.setCachable(false);
		workspaceRepository.setDominanceFilter(AllMatchingArtifactFilter.T.create());
		workspaceRepository.setName("codebase");

		dependencies.add(new WorkspaceRepositoryModule(singletonList(workspaceRepository)));
	}

	private Set<Artifact> scanCodebaseArtifacts(File gitFolder) {
		Set<Artifact> artifacts = new HashSet<>();
		scanCodebaseArtifacts(gitFolder, artifacts);
		return artifacts;
	}
	
	private void scanCodebaseArtifacts(File folder, Set<Artifact> artifacts) {
		File pomFile = new File(folder, "pom.xml");
		
		if (pomFile.exists()) {
			File jsProjectFile = new File(folder, "js-project.yml");
			
			if (!jsProjectFile.exists())
				return;

			Maybe<CompiledArtifact> extractMinimalArtifact = DeclaredArtifactIdentificationExtractor.extractMinimalArtifact(pomFile);
			
			if (!extractMinimalArtifact.isSatisfied()) {
				// TODO: log or print warning
				return;
			}
			
			CompiledArtifact compiledArtifact = extractMinimalArtifact.get();
			
			Artifact artifact = Artifact.T.create();
			artifact.setGroupId(compiledArtifact.getGroupId());
			artifact.setArtifactId(compiledArtifact.getArtifactId());
			artifact.setVersion(compiledArtifact.getVersion().asString());
			artifact.setArchetype(compiledArtifact.getArchetype());
			artifact.setPackaging(compiledArtifact.getPackaging());
			
			addPartFromFile(artifact, pomFile, PartIdentifications.pom);

			File assetFile = new File(folder, "asset.man");
			
			if (assetFile.exists()) {
				addPartFromFile(artifact, assetFile, PART_IDENTIFICATION_ASSET);
			}
			
			artifacts.add(artifact);
			
			return;
		}
		
		for (File file: folder.listFiles()) {
			if (file.isDirectory()) {
				scanCodebaseArtifacts(file, artifacts);
			}
		}
	}

	private void addPartFromFile(Artifact artifact, File assetFile, PartIdentification partIdentification) {
		Part part = Part.T.create();
		part.setClassifier(partIdentification.getClassifier());
		part.setType(partIdentification.getType());
		
		FileResource resource = FileResource.T.create();
		resource.setPath(assetFile.getAbsolutePath());
		resource.setName(assetFile.getName());
		resource.setFileSize(assetFile.length());

		part.setResource(resource);
		part.setRepositoryOrigin("codebase");
		
		artifact.getParts().put(partIdentification.asString(), part);
	}

	@Override
	public List<WireModule> dependencies() {
		return dependencies;
	}

	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		contextBuilder.bindContract(DevelopmentEnvironmentContract.class, this);
		
		if (repositoryConfigurationLocator != null)
			contextBuilder.bindContract(RepositoryConfigurationLocatorContract.class, () -> repositoryConfigurationLocator);
	}
	
	@Override
	public File developmentEnvironmentRoot() {
		return developmentEnvironmentRoot;
	}

}
