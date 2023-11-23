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
package com.braintribe.model.processing.exchange.service;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.model.exchangeapi.ExportDescriptor;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;

public class DetachingAndShallowfyingCloningContext extends StandardCloningContext {
	
	private static final Property globalIdProperty = GenericEntity.T.getProperty(GenericEntity.globalId);
	private static final Property partitionProperty = GenericEntity.T.getProperty(GenericEntity.partition);
	
	private Predicate<GenericEntity> matcher;
	private ExportDescriptor descriptor;
	private Set<GenericEntity> externalReferences = new HashSet<>();
	private Set<GenericEntity> followedReferences = new HashSet<>();

	public DetachingAndShallowfyingCloningContext(Predicate<GenericEntity> matcher, ExportDescriptor descriptor) {
		this.matcher = matcher;
		this.descriptor = descriptor;
	}
	
	public Set<GenericEntity> getExternalReferences() {
		return externalReferences;
	}
	
	public Set<GenericEntity> getFollowedReferences() {
		return followedReferences;
	}
	
	@Override
	public boolean isAbsenceResolvable(Property property, GenericEntity entity, AbsenceInformation absenceInformation) {
		return true;
	}
	@Override
	public <T> T getAssociated(GenericEntity entity) {
		T associated = super.getAssociated(entity);
		if (associated != null)
			return associated;
		
		if(this.matcher.test(entity)) {
			return createShallowAssociatedCopy(entity);
		}
		
		followedReferences.add(entity);
		return null;
	}
	private <T extends GenericEntity> T createShallowAssociatedCopy(GenericEntity entity) {
		T shallowEntity = entity.<T> entityType().createRaw();
		shallowEntity.setGlobalId(entity.getGlobalId());
		registerAsVisited(entity, shallowEntity); // i.e. associate
		externalReferences.add(shallowEntity);
		return shallowEntity;
	}
	@Override
	public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
			GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
		
		if (property.isIdentifier()) {
			return !descriptor.getSkipId();
		}
		if (globalIdProperty == property) {
			return !descriptor.getSkipGlobalId();
		}
		if (partitionProperty == property) {
			return !descriptor.getSkipPartition();
		}
		return true;

	}
}
