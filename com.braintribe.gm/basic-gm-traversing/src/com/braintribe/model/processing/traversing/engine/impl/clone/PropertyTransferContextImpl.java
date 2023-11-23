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
package com.braintribe.model.processing.traversing.engine.impl.clone;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyModelPathElement;
import com.braintribe.model.processing.traversing.engine.api.customize.PropertyTransferContext;

/**
 * @author peter.gazdik
 */
class PropertyTransferContextImpl implements PropertyTransferContext {

	public GmTraversingContext traversingContext; 
	public TraversingPropertyModelPathElement propertyPathElement;
	public GenericEntity clonedEntity;

	@Override
	public GmTraversingContext getTraversingContext() {
		return traversingContext;
	}

	@Override
	public TraversingPropertyModelPathElement getPropertyPathElement() {
		return propertyPathElement;
	}

	@Override
	public Property getProperty() {
		return propertyPathElement.getProperty();
	}

	@Override
	public GenericEntity getInstanceToBeCloned() {
		return (GenericEntity) propertyPathElement.getPrevious().getValue();
	}

	@Override
	public GenericEntity getClonedInstance() {
		return clonedEntity;
//		return traversingContext.getSharedCustomValue(propertyPathElement.getPrevious());
	}

	@Override
	public Object getValueToBeCloned() {
		return getProperty().get(getInstanceToBeCloned());
	}


}
