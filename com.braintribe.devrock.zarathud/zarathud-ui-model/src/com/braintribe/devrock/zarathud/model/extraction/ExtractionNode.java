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
package com.braintribe.devrock.zarathud.model.extraction;

import java.util.List;
import java.util.Map;

import com.braintribe.devrock.zarathud.model.common.Node;
import com.braintribe.devrock.zarathud.model.extraction.subs.ContainerNode;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * basic extraction node - all involved nodes derive from this
 */
public interface ExtractionNode extends Node {

	
	EntityType<ExtractionNode> T = EntityTypes.T(ExtractionNode.class);

	String name = "name";
	String treepathElements = "treepathElements";
	String parent = "parent";
	String isOwner = "isOwner";
	String isOther = "isOther";
	String containerNodes = "containerNodes";
	
	String getName();
	void setName(String value);
	
	
	List<ExtractionNode> getTreepathElements();
	void setTreepathElements(List<ExtractionNode> value);

	ExtractionNode getParent();
	void setParent(ExtractionNode value);

	boolean getIsOwner();
	void setIsOwner( boolean isOwner);
	
	boolean getIsOther();
	void setIsOther(boolean isOther);

	Map<String,ContainerNode> getContainerNodes();
	void setContainerNodes(Map<String,ContainerNode> value);


}
