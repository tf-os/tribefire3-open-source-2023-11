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
package com.braintribe.model.processing.smart.query.planner.tools;

import java.util.Comparator;

import com.braintribe.model.accessdeployment.IncrementalAccess;

/**
 * 
 */
public class AccessComparator implements Comparator<IncrementalAccess> {

	public static final AccessComparator INSTANCE = new AccessComparator();

	private AccessComparator() {
	}

	@Override
	public int compare(IncrementalAccess a1, IncrementalAccess a2) {
		return a1.getExternalId().compareTo(a2.getExternalId());
	}

}
