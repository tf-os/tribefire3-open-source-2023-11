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

import java.util.Collection;
import java.util.Map;

import com.braintribe.model.bvd.convert.ToCollection;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

public class ToCollectionVde<T extends ToCollection> implements ValueDescriptorEvaluator<T> {

	@Override
	public VdeResult evaluate(VdeContext context, T valueDescriptor) throws VdeRuntimeException {

		Object operand = context.evaluate(valueDescriptor.getOperand());
		Collection<Object> toCollection = valueDescriptor.newCollection();
		
		if (operand instanceof Collection<?>) {
			toCollection.addAll((Collection<?>)operand);
		} else if (operand instanceof Map<?, ?>) {
			toCollection.addAll(((Map<?,?>)operand).keySet());
		} else {
			toCollection.add(operand);
		}
		
		return new VdeResultImpl(toCollection, false);
	}

}
