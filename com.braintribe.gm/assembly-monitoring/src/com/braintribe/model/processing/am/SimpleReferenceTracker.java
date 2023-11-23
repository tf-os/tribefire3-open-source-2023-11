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
package com.braintribe.model.processing.am;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;

public class SimpleReferenceTracker implements ReferenceTracker {

	protected Map<GenericEntity, RefereeData> entities = new HashMap<GenericEntity, RefereeData>();

	protected Set<GenericEntity> entitiesView = null;
	protected Map<GenericEntity, RefereeData> entitiesDataView = null;

	public Set<GenericEntity> getEntities() {
		if (entitiesView == null) {
			entitiesView = Collections.unmodifiableSet(entities.keySet());
		}

		return entitiesView;
	}

	public Map<GenericEntity, RefereeData> getReferenceMap() {
		if (entitiesDataView == null) {
			entitiesDataView = Collections.unmodifiableMap(entities);
		}

		return entitiesDataView;
	}

	public int getReferenceCount(GenericEntity entity) {
		RefereeData refereeData = entities.get(entity);
		return refereeData != null ? refereeData.totalReferences : 0;
	}

	@Override
	public void addReference(GenericEntity referee, GenericEntity entity) {
		addReferenceImpl(referee, entity, 1);
	}

	protected RefereeData addReferenceImpl(GenericEntity referee, GenericEntity entity, int count) {
		RefereeData refereeData = entities.get(entity);

		if (refereeData == null) {
			refereeData = new RefereeData();
			entities.put(entity, refereeData);
		}

		refereeData.addReferee(referee, count);

		return refereeData;
	}

	@Override
	public void removeReference(GenericEntity referee, GenericEntity entity) {
		removeReferenceImpl(referee, entity, 1);
	}

	protected RefereeData removeReferenceImpl(GenericEntity referee, GenericEntity entity, int count) {
		RefereeData refereeData = entities.get(entity);
		refereeData.removeReferee(referee, count);
		
		if (refereeData.totalReferences == 0) {
			entities.remove(entity);
			
		} else if (refereeData.totalReferences < 0) {
			throw new IllegalStateException("We hava somehow achieved a negative number of references.");
		}
		
		return refereeData;
	}

	public boolean hasReference(GenericEntity entity) {
		return entities.containsKey(entity);
	}
}
