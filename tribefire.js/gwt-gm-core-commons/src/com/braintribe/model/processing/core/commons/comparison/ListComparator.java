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
import java.util.Iterator;
import java.util.List;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.traversing.api.path.TraversingListItemModelPathElement;

public class ListComparator implements Comparator<Object> {
	private Comparator<Object> elementComparator;
	private AssemblyComparison assemblyComparison;
	private GenericModelType elementType;
	
	public ListComparator(AssemblyComparison assemblyComparison, GenericModelType elementType, Comparator<Object> elementComparator) {
		super();
		this.assemblyComparison = assemblyComparison;
		this.elementType = elementType;
		this.elementComparator = elementComparator;
	}

	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == o2)
			return 0;
		
		if (o1 == null)
			return -1;
		
		if (o2 == null)
			return 1;
		
		List<Object> l1 = (List<Object>)o1;
		List<Object> l2 = (List<Object>)o2;
		
		int res = l1.size() - l2.size();

		if (res != 0) {
			assemblyComparison.setMismatchDescription("lists differ in size: " + l1.size() + " vs. " + l2.size());
			return res; 
		}
		
		Iterator<Object> it1 = l1.iterator();
		Iterator<Object> it2 = l2.iterator();
		
		int index = 0;
		while (it1.hasNext()) {
			Object e1 = it1.next();
			Object e2 = it2.next();
			
			int currentIndex = index;
			
			assemblyComparison.pushElement(p -> new TraversingListItemModelPathElement(p, e1, elementType.getActualType(e1), currentIndex));

			res = elementComparator.compare(e1, e2);
			
			if (res != 0)
				return res;
			else
				assemblyComparison.popElement();
			
			index++;
		}
		
		return 0;
	}
}
