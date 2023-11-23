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
package com.braintribe.model.generic.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.exception.Exceptions;

public class GwtCompatibilityUtils {

	public static <K, V> Map<K, V> newConcurrentMap() {
		return new ConcurrentHashMap<K, V>();
	}
	
	public static Class<?> getEnumClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw Exceptions.unchecked(e, "Enum class not found: " + className);
		}
	}

}
