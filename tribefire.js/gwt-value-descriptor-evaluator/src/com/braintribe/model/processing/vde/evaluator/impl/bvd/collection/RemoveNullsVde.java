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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.collection;

import java.util.Collection;

import com.braintribe.model.bvd.convert.collection.RemoveNulls;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;
import com.braintribe.utils.lcd.CollectionTools;

/**
 * {@link ValueDescriptorEvaluator} for {@link RemoveNulls}
 * 
 */
public class RemoveNullsVde implements ValueDescriptorEvaluator<RemoveNulls> {

	@Override
	public VdeResult evaluate(VdeContext context, RemoveNulls valueDescriptor) throws VdeRuntimeException {
		Object operand = context.evaluate(valueDescriptor.getCollection());
		
		if (!(operand instanceof Collection<?>)) {
			throw new VdeRuntimeException("RemoveNull is not applicable to:" + operand);
		}

		Collection<?> collection = (Collection<?>) operand;
		CollectionTools.removeNulls(collection);
		return new VdeResultImpl(collection, false);
	}

}
