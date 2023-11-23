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

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.weaving.override.ProtoGmEnumConstantOverride;

@SelectiveInformation(value = "${enumConstant.name} (override)")
public interface GmEnumConstantOverride extends ProtoGmEnumConstantOverride, GmEnumConstantInfo {

	EntityType<GmEnumConstantOverride> T = EntityTypes.T(GmEnumConstantOverride.class);

	@Override
	GmEnumConstant getEnumConstant();
	void setEnumConstant(GmEnumConstant enumConstant);

	@Override
	GmEnumTypeOverride getDeclaringTypeOverride();
	void setDeclaringTypeOverride(GmEnumTypeOverride declaringTypeOverride);

	@Override
	default GmEnumConstant relatedConstant() {
		return getEnumConstant();
	}

	@Override
	default GmEnumTypeInfo declaringTypeInfo() {
		return getDeclaringTypeOverride();
	}

}
