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

import com.braintribe.model.bvd.convert.ToDate;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;
import com.braintribe.utils.format.lcd.FormatTool;

/**
 * {@link ValueDescriptorEvaluator} for {@link ToDate}
 * 
 */
public class ToDateVde implements ValueDescriptorEvaluator<ToDate> {

	@Override
	public VdeResult evaluate(VdeContext context, ToDate valueDescriptor) throws VdeRuntimeException {

		Object operand = context.evaluate(valueDescriptor.getOperand());
		Object format = valueDescriptor.getFormat();
		Object result = null;
		if (operand instanceof String) {
			if (format instanceof String) {
				try {
					result =  FormatTool.getExpert().getDateFormat().parseDate((String) operand, (String) format);
				} catch (Exception e) {
					throw new VdeRuntimeException("Convert to Date with String operand expects a format compatible with SimpleDateFormat", e);
				}
			} else {
				throw new VdeRuntimeException("Convert ToDate with String operand is not applicable for format:" + format);
			}
		} else if (operand instanceof Long) {
			if (format != null) {
				throw new VdeRuntimeException("Convert ToDate with String operand is not applicable for any format");
			}
			result = new Date((Long) operand);
		} else {
			throw new VdeRuntimeException("Convert ToDate is not applicable to:" + operand);
		}

		return new VdeResultImpl(result, false);
	}

}
