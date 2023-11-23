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
package com.braintribe.model.processing.mpc.evaluator.api.multievaluation;

import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;

/**
 * A sub class of {@link MpcMatch} where it represents a valid {@link MpcMatch} with the possibility of further
 * processing.
 * 
 */
public interface MpcPotentialMatch extends MpcMatch {

	/**
	 * Retrieves {@link MpcEvaluationResumptionStrategy} that is relative to this match
	 * @return applicable {@link MpcEvaluationResumptionStrategy}
	 */
	MpcEvaluationResumptionStrategy getResumptionStrategy();

	/**
	 * @param strategy
	 * 			{@link MpcEvaluationResumptionStrategy} that should be applied to this match
	 */
	void setResumptionStrategy(MpcEvaluationResumptionStrategy strategy);

	/**
	 * @return true iff this match has more possible solutions 
	 */
	boolean hasAnotherAttempt();

}
