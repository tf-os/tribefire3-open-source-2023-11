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

import java.util.Map;

import com.braintribe.model.mpc.ModelPathCondition;

/**
 * This is a registry that allows the evaluation of ModelPathConditions.
 * It is comprised of a maps of Experts, where each expert is a MPC expert that
 * matches the provided ModelPathCondition directly.
 * @see ModelPathCondition
 */
public interface MpcRegistry {

	/**
	 * @return A map of all experts, where the key is a ModelPathCondition
	 *         class and the value is the MPC expert
	 */
	Map<Class<? extends ModelPathCondition>, MpcEvaluator<?>> getExperts();

	/**
	 * Sets the expert maps in the registry
	 * @param experts
	 *            A map where key is a ModelPathCondition class and the value is
	 *            the MPC expert
	 */
	void setExperts(Map<Class<? extends ModelPathCondition>, MpcEvaluator<?>> experts);

	/**
	 * Augment the registry with an expert
	 * 
	 * @param mpcType
	 *            type of ModelPathCondition
	 * @param mpcEvaluator
	 *            MPC Expert for ModelPathCondition
	 */
	<D extends ModelPathCondition> void putExpert(Class<D> mpcType, MpcEvaluator<? super D> mpcEvaluator);

	/**
	 * Remove an expert from the registry if it exists
	 * 
	 * @param mpcType
	 *            The type of ModelPathCondition
	 */
	void removeExpert(Class<? extends ModelPathCondition> mpcType);

	/**
	 * Reset all the experts and cache
	 */
	void resetRegistry();

	/**
	 * Merge the content of another registry into the existing content of this
	 * registry
	 * 
	 * @param otherRegistry
	 *            An external registry that will be merged with the current
	 *            content
	 */
	void loadOtherRegistry(MpcRegistry otherRegistry);

}
