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
package com.braintribe.model.generic.enhance;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.collection.PlainList;
import com.braintribe.model.generic.collection.PlainMap;
import com.braintribe.model.generic.collection.PlainSet;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.reflection.SetType;

public class NonNullCollectionEnsuringPropertyAccessInterceptor extends PropertyAccessInterceptor {

	@Override
	public Object getProperty(Property property, GenericEntity entity, boolean isVd) {
		Object result = next.getProperty(property, entity, isVd);
		if (result != null)
			return result;

		GenericModelType propertyType = property.getType();
		if (propertyType.isCollection()) {
			if (property.isAbsent(entity))
				return null;

			result = newCollection((CollectionType) propertyType);

			// We must set the value that we have created here!
			next.setProperty(property, entity, result, false);
		}

		return result;
	}

	private static Object newCollection(CollectionType collectionType) {
		switch (collectionType.getTypeCode()) {
			case listType:
				return new PlainList<>((ListType) collectionType);
			case mapType:
				return new PlainMap<>((MapType) collectionType);
			case setType:
				return new PlainSet<>((SetType) collectionType);
			default:
				throw new RuntimeException("Unsupported collection type: " + collectionType.getTypeCode());
		}
	}

}
