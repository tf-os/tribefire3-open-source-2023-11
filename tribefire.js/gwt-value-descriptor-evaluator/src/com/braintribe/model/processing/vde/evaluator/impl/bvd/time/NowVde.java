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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.time;

import java.util.Date;

import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.aspects.DateAspect;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

/**
 * {@link ValueDescriptorEvaluator} for {@link Now}
 * 
 */
public class NowVde implements ValueDescriptorEvaluator<Now> {

	@Override
	public VdeResult evaluate(VdeContext context, Now valueDescriptor) throws VdeRuntimeException {

		VdeResultImpl result = new VdeResultImpl(((valueDescriptor != null) ? getNow(context) : null), false);
		return result;
	}

	private Date getNow(VdeContext context) {
		Date date = context.get(DateAspect.class);
		if (date == null) {
			date = new Date();
		}
		return date;
	}

}
