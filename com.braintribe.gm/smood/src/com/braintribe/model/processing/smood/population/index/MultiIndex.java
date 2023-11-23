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

import java.util.Collection;
import java.util.Comparator;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.processing.query.eval.tools.EntityComparator;
import com.braintribe.model.processing.smood.population.SmoodIndexTools;
import com.braintribe.model.processing.smood.population.info.IndexInfoImpl;
import com.braintribe.utils.collection.api.NavigableMultiMap;
import com.braintribe.utils.collection.impl.ComparatorBasedNavigableMultiMap;

/**
 * 
 */
public abstract class MultiIndex extends SmoodIndex {

	protected final IndexInfoImpl indexInfo;
	protected final Comparator<Object> keyComparator;
	protected final NavigableMultiMap<Object, GenericEntity> map;

	public MultiIndex(GenericModelType keyType) {
		this.indexInfo = new IndexInfoImpl();
		this.keyComparator = SmoodIndexTools.getComparator(keyType);
		this.map = new ComparatorBasedNavigableMultiMap<>(keyComparator, EntityComparator.INSTANCE);
	}

	@Override
	public void addEntity(GenericEntity entity, Object value) {
		map.put(value, entity);
	}

	@Override
	public void removeEntity(GenericEntity entity, Object propertyValue) {
		if (!map.remove(propertyValue, entity))
			throw new IllegalStateException("Entity was not in the index (" + indexInfo.getIndexId() + "), but should have been. Entity: " + entity
					+ ", property value: " + propertyValue);
	}

	@Override
	public void onChangeValue(GenericEntity entity, Object oldValue, Object newValue) {
		if (!VdHolder.isVdHolder(oldValue))
			if (!map.remove(oldValue, entity))
				throw new IllegalStateException("Entity was not in the index (" + indexInfo.getIndexId() + "), but should have been. Entity: "
						+ entity + ", oldValue: " + oldValue + ", newValue: " + newValue);

		map.put(newValue, entity);
	}

	@Override
	protected GenericEntity getThisLevelValue(Object indexValue) {
		return map.get(indexValue);
	}

	@Override
	protected Collection<? extends GenericEntity> getThisLevelValues(Object indexValue) {
		return map.getAll(indexValue);
	}

	@Override
	protected Collection<? extends GenericEntity> allThisLevelValues() {
		return map.values();
	}

	@Override
	public IndexInfoImpl getIndexInfo() {
		return indexInfo;
	}

}
