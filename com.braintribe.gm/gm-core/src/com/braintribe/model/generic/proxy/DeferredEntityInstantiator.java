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
package com.braintribe.model.generic.proxy;

import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;

public class DeferredEntityInstantiator implements DeferredApplier {

	private final ProxyEntity proxyEntity;
	
	public DeferredEntityInstantiator(ProxyEntity proxyEntity) {
		super();
		this.proxyEntity = proxyEntity;
	}

	@Override
	public void apply() {
		EntityType<?> entityType = proxyEntity.entityType().getResolvedType();
		
		GenericEntity entity = null;
		
		if (entityType != null) {
			entity = entityType.create();
			Map<AbstractProxyProperty, Object> properties = proxyEntity.properties();
			
			for (Entry<AbstractProxyProperty, Object> entry : properties.entrySet()) {
				ProxyProperty proxyProperty = (ProxyProperty) entry.getKey();
				Object propertyValue = entry.getValue();
				Property property = proxyProperty.getActualProperty();
				if (property != null) {
					property.setDirectUnsafe(entity, propertyValue);
				}
			}
		}
		proxyEntity.linkActualEntity(entity);
	}
}
