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
package com.braintribe.devrock.artifactcontainer.control.walk.resolver;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.retrieval.multi.coding.SolutionWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.build.artifact.retrieval.multi.resolving.AbstractDependencyResolverImpl;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.ResolvingException;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.control.workspace.WorkspaceProjectRegistry;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;

/**
 * an implementation of the {@link MultiRepositoryDependencyResolver} that reflects on the workspace
 * dependency resolving is delegated anyhow
 * 
 * @author pit
 *
 */
public class WorkspaceDependencyResolver extends AbstractDependencyResolverImpl implements DependencyResolver {

	@Override
	protected String getPomName(Part part) throws ResolvingException {
		return "pom.xml";
	}
	

	@Override
	protected String getPartLocation(Part pomPart) throws ResolvingException{
		try {
			String partLocation = RepositoryReflectionHelper.getHotfixSavySolutionFilesystemLocation( locationExpert.getLocalRepository(null), pomPart) + File.separator + getPomName( pomPart);
			return partLocation;
		} catch (RavenhurstException e) {
			throw new ResolvingException(e);			
		} catch (RepresentationException e) {
			throw new ResolvingException(e);
		}
	}

	
	

	@Override
	public Set<Solution> resolveMatchingDependency(String walkScopeId, Dependency dependency) throws ResolvingException {
		return delegate.resolveMatchingDependency(walkScopeId, dependency);
	}


	@Override
	public Set<Solution> resolveTopDependency(String walkScopeId, Dependency dependency) throws ResolvingException {
		return resolveDependencyInWorkspace(walkScopeId, dependency);
	}



	private Set<Solution> resolveDependencyInWorkspace(String walkId, Dependency dependency) throws ResolvingException {
		Set<Solution> result = CodingSet.createHashSetBased( new SolutionWrapperCodec());
		// query the workspace for solutions
		WorkspaceProjectRegistry workspaceProjectRegistry = ArtifactContainerPlugin.getWorkspaceProjectRegistry();
		List<IProject> projects = workspaceProjectRegistry.getLoadedArtifacts(dependency);		
		if (projects != null) {			
			for (IProject project : projects) {
				if (!project.isAccessible()) {
					continue;
				}
				
				Artifact artifact = workspaceProjectRegistry.getArtifactForProject(project);
				
				if (!VersionRangeProcessor.matches( dependency.getVersionRange(), artifact.getVersion())) {
					continue;
				}
				
				Solution solution = Solution.T.create();
				ArtifactProcessor.transferIdentification(solution, artifact);
				solution.setVersion( artifact.getVersion());
				result.add(solution);
				
				Part pomPart = Part.T.create();
				pomPart.setType( PartTupleProcessor.createPomPartTuple());
				ArtifactProcessor.transferIdentification(pomPart, artifact);
				pomPart.setVersion( artifact.getVersion());
				IResource pomResource = project.findMember( "pom.xml");
				
				if (pomResource == null) {
					continue;
				}
				
				pomPart.setLocation( pomResource.getLocation().toOSString());				
				solution.getParts().add(pomPart);
			}
		} 
		// add any others from the delegate 
		Set<Solution> delegatesSolutions = delegate.resolveTopDependency(walkId, dependency);
		// modified resolvers return only *one* matching solutions (if any)
		// logically, this version here has a higher importance than the one from the delegate,
		// so this is only of interest, if we do not have a solution in the WS, 
		// otherwise, our's the one that counts
		if (delegatesSolutions != null && !delegatesSolutions.isEmpty() && result.isEmpty()) {
			result.addAll(delegatesSolutions);
		}
		
		return result;
	}

	@Override
	public Part resolvePom(String walkId, Identification id, Version version) throws ResolvingException {
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( id.getGroupId());
		artifact.setArtifactId( id.getArtifactId());
		artifact.setVersion(version);
		IProject project = ArtifactContainerPlugin.getWorkspaceProjectRegistry().getProjectForArtifact(artifact);
		if (project == null) {
			return delegate.resolvePom(walkId, id, version);
		}
		IResource pomResource = project.findMember( "pom.xml");
		if (pomResource != null) {
			Part pomPart = Part.T.create();
			pomPart.setGroupId( id.getGroupId());
			pomPart.setArtifactId( id.getArtifactId());
			pomPart.setVersion(version);
			pomPart.setLocation( pomResource.getLocation().toOSString());
			return pomPart;
		}
		if (delegate != null) {
			return delegate.resolvePom(walkId, id, version);
		}
		return null;
	}

	@Override
	public Part resolvePomPart(String walkId, Part part) throws ResolvingException {
		
		IProject project = ArtifactContainerPlugin.getWorkspaceProjectRegistry().getProjectForArtifact( part);
		if (project == null) {
			return delegate.resolvePomPart(walkId, part);
		}
		IResource pomResource = project.findMember( "pom.xml");
		if (pomResource != null) {
			part.setLocation( pomResource.getLocation().toOSString());
			return part;
		}
		if (delegate != null) {
			return delegate.resolvePomPart(walkId, part);
		}
		return null;
	}
	

}
