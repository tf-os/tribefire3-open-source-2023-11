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
package com.braintribe.devrock.eclipse.model.identification;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.version.VersionExpression;

/**
 * represents a {@link CompiledArtifactIdentification} reflecting an artifact on a remote-repository
 * 
 * @author pit
 *
 */
public interface RemoteCompiledDependencyIdentification extends CompiledDependencyIdentification, HasArchetype {
	
	EntityType<RemoteCompiledDependencyIdentification> T = EntityTypes.T(RemoteCompiledDependencyIdentification.class);

	String repositoryOrigin = "repositoryOrigin";
	String sourceOrigin = "sourceOrigin";
	String sourceRepositoryOrigin = "sourceRepositoryOrigin";
	String sourceRepositoryKey = "sourceRepositoryKey";
	
	/**
	 * @return - the {@link Repository} where the artifact resides
	 */
	Repository getRepositoryOrigin();
	void setRepositoryOrigin(Repository value);
	
 
	/**
	 * @return - the directory the source is from (real filesystem)
	 */
	String getSourceOrigin();
	void setSourceOrigin(String value);
	
	
	/**
	 * @return - the scan repository of the source (cut to scan-repo) 
	 */
	String getSourceRepositoryOrigin();
	void setSourceRepositoryOrigin(String value);

	
	/**
	 * @return - the key of the scan repository of the source
	 */
	String getSourceRepositoryKey();
	void setSourceRepositoryKey(String value);

	
	/**
	 * creates a {@link RemoteCompiledDependencyIdentification}
	 * @param groupId - the group id
	 * @param artifactId - the artifact id
	 * @param version - the version 
	 * @param repository - the {@link Repository}
	 * @param sourcesLocation - alternatively : the origin of the sources (actually a source artifact)
	 * @return - the {@link RemoteCompiledDependencyIdentification} created
	 */
	static RemoteCompiledDependencyIdentification create(String groupId, String artifactId, String version, Repository repository, String sourcesLocation) {
		RemoteCompiledDependencyIdentification rai = RemoteCompiledDependencyIdentification.T.create();
		rai.setGroupId(groupId);
		rai.setArtifactId(artifactId);
		rai.setVersion( VersionExpression.parse(version));
		rai.setRepositoryOrigin(repository);
		
		return rai;
	}
	
	/**
	 * creates a {@link RemoteCompiledDependencyIdentification} from the expression (parseable as {@link VersionedArtifactIdentification}) 
	 * @param expression - the {@link VersionedArtifactIdentification} paresable string
	 * @param repository - the {@link Repository}
	 * @param sourcesLocation - alternatively : the origin of the sources (actually a source artifact)
	 * @return - the {@link RemoteCompiledDependencyIdentification} created
	 */
	static RemoteCompiledDependencyIdentification create(String expression, Repository repository, String sourcesLocation) {
		VersionedArtifactIdentification vai = VersionedArtifactIdentification.parse(expression);
		return create(vai.getGroupId(), vai.getArtifactId(), vai.getVersion(), repository, sourcesLocation);
	}

	/**
	 * @return - a new {@link VersionedArtifactIdentification} parametrized by this {@link RemoteCompiledDependencyIdentification}
	 */
	default VersionedArtifactIdentification asVersionedArtifactIdentification() {
		return VersionedArtifactIdentification.create( this.getGroupId(), this.getArtifactId(), this.getVersion().asString());
	}
	
	
	/**
	 * @param ecai - an {@link EnhancedCompiledArtifactIdentification}
	 * @return - an adapted {@link RemoteCompiledDependencyIdentification}
	 */
	static RemoteCompiledDependencyIdentification from( EnhancedCompiledArtifactIdentification ecai) {
		RemoteCompiledDependencyIdentification rcdi = create( ecai.getGroupId(), ecai.getArtifactId(), ecai.getVersion().asString(), null, ecai.getOrigin());				
		return rcdi;
	}
}
