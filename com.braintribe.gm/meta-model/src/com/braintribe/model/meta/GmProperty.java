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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.meta.restriction.GmTypeRestriction;
import com.braintribe.model.weaving.ProtoGmProperty;

@SelectiveInformation("${name}")
@ToStringInformation("${declaringType.typeSignature}.${name}")
public interface GmProperty extends ProtoGmProperty, GmPropertyInfo {

	EntityType<GmProperty> T = EntityTypes.T(GmProperty.class);

	@Override
	String getName();
	@Override
	void setName(String name);

	@Override
	GmType getType();
	void setType(GmType type);

	@Override
	GmEntityType getDeclaringType();
	void setDeclaringType(GmEntityType declaringType);

	@Override
	GmTypeRestriction getTypeRestriction();
	void setTypeRestriction(GmTypeRestriction typeRestriction);

	@Override
	@Initializer("true")
	boolean getNullable();
	@Override
	void setNullable(boolean nullable);

	default boolean isId() {
		return GenericEntity.id.equals(getName());
	}

	@Override
	default GmProperty relatedProperty() {
		return this;
	}

	@Override
	default GmEntityTypeInfo declaringTypeInfo() {
		return getDeclaringType();
	}

}
