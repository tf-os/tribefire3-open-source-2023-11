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
package com.braintribe.model.generic.typecondition.logic;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.TypeConditionType;
import com.braintribe.model.meta.GmType;

public interface TypeConditionConjunction extends TypeConditionJunction {

	EntityType<TypeConditionConjunction> T = EntityTypes.T(TypeConditionConjunction.class);

	@Override
	default boolean matches(GenericModelType type) {
		List<TypeCondition> operands = getOperands();

		if (operands != null)
			for (TypeCondition operand : operands)
				if (!operand.matches(type))
					return false;

		return true;
	}

	@Override
	default boolean matches(GmType type) {
		List<TypeCondition> operands = getOperands();

		if (operands != null)
			for (TypeCondition operand : operands)
				if (!operand.matches(type))
					return false;

		return true;
	}

	@Override
	default TypeConditionType tcType() {
		return TypeConditionType.conjunction;
	}

}
