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
import com.braintribe.model.processing.mpc.evaluator.api.multievaluation.MpcNestedConditionExpertScope;
import com.braintribe.model.processing.mpc.evaluator.api.multievaluation.MpcPotentialMatch;

/**
 * The context that is used by all {@link MpcEvaluator}
 */
public interface MpcEvaluatorContext {

	/**
	 * Setting the {@link MpcRegistry} that is used by the context
	 */
	void setMpcRegistry(MpcRegistry registry);

	/**
	 * Retrieves the registry
	 * 
	 * @return {@link MpcRegistry} used by context
	 */
	MpcRegistry getMpcRegistry();

	/**
	 * Evaluates given conditions against given {@link IModelPathElement}.
	 * Basically this finds the expert corresponding to the type of given
	 * {@link ModelPathCondition} and uses it's <tt>matches</tt> method.
	 * 
	 * @return the result of the
	 *         {@link MpcEvaluator#matches(MpcEvaluatorContext, ModelPathCondition, IModelPathElement)}
	 *         method of the corresponding {@link MpcEvaluator}.
	 */
	MpcMatch matches(Object condition, IModelPathElement element) throws MpcEvaluatorRuntimeException;

	/**
	 * Retrieves the preserved state for {@link MpcEvaluator} within the current
	 * {@link MpcNestedConditionExpertScope} scope.
	 * 
	 * @param expert
	 *            {@link MpcEvaluator} that requests the state
	 * @param condition
	 *            {@link ModelPathCondition} that is associated with the
	 *            {@link MpcEvaluator} expert
	 * @return {@link MpcPotentialMatch} that represents the state of the
	 *         {@link MpcEvaluator}, null if no valid preserved state exists
	 */
	<M extends MpcPotentialMatch, C extends ModelPathCondition> M getPreservedState(MpcEvaluator<C> expert, C condition);

}
