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
package com.braintribe.devrock.eclipse.model.resolution.nodes;

import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents a part
 * 
 * @author pit
 *
 */
public interface PartNode extends Node {
	
	EntityType<PartNode> T = EntityTypes.T(PartNode.class);
	
	String ownerIdentification = "ownerIdentification";
	String part = "part";
	String symbolicName = "symbolicName";
	
	/**
	 * @return - the {@link VersionedArtifactIdentification} of the owning artifact
	 */
	VersionedArtifactIdentification getOwnerIdentification();
	void setOwnerIdentification(VersionedArtifactIdentification value);
	
	/**
	 * @return - the actual part
	 */
	Part getPart();
	void setPart(Part value);

	/**
	 * @return - the name as it should appear
	 */
	String getSymbolicName();
	void setSymbolicName(String value);


}
