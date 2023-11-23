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
package com.braintribe.devrock.zarathud.model.model;

import java.util.List;

import com.braintribe.devrock.zarathud.model.common.HasRelatedFingerPrint;
import com.braintribe.devrock.zarathud.model.common.MethodNode;
import com.braintribe.devrock.zarathud.model.common.Node;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface GenericEntityNode extends Node, HasRelatedFingerPrint {
	
	EntityType<GenericEntityNode> T = EntityTypes.T(GenericEntityNode.class);
	
	String properties = "properties";
	String name = "name";
	String conformMethods = "conformMethods";
	String unconformMethods = "unconformMethods";

	/**
	 * @return - name of the GenericEntity
	 */
	String getName();
	void setName(String value);

	/**
	 * @return - the {@link List} of properties
	 */
	List<PropertyNode> getProperties();
	void setProperties(List<PropertyNode> value);

	/**
	 * @return - {@link List} of methods that are compatible with a GenericEntity
	 */
	List<MethodNode> getConformMethods();
	void setConformMethods(List<MethodNode> value);
	
	/**
	 * @return - {@link List} of methods that are incompatible with a GenericEntity
	 */
	List<MethodNode> getNonConformMethods();
	void setNonConformMethods(List<MethodNode> value);


}
