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
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.PropertyMetaData;
import com.braintribe.model.meta.data.UniversalMetaData;
import com.braintribe.model.weaving.info.ProtoGmPropertyInfo;

@Abstract
public interface GmPropertyInfo extends ProtoGmPropertyInfo, HasMetaData, GmCustomModelElement {

	EntityType<GmPropertyInfo> T = EntityTypes.T(GmPropertyInfo.class);

	@Override
	Object getInitializer();
	@Override
	void setInitializer(Object initializer);

	@Override
	@TypeRestriction({ PropertyMetaData.class, UniversalMetaData.class })
	Set<MetaData> getMetaData();

	@Override
	GmProperty relatedProperty();

	@Override
	GmEntityTypeInfo declaringTypeInfo();

	@Override
	default GmMetaModel declaringModel() {
		return declaringTypeInfo().getDeclaringModel();
	}

	@Override
	default String nameWithOrigin() {
		GmEntityTypeInfo declaringType = declaringTypeInfo();
		String origin = declaringType == null ? " (free)" : " of " + declaringType.nameWithOrigin();
		return "property:" + toSelectiveInformation() + origin;
	}

}
