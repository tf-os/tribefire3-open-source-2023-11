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
import com.braintribe.model.mpc.ModelPathCondition;

/**
 * {@link ModelPathCondition} evaluation expert.
 */
public interface MpcEvaluator<C extends ModelPathCondition> {

	/**
	 * @return {@link IModelPathElement} rest of the path remaining after a match (match can consume 1 or more elements
	 *         from the path tail) or <tt>null</tt> in case the condition does not match. The result is wrapped in a
	 *         {@link MpcMatch}.
	 */
	MpcMatch matches(MpcEvaluatorContext context, C condition, IModelPathElement element)
			throws MpcEvaluatorRuntimeException;

	/**
	 * @param condition
	 *            {@link ModelPathCondition} that is applicable to the Evaluator
	 * @return true iff the {@link ModelPathCondition} properties allow the evaluator to perform multiple evaluations
	 */
	boolean allowsPotentialMatches(C condition);

	/**
	 * @param condition
	 *            {@link ModelPathCondition} that is applicable to the Evaluator
	 * @return true iff {@link ModelPathCondition} has more conditions as one or more of its properties and there is a
	 *         possibility that {@link MpcEvaluatorContext} will be invoked to evaluate them
	 */
	boolean hasNestedConditions(C condition);

}
