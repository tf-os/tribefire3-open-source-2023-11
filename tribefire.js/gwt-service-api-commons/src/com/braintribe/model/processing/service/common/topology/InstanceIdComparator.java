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
package com.braintribe.model.processing.service.common.topology;

import java.util.Comparator;

import com.braintribe.model.service.api.InstanceId;

public class InstanceIdComparator implements Comparator<InstanceId> {

	public static InstanceIdComparator instance = new InstanceIdComparator();
	
	private InstanceIdComparator() {
		//Do nothing; use instance instead
	}
	
	@Override
	public int compare(InstanceId o1, InstanceId o2) {
		if (o1 == null && o2 == null) {
			return 0;
		}
		if (o1 == null) {
			return 1;
		}
		if (o2 == null) {
			return -1;
		}
		
		String leftNode = o1.getNodeId();
		String rightNode = o2.getNodeId();
		
		if (leftNode == null && rightNode != null) {
			return 1;
		}
		if (rightNode == null && leftNode != null) {
			return -1;
		}

		if (leftNode != null && rightNode != null) {
			int cmp = leftNode.compareTo(rightNode);
			if (cmp != 0) {
				return cmp;
			}
		}
		
		String leftApp = o1.getApplicationId();
		String rightApp = o2.getApplicationId();
		
		if (leftApp == null || rightApp == null) {
			if (leftApp == null) {
				return 1;
			}
			if (rightApp == null) {
				return -1;
			}
		}
		
		return leftApp.compareTo(rightApp);
	}

}
