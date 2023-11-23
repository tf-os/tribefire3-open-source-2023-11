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
package com.braintribe.model.processing.vde.evaluator.api;

import java.util.List;
import java.util.Map;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.value.ValueDescriptor;

/**
 * This is a registry that allows the evaluation of {@see ValueDescriptor}s. It
 * is comprised of the following it:
 * <ul>
 * 
 * <li>Concrete Experts: A concrete expert is a VDE expert that matches the
 * provided valueDescriptor directly.</li>
 * 
 * <li>Abstract Experts: An abstract expert is a VDE expert that does NOT match
 * the provided valueDescriptor directly, but rather through assignability</li>
 * 
 * 
 * </ul>
 */
public interface VdeRegistry {

	/**
	 * @return A map of all concrete experts, where the key is a valueDescriptor
	 *         class and the value is the VDE expert
	 */
	Map<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>> getConcreteExperts();

	/**
	 * Sets the concrete experts maps in the registry
	 * 
	 * @param concreteExperts
	 *            A map where key is a valueDescriptor class and the value is
	 *            the VDE expert
	 */
	void setConcreteExperts(Map<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>> concreteExperts);

	// TODO check with Dirk if abstract experts should be set instead of list
	/**
	 * @return A list of all the abstract experts, where the contents of the
	 *         list are Pairs of ValueDescriptor class and the VDE expert
	 */
	List<Pair<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>>> getAbstractExperts();

	/**
	 * Sets the abstract experts in the registry
	 * 
	 * @param abstractExperts
	 *            A list where the elemets are pairs of ValueDescriptor class
	 *            and the VDE expert
	 */
	void setAbstractExperts(List<Pair<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>>> abstractExperts);

	/**
	 * Augment the registry with a concrete expert
	 * 
	 * @param vdType
	 *            type of ValueDescriptor
	 * @param vdEvaluator
	 *            VDE Expert for ValueDescriptor
	 */
	<D extends ValueDescriptor> void putConcreteExpert(Class<D> vdType, ValueDescriptorEvaluator<? super D> vdEvaluator);

	/**
	 * Remove a concrete expert from the registry if it exists
	 * 
	 * @param valueDescriptorClass
	 *            The type of ValueDescriptor
	 */
	void removeConcreteExpert(Class<? extends ValueDescriptor> valueDescriptorClass);

	/**
	 * Augment the registry with an abstract expert
	 * 
	 * @param vdType
	 *            type of ValueDescriptor
	 * @param vdEvaluator
	 *            VDE expert for ValueDescriptor
	 */
	<D extends ValueDescriptor> void putAbstractExpert(Class<D> vdType, ValueDescriptorEvaluator<? super D> vdEvaluator);

	/**
	 * Remove an abstract expert from the registry if it exists
	 * 
	 * @param valueDescriptorClass
	 *            The type of ValueDescriptor
	 */
	void removeAbstractExpert(Class<? extends ValueDescriptor> valueDescriptorClass);

	/**
	 * Reset all the experts 
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
	void loadOtherRegistry(VdeRegistry otherRegistry);

}
