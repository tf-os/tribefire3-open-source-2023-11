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

import java.util.List;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents a {@link Reason} (and an Origination)
 * @author pit
 *
 */
public interface ReasonNode extends Node {
	EntityType<ReasonNode> T = EntityTypes.T(ReasonNode.class);

	String type = "type";
	String classification = "classification";
	String text = "text";
	String children = "children";
	String customProperties = "customProperties";
	
	
	/**
	 * @return - the type of the {@link Reason} (its EntityType)
	 */
	String getType();
	void setType(String value);
	
	/**
	 * @return -  the {@link ReasonClassification}, mainly used to assign an image to node in the viewers
	 */
	ReasonClassification getClassification();
	void setClassification(ReasonClassification value);

	
	/**
	 * @return - the text of the {@link Reason} as {@link String}
	 */
	String getText();
	void setText(String value);	

	/**
	 * @return - a {@link List} of {@link ReasonNode} standing for the 'custom properties' - not supported yet
	 */
	List<ReasonNode> getCustomProperties();
	void setCustomProperties(List<ReasonNode> value);
	
	/**
	 * @return - the actual {@link Reason}
	 */
	Reason getBackingReason();
	void setBackingReason(Reason value);

	
}
