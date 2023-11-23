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
package com.braintribe.model.processing.meta.cmd.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.model.processing.meta.cmd.index.LRUMap;

/** Utility class for all code that would not work in GWT (must be emulated). */
public class CmdGwtUtils {

	public static <K, V> Map<K, V> newCacheMap() {
		return new ConcurrentHashMap<K, V>();
	}

	public static <K, V> Map<K, V> newWeakCacheMap(int maxSize) {
		return new LRUMap<K, V>(maxSize);
	}

	/**
	 * In GWT, this only works iff <tt>clazz</tt> is really a class, not an interface..
	 */
	public static boolean isInstanceOf(Class<?> clazz, Object o) {
		return clazz.isInstance(o);
	}

	public static <T> T cast(Class<T> clazz, Object o) {
		return clazz.cast(o);
	}

	public static <T> Class<? extends T> asSubclass(Class<?> c, Class<T> clazz) {
		return c.asSubclass(clazz);
	}

}
