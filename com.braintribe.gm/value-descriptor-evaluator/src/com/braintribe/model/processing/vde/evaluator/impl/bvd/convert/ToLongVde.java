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

import java.util.Date;

import com.braintribe.model.bvd.convert.ToLong;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

/**
 * {@link ValueDescriptorEvaluator} for {@link ToLong}
 * 
 */
public class ToLongVde implements ValueDescriptorEvaluator<ToLong> {

	@Override
	public VdeResult evaluate(VdeContext context, ToLong valueDescriptor) throws VdeRuntimeException {

		Object format = valueDescriptor.getFormat();
		if (format != null) {
			throw new VdeRuntimeException("Format is not supported for ToLong yet, format:" + format);
		}

		Object result = null;
		Object operand = context.evaluate(valueDescriptor.getOperand());

		if (operand instanceof String) {
			
			result = Long.parseLong((String) operand);
		} else if (operand instanceof Boolean) {

			if ((Boolean) operand) {
				result = Long.valueOf(1);
			} else {
				result = Long.valueOf(0);
			}

		} else if (operand instanceof Date) {
			
			result = ((Date) operand).getTime();
			
		} else {
			throw new VdeRuntimeException("Convert ToLong is not applicable to:" + operand);
		}

		return new VdeResultImpl(result, false);
	}

}
