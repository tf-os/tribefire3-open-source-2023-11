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
package com.braintribe.web.api.registry;

import java.util.Map;

public interface Registration extends Comparable<Registration> {

	Map<String, String> getInitParameters();

	Integer getOrder();

	@Override
	public default int compareTo(Registration o) {
		Integer f = getOrder();
		Integer s = o.getOrder();
		if (f == null && s == null)
			return 0;
		if (f == null)
			return -1;
		if (s == null)
			return 1;
		return Integer.compare(f, s);
	}

}
