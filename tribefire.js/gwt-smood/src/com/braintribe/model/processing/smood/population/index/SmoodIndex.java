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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * @author peter.gazdik
 */
public abstract class SmoodIndex implements LookupIndex {

	protected EntityType<?> entityType = GenericEntity.T;
	protected List<SmoodIndex> superRootIndices = newList();
	protected List<SmoodIndex> meAndSubIndices = newList();

	public SmoodIndex() {
		this.superRootIndices.add(this);
		this.meAndSubIndices.add(this);
	}

	public void setEntityType(EntityType<?> entityType) {
		this.entityType = entityType;
	}

	public void linkSuperIndex(SmoodIndex superIndex) {
		superIndex.meAndSubIndices.add(this);

		for (SmoodIndex superRootIndex : superRootIndices)
			if (superRootIndex.entityType.isAssignableFrom(superIndex.entityType))
				// the new superIndex is a sub-type of some superRoot
				return;

		// the new superIndes is a new superRoot, now we remove all existing superRoots that are it's sub-types
		Iterator<SmoodIndex> it = superRootIndices.iterator();
		while (it.hasNext()) {
			SmoodIndex superRootIndex = it.next();
			if (superIndex.entityType.isAssignableFrom(superRootIndex.entityType))
				it.remove();
		}

		superRootIndices.add(superIndex);
	}

	@Override
	public final <T extends GenericEntity> T getValue(Object indexValue) {
		for (SmoodIndex index : meAndSubIndices) {
			GenericEntity entity = index.getThisLevelValue(indexValue);
			if (entity != null)
				return (T) entity;
		}

		return null;
	}

	protected abstract GenericEntity getThisLevelValue(Object indexValue);

	@Override
	public final Collection<? extends GenericEntity> getValues(Object indexValue) {
		List<GenericEntity> result = newList();

		for (SmoodIndex index : meAndSubIndices)
			result.addAll(index.getThisLevelValues(indexValue));

		return result;
	}

	protected abstract Collection<? extends GenericEntity> getThisLevelValues(Object indexValue);

	@Override
	public final Collection<? extends GenericEntity> allValues() {
		List<GenericEntity> result = newList();

		for (SmoodIndex index : meAndSubIndices)
			result.addAll(index.allThisLevelValues());

		return result;
	}

	protected abstract Collection<? extends GenericEntity> allThisLevelValues();

}
