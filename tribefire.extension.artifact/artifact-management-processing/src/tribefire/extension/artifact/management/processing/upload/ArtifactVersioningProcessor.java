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
package tribefire.extension.artifact.management.processing.upload;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.devrock.mc.api.resolver.ArtifactResolver;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.version.Version;

/**
 * @author pit
 *
 */
public class ArtifactVersioningProcessor {
	
	/**
	 * retrieves all versions that are associated with the {@link ArtifactIdentification}
	 * @param ai - the {@link ArtifactIdentification}
	 * @param artifactResolver the resolver to use for determination
	 * @return - a {@link List} of {@link Version}
	 */
	public static List<Version> getArtifactVersions( ArtifactIdentification ai, ArtifactResolver artifactResolver, Predicate<Version> versionFilter) {
		return artifactResolver.getVersions(ai).stream().map(VersionInfo::version).filter(versionFilter).collect(Collectors.toList());
	}
	
	public static List<Version> getArtifactVersions( ArtifactIdentification ai, ArtifactResolver artifactResolver) {
		return getArtifactVersions(ai, artifactResolver, v -> true);
	}
	
	/**
	 * retrieves all versions that match the {@link CompiledDependencyIdentification}' range 
	 * @param cdi - the {@link CompiledDependencyIdentification}
	 * @param artifactResolver the resolver to use for determination
	 * @return - a {@link List} of {@link Version}
	 */
	public static List<Version> getArtifactVersions( CompiledDependencyIdentification cdi, ArtifactResolver artifactResolver) {
		return getArtifactVersions(cdi, artifactResolver, cdi.getVersion()::matches);
	}
	
}
