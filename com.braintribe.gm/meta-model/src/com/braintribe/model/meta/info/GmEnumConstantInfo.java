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
import com.braintribe.model.meta.GmCustomModelElement;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.EnumConstantMetaData;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.UniversalMetaData;
import com.braintribe.model.weaving.info.ProtoGmEnumConstantInfo;

@Abstract
public interface GmEnumConstantInfo extends ProtoGmEnumConstantInfo, HasMetaData, GmCustomModelElement {

	EntityType<GmEnumConstantInfo> T = EntityTypes.T(GmEnumConstantInfo.class);

	@Override
	@TypeRestriction({ EnumConstantMetaData.class, UniversalMetaData.class })
	Set<MetaData> getMetaData();

	@Override
	GmEnumConstant relatedConstant();

	@Override
	GmEnumTypeInfo declaringTypeInfo();

	@Override
	default GmMetaModel declaringModel() {
		return declaringTypeInfo().getDeclaringModel();
	}

	@Override
	default String nameWithOrigin() {
		GmEnumTypeInfo declaringTypeInfo = declaringTypeInfo();
		String origin = declaringTypeInfo == null ? " (free)" : " of " + declaringTypeInfo.nameWithOrigin();
		return "constant:" + toSelectiveInformation() + origin;
	}

}
