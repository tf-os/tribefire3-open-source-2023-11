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
package com.braintribe.devrock.mc.core.filters;

import com.braintribe.devrock.model.repository.filters.NoneMatchingArtifactFilter;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * Expert implementation for {@link NoneMatchingArtifactFilter}. Since this filter (expert) doesn't match anything, it
 * requires no configuration and thus a single {@link #instance} is sufficient.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class NoneMatchingArtifactFilterExpert implements ArtifactFilterExpert {

	public static final NoneMatchingArtifactFilterExpert instance = new NoneMatchingArtifactFilterExpert();

	private NoneMatchingArtifactFilterExpert() {
		// nothing to do
	}

	@Override
	public boolean matchesGroup(String groupId) {
		return false;
	}	
	
	@Override
	public boolean matches(ArtifactIdentification artifactIdentification) throws IllegalArgumentException {
		return false;
	}

	@Override
	public boolean matches(CompiledArtifactIdentification compiledArtifactIdentification) {
		return false;
	}

	@Override
	public boolean matches(CompiledPartIdentification compiledPartIdentification) {
		return false;
	}
}
