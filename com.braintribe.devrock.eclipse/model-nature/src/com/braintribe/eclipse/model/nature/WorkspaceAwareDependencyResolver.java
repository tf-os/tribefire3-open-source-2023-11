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
package com.braintribe.eclipse.model.nature;


import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.ResolvingException;
import com.braintribe.build.artifact.retrieval.multi.resolving.listener.DependencyResolverNotificationListener;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionMetricTuple;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;

/**
 * 
 * @author Dirk Scheffler
 *
 */
public class WorkspaceAwareDependencyResolver implements DependencyResolver {
	private DependencyResolver delegate;
	private Map<VersionedIdentification, Part> cachedPomParts = new HashMap<>();
	private ArtifactPomReader pomReader;

	@Required
	public void setPomReader(ArtifactPomReader pomReader) {
		this.pomReader = pomReader;
	}
	
	@Required
	public void setDelegate(DependencyResolver delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public void addListener(DependencyResolverNotificationListener listener) {
		delegate.addListener(listener);
		
	}

	@Override
	public void removeListener(DependencyResolverNotificationListener listener) {
		delegate.removeListener(listener);
	}
	
	

	@Override
	public Set<Solution> resolveMatchingDependency(String arg0, Dependency arg1) throws ResolvingException {	
		throw new UnsupportedOperationException("not implemented for this class");
	}

	@Override
	public Set<Solution> resolveTopDependency(String scope, Dependency dependency) throws ResolvingException {
		Version version = getSimplifiedVersion(dependency);
		VersionMetricTuple versionMetric = VersionProcessor.getVersionMetric(version);
		String majorMinor = versionMetric.major + "." + versionMetric.minor;
		String projectName = dependency.getArtifactId() + "-" + majorMinor;
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject projectCandidate = workspaceRoot.getProject(projectName);
		
		if (projectCandidate.exists() && projectCandidate.isAccessible()) {
			Pair<Solution, Part> solutionAndPart = getSolutionIfMatching(scope, projectCandidate, dependency);
			
			if (solutionAndPart != null) {
				Solution solution = solutionAndPart.getFirst();
				Part part = solutionAndPart.getSecond();
				VersionedIdentification versionedIdentification = new VersionedIdentification(solution, solution.getVersion());
				cachedPomParts.put(versionedIdentification, part);
				
				return Collections.singleton(solution);
			}
		}

		return delegate.resolveTopDependency(scope, dependency);
	}
	
	private Pair<Solution, Part> getSolutionIfMatching(String scope, IProject projectCandidate, Dependency dependency) {
		IResource pomResource = projectCandidate.findMember("pom.xml");
		
		if (pomResource == null)
			return null;
		
		String pomFileName = pomResource.getLocation().toString();
		
		Solution solution;
		try {
			solution = pomReader.readPom(scope, new File(pomFileName));
		} catch (PomReaderException e) {
			throw Exceptions.unchecked(e, "Error while reading pom", IllegalStateException::new);
		}
		
		String groupId = solution.getGroupId();
		String artifactId = solution.getGroupId();
		Version version = solution.getVersion();
		
		boolean matches = dependency.getGroupId().equals(groupId) && 
				dependency.getArtifactId().equals(artifactId) && 
				VersionRangeProcessor.matches(dependency.getVersionRange(), version);
		
		if (matches) {
			Part pomPart = PartProcessor.createPartFromIdentification(solution, solution.getVersion(), PartTupleProcessor.createPomPartTuple());
			pomPart.setLocation(pomFileName);
			return new Pair<>(solution, pomPart);
		}
		else {
			return null;
		}
	}

	private Version getSimplifiedVersion(Dependency dependency) {
		VersionRange versionRange = dependency.getVersionRange();
		
		Version version = versionRange.getInterval()? 
				versionRange.getMinimum(): 
				versionRange.getDirectMatch();
		return version;
	}

	@Override
	public Part resolvePom(String scope, Identification identification, Version version) throws ResolvingException {
		Part pomPart = cachedPomParts.get(new VersionedIdentification(identification, version));
		
		if (pomPart != null) {
			return pomPart;
		}
		else  {
			return delegate.resolvePom(scope, identification, version);
		}
	}

	@Override
	public Part resolvePomPart(String scope, Part part) throws ResolvingException {
		return resolvePom(scope, part, part.getVersion());
	}

	private static class VersionedIdentification {
		private Identification identification;
		private Version version;
		public VersionedIdentification(Identification identification, Version version) {
			super();
			this.identification = identification;
			this.version = version;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			
			String groupId = identification.getGroupId();
			String artifactId = identification.getArtifactId();
			
			result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
			result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
			
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			VersionedIdentification other = (VersionedIdentification) obj;
			if (identification == null) {
				if (other.identification != null)
					return false;
			} else if (!ArtifactProcessor.identificationEquals(identification, other.identification))
				return false;
			if (version == null) {
				if (other.version != null)
					return false;
			} else if (VersionProcessor.compare(version, other.version) != 0)
				return false;
			return true;
		}
		
	}
}
