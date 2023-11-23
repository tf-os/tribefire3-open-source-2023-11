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

import com.braintribe.model.bvd.convert.ToFloat;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

/**
 * {@link ValueDescriptorEvaluator} for {@link ToFloat}
 * 
 */
public class ToFloatVde implements ValueDescriptorEvaluator<ToFloat> {

	@Override
	public VdeResult evaluate(VdeContext context, ToFloat valueDescriptor) throws VdeRuntimeException {

		Object format = valueDescriptor.getFormat();
		if (format != null) {
			throw new VdeRuntimeException("Format is not supported for ToFloat yet, format:" + format);
		}

		Object result = null;
		Object operand = context.evaluate(valueDescriptor.getOperand());

		if (operand instanceof String) {
			
			result = Float.parseFloat((String) operand);
		} else if (operand instanceof Boolean) {

			if ((Boolean) operand) {
				result = Float.valueOf(1);
			} else {
				result = Float.valueOf(0);
			}

		} else {
			throw new VdeRuntimeException("Convert ToFloat is not applicable to:" + operand);
		}

		return new VdeResultImpl(result, false);
	}

}
