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
package com.braintribe.model.processing.smood.population.index;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.processing.smood.population.info.IndexInfoImpl;

/**
 * 
 */
abstract class UniqueIndex extends SmoodIndex {

	protected final IndexInfoImpl indexInfo;
	protected final Map<Object, GenericEntity> map;
	protected final Set<GenericEntity> nullValueEntities;

	protected UniqueIndex(Map<Object, GenericEntity> map) {
		this.indexInfo = new IndexInfoImpl();
		this.map = map;
		this.nullValueEntities = newSet();
	}

	@Override
	public void addEntity(GenericEntity entity, Object value) {
		if (value == null) {
			nullValueEntities.add(entity);
			return;
		}

		for (SmoodIndex superRootIndex : superRootIndices) {
			GenericEntity otherEntity = superRootIndex.getValue(value);
			if (otherEntity != null && otherEntity != entity)
				throw new IllegalStateException("Another entity is already indexed (" + superRootIndex.getIndexInfo().getIndexId() + ") for key '"
						+ value + "'. ADDED ENTITY: " + entity + ", INDEXED ENTITY: " + otherEntity);
		}

		map.put(value, entity);
	}

	@Override
	public void onChangeValue(GenericEntity entity, Object oldValue, Object newValue) {
		removeEntity(entity, oldValue);

		try {
			addEntity(entity, newValue);

		} catch (IllegalStateException e) {
			addEntity(entity, oldValue);
			throw e;
		}
	}

	@Override
	public void removeEntity(GenericEntity entity, Object value) {
		GenericEntity removedEntity = actualRemove(value, entity);
		if (removedEntity != entity) {
			// this can only happen if value != null, so we do not have to handle that case
			map.put(value, removedEntity);
			throw new IllegalStateException("Different entity found in index (" + indexInfo.getIndexId() + ") for key '" + value + "', EXPECTED: "
					+ entity + ", FOUND : " + removedEntity);
		}
	}

	private GenericEntity actualRemove(Object key, GenericEntity entity) {
		if (key == null) {
			nullValueEntities.remove(entity);
			return entity;

		} else if (VdHolder.isVdHolder(key)) {
			return entity;

		} else {
			return map.remove(key);
		}
	}

	@Override
	protected Collection<? extends GenericEntity> getThisLevelValues(Object indexValue) {
		if (indexValue == null)
			return nullValueEntities;

		GenericEntity value = getValue(indexValue);

		return value == null ? Collections.<GenericEntity> emptySet() : Arrays.asList(value);
	}

	@Override
	protected GenericEntity getThisLevelValue(Object indexValue) {
		return indexValue == null ? anyNullValue() : map.get(indexValue);
	}

	private GenericEntity anyNullValue() {
		return nullValueEntities.isEmpty() ? null : nullValueEntities.iterator().next();
	}

	@Override
	protected Collection<? extends GenericEntity> allThisLevelValues() {
		return nullValueEntities.isEmpty() ? map.values() : union(nullValueEntities, map.values());
	}

	private static Set<GenericEntity> union(Collection<GenericEntity> c1, Collection<GenericEntity> c2) {
		// TODO there is no reason here to copy the data, just create a view which combines both
		Set<GenericEntity> result = newSet();
		result.addAll(c1);
		result.addAll(c2);

		return result;
	}

	@Override
	public IndexInfoImpl getIndexInfo() {
		return indexInfo;
	}

}
