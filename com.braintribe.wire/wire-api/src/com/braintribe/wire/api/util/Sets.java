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
package com.braintribe.wire.api.util;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public interface Sets {

	static <E> HashSet<E> set() {
		return new HashSet<E>();
	}

	@SafeVarargs
	static <E> HashSet<E> set(E... entries) {
		HashSet<E> set = new HashSet<E>();
		add(set, entries);
		return set;
	}

	static <E> LinkedHashSet<E> linkedSet() {
		return new LinkedHashSet<E>();
	}

	@SafeVarargs
	static <E> LinkedHashSet<E> linkedSet(E... entries) {
		LinkedHashSet<E> set = new LinkedHashSet<E>();
		add(set, entries);
		return set;
	}

	@SafeVarargs
	static <E> void add(Set<E> set, E... entries) {
		if (set != null && entries != null) {
			for (E entry : entries) {
				set.add(entry);
			}
		}
	}

}
