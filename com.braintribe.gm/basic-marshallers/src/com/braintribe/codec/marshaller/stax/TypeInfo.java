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
package com.braintribe.codec.marshaller.stax;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;

public class TypeInfo implements Comparable<TypeInfo> {
	public GenericModelType type;
	public String alias;
	public String as;
	protected int count;
	
	public TypeInfo() {
	}
	
	public int getCount() {
		return count;
	}
	
	@Override
	public int compareTo(TypeInfo o) {
		return as.compareTo(o.as);
	}
	
	public String nextId(GenericEntity entity) {
		return alias + "-" + Integer.toHexString(count++);
	}

	public void setCount(int count) {
		this.count = count;
	}
}
