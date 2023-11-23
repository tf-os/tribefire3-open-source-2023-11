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

/**
 * Scope of nested condition expert, it is used to contain all evaluation states within a nested conidtion expert
 * 
 */
public interface MpcNestedConditionExpertScope {
	
	/**
	 * Resets the tracking cursor of the multi evaluation experts within the scope of this instance
	 */
	void resetIteration();

	/**
	 * Increments the index for multi evaluation experts within the scope
	 */
	void incrementMultiEvaluationExpertIndex();

	/**
	 * Checks if the current expert is allowed to resume operation or not
	 * 
	 * @return true if the expert is allows to resume operations
	 */
	boolean isCurrentMultiEvaluationExpertActive();

	/**
	 * Checks if the current expert is new or there is an existing state
	 * 
	 * @return true if the expert has no preserved states with respect to the scope ( a previous state might have been
	 *         internally nullified to allow for all path exploration)
	 */
	boolean isCurrentMultiEvaluationExpertNew();

	/**
	 * Retrieves the state of the current multiple evaluation expert
	 * 
	 * @return {@link MpcPotentialMatch} representing the state of the expert
	 */
	MpcPotentialMatch getMultiEvaluationExpertState();

	/**
	 * Sets the state for the current multiple evaluation expert.
	 * 
	 * @param potentialMatch
	 *            The value of the state that should be preserved
	 */
	void setMultiEvaluationExpertState(MpcPotentialMatch potentialMatch);

	/**
	 * boolean indicating if there are unexplored paths from the perspective of the scope as a whole
	 * 
	 * @return true, if at least one preserved state allows has the potential to provide more matches
	 */
	boolean isUnexploredPathAvailable();
}
