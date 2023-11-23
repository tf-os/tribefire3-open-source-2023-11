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
package com.braintribe.devrock.zed.analyze.dependency;

import java.util.function.Predicate;

import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ParameterEntity;
import com.braintribe.zarathud.model.data.ZedEntity;

/**
 * a filter that only allows {@link Artifact}s to pass if they are matching the set one.
 * @author pit
 *
 */
public class StandardFilter implements Predicate<ZedEntity> {
	
	private Artifact artifact;
	
	public StandardFilter(Artifact owner) {
		artifact = owner;
	}

	@Override
	public boolean test(ZedEntity entity) {
		if (entity instanceof ParameterEntity) {			
			return false;
		}
		boolean matches = ArtifactMatcher.matchArtifactTo(artifact, entity.getArtifacts());
	
		/*
		String cases = entity.getArtifacts().stream().map( a -> a.toVersionedStringRepresentation()).collect(Collectors.joining(","));		
		if (matches) {
			System.out.println( "owner [" + artifact.toVersionedStringRepresentation() + "] matches " + cases);
		}
		else {
			System.out.println( "owner [" + artifact.toVersionedStringRepresentation() + "] doesn't match " + cases);
		}
		*/
	
		return matches;
	}

	
	
}
