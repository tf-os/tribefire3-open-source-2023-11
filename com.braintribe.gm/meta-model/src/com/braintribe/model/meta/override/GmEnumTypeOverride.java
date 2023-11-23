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
package com.braintribe.model.meta.override;

import java.util.List;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.weaving.override.ProtoGmEnumTypeOverride;

@SelectiveInformation(value = "${enumType.typeSignature} (override)")
public interface GmEnumTypeOverride extends ProtoGmEnumTypeOverride, GmEnumTypeInfo, GmCustomTypeOverride {

	EntityType<GmEnumTypeOverride> T = EntityTypes.T(GmEnumTypeOverride.class);

	@Override
	GmEnumType getEnumType();
	void setEnumType(GmEnumType enumType);

	@Override
	List<GmEnumConstantOverride> getConstantOverrides();
	void setConstantOverrides(List<GmEnumConstantOverride> constantOverrides);

	@Override
	default boolean isGmEnumOverride() {
		return true;
	}

	@Override
	default GmEnumType addressedType() {
		return getEnumType();
	}

}
