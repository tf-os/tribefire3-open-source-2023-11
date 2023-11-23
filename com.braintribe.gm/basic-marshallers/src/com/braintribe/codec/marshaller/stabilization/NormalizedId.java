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

import com.braintribe.model.generic.GenericEntity;

public class NormalizedId implements Comparable<NormalizedId> {
	private IdQualifier qualifier;
	private Comparable<Object> id;
	
	public NormalizedId(GenericEntity entity) {
		
		Comparable<Object> persistenceId = (Comparable<Object>)entity.getId();
		
		if (persistenceId != null) {
			QualifiedPersistenceId qualifiedPersistenceId = new QualifiedPersistenceId(entity.getPartition(), persistenceId);
			qualifier = qualifiedPersistenceId.hasPartition()? IdQualifier.partitionedPersistenceIdQualifier: IdQualifier.persistenceIdQualifier;
			id = (Comparable<Object>)(Comparable<?>)qualifiedPersistenceId;
			return;
		}
		
		String globalId = entity.getGlobalId();
		
		if (globalId != null) {
			qualifier = IdQualifier.globalIdQualifier;
			id = (Comparable<Object>)(Comparable<?>)globalId;
			return;
		}
		
		id = (Comparable<Object>)(Comparable<?>)entity.runtimeId();
		qualifier = IdQualifier.runtimeIdQualfier;
	}
	
	@Override
	public int compareTo(NormalizedId o) {
		int res = qualifier.compareTo(o.qualifier);
		
		if (res != 0)
			return res;
		
		return id.compareTo(o.id);
	}
	
	@Override
	public String toString() {
		return qualifier.getCode()  + id.toString();
	}
	
}
