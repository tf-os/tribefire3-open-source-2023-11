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
package com.braintribe.model.processing.mpc.evaluator.api.builder;

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.api.MpcRegistry;

/**
 * The builder of the MPC context
 * 
 */
public interface MpcContextBuilder {

	/**
	 * Adds a {@link MpcRegistry} to be used for evaluation
	 * 
	 * @return A context builder with a registry
	 */
	MpcContextBuilder withRegistry(MpcRegistry registry);

	/**
	 * Matches a model path condition with a modelpathElement by using an
	 * MpcEvaluatorContext which delegates to it's matches method using the
	 * 
	 * experts of the registry for evaluation.
	 * If there is no match a null is returned, otherwise an instance of
	 * MpcMatch.
	 * 
	 * @param condition
	 *            ModelPathCondition that requires matching
	 * @param element
	 *            Path that will be used to evaluate the condition
	 * @return MpcMatch instance of the result or null
	 * @throws MpcEvaluatorRuntimeException
	 */
	MpcMatch mpcMatches(Object condition, IModelPathElement element) throws MpcEvaluatorRuntimeException;

}
