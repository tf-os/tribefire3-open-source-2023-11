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
package com.braintribe.model.processing.validation.expert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Utility class that hosts methods commonly being used by the validation tasks.
 * </p>
 * 
 *
 */
public final class CommonChecks {

	private CommonChecks() {
	}

	public static boolean isNotNull(Object object) {
		return object != null;
	}

	public static boolean areValuesUnique(List<String> values) {
		Set<String> set = new HashSet<>();
		for (String value : values) {
			if (!set.add(value))
				return false;
		}
		return true;
	}
}
