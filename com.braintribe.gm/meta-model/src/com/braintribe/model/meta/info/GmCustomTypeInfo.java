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

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmCustomModelElement;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.IsDeclaredInModel;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.model.meta.override.GmCustomTypeOverride;
import com.braintribe.model.weaving.info.ProtoGmCustomTypeInfo;

@Abstract
public interface GmCustomTypeInfo extends ProtoGmCustomTypeInfo, HasMetaData, GmCustomModelElement, IsDeclaredInModel {

	EntityType<GmCustomTypeInfo> T = EntityTypes.T(GmCustomTypeInfo.class);

	@Override
	@Mandatory
	GmMetaModel getDeclaringModel();
	void setDeclaringModel(GmMetaModel declaringModel);

	/**
	 * @return actual GmCustomType for given info. If we call this on a {@link GmCustomType} itself, this method returns
	 *         that same instance, if we call it on a {@link GmCustomTypeOverride}, this method returns the type we are
	 *         overriding.
	 *         
	 * @deprecated use {@link #addressedType()} instead.
	 */
	@Deprecated
	default <T extends GmCustomType> T relatedType() {
		return (T) addressedType();
	}

	@Override
	GmCustomType addressedType();
	
	@Override
	default GmMetaModel declaringModel() {
		return getDeclaringModel();
	}

}
