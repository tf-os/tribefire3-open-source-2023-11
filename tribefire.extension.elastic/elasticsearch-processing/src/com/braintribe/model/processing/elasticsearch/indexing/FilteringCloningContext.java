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
package com.braintribe.model.processing.elasticsearch.indexing;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.processing.elasticsearch.util.ElasticsearchUtils;

public class FilteringCloningContext extends StandardCloningContext {

	private List<Property> properties;

	public FilteringCloningContext(List<Property> properties) {
		this.properties = properties;
	}

	@Override
	public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property, GenericEntity instanceToBeCloned,
			GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {

		boolean result = false;

		if (property.isIdentifier()) {
			result = true;

		} else if (!properties.contains(property)) {
			result = false;

		} else {
			GenericModelType propertyType = property.getType();
			TypeCode typeCode = propertyType.getTypeCode();

			switch (typeCode) {
				case entityType:
					result = false;
					break;
				case listType:
				case setType:
				case mapType:
					GenericModelType elementType = ((CollectionType) propertyType).getCollectionElementType();
					result = ElasticsearchUtils.isScalarType(elementType);
					break;

				default:
					result = true;
					break;

			}

		}

		return result;

	}

}
