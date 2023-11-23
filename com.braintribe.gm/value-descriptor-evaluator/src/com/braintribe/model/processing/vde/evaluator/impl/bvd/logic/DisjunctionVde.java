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

import java.util.List;

import com.braintribe.model.bvd.logic.Disjunction;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

/**
 * {@link ValueDescriptorEvaluator} for {@link Disjunction}
 * 
 */
public class DisjunctionVde implements ValueDescriptorEvaluator<Disjunction> {

	@Override
	public VdeResult evaluate(VdeContext context, Disjunction valueDescriptor) throws VdeRuntimeException {

		Boolean result = false;
		List<Object> operands = valueDescriptor.getOperands();
		
		if(operands == null){
			throw new VdeRuntimeException("Disjunction operands can not be null");
		}
		
		for (Object operand: valueDescriptor.getOperands()){
			
			Object evaluatedOperand = context.evaluate(operand);
			if(!(evaluatedOperand instanceof Boolean)){
				throw new VdeRuntimeException("Disjunction operates on BooleanDescriptor Values only. This is not valid:" + evaluatedOperand);
			}
			result = result || (Boolean)evaluatedOperand;
		}
		
		return new VdeResultImpl(result, false);
	}

}
