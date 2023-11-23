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
package com.braintribe.model.processing.validation.expert;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.processing.ValidationContext;
import com.braintribe.model.processing.ValidationExpert;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;

/**
 * Validates the type of the currently visited {@link TraversingModelPathElement} against meeting a certain
 * {@link TypeCondition}
 * 
 * @author Neidhart.Orlich
 *
 */
public class TypeExpert implements ValidationExpert {
	private final TypeCondition typeCondition;

	public TypeExpert(TypeCondition typeCondition) {
		this.typeCondition = typeCondition;
	}

	@Override
	public void validate(ValidationContext context) {
		GenericModelType type = context.getPathElement().getType();
		if (!typeCondition.matches(type)) {
			String errorMessage;
			if (typeCondition instanceof IsAssignableTo) {
				IsAssignableTo isType = (IsAssignableTo) typeCondition;
				errorMessage = "Root type mismatch: Expected: type " + isType.getTypeSignature() + " but got type " + type.getTypeSignature();
			} else {
				errorMessage = "Root type (" + type.getTypeSignature() + ") did not match type condition: " + typeCondition;
			}

			context.notifyConstraintViolation(errorMessage);
		}
	}

}
