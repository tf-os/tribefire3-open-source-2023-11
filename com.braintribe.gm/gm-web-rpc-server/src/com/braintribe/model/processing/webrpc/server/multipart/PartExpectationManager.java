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
package com.braintribe.model.processing.webrpc.server.multipart;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.resource.Resource;

public class PartExpectationManager {

	private Set<String> expectedParts;
	private final Set<String> foundParts = new HashSet<>();

	public void expect(Iterable<Resource> resources) {
		expectedParts = new HashSet<>();

		for (Resource resource : resources) {
			if (resource.isTransient()) {
				expectedParts.add(resource.getGlobalId());
			}
		}

		expectedParts.removeAll(foundParts);
	}

	public boolean isLastExpected(String name) {
		foundParts.add(name);
		if (expectedParts != null) {
			expectedParts.remove(name);
			return expectedParts.isEmpty();
		} else {
			return false;
		}
	}

}
