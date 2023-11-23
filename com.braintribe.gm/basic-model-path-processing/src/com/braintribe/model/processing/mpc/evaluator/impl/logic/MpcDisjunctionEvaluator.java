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
package com.braintribe.model.processing.mpc.evaluator.impl.logic;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.mpc.logic.MpcDisjunction;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluator;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorContext;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;

/**
 * {@link MpcEvaluator} for {@link MpcDisjunction}
 * 
 */
public class MpcDisjunctionEvaluator extends MpcJunctionEvaluatorImpl<MpcDisjunction> {

	private static Logger logger = Logger.getLogger(MpcDisjunctionEvaluator.class);
	private static boolean debug = logger.isDebugEnabled();
	private static boolean trace = logger.isTraceEnabled();
	
	@Override
	public MpcMatch matches(MpcEvaluatorContext context, MpcDisjunction condition, IModelPathElement element) throws MpcEvaluatorRuntimeException {

		if(trace) logger.trace("init values to assist in identifying the actual output that will be used by this method.");
		MpcJunctionCaptureResultImpl result = initResult(condition.getJunctionCapture(), element);
		boolean firstEntry = true;

		if(debug) logger.debug("loop through all the operands");
		for (Object operand : condition.getOperands()) {

			if(debug) logger.debug("evaluate each operand");
			MpcJunctionCaptureResultImpl currentResult = getNewCaptureResult();
			currentResult.setReturnPath(context.matches(operand, element));

			if(trace) logger.trace("if at least one operand succeeds then the disjunction succeeds, second part of the condition to cover the root case " + currentResult.getReturnPath() + " " + currentResult.getPathLength());
			if (currentResult.getReturnPath() != null ||
			    (currentResult.getReturnPath() == null && currentResult.getPathLength() == -1)) {

				if(trace) logger.trace("evaluate the return value according to the junction capture");
				result = (MpcJunctionCaptureResultImpl) evaluateMatchResult(condition.getJunctionCapture(), result, currentResult, firstEntry);

				if(trace) logger.trace("Set firstEntry to false as at least one operand has been evaluated");
				firstEntry = false;
			}
		}

		if(debug) logger.debug("At least one successful: "+ !firstEntry +" path so far: " + result.getMatchResult());
		
		return (!firstEntry) ? result.getMatchResult() : null;

	}

	@Override
	public boolean allowsPotentialMatches(MpcDisjunction condition) {
		return false;
	}

	@Override
	public boolean hasNestedConditions(MpcDisjunction condition) {
		return true;
	}

}
