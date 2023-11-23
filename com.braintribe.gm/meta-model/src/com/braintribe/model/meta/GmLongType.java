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
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.weaving.ProtoGmLongType;

public interface GmLongType extends ProtoGmLongType, GmSimpleType {

	EntityType<GmLongType> T = EntityTypes.T(GmLongType.class);

	@Initializer("'long'")
	@Override
	String getTypeSignature();

	@Initializer("'type:long'")
	@Override
	String getGlobalId();

	@Override
	default GmTypeKind typeKind() {
		return GmTypeKind.LONG;
	}

	@Override
	default GenericModelType reflectionType() {
		return SimpleTypes.TYPE_LONG;
	}

	@Override
	default boolean isGmNumber() {
		return true;
	}

}
