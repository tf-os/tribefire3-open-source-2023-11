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
package com.braintribe.model.generic.typecondition.basic;

import java.util.List;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.TypeConditionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmType;

public interface IsAssignableTo extends TypeCondition {

	EntityType<IsAssignableTo> T = EntityTypes.T(IsAssignableTo.class);

	String getTypeSignature();
	void setTypeSignature(String typeSignature);

	@Override
	default boolean matches(GenericModelType type) {
		String typeSig = getTypeSignature();
		if (typeSig == null)
			return false;

		GenericModelType thisType = GMF.getTypeReflection().findType(typeSig);
		if (thisType == null)
			return false;

		return thisType.isAssignableFrom(type);
	}

	@Override
	default boolean matches(GmType type) {
		String typeSignature = getTypeSignature();
		if (typeSignature == null)
			return false;

		return isAssignableTo(type, typeSignature);
	}

	static boolean isAssignableTo(GmType type, String superSignature) {
		if (type.getTypeSignature().equals(superSignature))
			return true;

		switch (type.typeKind()) {
			case ENTITY:
				List<GmEntityType> superTypes = ((GmEntityType) type).getSuperTypes();

				if (superTypes != null)
					for (GmEntityType superType : superTypes)
						if (isAssignableTo(superType, superSignature))
							return true;

				return false;

			case BASE:
				return false;

			default:
				return "object".equals(superSignature);
		}
	}

	@Override
	default TypeConditionType tcType() {
		return TypeConditionType.isAssignableTo;
	}

}
