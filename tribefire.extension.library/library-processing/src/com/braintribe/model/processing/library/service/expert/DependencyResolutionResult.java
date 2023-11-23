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
package com.braintribe.model.processing.library.service.expert;

import java.util.List;
import java.util.Set;

import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.utils.collection.api.MultiMap;

public class DependencyResolutionResult {

	private ArtifactIdentification resolvedArtifact;
	private Set<Artifact> dependencies;
	private MultiMap<Artifact, Artifact> transitiveDependersMap;
	private List<CompiledTerminal> terminalClasspathArtifacts;

	public ArtifactIdentification getResolvedArtifact() {
		return resolvedArtifact;
	}
	public void setResolvedArtifact(ArtifactIdentification resolvedArtifact) {
		this.resolvedArtifact = resolvedArtifact;
	}
	public Set<Artifact> getDependencies() {
		return dependencies;
	}
	public void setDependencies(Set<Artifact> resultingDependencies) {
		this.dependencies = resultingDependencies;
	}
	public MultiMap<Artifact, Artifact> getTransitiveDependersMap() {
		return transitiveDependersMap;
	}
	public void setTransitiveDependersMap(MultiMap<Artifact, Artifact> transitiveDependersMap) {
		this.transitiveDependersMap = transitiveDependersMap;
	}
	public List<CompiledTerminal> getTerminalClasspathArtifacts() {
		return terminalClasspathArtifacts;
	}
	public void setTerminalClasspathArtifacts(List<CompiledTerminal> terminalClasspathArtifacts) {
		this.terminalClasspathArtifacts = terminalClasspathArtifacts;
	}

}
