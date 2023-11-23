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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.convert;

import com.braintribe.model.bvd.convert.ToEnum;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

/**
 * {@link ValueDescriptorEvaluator} for {@link ToEnum}
 * 
 */
public class ToEnumVde implements ValueDescriptorEvaluator<ToEnum> {

	@Override
	public VdeResult evaluate(VdeContext context, ToEnum valueDescriptor) throws VdeRuntimeException {

		Object result = null;
		Object operand = context.evaluate(valueDescriptor.getOperand());
		
		EnumType enumType = GMF.getTypeReflection().getType(valueDescriptor.getTypeSignature());
		// operand is either a string or ordinal
		if (operand instanceof String) {
			result = enumType.getInstance((String) operand);
		} else if (operand instanceof Integer) {
			result = enumType.getEnumValues()[(Integer) operand];
		} else {
			throw new VdeRuntimeException("Operand must be of type String or Integer for ToEnum, not " + operand);
		}
		return new VdeResultImpl(result, false);
	}

}
