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

import com.braintribe.model.mpc.ModelPathCondition;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluator;
import com.braintribe.model.processing.mpc.evaluator.api.MpcRegistry;

/**
 * A builder for {@link MpcRegistry} that facilitates its construction and
 * manipulation. It also provides default registry setups.
 *
 */
public interface MpcRegistryBuilder {

	/**
	 * @return A registry with only all the default experts loaded.
	 */
	MpcRegistry defaultSetup();

	/**
	 * Includes the content of one {@link MpcRegistry} into another.
	 * 
	 * @param otherRegistry
	 *            The registry that will be merged
	 * @return An updated registry builder with the contents of otherRegistry
	 */
	MpcRegistryBuilder addRegistry(MpcRegistry otherRegistry);

	/**
	 * Adds the default setup to the existing registry.
	 * 
	 * @see MpcRegistryBuilder#defaultSetup()
	 * @return An updated registry builder with the default setup as part of its
	 *         structure
	 */
	MpcRegistryBuilder loadDefaultSetup();

	/**
	 * Adds a custom concrete expert to the context
	 * 
	 * A concrete expert is a VDE expert that matches the provided
	 * valueDescriptor directly.
	 * 
	 * @param mpcType
	 *            A class type that extends ValueDescriptor
	 * @param mpcEvaluator
	 *            A ValueDescriptorEvaluator for the provided class type
	 * @return A registry builder with the new expert added to its concrete
	 *         experts
	 */
	<D extends ModelPathCondition> MpcRegistryBuilder withExpert(Class<D> mpcType, MpcEvaluator<? super D> mpcEvaluator);


	/**
	 * Remove a concrete expert from the {@link MpcRegistry}
	 * 
	 * @param mpcType
	 *            The class that identifies the expert
	 * @return A registry builder with the concrete expert removed from its
	 *         context, if it existed
	 */
	MpcRegistryBuilder removeExpert(Class<? extends ModelPathCondition> mpcType);

	/**
	 * @return A {@link MpcRegistry} as the result of building process.
	 */
	MpcRegistry done();

}
