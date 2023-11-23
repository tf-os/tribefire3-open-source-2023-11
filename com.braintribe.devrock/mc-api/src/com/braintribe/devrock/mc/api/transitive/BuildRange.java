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
package com.braintribe.devrock.mc.api.transitive;

import java.util.function.Function;

import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

/**
 * a build range - basically a container for functions that can test boundary hits
 * @author pit / dirk
 *
 */
public interface BuildRange {
	static CompiledArtifactIdentification boundaryFloor = CompiledArtifactIdentification.create("<floor>", "<floor>", "0");
	Function<CompiledArtifactIdentification, BoundaryHit> lowerBound();
	Function<CompiledArtifactIdentification, BoundaryHit> upperBound();
	
	
	/**
	 * creates a build range of the two functions for the lower/upper bounds 
	 * @param lowerBound - a function to return {@link BoundaryHit} for lower bounds of the build range 
	 * @param upperBound - a function to return {@link BoundaryHit} for upper bounds of the build range
	 * @return - the new {@link BuildRange}
	 */
	static BuildRange of(Function<CompiledArtifactIdentification, BoundaryHit> lowerBound, Function<CompiledArtifactIdentification, BoundaryHit> upperBound) {
		return new BuildRange() {
			@Override
			public Function<CompiledArtifactIdentification, BoundaryHit> lowerBound() {
				return lowerBound;
			}
			
			@Override
			public Function<CompiledArtifactIdentification, BoundaryHit> upperBound() {
				return upperBound;
			}
		};
	}
}
