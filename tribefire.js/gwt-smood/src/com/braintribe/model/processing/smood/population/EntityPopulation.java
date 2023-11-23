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
package com.braintribe.model.processing.smood.population;

import com.braintribe.common.lcd.UnsupportedEnumException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Indexed;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.query.IndexType;
import com.braintribe.model.processing.smood.population.index.IndexKind;
import com.braintribe.model.processing.smood.population.index.LookupIndex;
import com.braintribe.model.processing.smood.population.index.MetricIndex;
import com.braintribe.model.processing.smood.population.index.MultiLookupIndex;
import com.braintribe.model.processing.smood.population.index.MultiMetricIndex;
import com.braintribe.model.processing.smood.population.index.SmoodIndex;
import com.braintribe.model.processing.smood.population.index.UniqueLookupIndex;
import com.braintribe.model.processing.smood.population.index.UniqueMetricIndex;
import com.braintribe.model.processing.smood.population.info.IndexInfoImpl;

/**
 * Manages all entities of one {@link EntityType}. The entities are stored in indices, with id index being always there and other indices too based on
 * {@link Indexed} meta-data.
 * <p>
 * Note that the id index is referenced internally, but the other ones are only stored inside {@link PopulationManager}, by their
 * s{@link SmoodIndexTools#indexId(String, String) index ids} , as they are needed there anyway.
 * 
 * @author peter.gazdik
 */
public class EntityPopulation {

	private final EntityType<?> et;
	private final PopulationManager populationManager;
	private final LookupIndex idIndex;

	public EntityPopulation(EntityType<?> et, PopulationManager populationManager) {
		this.et = et;
		this.populationManager = populationManager;
		this.idIndex = acquireIndex(et.getIdProperty());
	}

	public LookupIndex getIdIndex() {
		return idIndex;
	}

	public LookupIndex findIndexIfRelevant(Property p) {
		if (p.isGlobalId())
			throw new IllegalStateException("globalId index is not expected to be acquired this way!");

		p = et.findProperty(p.getName());

		return p != null && isIndexed(p) ? acquireIndex(p) : null;
	}

	public void registerEntity(GenericEntity entity) {
		for (Property p : et.getProperties()) {
			// this should be cached so we don't have to call CMD each time
			if (isIndexed(p))
				addEntityToIndex(entity, p);
		}
	}

	/* only called if p is indexed property */
	private void addEntityToIndex(GenericEntity entity, Property p) {
		if (p.isAbsent(entity))
			return;

		Object propertyValue = p.get(entity);

		if (p.isGlobalId())
			populationManager.globalIdIndex.addEntity(entity, propertyValue);
		else
			acquireIndex(p).addEntity(entity, propertyValue);
	}

	public void removeEntity(GenericEntity entity) {
		for (Property p : et.getProperties())
			if (!p.isAbsent(entity))
				if (isIndexed(p))
					removeFromIndex(entity, p);
	}

	private void removeFromIndex(GenericEntity entity, Property p) {
		Object propertyValue = p.get(entity);

		if (p.isGlobalId())
			populationManager.globalIdIndex.removeEntity(entity, propertyValue);
		else
			acquireIndex(p).removeEntity(entity, propertyValue);
	}

	public void onChangeValue(GenericEntity entity, Property p, Object oldValue, Object newValue) {
		IndexType indexType = getIndexType(p);
		if (indexType == IndexType.none)
			return;

		if (p.isGlobalId())
			populationManager.globalIdIndex.onChangeValue(entity, oldValue, newValue);
		else
			acquireIndex(indexType, p).onChangeValue(entity, oldValue, newValue);
	}

	private boolean isIndexed(Property p) {
		return getIndexType(p) != IndexType.none;
	}

	private LookupIndex acquireIndex(Property p) {
		IndexType indexType = getIndexType(p);
		if (indexType == null || indexType == IndexType.none)
			return null;
		else
			return acquireIndex(indexType, p);
	}

	private IndexType getIndexType(Property p) {
		if (p.isIdentifier())
			return idIndexType(p);

		if (p.isGlobalId())
			return IndexType.lookup;

		return populationManager.mdResolver.resolveIndexType(et, p);
	}

