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
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.TypeConditionType;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;

/**
 * Experimental. Not fully supported yet, use at your own risk!!!
 * 
 * Matches a {@link GmCustomType}, iff it is declared in the model of given name.
 */
public interface IsDeclaredIn extends TypeCondition {

	EntityType<IsDeclaredIn> T = EntityTypes.T(IsDeclaredIn.class);

	String getModelName();
	void setModelName(String modelName);

	@Override
	default boolean matches(GenericModelType type) {
		if (type.isCollection())
			return false;

		String modelName = getModelName();
		if (modelName == null)
			return false;

		if (type.isBase() || type.isSimple())
			return GenericModelTypeReflection.rootModelName.equals(modelName);

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

		if (type.isGmCollection())
			return false;

		GmMetaModel m = type.getDeclaringModel();
		return m != null && modelName.equals(m.getName());

	}

	@Override
	default TypeConditionType tcType() {
		return TypeConditionType.isDeclaredIn;
	}

}
