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
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Node extends GenericEntity{
		
	EntityType<Node> T = EntityTypes.T(Node.class);
	
	String children = "children";
	String reason = "reason";
	String function = "function";
	String archetype = "archetype";
	String topLevel = "topLevel";
	String isAProject = "isAProject";

	/**
	 * @return - a {@link List} of {@link Node} attached
	 */
	List<Node> getChildren();
	void setChildren(List<Node> value);
	
	
	boolean getTopLevel();
	void setTopLevel(boolean value);

	/**
	 * @return - true if this node represents a project
	 */
	boolean getIsAProject();
	void setIsAProject(boolean value);

		
	/**
	 * @return - the {@link Reason} if anything's wrong, especially in the incomplete artifact tab
	 */
	Reason getReason();
	void setReason(Reason value);

	/**
	 * @return - the {@link NodeFunction} that describes the 'functional value', i.e. dependency, depender etc 
	 */
	NodeFunction getFunction();
	void setFunction(NodeFunction value);
	
	/**
	 * @return - the {@link NodeArchetype} that describes the type, i.e. jar or pom 
	 */
	NodeArchetype getArchetype();
	void setArchetype(NodeArchetype value);
	
	
}
