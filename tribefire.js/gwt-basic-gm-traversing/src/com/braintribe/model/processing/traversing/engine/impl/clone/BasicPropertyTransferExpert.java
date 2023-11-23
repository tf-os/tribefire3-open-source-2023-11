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
import com.braintribe.model.processing.traversing.engine.api.customize.PropertyTransferContext;
import com.braintribe.model.processing.traversing.engine.api.customize.PropertyTransferExpert;

/**
 * @author peter.gazdik
 */
public class BasicPropertyTransferExpert implements PropertyTransferExpert {

	public static final BasicPropertyTransferExpert INSTANCE = new BasicPropertyTransferExpert();

	private BasicPropertyTransferExpert() {
	}

	@Override
	public void transferProperty(GenericEntity clonedEntity, Property property, Object clonedValue, PropertyTransferContext context) {
		property.set(clonedEntity, clonedValue);
	}

}
