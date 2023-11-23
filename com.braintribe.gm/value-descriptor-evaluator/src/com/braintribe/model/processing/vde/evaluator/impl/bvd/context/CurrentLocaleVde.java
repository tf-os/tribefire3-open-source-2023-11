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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.context;

import java.util.function.Supplier;

import com.braintribe.model.bvd.context.CurrentLocale;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.aspects.CurrentLocaleAspect;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;


/**
 * {@link ValueDescriptorEvaluator} for {@link CurrentLocale}
 * 
 */
public class CurrentLocaleVde implements ValueDescriptorEvaluator<CurrentLocale> {

	@Override
	public VdeResult evaluate(VdeContext context, CurrentLocale valueDescriptor) throws VdeRuntimeException {

		// get the provider associated with the CurrentLocaleAspect
		Supplier<String> provider = context.get(CurrentLocaleAspect.class);
		try {
			return new VdeResultImpl(provider.get(), false);

		} catch (RuntimeException e) {
			throw new VdeRuntimeException("CurrentLocale evaluation failed", e);
		}

	}

}
