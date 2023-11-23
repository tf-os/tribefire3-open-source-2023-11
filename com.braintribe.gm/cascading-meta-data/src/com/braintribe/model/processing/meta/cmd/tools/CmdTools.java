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

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public class CmdTools {

	public static String asString(List<? extends Class<?>> classes) {
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (Class<?> clazz: classes) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}

			sb.append(clazz.getName());
		}

		return sb.toString();
	}

	public static <T extends GenericEntity> Map<EntityType<? extends T>, T> indexByEntityType(Set<? extends T> set) {
		Map<EntityType<? extends T>, T> result = newMap();

		for (T t: set) {
			result.put(t.entityType(), t);
		}

		return result;
	}
}
