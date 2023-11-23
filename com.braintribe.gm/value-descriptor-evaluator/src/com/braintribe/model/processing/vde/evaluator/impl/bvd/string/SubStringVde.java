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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.string;

import com.braintribe.model.bvd.string.SubString;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

/**
 * {@link ValueDescriptorEvaluator} for {@link SubString}
 * 
 */
public class SubStringVde implements ValueDescriptorEvaluator<SubString> {

	@Override
	public VdeResult evaluate(VdeContext context, SubString valueDescriptor) throws VdeRuntimeException {

		Object operand = context.evaluate(valueDescriptor.getOperand());
		Integer startIndex = valueDescriptor.getStartIndex();
		Integer endIndex = valueDescriptor.getEndIndex();
		
		if(startIndex == null){
			throw new VdeRuntimeException("SubString requires a start index");
		}

		if (operand instanceof String) {
			if(endIndex != null){
				return new VdeResultImpl(((String) operand).substring(startIndex, endIndex), false);	
			}
			else{
				return new VdeResultImpl(((String) operand).substring(startIndex), false);
			}
		}
		throw new VdeRuntimeException("SubString is only applicable to String, and not:" + operand);
	}

}
