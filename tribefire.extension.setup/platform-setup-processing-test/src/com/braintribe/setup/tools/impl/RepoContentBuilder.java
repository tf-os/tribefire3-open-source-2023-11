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
package com.braintribe.setup.tools.impl;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.io.ByteArrayInputStream;
import java.util.Map;

import com.braintribe.devrock.model.repolet.content.Artifact;
import com.braintribe.devrock.model.repolet.content.Dependency;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.Resource;

/**
 * @author peter.gazdik
 */
public class RepoContentBuilder {

	private final Resource emptyRes = Resource.createTransient(() -> new ByteArrayInputStream(new byte[0]));

	private final Map<String, Artifact> artifactByName = newMap();

	public RepoletContent build() {
		RepoletContent result = RepoletContent.T.create();
		result.getArtifacts().addAll(artifactByName.values());

		return result;

	}

	public Dependency addDependency(String name, String depName) {
		return addDependency(name, depName, null);
	}

	public Dependency addDependency(String name, String depName, String classifier) {
		Artifact artifact = acquireArtifact(name);

		Dependency result = newDependencyWithExistingArtifact(depName, classifier);
		artifact.getDependencies().add(result);
		return result;
	}

	private Dependency newDependencyWithExistingArtifact(String depName, String classifier) {
		Artifact artifact = acquireArtifact(depName);
		Dependency result = Dependency.parse(depName);
		if (classifier != null) {
			result.setClassifier(classifier);
			artifact.getParts().put(classifier + ":jar", emptyRes);
		}

		return result;
	}

	public Artifact requireArtifact(String condensedName) {
		return artifactByName.computeIfAbsent(condensedName, n -> {
			throw new IllegalStateException("Artifact does not exist: " + condensedName);
		});

	}

	public Artifact acquireArtifact(String condensedName) {
		return artifactByName.computeIfAbsent(condensedName, Artifact::from);
	}

	public void enrichArtifact(String condensedName, PartIdentification part, Resource resource) {
		acquireArtifact(condensedName).getParts().put(part.asString(), resource);
	}

}
