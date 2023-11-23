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

import java.util.List;

import com.braintribe.zarathud.model.data.Artifact;

/**
 * matches artifacts - ie. diverts to {@link Artifact} itself 
 * @author pit
 *
 */
public class ArtifactMatcher {
	
	/**
	 * matches two artifacts
	 * @param one - the first {@link Artifact}
	 * @param two - second {@link Artifact}
	 * @return - true if the first's compare run returns 0, false otherwise
	 */
	public static boolean matchArtifactTo( Artifact one, Artifact two) {
		if (one == two)
			return true;
		
		if (two == null) {
			// some java complex type leads to this
			return false;
		}
		
		return one.compareTo(two) == 0;
	}
	
	/**
	 * finds whether the first artifact is contained within the passed list 
	 * @param one - the first {@link Artifact}
	 * @param twos - the second {@link Artifact}
	 * @return - true if the first is contained in the second
	 */
	public static boolean matchArtifactTo( Artifact one, List<Artifact> twos) {
		for (Artifact two : twos) {
			if (matchArtifactTo(one, two)) 
				return true;
		}
		return false;
	}
}
