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
package com.braintribe.model.processing.core.commons;

import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.value.EntityReference;

/**
 * Use {@link EntRefHashingComparator} directly with your CodingMap/Set.
 */
@Deprecated
public class EntityReferenceWrapperCodec extends HashSupportWrapperCodec<EntityReference> {
	
	public static final EntityReferenceWrapperCodec INSTANCE = new EntityReferenceWrapperCodec();
	
	public EntityReferenceWrapperCodec() {
		super(true);
	}
	
	@Override
	protected int entityHashCode(EntityReference e) {
		return EntRefHashingComparator.INSTANCE.computeHash(e);
	}

	@Override
	protected boolean entityEquals(EntityReference e1, EntityReference e2) {
		return EntRefHashingComparator.INSTANCE.compare(e1, e2);
	}			
}		
