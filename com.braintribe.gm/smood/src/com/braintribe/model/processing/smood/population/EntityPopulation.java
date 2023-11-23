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

	public static EntityPopulation w_create(EntityType<?> et, PopulationManager populationManager) {
		return new EntityPopulation(et, populationManager);
	}

	// only reachable with a write-lock
	private EntityPopulation(EntityType<?> et, PopulationManager populationManager) {
		this.et = et;
		this.populationManager = populationManager;
		this.idIndex = w_acquireIndex(et.getIdProperty());
	}

	public LookupIndex getIdIndex() {
		return idIndex;
	}

	private LookupIndex w_findIndexIfRelevant(Property p) {
		if (p.isGlobalId())
			throw new IllegalStateException("globalId index is not expected to be acquired this way!");

		p = et.findProperty(p.getName());

		return p != null && isIndexed(p) ? w_acquireIndex(p) : null;
	}

	public void w_registerEntity(GenericEntity entity) {
		for (Property p : et.getProperties()) {
			// this should be cached so we don't have to call CMD each time
			if (isIndexed(p))
				w_addEntityToIndex(entity, p);
		}
	}

	/* only called if p is indexed property */
	private void w_addEntityToIndex(GenericEntity entity, Property p) {
		if (p.isAbsent(entity))
			return;

		Object propertyValue = p.get(entity);

		if (p.isGlobalId())
			populationManager.globalIdIndex.addEntity(entity, propertyValue);
		else
			w_acquireIndex(p).addEntity(entity, propertyValue);
	}

	public void w_removeEntity(GenericEntity entity) {
		for (Property p : et.getProperties())
			if (!p.isAbsent(entity))
				if (isIndexed(p))
					w_removeFromIndex(entity, p);
	}

	private void w_removeFromIndex(GenericEntity entity, Property p) {
		Object propertyValue = p.get(entity);

		if (p.isGlobalId())
			populationManager.globalIdIndex.removeEntity(entity, propertyValue);
		else
			w_acquireIndex(p).removeEntity(entity, propertyValue);
	}

	public void w_onChangeValue(GenericEntity entity, Property p, Object oldValue, Object newValue) {
		IndexType indexType = getIndexType(p);
		if (indexType == IndexType.none)
			return;

		if (p.isGlobalId())
			populationManager.globalIdIndex.onChangeValue(entity, oldValue, newValue);
		else
			w_acquireIndex(indexType, p).onChangeValue(entity, oldValue, newValue);
	}

	private boolean isIndexed(Property p) {
		return getIndexType(p) != IndexType.none;
	}

	private LookupIndex w_acquireIndex(Property p) {
		IndexType indexType = getIndexType(p);
		if (indexType == null || indexType == IndexType.none)
			return null;
		else
			return w_acquireIndex(indexType, p);
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

	private LookupIndex w_acquireIndex(IndexType indexType, Property p) {
		String indexId = indexId(p.getName());
		LookupIndex result = populationManager.getIndexOrNull(indexId);

		if (result == null)
			result = w_newIndex(indexType, p);

		return result;
	}

	private LookupIndex w_newIndex(IndexType indexType, Property p) {
		String propertyName = p.getName();

		SmoodIndex result = createIndex(indexType, p);
		w_linkWithSuperIndices(result, p);
		fillIndexInfo(result, propertyName);

		populationManager.w_onNewIndex(result);

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

	private void w_linkWithSuperIndices(SmoodIndex result, Property p) {
		result.setEntityType(et);

		for (EntityType<?> superType : populationManager.acquireSuperTypes(et)) {
			LookupIndex superIndex = populationManager.w_acquireEntityPopulation(superType).w_findIndexIfRelevant(p);
			if (superIndex != null)
				result.w_linkSuperIndex((SmoodIndex) superIndex);
		}
	}

	private String indexId(String propertyName) {
		return SmoodIndexTools.indexId(et, propertyName);
	}

}
