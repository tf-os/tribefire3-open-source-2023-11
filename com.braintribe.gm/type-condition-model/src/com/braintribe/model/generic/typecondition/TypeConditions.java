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
package com.braintribe.model.generic.typecondition;

import java.util.Arrays;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.typecondition.basic.IsAnyType;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.generic.typecondition.basic.IsType;
import com.braintribe.model.generic.typecondition.basic.IsTypeKind;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.generic.typecondition.logic.TypeConditionConjunction;
import com.braintribe.model.generic.typecondition.logic.TypeConditionDisjunction;
import com.braintribe.model.generic.typecondition.logic.TypeConditionNegation;
import com.braintribe.model.generic.typecondition.origin.IsDeclaredIn;
import com.braintribe.model.generic.typecondition.origin.IsRelatedToTypeDeclaredIn;
import com.braintribe.model.generic.typecondition.param.CollectionElementCondition;
import com.braintribe.model.generic.typecondition.param.MapKeyCondition;
import com.braintribe.model.generic.typecondition.param.MapValueCondition;

public interface TypeConditions {

	/** same as {@link #or} for namespace clashes */
	static TypeConditionDisjunction orTc(TypeCondition... operands) {
		return or(operands);
	}

	static TypeConditionDisjunction or(TypeCondition... operands) {
		TypeConditionDisjunction result = TypeConditionDisjunction.T.create();
		result.getOperands().addAll(Arrays.asList(operands));
		return result;
	}

	/** same as {@link #and} for namespace clashes */
	static TypeConditionConjunction andTc(TypeCondition... operands) {
		return and(operands);
	}

	static TypeConditionConjunction and(TypeCondition... operands) {
		TypeConditionConjunction result = TypeConditionConjunction.T.create();
		result.getOperands().addAll(Arrays.asList(operands));
		return result;
	}

	/** same as {@link #not} for namespace clashes */
	static TypeConditionNegation notTc(TypeCondition operand) {
		return not(operand);
	}

	static TypeConditionNegation not(TypeCondition operand) {
		TypeConditionNegation result = TypeConditionNegation.T.create();
		result.setOperand(operand);
		return result;
	}

	static IsTypeKind isKind(TypeKind kind) {
		IsTypeKind result = IsTypeKind.T.create();
		result.setKind(kind);
		return result;
	}

	static IsType isType(String typeSignature) {
		IsType result = IsType.T.create();
		result.setTypeSignature(typeSignature);
		return result;
	}

	static IsType isType(GenericModelType type) {
		return isType(type.getTypeSignature());
	}

	static IsAssignableTo isAssignableTo(String typeSignature) {
		IsAssignableTo result = IsAssignableTo.T.create();
		result.setTypeSignature(typeSignature);
		return result;
	}

	static IsAssignableTo isAssignableTo(GenericModelType type) {
		return isAssignableTo(type.getTypeSignature());
	}

	static IsAnyType isAny() {
		return IsAnyType.T.create();
	}

	static MapKeyCondition hasMapKey(TypeCondition operand) {
		MapKeyCondition result = MapKeyCondition.T.create();
		result.setCondition(operand);
		return result;
	}

	static MapValueCondition hasMapValue(TypeCondition operand) {
		MapValueCondition result = MapValueCondition.T.create();
		result.setCondition(operand);
		return result;
	}

	static CollectionElementCondition hasCollectionElement(TypeCondition operand) {
		CollectionElementCondition result = CollectionElementCondition.T.create();
		result.setCondition(operand);
		return result;
	}

	static IsDeclaredIn isDeclaredIn(String modelName) {
		IsDeclaredIn result = IsDeclaredIn.T.create();
		result.setModelName(modelName);
		return result;
	}

	static IsRelatedToTypeDeclaredIn isRelatedToTypeDeclaredIn(String modelName) {
		IsRelatedToTypeDeclaredIn result = IsRelatedToTypeDeclaredIn.T.create();
		result.setModelName(modelName);
		return result;
	}

}
