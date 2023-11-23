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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.smood.population.info.IndexInfoImpl;

/**
 * We use this for globalId index for a concrete type - i.e. only instances of that type pass the filter.
 * 
 * @author peter.gazdik
 */
public class FilteringLookupIndex implements LookupIndex {

	private final LookupIndex delegate;
	private final Predicate<? super GenericEntity> filter;
	protected final IndexInfoImpl indexInfo;

	public FilteringLookupIndex(LookupIndex delegate, Predicate<GenericEntity> filter) {
		this.delegate = delegate;
		this.filter = filter;
		this.indexInfo = new IndexInfoImpl();
	}

	@Override
	public void addEntity(GenericEntity entity, Object value) {
		delegate.addEntity(entity, value);
	}

	@Override
	public void removeEntity(GenericEntity entity, Object propertyValue) {
		delegate.removeEntity(entity, propertyValue);
	}

	@Override
	public void onChangeValue(GenericEntity entity, Object oldValue, Object newValue) {
		delegate.onChangeValue(entity, oldValue, newValue);
	}

	@Override
	public <T extends GenericEntity> T getValue(Object indexValue) {
		T result = delegate.getValue(indexValue);
		return filter.test(result) ? result : null;
	}

	@Override
	public Collection<? extends GenericEntity> getValues(Object indexValue) {
		return delegate.getValues(indexValue).stream() //
				.filter(filter) //
				.collect(Collectors.toList());
	}

	@Override
	public Collection<? extends GenericEntity> allValues() {
		return delegate.allValues().stream() //
				.filter(filter) //
				.collect(Collectors.toList());
	}

	@Override
	public IndexInfoImpl getIndexInfo() {
		return indexInfo;
	}

}
