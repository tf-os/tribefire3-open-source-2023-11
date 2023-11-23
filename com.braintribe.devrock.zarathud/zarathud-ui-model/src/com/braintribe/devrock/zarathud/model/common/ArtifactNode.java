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
package com.braintribe.devrock.zarathud.model.common;

import com.braintribe.devrock.zarathud.model.extraction.ExtractionNode;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents an artifact
 * @author pit
 *
 */
public interface ArtifactNode extends ExtractionNode {
	
	EntityType<ArtifactNode> T = EntityTypes.T(ArtifactNode.class);

	String identification = "identification";
	String isTerminal = "isTerminal";
	String entries = "entries";
	
	/**
	 * @return - the {@link VersionedArtifactIdentification} that identifies the node
	 */
	VersionedArtifactIdentification getIdentification();
	void setIdentification(VersionedArtifactIdentification value);
	
	/**
	 * @return - whether it's the terminal
	 */
	boolean getIsTerminal();
	void setIsTerminal(boolean value);


}
