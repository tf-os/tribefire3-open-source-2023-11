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
package com.braintribe.model.generic.value;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.annotation.ForwardDeclaration;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.value.type.DynamicallyTypedDescriptor;

@ForwardDeclaration("com.braintribe.gm:value-descriptor-model")
@SuppressWarnings("unusable-by-js")
public interface EnumReference extends DynamicallyTypedDescriptor {

	EntityType<EnumReference> T = EntityTypes.T(EnumReference.class);

	void setConstant(String constant);
	String getConstant();

	default EnumType enumType() {
		return GMF.getTypeReflection().getType(getTypeSignature());
	}

	default Enum<?> constant() {
		return enumType().getEnumValue(getConstant());
	}

	static EnumReference of(Enum<?> enumConstant) {
		return create(enumConstant.getDeclaringClass().getName(), enumConstant.name());
	}

	static EnumReference create(String enumTypeSignature, String constantName) {
		EnumReference ref = EnumReference.T.create();
		ref.setTypeSignature(enumTypeSignature);
		ref.setConstant(constantName);

		return ref;
	}

}
