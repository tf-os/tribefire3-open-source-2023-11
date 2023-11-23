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
package com.braintribe.model.processing.query.stringifier.experts;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Source;

/**
 * 
 */
/* package */ class PropertyResolver {

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	public static Property resolveProperty(Source source, String propertyPath, String defaultType) {
		EntityType<?> entityType = resolveType(source, defaultType);
		if (entityType == null)
			return null;

		String[] propertyNames = propertyPath.split("\\.");
		int counter = 0;
		for (String propertyName : propertyNames) {
			boolean isLast = ++counter == propertyNames.length;
			Property property = entityType.findProperty(propertyName);

			if (property == null || isLast)
				return property;

			GenericModelType propertyType = property.getType();

			if (!propertyType.isEntity())
				return null;

			entityType = (EntityType<?>) propertyType;
		}

		return null;
	}

	private static <T extends GenericModelType> T resolveType(Source source, String defaultType) {
		if (source == null)
			return (T) typeReflection.findEntityType(defaultType);

		if (source instanceof From) {
			return (T) typeReflection.findEntityType(((From) source).getEntityTypeSignature());

		} else {
			Join join = (Join) source;
			return (T) resolvePropertyTypeHelper(join.getSource(), join.getProperty(), defaultType);
		}
	}

	private static GenericModelType resolvePropertyTypeHelper(Source source, String propertyName, String defaultType) {
		EntityType<?> entityType = resolveType(source, defaultType);
		if (entityType == null)
			return null;

		Property property = entityType.findProperty(propertyName);
		if (property == null)
			return null;

		GenericModelType propertyType = property.getType();

		switch (propertyType.getTypeCode()) {
			case listType:
			case mapType:
			case setType:
				return resolveCollectionElementType(propertyType);
			default:
				break;
		}

		return propertyType;
	}

	private static GenericModelType resolveCollectionElementType(GenericModelType propertyType) {
		return ((CollectionType) propertyType).getCollectionElementType();
	}

}
