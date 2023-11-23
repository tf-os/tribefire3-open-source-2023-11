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

import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.PersistentEntityReference;

/**
 * @author peter.gazdik
 */
public interface PersistenceReferenceResolver {

	/**
	 * Returns a coding map from {@link PersistentEntityReference} to the corresponding entity. Not that returned
	 * {@link PersistentEntityReference}s, i.e. the keys in the returned map, don't have to be the same instances as
	 * references given on input. The fact that the method returns a coding map ensures though that the lookup will work
	 * with the original references anyway.
	 */
	Map<PersistentEntityReference, GenericEntity> resolve(Iterable<PersistentEntityReference> references);

	/**
	 * Optimized version of {@link #resolve(Iterable)}, which already gets the references of one type only, thus it
	 * doesn't need to group the references.
	 * <p>
	 * However, for convenience it is possible to pass null as typeSignature, it which case this is equivalent to
	 * calling {@code resolve(references)}.
	 */
	Map<PersistentEntityReference, GenericEntity> resolve(String typeSignature, Iterable<PersistentEntityReference> references);

	static Map<String, List<PersistentEntityReference>> groupReferences(Iterable<PersistentEntityReference> references) {
		Map<String, List<PersistentEntityReference>> result = newMap();

		for (PersistentEntityReference ref : references)
			acquireList(result, ref.getTypeSignature()).add(ref);

		return result;
	}

}
