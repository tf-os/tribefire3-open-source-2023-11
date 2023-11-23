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
package com.braintribe.devrock.zed.api.core;

import com.braintribe.devrock.zed.api.context.CommonZedCoreContext;
import com.braintribe.zarathud.model.data.Artifact;

/**
 * a registry to contain information about all {@link Artifact}s
 * @author pit
 *
 */
public interface ArtifactRegistry {
	
	/**
	 * looks up an {@link Artifact} based on whether it contains a specified type (via its signature)
	 * @param context - the {@link CommonZedCoreContext}
	 * @param signature - the signature of a type within the {@link Artifact} 
	 * @return - the {@link Artifact} 
	 */
	Artifact artifact( CommonZedCoreContext context, String signature);
	
	/**
	 * returns the {@link Artifact} that was identified as runtime artifact (java JRE libs)
	 * @param context - the {@link CommonZedCoreContext}
	 * @return - the runtime {@link Artifact} 
	 */
	Artifact runtimeArtifact( CommonZedCoreContext context);
	
	/**
	 * returns the single {@link Artifact} that cannot be identified, and contains all such type references 
	 * @param context - the {@link CommonZedCoreContext}
	 * @return - the 'unknown' {@link Artifact}
	 */
	Artifact unknownArtifact( CommonZedCoreContext context);
	
	
	/**
	 * @param context
	 * @param name
	 * @return
	 */
	Artifact unknownArtifact( CommonZedCoreContext context, String name);
}
