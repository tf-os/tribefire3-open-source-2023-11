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

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.model.service.api.InstanceId;

public class InstanceIdHashingComparator implements HashingComparator<InstanceId> {

	public final static InstanceIdHashingComparator instance = new InstanceIdHashingComparator();
	
	@Override
	public boolean compare(InstanceId e1, InstanceId e2) {
		if (e1 == null && e2 == null) {
			return true;
		}
		if (e1 == null) {
			return false;
		}
		if (e2 == null) {
			return false;
		}
		
		String leftNode = e1.getNodeId();
		String rightNode = e2.getNodeId();
		
		if (leftNode == null && rightNode != null) {
			return false;
		}
		if (rightNode == null && leftNode != null) {
			return false;
		}

		if (leftNode != null && rightNode != null) {
			if (!leftNode.equals(rightNode)) {
				return false;
			}
		}
		
		String leftApp = e1.getApplicationId();
		String rightApp = e2.getApplicationId();
		
		if (leftApp == null && rightApp != null) {
			return false;
		}
		if (rightApp == null && leftApp != null) {
			return false;
		}

		if (leftApp != null && rightApp != null) {
			if (!leftApp.equals(rightApp)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int computeHash(InstanceId e) {
		
		if (e == null) {
			return 0;
		}
		String text = e.stringify();
		return text.hashCode();
	}

}
