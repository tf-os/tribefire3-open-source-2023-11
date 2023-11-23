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
package com.braintribe.model.processing.manipulation.basic.oracle;

import static com.braintribe.model.processing.manipulation.api.PersistenceReferenceResolver.groupReferences;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.manipulation.api.PersistenceReferenceResolver;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * @author peter.gazdik
 */
public abstract class AbstractReferenceResolver implements PersistenceReferenceResolver {

	protected final int bulkSize;

	public AbstractReferenceResolver(int bulkSize) {
		this.bulkSize = bulkSize;
	}

	@Override
	public Map<PersistentEntityReference, GenericEntity> resolve(Iterable<PersistentEntityReference> references) {
		Map<PersistentEntityReference, GenericEntity> result = CodingMap.create(EntRefHashingComparator.INSTANCE);

		Map<String, List<PersistentEntityReference>> groupedRefs = groupReferences(references);

		for (Entry<String, List<PersistentEntityReference>> e : groupedRefs.entrySet())
			result.putAll(doResolve(e.getKey(), e.getValue()));

		return result;
	}

	@Override
	public Map<PersistentEntityReference, GenericEntity> resolve(String typeSignature, Iterable<PersistentEntityReference> references) {
		return typeSignature == null ? resolve(references) : doResolve(typeSignature, references);
	}

	private Map<PersistentEntityReference, GenericEntity> doResolve(String typeSignature, Iterable<PersistentEntityReference> references) {

		Map<PersistentEntityReference, GenericEntity> result = CodingMap.create(EntRefHashingComparator.INSTANCE);

		List<Set<PersistentEntityReference>> bulks = CollectionTools2.splitToSets(references, bulkSize);

		for (Set<PersistentEntityReference> bulk : bulks)
			result.putAll(resolveBulk(typeSignature, bulk));

		return result;
	}

	protected abstract Map<PersistentEntityReference, GenericEntity> resolveBulk(String typeSignature, Set<PersistentEntityReference> references);

}
