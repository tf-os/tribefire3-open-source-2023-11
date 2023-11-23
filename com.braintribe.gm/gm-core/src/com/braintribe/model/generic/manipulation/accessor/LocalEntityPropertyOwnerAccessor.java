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
package com.braintribe.model.generic.manipulation.accessor;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;

public class LocalEntityPropertyOwnerAccessor extends OwnerAccessor<LocalEntityProperty> {

	@SuppressWarnings("unchecked")
	@Override
	public <T1> T1 get(LocalEntityProperty owner) {
		String propertyName = owner.getPropertyName();
		GenericEntity entity = owner.getEntity();
		EntityType<GenericEntity> entityType = entity.entityType();
		return (T1) entityType.getProperty(propertyName).get(entity);
	}

	@Override
	public EntityReference replace(LocalEntityProperty owner, Object newValue) {
		String propertyName = owner.getPropertyName();
		GenericEntity entity = owner.getEntity();
		
		EntityType<GenericEntity> entityType = entity.entityType();
		Property property = entityType.getProperty(propertyName);
		property.set(entity, newValue);
		
		if (property.isIdentifier()) {
			return entity.reference();
		} else
			return null;
	}

	@Override
	public void markAsAbsent(LocalEntityProperty owner, AbsenceInformation absenceInformation, String propertyName) {
		GenericEntity entity = owner.getEntity();
		if (entity instanceof EnhancedEntity) {
			entity.entityType().getProperty(propertyName).setAbsenceInformation(entity, GMF.absenceInformation());
		}
	}

}
