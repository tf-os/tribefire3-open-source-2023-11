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
package com.braintribe.model.generic.collection;

import java.util.LinkedHashMap;
import java.util.Map;

import com.braintribe.model.generic.reflection.MapType;

/**
 * @author peter.gazdik
 */
public class PlainMap<K, V> extends LinkedHashMap<K, V> implements MapBase<K, V> {

	private static final long serialVersionUID = -6029303086432557408L;

	private final MapType mapType;

	public PlainMap(MapType mapType) {
		this.mapType = mapType;
	}

	public PlainMap(MapType mapType, Map<? extends K, ? extends V> m) {
		super(m);
		this.mapType = mapType;
	}

	@Override
	public MapType type() {
		return mapType;
	}

}
