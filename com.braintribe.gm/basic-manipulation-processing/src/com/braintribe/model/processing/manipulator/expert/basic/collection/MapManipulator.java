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

import com.braintribe.model.processing.manipulator.api.CollectionManipulator;

public class MapManipulator implements CollectionManipulator<Map<Object, Object>, Object> {

	public static final MapManipulator INSTANCE = new MapManipulator();

	private MapManipulator() {
	}

	@Override
	public void insert(Map<Object, Object> map, Object index, Object value) {
		map.put(index, value);
	}

	@Override
	public void insert(Map<Object, Object> map, Map<Object, Object> values) {
		map.putAll(values);
	}

	@Override
	public void remove(Map<Object, Object> map, Object index, Object value) {
		map.remove(index);
	}

	@Override
	public void remove(Map<Object, Object> map, Map<Object, Object> values) {
		for (Object key: values.keySet()) {
			map.remove(key);
		}
	}

	@Override
	public void clear(Map<Object, Object> map) {
		map.clear();
	}

}
