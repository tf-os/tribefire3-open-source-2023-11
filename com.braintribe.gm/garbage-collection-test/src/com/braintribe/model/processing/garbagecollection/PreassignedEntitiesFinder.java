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
package com.braintribe.model.processing.garbagecollection;

import java.util.Set;

import com.braintribe.gwt.utils.genericmodel.EntitiesFinder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class PreassignedEntitiesFinder implements EntitiesFinder {

	Set<GenericEntity> preassignedEntities;

	public PreassignedEntitiesFinder() {
	}

	public PreassignedEntitiesFinder(final Set<GenericEntity> preassignedEntities) {
		this.preassignedEntities = preassignedEntities;
	}

	public Set<GenericEntity> getPreassignedEntities() {
		return this.preassignedEntities;
	}

	public void setPreassignedEntities(final Set<GenericEntity> preassignedEntities) {
		this.preassignedEntities = preassignedEntities;
	}

	// TODO add comment
	@Override
	public Set<GenericEntity> findEntities(final PersistenceGmSession session) {
		return getPreassignedEntities();
	}

}
