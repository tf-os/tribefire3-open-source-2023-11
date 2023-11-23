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

import com.braintribe.codec.marshaller.stax.tree.EntityNode;

public class EntityNodeComparator implements Comparator<EntityNode> {

	public static final EntityNodeComparator INSTANCE = new EntityNodeComparator(); 

	@Override
	public int compare(EntityNode n1, EntityNode n2) {
		
		return EntityComparator.INSTANCE.compare(n1.getEntity(), n2.getEntity());
	}
}
