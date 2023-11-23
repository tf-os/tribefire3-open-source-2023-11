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
package com.braintribe.model.generic.sync;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.CompoundManipulation;

public class SynchronizationContext {
	private Object syncedValue;
	private Set<GenericEntity> newEntities = new HashSet<GenericEntity>();
	private CompoundManipulation updateManipulation = compound();
	
	public CompoundManipulation getUpdateManipulation() {
		if (updateManipulation == null) {
			updateManipulation = compound();
		}

		return updateManipulation;
	}

	public void registerNewEntity(GenericEntity entity) {
		newEntities.add(entity);
	}
	
	public Set<GenericEntity> getNewEntities() {
		return newEntities;
	}
	
	public void setSyncedResult(Object syncedResult) {
		this.syncedValue = syncedResult;
	}

	public <T> T getSyncedResult() {
		return (T)syncedValue;
	}
}
 
