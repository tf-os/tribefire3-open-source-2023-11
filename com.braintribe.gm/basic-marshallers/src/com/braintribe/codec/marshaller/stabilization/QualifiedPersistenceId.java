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

import com.braintribe.utils.lcd.NullSafe;

public class QualifiedPersistenceId implements Comparable<QualifiedPersistenceId> {
	private String partition;
	private Comparable<Object> id;
	
	public QualifiedPersistenceId(String partition, Comparable<Object> id) {
		super();
		this.partition = partition;
		this.id = id;
	}
	
	@Override
	public int compareTo(QualifiedPersistenceId o) {
		int res = NullSafe.cmp(partition, o.partition);
		
		if (res != 0)
			return res;
		
		return id.compareTo(o.id);
	}
	
	@Override
	public String toString() {
		if (partition == null) {
			return id.toString();
		}
		else {
			return partition + ':' + id;
		}
	}
	
	public boolean hasPartition() {
		return partition != null;
	}
}
