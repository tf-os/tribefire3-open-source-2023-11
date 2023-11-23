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
package com.braintribe.model.processing.smart.query.planner.structure;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;

/**
 * @see #resolveRelevantQueryableTypesFor(GmEntityType)
 * 
 * @author peter.gazdik
 */
public class QueryableHierarchyExpert {

	private final ModelExpert modelExpert;

	// TODO RENAME
	private final Map<GmEntityType, List<EntityMapping>> typeToQueryableSubTypes = newMap();

	public QueryableHierarchyExpert(ModelExpert modelExpert) {
		this.modelExpert = modelExpert;
	}

	/**
	 * Not every smart entity type is queryable directly. Either it is not mapped to anything at all (very common), or
	 * at least might have some sub-type which is mapped to different hierarchy (I would say this is possible just
	 * theoretically). In those cases, we have to find all the sub-types which actually can be used as sources which are
	 * queryable.
	 * 
	 * Simple example for the first case: <tt>select ge from GenericEntity ge</tt>
	 * 
	 * Assuming the {@link GenericEntity} itself is not mapped, we simply find all the entities which are mapped, for
	 * which none of the super-types is mapped, and those are our queryable sub-types.
	 */
	// TODO rename, originally this was returning types, but now we can have various mappings for single type
	public List<EntityMapping> resolveRelevantQueryableTypesFor(GmEntityType smartType) {
		return typeToQueryableSubTypes.computeIfAbsent(smartType, this::resolveQueryableTypeMappingsHelper);
	}

	private List<EntityMapping> resolveQueryableTypeMappingsHelper(GmEntityType smartType) {
		List<EntityMapping> result = newList();

		Collection<IncrementalAccess> accesses = modelExpert.getSortedAccesses();
		
		for (IncrementalAccess access: accesses) {
			List<EntityMapping> queryableTypesForAccess = resolveQueryableTypesHelper(smartType, access);
			if (!queryableTypesForAccess.isEmpty())
				result.addAll(queryableTypesForAccess);
		}
		
		return result;
	}

	private List<EntityMapping> resolveQueryableTypesHelper(GmEntityType smartType, IncrementalAccess access) {
		Set<EntityMapping> set = newSet();

		EntityMapping em = modelExpert.resolveEntityMappingIfPossible(smartType, access, null);

		if (em != null)
			set.add(em);

		checkForSubTypes(smartType, access, em, set);

		List<EntityMapping> result = newList(set);
		Collections.sort(result, EntityMappingComparator.INSTANCE);

		return result;
	}
	private void checkForSubTypes(GmEntityType rootType, IncrementalAccess access, EntityMapping rootEm, Set<EntityMapping> set) {
		for (GmEntityType subType: nullSafe(modelExpert.getDirectSmartSubTypes(rootType))) {
			// TODO is this the right use-case
			EntityMapping subEm = modelExpert.resolveEntityMappingIfPossible(subType, access, null);

			if (mapsToNewDelegateHierarchy(rootEm, subEm))
				set.add(subEm);

			checkForSubTypes(subType, access, subEm, set);
		}
	}

	private boolean mapsToNewDelegateHierarchy(EntityMapping superEm, EntityMapping subEm) {
		if (superEm == null)
			return subEm != null;

		if (subEm == null)
			return false;

		return !modelExpert.isFirstAssignableFromSecond(superEm.getDelegateEntityType(), subEm.getDelegateEntityType());
	}

	// ####################################
	// ## . . . . . Comparator . . . . . ##
	// ####################################

	static class GmEntityTypeComparator implements Comparator<GmEntityType> {
		public static final GmEntityTypeComparator INSTANCE = new GmEntityTypeComparator();

		private GmEntityTypeComparator() {
		}

		@Override
		public int compare(GmEntityType type1, GmEntityType type2) {
			return type1.getTypeSignature().compareTo(type2.getTypeSignature());
		}

	}

	static class EntityMappingComparator implements Comparator<EntityMapping> {
		public static final EntityMappingComparator INSTANCE = new EntityMappingComparator();

		private EntityMappingComparator() {
		}

		@Override
		public int compare(EntityMapping em1, EntityMapping em2) {
			int typeCmp = GmEntityTypeComparator.INSTANCE.compare(em1.getSmartEntityType(), em2.getSmartEntityType());

			return typeCmp != 0 ? typeCmp : em1.getAccess().getExternalId().compareTo(em2.getAccess().getExternalId());
		}

	}
}
