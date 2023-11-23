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
package com.braintribe.model.processing.traversing.api.path;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.api.HasEntityPropertyInfo;
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

public abstract class TraversingCollectionItemModelPathElement extends TraversingPropertyRelatedModelPathElement {

	public TraversingCollectionItemModelPathElement(TraversingModelPathElement previous, Object value, GenericModelType type) {
		super(previous, value, type);
	}
	
	protected HasEntityPropertyInfo getEntityPropertyInfo() {
		TraversingModelPathElement previous = getPrevious();
		if (previous.getElementType() == ModelPathElementType.Property) {
			return (HasEntityPropertyInfo)previous;
		}
		else
			return EmptyEntityPropertyInfo.INSTANCE;
	}
	
	@Override
	public <T extends GenericEntity> T getEntity() {
		return getEntityPropertyInfo().getEntity();
	}
	
	@Override
	public <T extends GenericEntity> EntityType<T> getEntityType() {
		return getEntityPropertyInfo().getEntityType();
	}
	
	@Override
	public Property getProperty() {
		return getEntityPropertyInfo().getProperty();
	}

}
