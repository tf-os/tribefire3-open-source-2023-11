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

import com.braintribe.logging.Logger;

/**
 * This is the state of an expert that can conduct multiple evaluations (e.g. MpcQuantiferEvaluator). This sate is
 * preserved within the scope of a nested condition expert.
 * 
 */
public class MpcMultiEvaluationState {

	private static Logger logger = Logger.getLogger(MpcMultiEvaluationState.class);
	private static boolean trace = logger.isTraceEnabled();
	
	/**
	 * Each state has a linked list structure to connect it with other evaluation states
	 */
	public MpcMultiEvaluationState next;
	public MpcMultiEvaluationState previous;

	/**
	 * States are defined by their potential match, from the previous iteration. If it is set to null, this indicates a
	 * fresh iteration will take place
	 */
	public MpcPotentialMatch potentialMatch;
	/**
	 * A flag to indicate if the evaluator related to this state will be invoked to resume evaluation (e.g.
	 * backtracking) or if it will maintain its previously computed state
	 */
	public boolean isActive;

	/**
	 * @return true iff there is a non-null potential match object that has the possibility to produce more results.
	 *         Otherwise, returns false
	 */
	public boolean hasAnotherProcessingAttempt() {
		if (trace) logger.trace("Check for possible evaluation attempts, where current potentialMatch:" + potentialMatch);
		if (potentialMatch != null) {
			return potentialMatch.hasAnotherAttempt();
		}
		return false;
	}

}
