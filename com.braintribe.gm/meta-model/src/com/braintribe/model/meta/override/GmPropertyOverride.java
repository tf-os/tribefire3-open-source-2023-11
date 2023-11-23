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
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.weaving.override.ProtoGmPropertyOverride;

@SelectiveInformation("${property.name} (override)")
public interface GmPropertyOverride extends ProtoGmPropertyOverride, GmPropertyInfo {

	EntityType<GmPropertyOverride> T = EntityTypes.T(GmPropertyOverride.class);

	@Override
	GmProperty getProperty();
	void setProperty(GmProperty property);

	@Override
	GmEntityTypeInfo getDeclaringTypeInfo();
	void setDeclaringTypeInfo(GmEntityTypeInfo declaringTypeInfo);

	@Override
	default GmProperty relatedProperty() {
		return getProperty();
	}

	@Override
	default GmEntityTypeInfo declaringTypeInfo() {
		return getDeclaringTypeInfo();
	}

}
