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
package com.braintribe.codec.marshaller.dom.coder.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.codec.marshaller.dom.DomDecodingContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;

public class ActivePropertyAbsenceHelper extends PropertyAbsenceHelper {
	private Set<Property> presentProperties = new HashSet<Property>();
	private DomDecodingContext context;
	
	public ActivePropertyAbsenceHelper(DomDecodingContext context) {
		super();
		this.context = context;
	}

	@Override
	public void addPresent(Property property) {
		presentProperties.add(property);
	}
	
	@Override
	public void ensureAbsenceInformation(EntityType<?> entityType,
			GenericEntity entity) {
		List<Property> properties = entityType.getProperties();
		
		if (properties.size() != presentProperties.size()) {
			for (Property property: properties) {
				if (!presentProperties.contains(property)) {
					property.setAbsenceInformation(entity, context.getAbsenceInformationForMissingProperties());
				}
			}
		}
		
	}
}
