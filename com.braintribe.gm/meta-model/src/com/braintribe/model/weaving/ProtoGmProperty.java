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
package com.braintribe.model.weaving;

import com.braintribe.model.weaving.info.ProtoGmEntityTypeInfo;
import com.braintribe.model.weaving.info.ProtoGmPropertyInfo;
import com.braintribe.model.weaving.restriction.ProtoGmTypeRestriction;

public interface ProtoGmProperty extends ProtoGmPropertyInfo {

	String getName();
	void setName(String name);

	ProtoGmType getType();

	ProtoGmEntityType getDeclaringType();

	ProtoGmTypeRestriction getTypeRestriction();

	boolean getNullable();
	void setNullable(boolean nullable);

	@Override
	default ProtoGmProperty relatedProperty() {
		return this;
	}

	@Override
	default ProtoGmEntityTypeInfo declaringTypeInfo() {
		return getDeclaringType();
	}

}
