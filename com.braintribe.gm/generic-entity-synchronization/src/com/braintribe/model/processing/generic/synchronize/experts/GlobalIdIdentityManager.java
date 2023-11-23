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
package com.braintribe.model.processing.generic.synchronize.experts;

import java.util.Arrays;
import java.util.Collection;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.generic.synchronize.api.IdentityManager;
import com.braintribe.model.processing.generic.synchronize.api.SynchronizationContext;

/**
 * An {@link IdentityManager} that synchronizes entities based on their globalId ({@link GenericEntity#globalId}).
 */
public class GlobalIdIdentityManager extends ConfigurableIdentityManager {

	private final EntityType<GenericEntity> type = typeReflection.getEntityType(GenericEntity.class);
	
	public GlobalIdIdentityManager() {
		super();
		setResponsibleFor(type);
	}
	
	@Override
	public boolean isResponsible(GenericEntity instance, EntityType<? extends GenericEntity> entityType,SynchronizationContext context) {
		return super.isResponsible(instance, entityType, context) && (instance.getGlobalId() != null);
	}

	@Override
	public Collection<String> getIdentityProperties(GenericEntity instance,EntityType<? extends GenericEntity> entityType, SynchronizationContext context) {
		return Arrays.asList(GenericEntity.globalId);
	}
	
}
