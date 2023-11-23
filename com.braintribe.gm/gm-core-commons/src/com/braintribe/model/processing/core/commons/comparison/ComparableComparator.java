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
package com.braintribe.model.processing.core.commons.comparison;

import java.util.Comparator;

public class ComparableComparator implements Comparator<Object> {
	private boolean internal;
	private AssemblyComparison assemblyComparision;
	
	public ComparableComparator(boolean internal, AssemblyComparison assemblyComparision) {
		super();
		this.internal = internal;
		this.assemblyComparision = assemblyComparision;
	}

	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == o2)
			return 0;
		
		int res = 0;
		
		if (o1 == null)  
			res = -1;
		else if (o2 == null)
			res = 1;
		else { 
			Comparable<Object> c1 = (Comparable<Object>)o1;
			res = c1.compareTo(o2);
		}
		
		if (res != 0) {
			if (!internal)
				assemblyComparision.setMismatchDescription("value mismatch: " + o1 + " vs. " + o2);
		}
		
		return res;
	}
}
