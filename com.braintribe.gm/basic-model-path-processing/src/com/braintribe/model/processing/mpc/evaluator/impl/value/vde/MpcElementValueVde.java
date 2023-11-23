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
package com.braintribe.model.processing.mpc.evaluator.impl.value.vde;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.mpc.value.MpcElementValue;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.impl.value.vde.aspect.MpcElementValueAspect;
import com.braintribe.model.processing.mpc.evaluator.utils.MpcEvaluationTools;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

/**
 * {@link ValueDescriptorEvaluator} for {@link MpcElementValue}
 * 
 */
public class MpcElementValueVde implements ValueDescriptorEvaluator<MpcElementValue> {

	private static Logger logger = Logger.getLogger(MpcElementValueVde.class);
	private static boolean debug = logger.isDebugEnabled();
	
	public VdeResult evaluate(VdeContext context, MpcElementValue valueDescriptor) throws VdeRuntimeException {
		
		if (debug) logger.debug("get the value for the aspect of MpcElementValueAspect");
		IModelPathElement element = context.get(MpcElementValueAspect.class);
		Object evaluationResult = null;
		
		try {
			evaluationResult = MpcEvaluationTools.resolve(valueDescriptor.getElementAxis(), element);
			if (debug) logger.debug("evaluationResult " + evaluationResult);
		} catch (MpcEvaluatorRuntimeException e) {
			logger.error("MpcElementValueVde failed to resolve the MpcElementValue",e);
			throw new VdeRuntimeException("MpcElementValueVde failed to resolve the MpcElementValue", e);
		}
		
		// TODO validate that the result will not be volatile
		return new VdeResultImpl(evaluationResult, false);
	}

}
