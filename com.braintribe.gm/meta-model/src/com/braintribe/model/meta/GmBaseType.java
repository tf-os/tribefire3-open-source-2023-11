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

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Singleton;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.weaving.ProtoGmBaseType;

@SelectiveInformation(value = "object")

@Singleton
public interface GmBaseType extends ProtoGmBaseType, GmType, IsDeclaredInModel {

	EntityType<GmBaseType> T = EntityTypes.T(GmBaseType.class);

	@Initializer("'object'")
	@Override
	String getTypeSignature();

	@Initializer("'type:object'")
	@Override
	String getGlobalId();

	@Override
	@Mandatory
	GmMetaModel getDeclaringModel();

	@Override
	default GmTypeKind typeKind() {
		return GmTypeKind.BASE;
	}

	@Override
	default boolean isGmBase() {
		return true;
	}

	@Override
	default GenericModelType reflectionType() {
		return BaseType.INSTANCE;
	}
	
	@Override
	default GmMetaModel declaringModel() {
		return getDeclaringModel();
	}

}
