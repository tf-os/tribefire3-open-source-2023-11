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
package com.braintribe.model.processing.manipulation.api;

import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;

/**
 * An interface to be used when processing {@link Manipulation}s. This provides a convenient way to access all the
 * information contained in a given {@link AtomicManipulation}.
 * 
 * The main idea behind this interface is that there exists a standard implementation of it (see the standard
 * implementation for this artifact - BasicModelProcessing), so other potential manipulation processors may take
 * advantage of it.
 */
public interface ManipulationExpositionContext {

	<T extends AtomicManipulation> T getCurrentManipulation();

	ManipulationType getCurrentManipulationType();

	/**
	 * Returns a {@link Property} instance corresponding to given {@link PropertyManipulation} or <tt>null</tt>
	 * otherwise.
	 */
	Property getTargetProperty();

	/**
	 * Returns a property name corresponding to given {@link PropertyManipulation} or <tt>null</tt> otherwise.
	 */
	String getTargetPropertyName();

	/**
	 * Returns the entity this manipulation is being executed on. In case of {@link PropertyManipulation} it is the
	 * owner of the property, in case of{@link InstantiationManipulation}/{@link DeleteManipulation} it is the entity
	 * itself.
	 */
	EntityReference getTargetReference();

	/**
	 * This method returns a valid {@link EntityReference} on given entity, but since multiple manipulations may use
	 * different instances of {@linkplain EntityReference} to access the same entity, this method is meant to always
	 * return the exact same instance (this makes more sense if you take into account that we can set various
	 * manipulations for one instance of manipulation context - and this method returns the same result if we change the
	 * manipulation to other one with equivalent entity reference).
	 */
	EntityReference getNormalizedTargetReference();

	/**
	 * Similar to {@link #getNormalizedTargetReference()}, but does this with an arbitrary entityReference passed as a
	 * parameter.
	 */
	EntityReference getNormalizeReference(EntityReference entityReference);

	/**
	 * Returns the {@link EntityType} corresponding to the {@link #getTargetReference() target reference}.
	 */
	EntityType<?> getTargetEntityType();

	/**
	 * Returns the type signature corresponding to the {@link #getTargetReference() target reference}.
	 */
	String getTargetSignature();
}
