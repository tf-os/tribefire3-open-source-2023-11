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

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;

public class ObjectComparator implements Comparator<Object> {
	
	public static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	protected AssemblyComparison assemblyComparison;
	private boolean internal;
	
	public ObjectComparator(AssemblyComparison assemblyComparison, boolean internal) {
		super();
		this.assemblyComparison = assemblyComparison;
		this.internal = internal;
	}

	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == o2)
			return 0;
		
		if (o1 == null)
			return -1;
		
		if (o2 == null)
			return 1;
		
		GenericModelType t1 = typeReflection.getType(o1);
		GenericModelType t2 = typeReflection.getType(o2);
		
		int res = t1.getTypeSignature().compareTo(t2.getTypeSignature());
		
		if (res != 0) {
			if (!internal)
				assemblyComparison.setMismatchDescription("type mismatch: " + t1.getTypeSignature() + " vs. " + t2.getTypeSignature());
			return res;
		}
		
		Comparator<Object> comparator = internal? 
				assemblyComparison.getInternalComparator(t1):
				assemblyComparison.getComparator(t1);
		
		return comparator.compare(o1, o2);
	}
}
