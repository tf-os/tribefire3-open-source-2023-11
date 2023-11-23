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
package com.braintribe.model.generic.typecondition.origin;

import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.TypeConditionType;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;

/**
 * Experimental. Not fully supported yet, use at your own risk!!!
 * 
 * Matches a {@link GmCustomType} if it is declared in the model of given name, or a {@link GmCollectionType} if the
 * type parameter of that collection (or one of the parameters two in case of a map) is a {@link GmCustomType} declared
 * in such a model.
 */
public interface IsRelatedToTypeDeclaredIn extends TypeCondition {

	EntityType<IsRelatedToTypeDeclaredIn> T = EntityTypes.T(IsRelatedToTypeDeclaredIn.class);

	String getModelName();
	void setModelName(String modelName);

	@Override
	default boolean matches(GenericModelType type) {
		String modelName = getModelName();
		if (modelName == null)
			return false;

		if (type.isBase() || type.isSimple())
			return GenericModelTypeReflection.rootModelName.equals(modelName);

		if (type.isCollection()) {
			if (type.getTypeCode() == TypeCode.mapType) {
				MapType mapType = (MapType) type;

				return matches(mapType.getKeyType()) || //
						matches(mapType.getValueType());

			} else {
				return matches(((LinearCollectionType) type).getCollectionElementType());
			}
		}

		CustomType ct = (CustomType) type;
		Model model = ct.getModel();
		if (model == null)
			throw new IllegalArgumentException("Cannot evaluate 'IsDeclaredIn' on a type that doesn't have a model: " + type.getTypeSignature());

		return modelName.equals(model.name());
	}

	@Override
	default boolean matches(GmType type) {
		String modelName = getModelName();
		if (modelName == null)
			return false;

		if (!type.isGmCollection()) {
			GmMetaModel m = type.getDeclaringModel();
			return m != null && modelName.equals(m.getName());
		}

		if (type instanceof GmLinearCollectionType)
			return matches(((GmLinearCollectionType) type).getElementType());

		if (type instanceof GmMapType) {
			GmMapType mapType = (GmMapType) type;

			return matches(mapType.getKeyType()) || //
					matches(mapType.getValueType());
		}

		return false;
	}

	@Override
	default TypeConditionType tcType() {
		return TypeConditionType.isRelatedToTypeDeclaredIn;
	}

}
