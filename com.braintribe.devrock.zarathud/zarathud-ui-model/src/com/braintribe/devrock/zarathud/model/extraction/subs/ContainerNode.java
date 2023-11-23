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
package com.braintribe.devrock.zarathud.model.extraction.subs;

import com.braintribe.devrock.zarathud.model.common.FingerPrintRating;
import com.braintribe.devrock.zarathud.model.common.FingerPrintRepresentation;
import com.braintribe.devrock.zarathud.model.extraction.ExtractionNode;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.zarathud.model.data.ZedEntity;

/**
 * a common base for the different container nodes
 */
public interface ContainerNode extends ExtractionNode {
	
	EntityType<ContainerNode> T = EntityTypes.T(ContainerNode.class);
	
	String contentOrigin = "contentOrigin";
	String contentStructure = "contentStructure";
	String representation = "representation";
	String owner = "owner";
	String rating = "rating";
	
	ContainerComparisonOrigin getContentOrigin();
	void setContentOrigin(ContainerComparisonOrigin value);
	
	ContainerComparisonContent getContentStructure();
	void setContentStructure(ContainerComparisonContent value);

	FingerPrintRepresentation getRepresentation();
	void setRepresentation(FingerPrintRepresentation value);

	ZedEntity getOwner();
	void setOwner(ZedEntity value);
	
	
	/**
	 * @return - rating of the fingerprints below, if any 
	 */
	FingerPrintRating getRating();
	void setRating(FingerPrintRating value);



}
