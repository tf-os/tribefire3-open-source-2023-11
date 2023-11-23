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
package com.braintribe.model.access;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.orTc;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.model.accessapi.ReferencesCandidate;
import com.braintribe.model.accessapi.ReferencesCandidates;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.record.ListRecord;
import com.braintribe.model.security.acl.AclTcs;

/**
 * @author peter.gazdik
 */
public class IncrementalAccesses {

	public static List<Object> cloneSelectQueryResults(List<?> results, StandardCloningContext cloningContext,
			StrategyOnCriterionMatch strategyOnCriterionMatch, Function<EntityType<?>, TraversingCriterion> tcFunction) {

		List<Object> clonedResults = new ArrayList<>(results.size());

		// clone and thereby cut results according to traversing criteria
		for (Object row : results) {
			if (isContainer(row)) {
				Object clonedRow;

				if (row instanceof ListRecord)
					clonedRow = cloneListRecord((ListRecord) row, cloningContext, strategyOnCriterionMatch, tcFunction);
				else
					throw new GenericModelException(
							"Unsupported query result container of type: " + ((GenericEntity) row).entityType().getTypeSignature());

				clonedResults.add(clonedRow);

			} else {
				Object clonedValue = cloneScalar(row, cloningContext, strategyOnCriterionMatch, tcFunction);
				clonedResults.add(clonedValue);
			}
		}

		return clonedResults;
	}

	private static ListRecord cloneListRecord(ListRecord row, StandardCloningContext cloningContext, StrategyOnCriterionMatch strategy,
			Function<EntityType<?>, TraversingCriterion> tcFunction) {

		ListRecord resultRecord = ListRecord.T.create();
		resultRecord.setValues(cloneList(row.getValues(), cloningContext, strategy, tcFunction));
		return resultRecord;
	}

	private static boolean isContainer(Object o) {
		return o instanceof GenericEntity && ((GenericEntity) o).getId() == null;
	}

	public static Object cloneObject(Object value, StandardCloningContext cloningContext, StrategyOnCriterionMatch strategy,
			Function<EntityType<?>, TraversingCriterion> tcFunction) {

		if (value instanceof List)
			return cloneList((List<Object>) value, cloningContext, strategy, tcFunction);

		if (value instanceof Set)
			return cloneSet((Set<Object>) value, cloningContext, strategy, tcFunction);

		if (value instanceof Map)
			return cloneMap((Map<Object, Object>) value, cloningContext, strategy, tcFunction);

		return cloneScalar(value, cloningContext, strategy, tcFunction);
	}

	public static <T> List<T> cloneList(List<T> list, StandardCloningContext cloningContext, StrategyOnCriterionMatch strategy,
			Function<EntityType<?>, TraversingCriterion> tcFunction) {

		List<T> result = new ArrayList<>(list.size());
		for (T value : list) {
			T clonedValue = cloneScalar(value, cloningContext, strategy, tcFunction);
			result.add(clonedValue);
		}

		return result;
	}

	private static <T> Set<T> cloneSet(Set<T> set, StandardCloningContext cloningContext, StrategyOnCriterionMatch strategy,
			Function<EntityType<?>, TraversingCriterion> tcFunction) {

		Set<T> result = newSet();
		for (T value : set) {
			T clonedValue = cloneScalar(value, cloningContext, strategy, tcFunction);
			result.add(clonedValue);
		}

		return result;
	}

	private static <K, V> Map<K, V> cloneMap(Map<K, V> map, StandardCloningContext cloningContext, StrategyOnCriterionMatch strategy,
			Function<EntityType<?>, TraversingCriterion> tcFunction) {

		Map<K, V> result = newMap();
		for (Entry<K, V> entry : map.entrySet()) {
			K key = entry.getKey();
			V value = entry.getValue();

			K clonedKey = cloneScalar(key, cloningContext, strategy, tcFunction);
			V clonedValue = cloneScalar(value, cloningContext, strategy, tcFunction);
			result.put(clonedKey, clonedValue);
		}

		return result;
	}

	private static <T> T cloneScalar(Object value, StandardCloningContext cloningContext, StrategyOnCriterionMatch strategy,
			Function<EntityType<?>, TraversingCriterion> tcFunction) {

		if (value == null)
			return null;

		if (value instanceof GenericEntity)
			return cloneEntity((GenericEntity) value, cloningContext, strategy, tcFunction);

		// simple + enum; we clone it just in case there is some trickery in the CC which replaces these
		return BaseType.INSTANCE.clone(cloningContext, value, strategy);
	}

	private static <T extends GenericEntity> T cloneEntity(GenericEntity entity, StandardCloningContext cloningContext,
			StrategyOnCriterionMatch strategy, Function<EntityType<?>, TraversingCriterion> tcFunction) {

		TraversingCriterion tc = getTc(entity, tcFunction);
		StandardMatcher matcher = createMatcher(tc);
		cloningContext.setMatcher(matcher);

		return entity.entityType().clone(cloningContext, entity, strategy);
	}

	private static TraversingCriterion getTc(GenericEntity entity, Function<EntityType<?>, TraversingCriterion> tcFunction) {
		return tcFunction.apply(entity.entityType());
	}

	private static StandardMatcher createMatcher(TraversingCriterion tc) {
		if (tc == null)
			return null;

		StandardMatcher matcher = new StandardMatcher();
		matcher.setCriterion(tc);
		return matcher;
	}

	public static TraversingCriterion createDefaultTraversionCriterion() {
		// @formatter:off
		return TC.create()
				.conjunction()
					.property()
					.typeCondition(orTc(
							isKind(TypeKind.collectionType),
							isKind(TypeKind.entityType)
					))
					.negation()
						.disjunction()
							.propertyType(LocalizedString.T)
							.pattern()
								.entity(LocalizedString.T)
								.property("localizedValues")
							.close()
							.criterion(AclTcs.TC_MATCHING_ACL_PROPS)
						.close()
				.close()
			.done();
		// @formatter:on
	}

	public static ReferencesResponse referenceCandidates(Set<ReferencesCandidate> candidates) {
		ReferencesCandidates response = ReferencesCandidates.T.create();
		response.setCandiates(candidates);
		return response;
	}

}
