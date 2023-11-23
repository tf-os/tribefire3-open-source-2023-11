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
package com.braintribe.model.meta.data;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

/**
 * This exists to enable referencing of properties of given {@link GmEntityType} which are inherited from a super-type.
 */
@ToStringInformation("${entityType.typeSignature}.${property.name}")
public interface QualifiedProperty extends GenericEntity {

	EntityType<QualifiedProperty> T = EntityTypes.T(QualifiedProperty.class);

	/**
	 * The entityType must be either a subType of {@link #getProperty() property}'s declaring entity type, or
	 * <code>null</code>, in which case we consider it to be the property's declaring type.
	 */
	GmEntityType getEntityType();
	void setEntityType(GmEntityType entityType);

	GmProperty getProperty();
	void setProperty(GmProperty property);

	default GmEntityType propertyOwner() {
		GmEntityType owner = getEntityType();
		return owner == null ? getProperty().getDeclaringType() : owner;
	}

	default String propertyName() {
		return getProperty().getName();
	}

	default GmType propertyType() {
		return getProperty().getType();
	}

}
