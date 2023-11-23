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
package com.braintribe.model.access.security.query;

import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.query.PropertyOperand;

/**
 * 
 */
class QueryOperandTools {

	public static EntityTypeProperty resolveEntityProperty(PropertyOperand po, SourcesDescriptor querySources) {
		EntityType<?> sourceType = querySources.getSourceType(po.getSource());
		String propertyPath = po.getPropertyName();

		String lastPropertyName = extractLastPropertyName(propertyPath);
		String pathToOwner = extractAllExceptLastProperty(propertyPath);

		EntityType<?> lastPropertyOwnerType = resolveEntityType(sourceType, pathToOwner, false);

		return new EntityTypeProperty(lastPropertyOwnerType, lastPropertyName);
	}

	static class EntityTypeProperty {
		public EntityType<?> entityType;
		public String propertyName;

		public EntityTypeProperty(EntityType<?> entityType, String propertyName) {
			this.entityType = entityType;
			this.propertyName = propertyName;
		}

		@Override
		public String toString() {
			return entityType.getTypeSignature() + "." + propertyName;
		}
	}

	private static String extractLastPropertyName(String path) {
		return path.contains(".") ? path.substring(path.lastIndexOf(".") + 1) : path;
	}

	private static String extractAllExceptLastProperty(String path) {
		return path.contains(".") ? path.substring(0, path.lastIndexOf(".")) : null;
	}

	private static EntityType<?> resolveEntityType(EntityType<?> sourceType, String path, boolean resolveCollectionType) {
		return (EntityType<?>) resolveType(sourceType, path, resolveCollectionType);
	}

	private static GenericModelType resolveType(EntityType<?> entityType, String path, boolean resolveCollectionType) {
		if (path == null) {
			return entityType;
		}

		Property p = resolveProperty(entityType, path);
		GenericModelType type = p.getType();

		if ((type instanceof CollectionType) && resolveCollectionType) {
			type = resolveCollectionElementType(type);
		}

		return type;
	}

	private static Property resolveProperty(EntityType<?> entityType, String path) {
		EntityType<?> startingEntityType = entityType;

		Property property = null;
		GenericModelType type = null;

		String[] properties = path.split("\\.");
		int counter = 0;
		for (String propertyName: properties) {
			boolean isLast = ++counter == properties.length;

			property = entityType.getProperty(propertyName);
			type = property.getType();

			if (!isLast) {
				if (!(type instanceof EntityType)) {
					throw new RuntimeException("Illegal attempt to dereference a type (" + startingEntityType.getTypeSignature() + "." +
							buildPropertyChain(properties, counter) + ".[" + properties[counter] + "])");
				}

				entityType = (EntityType<?>) type;

			}
		}

		return property;
	}

	private static String buildPropertyChain(String[] properties, int counter) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < counter; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(properties[i]);
		}

		return sb.toString();
	}

	private static GenericModelType resolveCollectionElementType(GenericModelType propertyType) {
		return ((CollectionType) propertyType).getCollectionElementType();
	}
}
