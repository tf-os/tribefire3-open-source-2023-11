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
package com.braintribe.model.processing.mpc.evaluator.api;

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.mpc.logic.MpcJunction;
import com.braintribe.model.mpc.logic.MpcJunctionCapture;
import com.braintribe.model.processing.mpc.evaluator.api.logic.MpcJunctionCaptureResult;

/**
 * This is used to encapsulate several methods pertaining to the evaluation the result of an MPC match according to
 * different {@link MpcJunctionCapture}.
 * 
 * Used by {@link MpcConjunctionEvaluator} and by {@link MpcDisjunctionEvaluator}
 * 
 * @param <C>
 *            Type of the ModelPathCondition, which should extend {@link MpcJunction}
 */
public interface MpcJunctionEvaluator<C extends MpcJunction> extends MpcEvaluator<C> {

	/**
	 * Computes the correct {@link MpcJunctionCaptureResult} based on the provided {@link MpcJunctionCapture}. It takes
	 * into consideration the current computed result and the result so far, where both are of type
	 * {@link MpcJunctionCaptureResult}. It also factors in if this is the first attempt for evaluation of the {@link MpcJunction}
	 * 
	 * @param capture
	 * 			{@link MpcJunctionCapture} that is provided by {@link MpcJunction}
	 * @param captureResult
	 * 			{@link MpcJunctionCapture} the result so far
	 * @param currentCaptureResult
	 * 			{@link MpcJunctionCapture} the result of the evaluation of the current evaluation
	 * @param firstEntry
	 * 			boolean that indicates if this is the first attempt to evaluate a result or not
	 * @return
	 * @throws MpcEvaluatorRuntimeException
	 */
	MpcJunctionCaptureResult evaluateMatchResult(MpcJunctionCapture capture, MpcJunctionCaptureResult captureResult,
			MpcJunctionCaptureResult currentCaptureResult, boolean firstEntry) throws MpcEvaluatorRuntimeException;

	/**
	 * Initialises a {@link MpcJunctionCaptureResult} based on the provided {@link MpcJunctionCapture}
	 * 
	 * @param capture
	 *            {@link MpcJunctionCapture} as provided in {@link MpcJunction}
	 * @param element
	 *            {@link IModelPathElement} as provided in {@link MpcJunction}
	 * @return
	 */
	MpcJunctionCaptureResult initResult(MpcJunctionCapture capture, IModelPathElement element);

}
