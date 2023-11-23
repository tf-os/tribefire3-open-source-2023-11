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
package com.braintribe.model.processing.index;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public abstract class SingleThreadIndex<K, V> implements Index<K, V> {

	protected Map<K, V> index = new HashMap<K, V>();

	/**
	 * Retrieves the indexed value if present, in other case asks for new value to be provided via {@link #provideValueFor(Object)} method.
	 * <p>
	 * This method guarantees that a value is created only once for given key.
	 */
	@Override
	public final V acquireFor(K key) {
		V value = index.get(key);

		if (value == null) {
			value = provideValueFor(key);
			index.put(key, value);
		}

		return value;
	}

	protected abstract V provideValueFor(K key);

}
