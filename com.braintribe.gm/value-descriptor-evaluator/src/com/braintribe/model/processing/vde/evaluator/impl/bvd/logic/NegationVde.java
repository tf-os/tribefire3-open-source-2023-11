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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.logic;

import com.braintribe.model.bvd.logic.Negation;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

/**
 * {@link ValueDescriptorEvaluator} for {@link Negation}
 * 
 */
public class NegationVde implements ValueDescriptorEvaluator<Negation> {

	@Override
	public VdeResult evaluate(VdeContext context, Negation valueDescriptor) throws VdeRuntimeException {

		Object operand = valueDescriptor.getOperand();
		
		if(operand == null){
			throw new VdeRuntimeException("Negation can't evaluate on null operands");
		}
		
		Object evaluatedOperand = context.evaluate(operand);

		if (!(evaluatedOperand instanceof Boolean)) {
			throw new VdeRuntimeException("Negation operates on BooleanDescriptor Values only. This is not valid:" + evaluatedOperand);
		}
		Boolean result = !(Boolean) evaluatedOperand;

		return new VdeResultImpl(result, false);
	}

}
