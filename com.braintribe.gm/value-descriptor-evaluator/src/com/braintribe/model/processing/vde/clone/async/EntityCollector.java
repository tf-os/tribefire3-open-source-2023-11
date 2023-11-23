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
package com.braintribe.model.processing.vde.clone.async;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.processing.async.api.AsyncCallback;

public class EntityCollector extends AsyncCollector<GenericEntity> {

	public EntityCollector(GenericEntity entity) {
		this(entity.entityType().getProperties().size(), entity);
	}

	private EntityCollector(int propertiesToClone, GenericEntity entity) {
		super(propertiesToClone, entity);
	}

	public AsyncCallback<Object> collectProperty(Property property) {
		return collect((e, v) -> {
			if (VdHolder.isVdHolder(v))
				property.setDirectUnsafe(e, v);
			else
				property.setDirect(e, v);
			});
	}

	public static EntityCollector forNonClonedEntity(GenericEntity entity) {
		return new EntityCollector(0, entity);
	}

}
