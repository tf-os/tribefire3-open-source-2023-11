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
package com.braintribe.model.platformreflection.streampipes;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface StreamPipeBlocksInfo extends GenericEntity {
	
	EntityType<StreamPipeBlocksInfo> T = EntityTypes.T(StreamPipeBlocksInfo.class);
	
	void setNumUnused(int used);
	int getNumUnused();
	
	void setNumTotal(int total);
	int getNumTotal();
	
	void setNumMax(int allocatable);
	int getNumMax();
	
	void setMbUnused(int used);
	int getMbUnused();
	
	void setMbTotal(int total);
	int getMbTotal();
	
	void setMbAllocatable(int allocatable);
	int getMbAllocatable();
	
	void setBlockSize(int size);
	int getBlockSize();
	
	void setLocation(String location);
	String getLocation();
	
	void setInMemory(boolean isInMemory);
	boolean getInMemory();
	
	void setPoolKind(PoolKind poolKind);
	PoolKind getPoolKind();
}
