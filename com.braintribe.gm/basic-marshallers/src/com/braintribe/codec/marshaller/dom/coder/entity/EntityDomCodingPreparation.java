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

import java.util.HashMap;
import java.util.Map;

public class EntityDomCodingPreparation {
	public PropertyDomCodingPreparation[] propertyPreparations;
	private Map<String, PropertyDomCodingPreparation> propertyPreparationsByName;
	
	public EntityDomCodingPreparation(PropertyDomCodingPreparation[] propertyPreparations) {
		super();
		this.propertyPreparations = propertyPreparations;
	}

	public PropertyDomCodingPreparation getPropertyCoderByName(String propertyName) {
		if (propertyPreparationsByName == null) {
			propertyPreparationsByName = new HashMap<String, PropertyDomCodingPreparation>();
			for (PropertyDomCodingPreparation propertyPreparation: propertyPreparations) {
				propertyPreparationsByName.put(propertyPreparation.property.getName(), propertyPreparation);
			}
		}

		return propertyPreparationsByName.get(propertyName);
	}

}
