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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.Optional;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;

/**
 * Experimental!!! DO NOT USE YET
 * 
 * @author peter.gazdik
 */
public interface ManipulationOracle {

	Manipulation getManipulation();

	Manipulation getInducedManipulation();
	
	/** Returns a {@link AtomicManipulationOracle} for this specific {@link AtomicManipulation}. */
	AtomicManipulationOracle forAtomic(AtomicManipulation manipulation);

	/**
	 * Resolves given entity reference with the underlying {@link PersistenceReferenceResolver} (typically a persistence
	 * session based). It throws an exception if the reference cannot be resolved. For resolution of multiple references
	 * use the more optimized {@link #resolveAll(Iterable)}.
	 * 
	 * <h3>Resolution rules</h3>
	 * <ul>
	 * <li>Resolution of a {@link PreliminaryEntityReference} is only possible if your oracle instance already contains
	 * a manipulation response, i.e. this is dependent on the context and it should always be documented properly what
	 * you can and cannot do with the oracle.</li>
	 * <li>Resolving the same entity reference multiple times does not cause multiple queries, as the implementation is
	 * expected to do an internal caching.</li>
	 * </ul>
	 */
	GenericEntity resolve(EntityReference reference);

	/**
	 * Optimized implementation for resolving multiple {@link EntityReference}s. Once the method has successfully
	 * finished, use {@link #getResolved(EntityReference)} or {@link #findResolved(EntityReference)} to get the actual
	 * entity for given {@link EntityReference}.
	 * <p>
	 * For more information on resolution see {@link #resolve(EntityReference)}.
	 */
	void resolveAll(Iterable<EntityReference> references);

	/**
	 * Even more optimized version of {@link #resolveAll(Iterable)}, which takes advantage of knowing all references are
	 * of the same type.
	 * <p>
	 * However, for convenience it is possible to pass null as typeSignature, it which case this is equivalent to
	 * calling {@code resolve(references)}.
	 */
	void resolveAll(String typeSignature, Iterable<EntityReference> references);

	/**
	 * Similar to {@link #findResolved(EntityReference)}, but throws an exception if no entity found.
	 * <p>
	 * This method shouldn't be called unless a we are sure the reference was resolved before, with either
	 * {@link #resolve(EntityReference)} or {@link #resolveAll(Iterable)}.
	 */
	GenericEntity getResolved(EntityReference reference);

	/** Returns an entity that was already resolved for this reference, or <tt>null</tt>, if not resolved yet. */
	Optional<GenericEntity> findResolved(EntityReference reference);

	// ##############################################
	// ## . . . . . . . . Defaults . . . . . . . . ##
	// ##############################################

	default <T extends GenericEntity> List<T> getAllResolved(Iterable<EntityReference> references) {
		List<T> result = newList();

		for (EntityReference ref : references)
			result.add((T) getResolved(ref));

		return result;
	}

}
