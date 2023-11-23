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

import java.util.ArrayList;
import java.util.List;

public interface Lists {

	static <E> ArrayList<E> list() {
		return new ArrayList<E>();
	}

	@SafeVarargs
	static <E> ArrayList<E> list(E... entries) {
		ArrayList<E> list = new ArrayList<E>();
		add(list, entries);
		return list;
	}

	@SafeVarargs
	static <E> void add(List<E> list, E... entries) {
		if (list != null && entries != null) {
			for (E entry : entries) {
				list.add(entry);
			}
		}
	}

}
