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
package com.braintribe.model.processing.manipulator.expert.basic.collection;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.processing.manipulator.api.CollectionManipulator;

public class SetManipulator implements CollectionManipulator<Set<Object>, Object> {

	public static final SetManipulator INSTANCE = new SetManipulator();

	private SetManipulator() {
	}
	
	@Override
	public void insert(Set<Object> set, Object index, Object value) {
		set.add(value);
	}

	@Override
	public void insert(Set<Object> set, Map<Object, Object> values) {
		set.addAll(values.values());
	}

	@Override
	public void remove(Set<Object> set, Object index, Object value) {
		set.remove(index);
	}

	@Override
	public void remove(Set<Object> set, Map<Object, Object> values) {
		set.removeAll(values.keySet());
	}

	@Override
	public void clear(Set<Object> set) {
		set.clear();
	}

}
