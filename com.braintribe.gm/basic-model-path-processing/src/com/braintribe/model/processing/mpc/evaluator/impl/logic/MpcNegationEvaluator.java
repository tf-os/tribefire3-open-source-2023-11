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
import com.braintribe.model.mpc.logic.MpcNegation;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluator;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorContext;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.MpcMatchImpl;

/**
 * {@link MpcEvaluator} for {@link MpcNegation}
 * 
 */
public class MpcNegationEvaluator implements MpcEvaluator<MpcNegation> {

	private static Logger logger = Logger.getLogger(MpcNegationEvaluator.class);
	private static boolean debug = logger.isDebugEnabled();
	
	@Override
	public MpcMatch matches(MpcEvaluatorContext context, MpcNegation condition, IModelPathElement element) throws MpcEvaluatorRuntimeException {

		if(debug) logger.debug("Evaluate operand" );
		MpcMatch evaluationResult = context.matches(condition.getOperand(), element);

		if(debug) logger.debug("just return the negation of the result, original result:" + evaluationResult );
		if (evaluationResult == null) {

			MpcMatchImpl result = new MpcMatchImpl(element);
			return result;
		} else {

			return null;
		}

	}

	@Override
	public boolean allowsPotentialMatches(MpcNegation condition) {
		return false;
	}

	@Override
	public boolean hasNestedConditions(MpcNegation condition) {
		return true;
	}

}
