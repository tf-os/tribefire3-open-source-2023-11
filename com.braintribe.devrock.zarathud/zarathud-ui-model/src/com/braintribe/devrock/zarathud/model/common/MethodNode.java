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

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface MethodNode extends Node, HasRelatedFingerPrint {
	
	EntityType<MethodNode> T = EntityTypes.T(MethodNode.class);
	String name = "name";
	String returnType = "returnType";
	String parameterTypes = "parameterTypes";
	

	/**
	 * @return - the name of the method
	 */
	String getName();
	void setName(String value);

	/**
	 * @return - the {@link EntityNode} of the return type 
	 */
	EntityNode getReturnType();
	void setReturnType(EntityNode value);

	/**
	 * @return - the {@link List} of {@link EntityNode} making up the parameter
	 */
	List<EntityNode> getParameterTypes();
	void setParameterTypes(List<EntityNode> value);

	
}
