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

import static com.braintribe.model.processing.smood.population.SmoodIndexTools.getComparator;

import java.util.Comparator;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;

/**
 * 
 */
public class UniqueMetricIndex extends UniqueIndex implements SmoodMetricIndex {

	private final NavigableMap<Object, GenericEntity> navigableMap;
	private final Comparator<Object> keyComparator;

	public UniqueMetricIndex(GenericModelType keyType) {
		this(getComparator(keyType));
	}

	protected UniqueMetricIndex(Comparator<Object> keyComparator) {
		super(new TreeMap<>(keyComparator));

		this.keyComparator = keyComparator;
		this.navigableMap = (NavigableMap<Object, GenericEntity>) map;
	}

	@Override
	public Comparator<Object> getKeyComparator() {
		return keyComparator;
	}

	@Override
	public NavigableMap<Object, GenericEntity> getNavigableMap() {
		return navigableMap;
	}

}
