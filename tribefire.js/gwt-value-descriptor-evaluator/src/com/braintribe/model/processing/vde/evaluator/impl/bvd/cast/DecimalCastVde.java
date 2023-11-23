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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.cast;

import java.math.BigDecimal;

import com.braintribe.model.bvd.cast.DecimalCast;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

/**
 * {@link ValueDescriptorEvaluator} for {@link DecimalCast}
 * 
 */
public class DecimalCastVde extends AbstractCastVde implements ValueDescriptorEvaluator<DecimalCast> {

	@Override
	public VdeResult evaluate(VdeContext context, DecimalCast valueDescriptor) throws VdeRuntimeException {

		Object operand = context.evaluate(valueDescriptor.getOperand());

		if ( !isValidCastOperand(operand)) {

			throw new VdeRuntimeException("Cast to Decimal is not applicable to:" + operand);
		}

		BigDecimal result = new BigDecimal(operand.toString());
		return new VdeResultImpl(result, false);
	}

}
