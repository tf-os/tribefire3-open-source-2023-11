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
package com.braintribe.model.processing.vde.evaluator.impl.root;

import java.util.function.Function;

import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.aspects.VariableProviderAspect;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;


/**
 * {@link ValueDescriptorEvaluator} for {@link Variable}
 * 
 * 
 */
public class VariableVde implements ValueDescriptorEvaluator<Variable> {

	@Override
	public VdeResult evaluate(VdeContext context, Variable variable) throws VdeRuntimeException {

		// get the provider associated with the VariableProviderAspect
		Function<Variable, ?> provider = context.get(VariableProviderAspect.class);
		VdeResult result = null;
		try {
			// if the provider provides a result, then return it, even if it is
			// null
			result = new VdeResultImpl(provider.apply(variable), false);

		} catch (RuntimeException e) {
			// An exception occurred because either the provider was not able to
			// return a result or the value does not exist in the provider
			String errorMessage  = "Variable evaluation failed";
			switch (context.getEvaluationMode()) {
				case Preliminary:
					result = new VdeResultImpl(errorMessage + e);
					break;
				case Final:
					throw new VdeRuntimeException(errorMessage, e);

			}
		}

		return result;
	}

}
