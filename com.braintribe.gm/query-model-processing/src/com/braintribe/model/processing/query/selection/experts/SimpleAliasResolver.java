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
package com.braintribe.model.processing.query.selection.experts;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Source;

public class SimpleAliasResolver extends AbstractAliasResolver {
	

	@Override
	public String getAliasForSource(Source source) {
		EntityType<GenericEntity> entityType = getEntityType(source);
		if (entityType != null) {
			return resolveTypeSignature(entityType.getTypeSignature());
		}
		return null;
	}

	protected EntityType<GenericEntity> getEntityType(Source source) {
		String typeSignature = null;
		
		if (source instanceof From) {
			typeSignature = ((From) source).getEntityTypeSignature();
		} else if (source instanceof Join) {
			Join join = (Join) source;
			EntityType<GenericEntity> joinType = getEntityType(join.getSource());
			String joinPropertyName = join.getProperty();
			Property property = joinType.findProperty(joinPropertyName);
				
			if (property != null) {
				GenericModelType propertyType = property.getType();
				switch (propertyType.getTypeCode()) {
				case entityType:
					@SuppressWarnings("unchecked")
					EntityType<GenericEntity> propertyEntityType = (EntityType<GenericEntity>) propertyType;
					typeSignature = propertyEntityType.getTypeSignature();
					break;
				case listType:
				case mapType:
				case setType:
					CollectionType propertyCollectionType = (CollectionType) propertyType;
					GenericModelType collectionElementType = propertyCollectionType.getCollectionElementType();
					if (collectionElementType instanceof EntityType<?>) {
						typeSignature = collectionElementType.getTypeSignature();
					}
					break;
				default:
					break;

				}
			} 
		}
		
		if (typeSignature != null) {
			return GMF.getTypeReflection().getEntityType(typeSignature);
		}
		return null;
	}

}
