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
package com.braintribe.model.generic.validation.expert;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class ValidationContext {
	
	private PersistenceGmSession gmSession;
	private boolean completeConfiguredValidation = false;
	private boolean groupValidations = false;
	private boolean shortMessageStyle = false;
	
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	public ManagedGmSession getGmSession() {
		return gmSession;
	}

	public Set<GenericEntity> getChangedEntitiesByEntityType(EntityType<GenericEntity> entityType) {
		Set<GenericEntity> changedEntities = new HashSet<>();
		gmSession.getTransaction().getManipulatedProperties().forEach(entityProperty -> {
			GenericEntity candidate = entityProperty.getEntity();
			EntityType<GenericEntity> candidateType = candidate.entityType();
			if (candidateType.isAssignableFrom(entityType))
				changedEntities.add(candidate);
		});
		
		return changedEntities;//gmSession.getTransaction().getManipulatedProperties();
	}

	public boolean isCompleteConfiguredValidation() {
		return completeConfiguredValidation;
	}

	public void setCompleteConfiguredValidation(boolean completeConfiguredValidation) {
		this.completeConfiguredValidation = completeConfiguredValidation;
	}

	public boolean isGroupValidations() {
		return groupValidations;
	}

	public void setGroupValidations(boolean groupValidations) {
		this.groupValidations = groupValidations;
	}

	public boolean isShortMessageStyle() {
		return shortMessageStyle;
	}

	public void setShortMessageStyle(boolean shortMessageStyle) {
		this.shortMessageStyle = shortMessageStyle;
	}
}
