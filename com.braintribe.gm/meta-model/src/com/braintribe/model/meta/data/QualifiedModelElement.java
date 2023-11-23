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
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;

/**
 * Base type for referencing an entity, property, enum or enum constant in a model.
 */
public interface QualifiedModelElement extends GenericEntity {

	EntityType<QualifiedModelElement> T = EntityTypes.T(QualifiedModelElement.class);

	GmModelElement getModelElement();
	void setModelElement(GmModelElement modelElement);

	GmModelElement getModelElementOwner();
	void setModelElementOwner(GmModelElement modelElementOwner);

	// Model

	default boolean targetsModel() {
		return getModelElement() == null;
	}

	// Entity

	default boolean targetsEntityType() {
		return getModelElement() instanceof GmEntityTypeInfo;
	}

	default GmEntityTypeInfo entityTypeInfo() {
		return (GmEntityTypeInfo) getModelElement();
	}

	default boolean targetsProperty() {
		return getModelElement() instanceof GmPropertyInfo;
	}

	default GmPropertyInfo propertyInfo() {
		return (GmPropertyInfo) getModelElement();
	}

	default GmEntityTypeInfo propertyOwner() {
		GmEntityTypeInfo owner = (GmEntityTypeInfo) getModelElementOwner();
		if (owner == null) {
			GmPropertyInfo modelElement = (GmPropertyInfo) getModelElement();
			owner = modelElement.declaringTypeInfo();
		}

		return owner;
	}

	// Enum

	default boolean targetsEnumType() {
		return getModelElement() instanceof GmEnumTypeInfo;
	}

	default GmEnumTypeInfo enumTypeInfo() {
		return (GmEnumTypeInfo) getModelElement();
	}

	default boolean targetsConstant() {
		return getModelElement() instanceof GmEnumConstantInfo;
	}

	default GmEnumConstantInfo constantInfo() {
		return (GmEnumConstantInfo) getModelElement();
	}

	default GmEnumTypeInfo constantOwner() {
		GmEnumTypeInfo owner = (GmEnumTypeInfo) getModelElementOwner();
		if (owner == null) {
			GmEnumConstantInfo modelElement = (GmEnumConstantInfo) getModelElement();
			owner = modelElement.declaringTypeInfo();
		}

		return owner;
	}

}
