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

import java.math.BigDecimal;
import java.math.MathContext;

import com.braintribe.model.bvd.convert.ToDecimal;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

/**
 * {@link ValueDescriptorEvaluator} for {@link ToDecimal}
 * 
 */
public class ToDecimalVde implements ValueDescriptorEvaluator<ToDecimal> {

	@Override
	public VdeResult evaluate(VdeContext context, ToDecimal valueDescriptor) throws VdeRuntimeException {

		Object format = valueDescriptor.getFormat();

		Object result = null;

		Object operand = context.evaluate(valueDescriptor.getOperand());

		if (operand instanceof String) {

			if (format != null) {
				if (format instanceof String) {
					try {
						result = new BigDecimal((String) operand, new MathContext((String) format));
					} catch (Exception e) {
						throw new VdeRuntimeException(
								"Format for ToDecimal with String operand should follow MathContext format", e);
					}
				} else {
					throw new VdeRuntimeException("Convert ToDecimal with String operand is not applicable for format:"
							+ format);
				}

			} else {
				result = new BigDecimal((String) operand);
			}
		} else if (operand instanceof Boolean) {

			if (format != null) {
				throw new VdeRuntimeException("Convert ToDecimal with Boolean operand is not applicable for any format");
			}

			if ((Boolean) operand) {
				result = new BigDecimal(1);
			} else {
				result = new BigDecimal(0);
			}

		} else {
			throw new VdeRuntimeException("Convert ToDecimal is not applicable to:" + operand);
		}

		return new VdeResultImpl(result, false);
	}

}
