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
package com.braintribe.model.processing.manipulator.expert.basic;

import java.util.Map;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.manipulator.api.ManipulatorContext;

public class RemoveManipulator extends AbstractCollectionManipulator<RemoveManipulation> {
	public static final RemoveManipulator defaultInstance = new RemoveManipulator();

	@Override
	public void apply(RemoveManipulation manipulation, ManipulatorContext context) {
		LocalEntityProperty owner = context.resolveOwner(manipulation);

		String propertyName = owner.getPropertyName();
		GenericEntity entity = owner.getEntity();
		EntityType<GenericEntity> entityType = entity.entityType();
		Property property = entityType.getProperty(propertyName);
		GenericModelType propertyType = property.getType().cast();
		CollectionType collectionType = propertyType instanceof CollectionType ? (CollectionType) propertyType : null;
		Object collection = entityType.getProperty(propertyName).get(entity);

		if (collection == null)
			return;

		Map<Object, Object> itemsToRemove = manipulation.getItemsToRemove();

		GenericModelType keyType = getKeyType(collection, collectionType);
		GenericModelType valueType = getValueType(collectionType);
		GenericModelType mapType = GMF.getTypeReflection().getCollectionType(Map.class, keyType, valueType);

		Map<Object, Object> items = context.resolveValue(mapType, itemsToRemove);
		getCollectionManipulator(collection, collectionType).remove(collection, items);
	}

}
