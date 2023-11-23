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
import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyModelPathElement;

public class EntityComparator implements Comparator<GenericEntity> {
	private final Set<EqualEntities> equalEntities;
	private final AssemblyComparison assemblyComparison;  
	private final boolean internal;
	private final boolean useGlobalId;
	
	public EntityComparator(AssemblyComparison assemblyComparison, boolean internal, boolean useGlobalId) {
		this.assemblyComparison = assemblyComparison;
		this.internal = internal;
		this.useGlobalId = useGlobalId;
		equalEntities = new HashSet<>();
	}
	
	@Override
	public int compare(GenericEntity e1, GenericEntity e2) {
		if (e1 == e2)
			return 0;
		
		if (e1 == null)
			return -1;
		
		if (e2 == null)
			return 1;
		
		
		if (!internal && !equalEntities.add(new EqualEntities(e1, e2)))
			return 0;
		
		EntityType<GenericEntity> t1 = e1.entityType();
		EntityType<GenericEntity> t2 = e2.entityType();
		
		// first level
		int res = t1.getTypeSignature().compareTo(t2.getTypeSignature());
		
		if (res != 0) {
			if (!internal)
				assemblyComparison.setMismatchDescription("type mismatch: " + t1.getTypeSignature() + " vs. " + t2.getTypeSignature());
			
			return res;
		}
		
		
		/*if (internal) {
			EnhancedEntity eh1 = (EnhancedEntity)e1;
			EnhancedEntity eh2 = (EnhancedEntity)e1;
			Long rid1 = eh1.runtimeId();
			Long rid2 = eh2.runtimeId();
			
			return rid1.compareTo(rid2);
		}*/
		
		// id
		Object id1 = null, id2 = null;
		
		if (useGlobalId) {
			id1 = e1.getGlobalId();
			id2 = e2.getGlobalId();
		}
		else {
			id1 = e1.getId();
			id2 = e2.getId();
		}
		 
		res = ((Comparable<Object>)id1).compareTo(id2);
		
		if (internal)
			return res;
		
		if (res != 0) {
			assemblyComparison.setMismatchDescription("identity mismatch: " + id1 + " vs. " + id2);
			return res;
		}
		
		// properties
		for (Property property: t1.getProperties()) {
			GenericModelType propertyType = property.getType();
			
			Comparator<Object> comparator = assemblyComparison.getComparator(propertyType);
			Object v1 = property.get(e1);
			Object v2 = property.get(e2);
			
			assemblyComparison.pushElement(p -> new TraversingPropertyModelPathElement(p, v1, propertyType.getActualType(v1), e1, t1, property, property.isAbsent(e1)));
			
			res = comparator.compare(v1, v2);
			if (res != 0)
				return res;
			else 
				assemblyComparison.popElement();
		}
		
		return 0;
	}
	
	private static class EqualEntities {
		private final GenericEntity e1;
		private final GenericEntity e2;
		
		public EqualEntities(GenericEntity e1, GenericEntity e2) {
			super();
			this.e1 = e1;
			this.e2 = e2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((e1 == null) ? 0 : e1.hashCode());
			result = prime * result + ((e2 == null) ? 0 : e2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EqualEntities other = (EqualEntities) obj;
			
			return (e1 == other.e1 && e2 == other.e2) || (e1 == other.e2 && e2 == other.e1);
		}

		
	}

}
