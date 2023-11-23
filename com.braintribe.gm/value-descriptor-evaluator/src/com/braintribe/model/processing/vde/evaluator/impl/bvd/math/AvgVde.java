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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.math;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.bvd.math.Avg;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.arithmetic.ArithmeticOperator;

/**
 * {@link ValueDescriptorEvaluator} for {@link Avg}
 * 
 */
public class AvgVde extends AbstractArithmeticVde implements ValueDescriptorEvaluator<Avg> {

	@Override
	public VdeResult evaluate(VdeContext context, Avg valueDescriptor) throws VdeRuntimeException {

		List<Object> operandsList = valueDescriptor.getOperands();

		if (operandsList == null || operandsList.isEmpty()) {
			throw new VdeRuntimeException("No operands provided for Arithmetic average operation");
		} else {

			int size = operandsList.size();
			Object result = super.evaluate(context, valueDescriptor, ArithmeticOperator.plus).getResult();

			List<Object> averageOperands = new ArrayList<Object>();
			averageOperands.add(result);
			averageOperands.add(Double.valueOf(size));

			valueDescriptor.setOperands(averageOperands);
			return super.evaluate(context, valueDescriptor, ArithmeticOperator.divide);
		}
	}
}
