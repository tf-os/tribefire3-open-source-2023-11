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
package com.braintribe.model.processing.platformreflection.java;

import java.util.Comparator;

public class ExtendedThreadInfoComparator implements Comparator<ExtendedThreadInfo> {
	
	private String type;

	public ExtendedThreadInfoComparator(String type) {
		this.type = type;
	}

	@Override
	public int compare(ExtendedThreadInfo o1, ExtendedThreadInfo o2) {
		 if ("cpu".equals(type)) {
             return (int) (o2.cpuTimeInNanos - o1.cpuTimeInNanos);
         } else if ("wait".equals(type)) {
             return (int) (o2.waitedTimeInMs - o1.waitedTimeInMs);
         } else if ("block".equals(type)) {
             return (int) (o2.blockedTimeInMs - o1.blockedTimeInMs);
         }
         throw new IllegalArgumentException("expected thread type to be either 'cpu', 'wait', or 'block', but was " + type);
	}

}
