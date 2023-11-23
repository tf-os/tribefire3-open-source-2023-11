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
package com.braintribe.devrock.zed.api.forensics;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ZedEntity;

public interface BasicZedForensics {

	/**
	 * returns a {@link Map} of caller {@link ZedEntity} in the terminal to a {@link List} of all callee {@link ZedEntity}
	 * @param runtime - the runtime artifact 
	 * @param artifact - the artifact that contains the callee
	 * @return - a {@link Map} of caller to callee 
	 */
	Map<ZedEntity, List<ZedEntity>> getTerminalReferencesToArtifact(Artifact runtime, Artifact artifact);

	/**
	 * returns a {@link Set} of {@link Artifact}s that were not declared 
	 * @param declared - the {@link Collection} of {@link Artifact}s the were declared
	 * @param found - the {@link Collection} of {@link Artifact}s that were found 
	 * @return
	 */
	Set<Artifact> extractUndeclaredDependencies(Collection<Artifact> declared, Collection<Artifact> found);

	/**
	 * not used? 
	 */
	List<ZedEntity> getEntitiesWithMultipleSources(List<ZedEntity> population);

	/**
	 * not used?
	 */
	Map<String, List<ZedEntity>> collectEntitiesWithSameMultipleSources(List<ZedEntity> multiSourcePopulation);

}