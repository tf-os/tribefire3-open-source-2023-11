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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.traversing.api.path.TraversingMapKeyModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingMapValueModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;

public class MapComparator implements Comparator<Object> {
	private AssemblyComparison assemblyComparison;
	private Comparator<Object> keyComparator;
	private Comparator<Object> valueComparator;
	private Comparator<Object> internalKeyComparator;
	private GenericModelType keyType;
	private GenericModelType valueType;
	
	public MapComparator(AssemblyComparison assemblyComparison, GenericModelType keyType, GenericModelType valueType,
			Comparator<Object> keyComparator, Comparator<Object> valueComparator, Comparator<Object> internalKeyComparator) {
		super();
		this.assemblyComparison = assemblyComparison;
		this.keyType = keyType;
		this.valueType = valueType;
		this.keyComparator = keyComparator;
		this.valueComparator = valueComparator;
		this.internalKeyComparator = internalKeyComparator;
	}
	
	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == o2)
			return 0;
		
		if (o1 == null)
			return -1;
		
		if (o2 == null)
			return 1;
		
		Map<Object, Object> m1 = (Map<Object, Object>)o1;
		Map<Object, Object> m2 = (Map<Object, Object>)o2;
		
		int res = m1.size() - m2.size();
		
		if (res != 0) {
			assemblyComparison.setMismatchDescription("maps differ in size: " + m1.size() + " vs. " + m2.size());
			return res;
		}
		
		SortedMap<Object, Object> sortedMap1 = new TreeMap<Object, Object>(internalKeyComparator);
		sortedMap1.putAll(m1);
		SortedMap<Object, Object> sortedMap2 = new TreeMap<Object, Object>(internalKeyComparator);
		sortedMap2.putAll(m2);
		
		Iterator<Map.Entry<Object, Object>> it1 = sortedMap1.entrySet().iterator();
		Iterator<Map.Entry<Object, Object>> it2 = sortedMap2.entrySet().iterator();
		
		while (it1.hasNext()) {
			Map.Entry<Object, Object> e1 = it1.next();
			Map.Entry<Object, Object> e2 = it2.next();
			
			Object k1 = e1.getKey();
			Object k2 = e2.getKey();
			Object v1 = e1.getValue();
			Object v2 = e2.getValue();
			
			TraversingMapKeyModelPathElement keyElement = null;
			TraversingMapValueModelPathElement valueElement = null;
			
			if (assemblyComparison.isTrackingEnabled()) {
				GenericModelType actualKeyType = keyType.getActualType(k1);
				GenericModelType actualValueType = valueType.getActualType(v1);
				TraversingModelPathElement predecessor = assemblyComparison.peekElement();
				keyElement = new TraversingMapKeyModelPathElement(predecessor, v1, actualValueType, k1, actualKeyType, e1);
				valueElement = new TraversingMapValueModelPathElement(keyElement);
			}
			
			// push and compare key
			assemblyComparison.pushElement(keyElement);

			res = keyComparator.compare(k1, k2);
			
			if (res != 0)
				return res;
			else
				assemblyComparison.popElement();

			// push and compare value
			assemblyComparison.pushElement(valueElement);

			res = valueComparator.compare(v1, v2);
			
			if (res != 0)
				return res;
			else
				assemblyComparison.popElement();
		}
		
		return 0;
	}
}
