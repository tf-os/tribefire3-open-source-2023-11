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
package com.braintribe.model.meta.info;

import java.util.Set;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.TypeRestriction;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.EnumConstantMetaData;
import com.braintribe.model.meta.data.EnumTypeMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.UniversalMetaData;
import com.braintribe.model.weaving.info.ProtoGmEnumTypeInfo;

@Abstract
public interface GmEnumTypeInfo extends ProtoGmEnumTypeInfo, GmCustomTypeInfo {

	EntityType<GmEnumTypeInfo> T = EntityTypes.T(GmEnumTypeInfo.class);

	@Override
	@TypeRestriction({ EnumTypeMetaData.class, UniversalMetaData.class })
	Set<MetaData> getMetaData();

	/**
	 * Gets {@link EnumConstantMetaData} which are valid for all the {@link GmEnumConstant}s of this enum type.
	 *
	 * For reason why we keep this property see remark for {@link GmEntityTypeInfo#getPropertyMetaData()}
	 */
	@Override
	@TypeRestriction({ EnumConstantMetaData.class, UniversalMetaData.class })
	Set<MetaData> getEnumConstantMetaData();
	@Override
	void setEnumConstantMetaData(Set<MetaData> enumConstantMetaData);

	@Override
	GmEnumType addressedType();

	@Override
	default String nameWithOrigin() {
		GmMetaModel declaringModel = getDeclaringModel();
		String origin = declaringModel == null ? " (free)" : " of " + declaringModel.getName();
		return "Enum:" + toSelectiveInformation() + origin;
	}

}
