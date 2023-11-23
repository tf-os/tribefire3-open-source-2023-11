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
package com.braintribe.devrock.eclipse.model.storage;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * simple entity to store some stuff for the workspace 
 * NOTE that in slotData, even though Object is the type of the value, only GE compatible objects 
 * can be serialized.
 * 
 * @author pit
 *
 */
public interface StorageLockerPayload extends GenericEntity {
	
	EntityType<StorageLockerPayload> T = EntityTypes.T(StorageLockerPayload.class);
	
	String slotData = "slotData";
	
	/** 
	 * @return - a simple map of 'slot'-name to 'slot'-value
	 */
	Map<String,Object> getSlotData();
	void setSlotData(Map<String,Object> value);


	default boolean isEmtpy() {
		return getSlotData().size() == 0;
	}
	
}
