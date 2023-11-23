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
import com.braintribe.model.mpc.logic.MpcJunction;
import com.braintribe.model.mpc.logic.MpcJunctionCapture;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcJunctionEvaluator;
import com.braintribe.model.processing.mpc.evaluator.api.logic.MpcJunctionCaptureResult;

/**
 * Abstract implementation of {@link MpcJunctionEvaluator}
 * 
 * @param <C>
 *            Type of the ModelPathCondition, which should extend {@link MpcJunction}
 * 
 */
public abstract class MpcJunctionEvaluatorImpl<C extends MpcJunction> implements MpcJunctionEvaluator<C> {

	private static Logger logger = Logger.getLogger(MpcJunctionEvaluatorImpl.class);
	private static boolean trace = logger.isTraceEnabled();
	
	@Override
	public MpcJunctionCaptureResultImpl initResult(MpcJunctionCapture capture, IModelPathElement element) {
		
		if (trace) logger.trace("initilaise MpcJunctionCaptureResult");
		MpcJunctionCaptureResultImpl result = getNewCaptureResult();

		result.setReturnPath(element);
		
		if (trace) logger.trace("If junction capture is longest then set depth to zero, otherwise set length to max length");
		result.setPathLength((capture == MpcJunctionCapture.longest) ? 0 : element.getDepth());
		if (trace) logger.trace("Path length is: " + result.getPathLength());
		
		return result;
	}

	@Override
	public MpcJunctionCaptureResult evaluateMatchResult(MpcJunctionCapture capture,MpcJunctionCaptureResult captureResult, MpcJunctionCaptureResult currentCaptureResult, boolean firstEntry) throws MpcEvaluatorRuntimeException {

		if (trace) logger.trace("Compute the result based on the consumption of the model path element of and the junction capture");
		switch (capture) {
			case shortest:
				if (trace) logger.trace("evaluate all then, return the shortest if all are successful");

				if (currentCaptureResult.getPathLength() <= captureResult.getPathLength()) {
					captureResult.setPathLength(currentCaptureResult.getPathLength());
					captureResult.setReturnPath(currentCaptureResult.getReturnPath());
				}

				break;
			case longest:
				if (trace) logger.trace("evaluate all then, return the longest if all are successful");
				if (currentCaptureResult.getPathLength() >= captureResult.getPathLength()) {
					captureResult.setPathLength(currentCaptureResult.getPathLength());
					captureResult.setReturnPath(currentCaptureResult.getReturnPath());
				}

				break;
			case last:
				if (trace) logger.trace("evaluate all then, return the last if all are successful");
				captureResult = currentCaptureResult;

				break;
			case first:
				if (trace) logger.trace("evaluate all then, return the first if all are successful");
				captureResult = (firstEntry == true) ? currentCaptureResult : captureResult;

				break;
			case none:
				if (trace) logger.trace("return original path");
				// the capture result object is already set to the initial element to avoid confusion
				// TODO maybe add some kind of validation here ?
				break;
			default:
				logger.error("Unsupported Junction Capture: " + capture);
				throw new MpcEvaluatorRuntimeException("Unsupported Junction Capture: " + capture);
		}

		return captureResult;
	}

	protected MpcJunctionCaptureResultImpl getNewCaptureResult() {
		return new MpcJunctionCaptureResultImpl();
	}

}
