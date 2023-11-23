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
package com.braintribe.model.processing.mpc;

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.processing.mp.builder.api.MpBuilder;
import com.braintribe.model.processing.mp.builder.impl.MpBuilderImpl;
import com.braintribe.model.processing.mpc.builder.api.MpcBuilder;
import com.braintribe.model.processing.mpc.builder.impl.MpcBuilderImpl;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorContext;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.api.builder.MpcContextBuilder;
import com.braintribe.model.processing.mpc.evaluator.api.builder.MpcRegistryBuilder;
import com.braintribe.model.processing.mpc.evaluator.impl.MpcEvaluatorContextImpl;
import com.braintribe.model.processing.mpc.evaluator.impl.builder.MpcRegistryBuilderImpl;

/**
 * Main Access point to all Model Path condition actions.
 * 
 * All methods are declared as static.
 * 
 */
public class MPC {

	/**
	 * Matches a model path condition with a modelpathElement, with the respect
	 * to a Context (In theory, the context will include all the preset values
	 * in a default context, and optionally more), invokes
	 * {@link #mpcMatches(Object, IModelPathElement)} Return true
	 * iff the result of the matching is not null
	 * 
	 * @param condition
	 *            ModelPathCondition that requires matching
	 * @param element
	 *            Path that will be used to evaluate the condition
	 * @return boolean indicating if there was a match
	 */
	public static Object evaluate(Object condition, IModelPathElement element, MpcContextBuilder contextBuilder) throws MpcEvaluatorRuntimeException {
		return contextBuilder.mpcMatches(condition, element);
	}

	/**
	 * Matches a model path condition with a modelpathElement, invokes
	 * {@link #mpcMatches(Object, IModelPathElement)} Return true
	 * iff the result of the matching is not null
	 * 
	 * @param condition
	 *            ModelPathCondition that requires matching
	 * @param element
	 *            Path that will be used to evaluate the condition
	 * @return boolean indicating if there was a match
	 */
	public static boolean matches(Object condition, IModelPathElement element) throws MpcEvaluatorRuntimeException {

		MpcMatch result = mpcMatches(condition, element);

		if (result == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Matches a model path condition with a modelpathElement by using an
	 * MpcEvaluatorContext which delegates to it's matches method.
	 * 
	 * If there is no match a null is returned, otherwise an instance of
	 * MpcMatch.
	 * 
	 * @param condition
	 *            ModelPathCondition that requires matching
	 * @param element
	 *            Path that will be used to evaluate the condition
	 * @return MpcMatch instance of the result or null
	 */
	public static MpcMatch mpcMatches(Object condition, IModelPathElement element) throws MpcEvaluatorRuntimeException {

		if (condition == null) {
			throw new MpcEvaluatorRuntimeException("MPC condition can not be null");
		}
		MpcEvaluatorContext context = new MpcEvaluatorContextImpl();
		return context.matches(condition, element);
	}

	/**
	 * Instantiates an {@link MpcBuilder}
	 */
	public static MpcBuilder builder() {
		return new MpcBuilderImpl();
	}
	
	/**
	 * Instantiates an {@link MpBuilder}
	 */
	public static MpBuilder mpBuilder() {
		return new MpBuilderImpl();
	}

	/**
	 * @return A new instance of {@link MpcRegistryBuilder}
	 */
	public static MpcRegistryBuilder registryBuilder() {
		return new MpcRegistryBuilderImpl();
	}
}