	private IndexType idIndexType(Property idProperty) {
		IndexType indexType = populationManager.mdResolver.resolveIndexType(et, idProperty);
		return indexType == IndexType.metric ? indexType : IndexType.lookup;
	}

	/**
	 * @param indexType
	 *            type of index to create, if no index exists yet. If the value is <tt>null</tt>, no index will be created
	 */
	private LookupIndex acquireIndex(IndexType indexType, Property p) {
		String indexId = indexId(p.getName());
		LookupIndex result = populationManager.allIndices.get(indexId);

		if (result == null && indexType != null)
			result = newIndex(indexType, p);

		return result;
	}

	private LookupIndex newIndex(IndexType indexType, Property p) {
		String propertyName = p.getName();

		SmoodIndex result = createIndex(indexType, p);
		linkWithSuperIndices(result, p);
		fillIndexInfo(result, propertyName);
		onNewIndex(result);

		return result;
	}

	private SmoodIndex createIndex(IndexType indexType, Property p) {
		GenericModelType type = p.getType();

		IndexKind indexKind = getIndexKind(indexType, p);
		switch (indexKind) {
			case multiLookup:
				return new MultiLookupIndex(type);
			case multiMetric:
				return new MultiMetricIndex(type);
			case uniqueLookup:
				return new UniqueLookupIndex();
			case uniqueMetric:
				return new UniqueMetricIndex(type);
		}

		throw new UnsupportedEnumException("Unknown index kind: " + indexKind + ". Property: " + propertyInfo(p));
	}

	private IndexKind getIndexKind(IndexType indexType, Property p) {
		if (p.isIdentifier())
			return indexType == IndexType.metric ? IndexKind.multiMetric : IndexKind.multiLookup;

		if (indexType == IndexType.auto)
			indexType = SmoodIndexTools.supportsMetric(p.getType()) ? IndexType.metric : IndexType.lookup;

		switch (indexType) {
			case lookup:
				return isUnique(p) ? IndexKind.uniqueLookup : IndexKind.multiLookup;
			case metric:
				return isUnique(p) ? IndexKind.uniqueMetric : IndexKind.multiMetric;
			case none:
				throw new IllegalArgumentException("Cannot create new index. IndexType is set to none for " + propertyInfo(p));
			default:
				break;
		}

		throw new UnsupportedEnumException("Unknown index type: " + indexType + ". Property: " + propertyInfo(p));
	}

	private String propertyInfo(Property p) {
		return et.getTypeSignature() + "." + p.getName();
	}

	private boolean isUnique(Property p) {
		return populationManager.mdResolver.isUnique(et, p);
	}

	private void fillIndexInfo(LookupIndex result, String propertyName) {
		IndexInfoImpl indexInfo = result.getIndexInfo();

		indexInfo.setEntitySignature(et.getTypeSignature());
		indexInfo.setPropertyName(propertyName);
		indexInfo.setIndexId(indexId(propertyName));
		indexInfo.setHasMetric(result instanceof MetricIndex);
	}

	private void linkWithSuperIndices(SmoodIndex result, Property p) {
		result.setEntityType(et);

		for (EntityType<?> superType : populationManager.acquireSuperTypes(et)) {
			LookupIndex superIndex = populationManager.acquireEntityPopulation(superType).findIndexIfRelevant(p);
			if (superIndex != null)
				result.linkSuperIndex((SmoodIndex) superIndex);
		}
	}

	private void onNewIndex(LookupIndex propertyIndex) {
		IndexInfoImpl indexInfo = propertyIndex.getIndexInfo();

		populationManager.allIndices.put(indexInfo.getIndexId(), propertyIndex);

		if (indexInfo.hasMetric())
			populationManager.metricIndices.put(indexInfo.getIndexId(), (MetricIndex) propertyIndex);

		populationManager.repositoryInfo.getIndexInfos().add(indexInfo);
	}

	private String indexId(String propertyName) {
		return SmoodIndexTools.indexId(et, propertyName);
	}

}
