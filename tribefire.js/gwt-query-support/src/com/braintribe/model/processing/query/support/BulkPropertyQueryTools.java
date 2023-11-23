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
package com.braintribe.model.processing.query.support;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newTreeMap;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedList;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedMap;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedSet;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.record.ListRecord;

/**
 * 
 */
public class BulkPropertyQueryTools {

	public static final TraversingCriterion scalarOnlyTc = TC.create().negation().typeCondition(isKind(TypeKind.scalarType)).done();

	private static final int ID_POSITION = 0;
	private static final int PARTITION_POSITION = 1;
	private static final int VALUE_POSITION = 2;
	private static final int KEY_POSITION = 3;

	public static SelectQuery buildQueryForProperty(Set<GenericEntity> ownersToLoad, Property property) {
		EntityType<?> ownerType = findOwnerType(ownersToLoad);

		// @formatter:off
		SelectQueryBuilder builder = new SelectQueryBuilder()
										.from(ownerType, "o")
											.join("o", property.getName(), "p", JoinType.inner)
										.select("o", GenericEntity.id)
										.select("o", GenericEntity.partition)
										.select("p")
										.where()
											.entity("o").inEntities(ownersToLoad)
										.tc(PreparedTcs.scalarOnlyTc);
		// @formatter:on

		TypeCode propertyTypeCode = property.getType().getTypeCode();
		if (propertyTypeCode == TypeCode.mapType)
			builder.select().mapKey("p");
		else if (propertyTypeCode == TypeCode.listType)
			builder.select().listIndex("p");

		return builder.done();
	}

	private static EntityType<?> findOwnerType(Set<GenericEntity> ownersToLoad) {
		EntityType<?> result = null;

		for (GenericEntity owner : ownersToLoad) {
			EntityType<GenericEntity> ownerType = owner.entityType();

			if (result == null || !result.isAssignableFrom(ownerType))
				result = ownerType;
		}

		return result;
	}

	/**
	 * Returns a map with the correct property value for each owner, based on the query result, EXCEPT for such owners,
	 * where the value would be null or an empty collection.
	 */
	public static Map<GenericEntity, Object> buildPropertyMap(Set<GenericEntity> ownersToLoad, Property property, SelectQueryResult sqResult) {
		List<ListRecord> records = (List<ListRecord>) (List<?>) sqResult.getResults();

		Map<String, Map<Object, GenericEntity>> ownerIndex = buildOwnerIndex(ownersToLoad);

		GenericModelType propertyType = property.getType();

		if (!propertyType.isCollection())
			return buildScalarPropertyMap(ownerIndex, records);
		else
			return buildCollectionPropertyMap(ownerIndex, records, (CollectionType) propertyType);
	}

	private static Map<String, Map<Object, GenericEntity>> buildOwnerIndex(Set<GenericEntity> ownersToLoad) {
		Map<String, Map<Object, GenericEntity>> ownerIndex = newMap();

		for (GenericEntity owner : ownersToLoad)
			ownerIndex.computeIfAbsent(owner.getPartition(), p -> newMap()).put(owner.getId(), owner);

		return ownerIndex;
	}

	private static Map<GenericEntity, Object> buildScalarPropertyMap(Map<String, Map<Object, GenericEntity>> ownerIndex, List<ListRecord> records) {
		Map<GenericEntity, Object> result = newMap();

		for (ListRecord listRecord : records) {
			List<Object> row = listRecord.getValues();

			Object partition = row.get(PARTITION_POSITION);
			Object id = row.get(ID_POSITION);
			Object value = row.get(VALUE_POSITION);

			GenericEntity owner = ownerIndex.get(partition).get(id);

			result.put(owner, value);
		}

		return result;
	}

	private static Map<GenericEntity, Object> buildCollectionPropertyMap(Map<String, Map<Object, GenericEntity>> ownerIndex, List<ListRecord> records,
			CollectionType propertyType) {

		Map<GenericEntity, List<ListRecord>> ownerToListRecords = groupRecordsByOwner(ownerIndex, records);

		Function<List<ListRecord>, Object> collectionValueConverter = getCollectionValueConverter(propertyType);

		return finishCollectionPropertyMap(ownerToListRecords, collectionValueConverter);
	}

	private static Map<GenericEntity, List<ListRecord>> groupRecordsByOwner(Map<String, Map<Object, GenericEntity>> ownerIndex,
			List<ListRecord> records) {

		Map<GenericEntity, List<ListRecord>> ownerToListRecords = newMap();

		for (ListRecord listRecord : records) {
			List<Object> row = listRecord.getValues();

			Object partition = row.get(PARTITION_POSITION);
			Object id = row.get(ID_POSITION);

			GenericEntity owner = ownerIndex.get(partition).get(id);

			ownerToListRecords.computeIfAbsent(owner, o -> newList()).add(listRecord);
		}

		return ownerToListRecords;
	}

	private static Function<List<ListRecord>, Object> getCollectionValueConverter(CollectionType collectionType) {
		switch (collectionType.getCollectionKind()) {
			case list:
				return records -> buildList(records, (ListType) collectionType);
			case set:
				return records -> buildSet(records, (SetType) collectionType);
			case map:
				return records -> buildMap(records, (MapType) collectionType);
			default:
				throw new RuntimeQueryEvaluationException("Unknown collection of kind: " + collectionType.getCollectionKind());
		}
	}

	private static Map<GenericEntity, Object> finishCollectionPropertyMap(Map<GenericEntity, List<ListRecord>> ownerToListRecords,
			Function<List<ListRecord>, Object> collectionValueConverter) {

		Map<GenericEntity, Object> result = newMap();

		for (Entry<GenericEntity, List<com.braintribe.model.record.ListRecord>> entry : ownerToListRecords.entrySet()) {
			GenericEntity owner = entry.getKey();
			List<ListRecord> ownerRecords = entry.getValue();

			Object collectionValue = collectionValueConverter.apply(ownerRecords);

			result.put(owner, collectionValue);
		}

		return result;
	}

	private static List<?> buildList(List<ListRecord> records, ListType listType) {
		Map<Integer, Object> listMap = newTreeMap();
		for (ListRecord record : records) {
			List<Object> rowValues = record.getValues();

			Integer key = (Integer) rowValues.get(KEY_POSITION);
			Object value = rowValues.get(VALUE_POSITION);

			listMap.put(key, value);
		}

		List<Object> result = new EnhancedList<>(listType);
		result.addAll(listMap.values());

		return result;
	}

	private static Set<?> buildSet(List<ListRecord> records, SetType setType) {
		Set<Object> result = new EnhancedSet<>(setType);

		for (ListRecord record : records) {
			result.add(record.getValues().get(VALUE_POSITION));
		}

		return result;
	}

	private static Map<?, ?> buildMap(List<ListRecord> records, MapType mapType) {
		Map<Object, Object> result = new EnhancedMap<>(mapType);

		for (ListRecord record : records) {
			List<Object> rowValues = record.getValues();

			Object key = rowValues.get(KEY_POSITION);
			Object value = rowValues.get(VALUE_POSITION);

			result.put(key, value);
		}

		return result;
	}

}
