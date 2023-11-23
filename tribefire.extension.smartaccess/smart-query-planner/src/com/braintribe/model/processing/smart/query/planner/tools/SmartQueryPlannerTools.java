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
package com.braintribe.model.processing.smart.query.planner.tools;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.Operand;
import com.braintribe.model.queryplan.set.CombinedSet;
import com.braintribe.model.queryplan.set.DistinctSet;
import com.braintribe.model.queryplan.set.PaginatedSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.TupleSetType;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;

/**
 * 
 */
public class SmartQueryPlannerTools {

	public static <K, V> Map<K, V> newConcurrentMap() {
		return new ConcurrentHashMap<>();
	}

	public static <K1, K2, V> Map<K2, V> acquireConcurrentMap(Map<K1, Map<K2, V>> map, K1 key) {
		return map.computeIfAbsent(key, k -> newConcurrentMap());
	}

	public static <K, V> MultiMap<K, V> newMultiMap() {
		return new HashMultiMap<>();
	}

	public static <T> T firstNotInSecond(Iterable<? super T> it, Collection<?> collection) {
		for (Object t : it)
			if (!collection.contains(t))
				return (T) t;

		return null;
	}

	/**
	 * By only getting not-nulls I do not have to check if the map contains a given key.
	 */
	public static <K, V> Set<V> getAllNotNull(Map<K, V> map, Iterable<? extends K> keys) {
		Set<V> result = newSet();

		for (K key : keys) {
			V v = map.get(key);
			if (v != null)
				result.add(v);
		}

		return result;
	}

	public static boolean isScalarOrId(Property p) {
		return p.isIdentifying() || p.getType().isScalar();
	}

	public static boolean isScalarOrId(GmProperty p) {
		return p.getType().isGmScalar() || p.isId();
	}

	public static boolean isString(GmType type) {
		return type.typeKind() == GmTypeKind.STRING;
	}

	public static boolean isLinearCollectionInstance(Object o) {
		return o instanceof Set || o instanceof List;
	}

	public static Collection<Object> newCompatibleLinearCollection(Collection<?> c) {
		return c instanceof Set ? newSet() : newList();
	}

	public static GmEnumType enumTypeOrNull(GmType type) {
		return type.isGmEnum() ? (GmEnumType) type : null;
	}

	public static GmCollectionType collectionTypeOrNull(GmType type) {
		return type.isGmCollection() ? (GmCollectionType) type : null;
	}

	public static GmEntityType entityType(GmType type) {
		if (type.isGmEntity())
			return (GmEntityType) type;

		if (!type.isGmCollection())
			return null;

		return entityType(collectionElementType((GmCollectionType) type));
	}

	public static GmType collectionElementType(GmCollectionType type) {
		return type instanceof GmLinearCollectionType ? ((GmLinearCollectionType) type).getElementType() : ((GmMapType) type).getValueType();
	}

	public static GmType keyTypeIfMap(GmCollectionType type) {
		return type.typeKind() == GmTypeKind.MAP ? ((GmMapType) type).getKeyType() : null;
	}

	// TODO add caching
	public static GmProperty resolveProperty(GmEntityType entityType, String propertyName) {
		GmProperty property = findProperty(entityType, propertyName);
		if (property == null)
			throw new RuntimeException("Property not found:  " + entityType.getTypeSignature() + "#" + propertyName);

		return property;
	}

	private static GmProperty findProperty(GmEntityType entityType, String propertyName) {
		GmProperty result = findPropertyDirectly(entityType, propertyName);

		if (result != null)
			return result;

		for (GmEntityType superType : nullSafe(entityType.getSuperTypes())) {
			result = findProperty(superType, propertyName);
			if (result != null)
				return result;
		}

		return null;
	}

	private static GmProperty findPropertyDirectly(GmEntityType entityType, String propertyName) {
		for (GmProperty gmProperty : nullSafe(entityType.getProperties()))
			if (propertyName.equals(gmProperty.getName()))
				return gmProperty;

		return null;
	}

	// ################################################
	// ## . . . . . QueryPlanModel methods . . . . . ##
	// ################################################

	public static boolean isOperand(Object o, Predicate<Object> evalExcludedCheck) {
		return o instanceof Operand && !evalExcludedCheck.test(o);
	}

	public static boolean isPersistentReference(Object o, Predicate<Object> evalExcludedCheck) {
		return o instanceof PersistentEntityReference && !evalExcludedCheck.test(o);
	}

	public static <K extends Comparable<K>, V> List<Map.Entry<K, V>> sortEntries(Collection<Map.Entry<K, V>> collection) {
		List<Map.Entry<K, V>> result = newList(collection);
		Collections.sort(result, KeyBasedEntryComparator.<K, V> instance());

		return result;
	}

	public static int computeResultComponentCount(TupleSet topLevelTupleSet) {
		TupleSetType type = topLevelTupleSet.tupleSetType();

		switch (type) {
			case pagination:
				return computeResultComponentCount(((PaginatedSet) topLevelTupleSet).getOperand());

			case distinctSet:
				return computeResultComponentCount(((DistinctSet) topLevelTupleSet).getOperand());

			case projection:
			case aggregatingProjection:
				return ((Projection) topLevelTupleSet).getValues().size();

			case concatenation:
			case intersection:
			case union:
				return computeResultComponentCount(((CombinedSet) topLevelTupleSet).getFirstOperand());
			default:
				return 0;
		}
	}

	public static boolean isAssymetricJoinType(JoinType joinType) {
		return joinType != null && (joinType == JoinType.left || joinType == JoinType.right);
	}

	public static JoinType reverseJoinType(JoinType joinType) {
		if (joinType == null)
			return null;

		switch (joinType) {
			case left:
				return JoinType.right;
			case right:
				return JoinType.left;
			default:
				return joinType;
		}
	}

	public static boolean isInstantiable(GmEntityType smartType) {
		return !Boolean.TRUE.equals(smartType.getIsAbstract());
	}

	public static boolean hasSubType(EntitySourceNode node, SmartQueryPlannerContext context) {
		return !context.modelExpert().getDirectSmartSubTypes(node.getSmartGmType()).isEmpty();
	}
}
