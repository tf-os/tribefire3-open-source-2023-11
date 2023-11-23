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

import com.braintribe.model.bvd.math.ApproximateOperation;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.approximate.ApproximateEvalContext;
import com.braintribe.model.processing.vde.evaluator.api.approximate.ApproximateOperator;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.ApproximateEvalContextImpl;

public abstract class AbstractApproximateVde {

	protected VdeResult evaluate(VdeContext context, ApproximateOperation valueDescriptor, ApproximateOperator operator) throws VdeRuntimeException {

		Object value = context.evaluate(valueDescriptor.getValue());
		Object precision = context.evaluate(valueDescriptor.getPrecision());
		if (value == null || precision == null) { 
			throw new VdeRuntimeException("Value and Precision must be provided for Approximate operation");
		} else {
			ApproximateEvalContext approximateContext = new ApproximateEvalContextImpl();
			Object result = approximateContext.evaluate(value, precision, operator);
			
			return new VdeResultImpl(result, false);
		}
	}}
