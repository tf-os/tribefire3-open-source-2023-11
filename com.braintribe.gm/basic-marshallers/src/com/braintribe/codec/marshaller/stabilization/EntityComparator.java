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
package com.braintribe.codec.marshaller.stabilization;

import java.util.Comparator;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public class EntityComparator implements Comparator<GenericEntity> {
	
	public static final EntityComparator INSTANCE = new EntityComparator();
	
	@Override
	public int compare(GenericEntity o1, GenericEntity o2) {
		if (o1 == o2)
			return 0;
		
		if (o1 == null)
			return -1;
		
		if (o2 == null)
			return 1;
		
		EntityType<?> t1 = o1.entityType();
		EntityType<?> t2 = o2.entityType();
		
		int res = t1.getShortName().compareTo(t2.getShortName());
		
		if (res != 0)
			return res;
		
		res = t1.compareTo(t2);
		
		if (res != 0)
			return res;
		
		NormalizedId id1 = new NormalizedId(o1);
		NormalizedId id2 = new NormalizedId(o2);
		
		return id1.compareTo(id2);
	}
}
