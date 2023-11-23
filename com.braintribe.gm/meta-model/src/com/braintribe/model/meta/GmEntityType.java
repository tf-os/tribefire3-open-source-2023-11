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
package com.braintribe.model.meta;

import java.util.List;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.weaving.ProtoGmEntityType;

@SelectiveInformation(value = "${typeSignature}")
public interface GmEntityType extends ProtoGmEntityType, GmCustomType, GmEntityTypeInfo {

	EntityType<GmEntityType> T = EntityTypes.T(GmEntityType.class);

	@Override
	boolean getIsAbstract();
	@Override
	void setIsAbstract(boolean isAbstract);

	@Override
	List<GmEntityType> getSuperTypes();
	void setSuperTypes(List<GmEntityType> superTypes);

	@Override
	List<GmProperty> getProperties();
	void setProperties(List<GmProperty> properties);

	@Override
	GmType getEvaluatesTo();
	void setEvaluatesTo(GmType evaluatesTo);

	@Override
	default GmTypeKind typeKind() {
		return GmTypeKind.ENTITY;
	}

	@Override
	default boolean isGmEntity() {
		return true;
	}

	@Override
	default GmEntityType addressedType() {
		return this;
	}

}
