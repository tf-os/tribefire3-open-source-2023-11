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
package com.braintribe.model.processing.session.impl.session.collection;

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.reflection.VdHolder;

public class CollectionEnhancingPropertyAccessInterceptor extends PropertyAccessInterceptor {

	@Override
	public Object getProperty(Property property, GenericEntity entity, boolean isVd) {
		Object result = next.getProperty(property, entity, isVd);

		GenericModelType propertyType = property.getType();
		boolean isVdHolder = VdHolder.isVdHolder(result);

		if (result != null && !isVdHolder) {
			switch (propertyType.getTypeCode()) {
				case objectType:
					propertyType = propertyType.getActualType(result);
					if (!(propertyType.isCollection())) {
						return result;
					}
					//$FALL-THROUGH$
				case listType:
				case mapType:
				case setType:
					if (!(result instanceof EnhancedCollection)) {
						EnhancedCollection enhancedCollection = EnhanceUtil.enhanceCollection((CollectionType) propertyType, result);
						enhancedCollection.setCollectionOwner(EnhanceUtil.newLocalOwner(entity, property));

						next.setProperty(property, entity, enhancedCollection, false);

						return enhancedCollection;
					}
					//$FALL-THROUGH$
				default:
					return result;
			}
		}

		if (propertyType.isCollection()) {
			if (isVdHolder) {
				VdHolder.checkIsAbsenceInfo(result, entity, property);
			}

			EnhancedCollection enhancedCollection = newEnhancedCollection((CollectionType) propertyType, isVdHolder);
			enhancedCollection.setCollectionOwner(EnhanceUtil.newLocalOwner(entity, property));

			// We must set the value that we have created here!
			next.setProperty(property, entity, enhancedCollection, false);

			return enhancedCollection;
		}

		return result;
	}

	private EnhancedCollection newEnhancedCollection(CollectionType collectionType, boolean isAbsent) {
		switch (collectionType.getTypeCode()) {
			case listType:
				return new EnhancedList<>((ListType) collectionType, newList(), isAbsent);
			case mapType:
				return new EnhancedMap<>((MapType) collectionType, newLinkedMap(), isAbsent);
			case setType:
				return new EnhancedSet<>((SetType) collectionType, newLinkedSet(), isAbsent);
			default:
				throw new RuntimeException("Unsupported collection type: " + collectionType.getTypeCode());
		}
	}

	@Override
	public Object setProperty(Property property, GenericEntity entity, Object value, boolean isVd) {
		if (isVd)
			return next.setProperty(property, entity, value, true);
		
		if (value != null) {
			GenericModelType type = property.getType();

			if (type.isBase())
				type = ((BaseType) type).getActualType(value);

			if (type.isCollection()) {
				CollectionType collectionType = (CollectionType) type;
				EnhancedCollection enhancedCollection = (EnhancedCollection) EnhanceUtil.ensureEnhanced(collectionType, value);

				LocalEntityProperty owner = enhancedCollection.getCollectionOwner();
				if (owner == null) {
					LocalEntityProperty collectionOwner = EnhanceUtil.newLocalOwner(entity, property);
					enhancedCollection.setCollectionOwner(collectionOwner);

				} else if (!isCorrectOwner(owner, entity, property)) {
					enhancedCollection = EnhanceUtil.cloneCollection(collectionType, enhancedCollection);

					LocalEntityProperty collectionOwner = EnhanceUtil.newLocalOwner(entity, property);
					enhancedCollection.setCollectionOwner(collectionOwner);
				}

				value = enhancedCollection;
			}
		}

		return next.setProperty(property, entity, value, false);
	}

	private boolean isCorrectOwner(LocalEntityProperty owner, GenericEntity entity, Property property) {
		return owner.getEntity() == entity && owner.getPropertyName().equals(property.getName());
	}
}
