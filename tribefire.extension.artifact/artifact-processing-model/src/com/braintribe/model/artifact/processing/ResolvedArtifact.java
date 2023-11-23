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
package com.braintribe.model.artifact.processing;

import java.util.List;

import com.braintribe.model.artifact.info.HasRepositoryOrigins;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a representation of an artifact
 * @author pit
 *
 */
public interface ResolvedArtifact extends ArtifactIdentification, HasRepositoryOrigins, HasParts {
	
	final EntityType<ResolvedArtifact> T = EntityTypes.T(ResolvedArtifact.class);
	
	
	/**
	 * @return - the DIRECT dependencies as {@link ResolvedArtifact}
	 */
	List<ResolvedArtifact> getDependencies();
	/**
	 * @param dependencies - the DIRECT dependencies as {@link ResolvedArtifact}
	 */
	void setDependencies( List<ResolvedArtifact> dependencies);
	
}
