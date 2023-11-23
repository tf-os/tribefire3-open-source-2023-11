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

import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.acquireConcurrentMap;
import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.newConcurrentMap;
import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static com.braintribe.utils.lcd.CollectionTools2.removeFirst;
import static com.braintribe.utils.lcd.CollectionTools2.union;
import static java.util.Collections.emptySet;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;

/**
 * Determines whether a given smart type is mapped entirely as-is to the given delegate, thus making direct query
 * delegation possible.
 * 
 * @author peter.gazdik
 */
public class AsIsDelegationExpert {

	private final ModelExpert modelExpert;

	Map<IncrementalAccess, Map<String, Boolean>> isAsIsCache = newConcurrentMap();

	public AsIsDelegationExpert(ModelExpert modelExpert) {
		this.modelExpert = modelExpert;
	}

	public boolean isMappedAsIs(String smartSignature, IncrementalAccess access) {
		Map<String, Boolean> asIsCache = acquireConcurrentMap(isAsIsCache, access);

		if (!asIsCache.containsKey(smartSignature))
			runAsIsResolution(smartSignature, access, asIsCache);

		return asIsCache.get(smartSignature);
	}

	private void runAsIsResolution(String smartSignature, IncrementalAccess access, Map<String, Boolean> asIsCache) {
		EntityType<?> et = GMF.getTypeReflection().getEntityType(smartSignature);
		new AsIsResolution(access, asIsCache).run(et);
	}

	class AsIsResolution {

		private final IncrementalAccess access;
		private final Map<String, Boolean> asIsCache;

		private final Map<EntityType<?>, Set<EntityType<?>>> typeToDepender = newMap();
		private final Set<EntityType<?>> visitedTypes = newSet();
		private final Set<EntityType<?>> nonAsIsTypes = newSet();

		private final Set<EntityType<?>> typesToVisit = newSet();

		GmEntityType currentSmartGmType;

		public AsIsResolution(IncrementalAccess access, Map<String, Boolean> asIsCache) {
			this.access = access;
			this.asIsCache = asIsCache;
		}

		public void run(EntityType<?> et) {
			traverse(et);

			if (nonAsIsTypes.isEmpty())
				markAsIsTypes();
			else
				markNonAsIsTypes();

		}

		private void traverse(EntityType<?> et) {
			typesToVisit.add(et);

			while (!typesToVisit.isEmpty()) {
				EntityType<?> etToVisit = removeFirst(typesToVisit);
				visit(etToVisit);
			}
		}

		private void visit(EntityType<?> et) {
			if (!visitedTypes.add(et))
				return;

			currentSmartGmType = modelExpert.resolveSmartEntityType(et.getTypeSignature());

			if (!isCurrentTypeMappedAsIs()) {
				markNonAsIs(et);
				return;
			}

			for (Property property : et.getProperties()) {
				if (!isCurrentPropertyMappedAsIs(property)) {
					markNonAsIs(et);
					return;
				}

				processDeps(et, property);
			}
		}

		private boolean isCurrentTypeMappedAsIs() {
			EntityMapping em = modelExpert.resolveEntityMappingIfPossible(currentSmartGmType, access, null);

			return em != null && em.getSmartEntityType() == em.getDelegateEntityType();
		}

		private boolean isCurrentPropertyMappedAsIs(Property property) {
			EntityPropertyMapping epm = modelExpert.resolveEntityPropertyMappingIfPossible(currentSmartGmType, access, property.getName(), null);

			return epm != null && //
					epm.getDelegatePropertyName().equals(property.getName()) && //
					epm.getConversion() == null;
		}

		private void markNonAsIs(EntityType<?> et) {
			nonAsIsTypes.add(et);
		}

		/**
		 * Add information about type dependencies and enqueue non-visited types to be visited.
		 */
		private void processDeps(EntityType<?> et, Property property) {
			Set<EntityType<?>> dependedTypes = resolveRelatedEntityTypes(property.getType());

			for (EntityType<?> dependedType : dependedTypes) {
				acquireSet(typeToDepender, dependedType).add(et);

				if (!visitedTypes.contains(et))
					typesToVisit.add(et);
			}
		}

		private void markAsIsTypes() {
			for (EntityType<?> et : visitedTypes)
				asIsCache.put(et.getTypeSignature(), Boolean.TRUE);
		}

		private void markNonAsIsTypes() {
			Set<EntityType<?>> allNonAsIsTypes = newSet();
			while (!nonAsIsTypes.isEmpty()) {
				EntityType<?> nonAsIsType = removeFirst(nonAsIsTypes);
				allNonAsIsTypes.add(nonAsIsType);

				Set<EntityType<?>> nonAsIsDependers = typeToDepender.remove(nonAsIsType);
				nonAsIsTypes.addAll(nullSafe(nonAsIsDependers));
			}

			for (EntityType<?> et : allNonAsIsTypes)
				asIsCache.put(et.getTypeSignature(), Boolean.FALSE);
		}

	}

	private static Set<EntityType<?>> resolveRelatedEntityTypes(GenericModelType type) {
		switch (type.getTypeCode()) {
			case entityType:
				return asSet(type.cast());

			case listType:
			case setType:
				return resolveRelatedEntityTypes(((CollectionType) type).getCollectionElementType());

			case mapType: {
				MapType mapType = (MapType) type;
				Set<EntityType<?>> relatedKey = resolveRelatedEntityTypes(mapType.getKeyType());
				Set<EntityType<?>> relatedValue = resolveRelatedEntityTypes(mapType.getValueType());

				return union(relatedKey, relatedValue);
			}

			default:
				return emptySet();
		}
	}
}
