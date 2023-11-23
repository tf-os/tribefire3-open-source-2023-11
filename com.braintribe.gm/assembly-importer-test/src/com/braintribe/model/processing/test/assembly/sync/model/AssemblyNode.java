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
package com.braintribe.model.processing.test.assembly.sync.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface AssemblyNode extends GenericEntity {

	EntityType<AssemblyNode> T = EntityTypes.T(AssemblyNode.class);

	String neighbors= "neighbors";
	
	String getName();
	void setName(String value);

	List<AssemblyNode> getNeighbors();
	void setNeighbors(List<AssemblyNode> value);

	Object getObject();
	void setObject(Object value);
	
	List<Integer> getIntList();
	void setIntList(List<Integer> value);
	
	Set<Integer> getIntSet();
	void setIntSet(Set<Integer> value);
	
	Map<Integer, Integer> getIntMap();
	void setIntMap(Map<Integer, Integer> value);

}
