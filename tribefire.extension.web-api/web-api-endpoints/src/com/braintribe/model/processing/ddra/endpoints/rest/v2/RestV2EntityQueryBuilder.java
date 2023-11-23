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
package com.braintribe.model.processing.ddra.endpoints.rest.v2;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.processing.query.fluent.AbstractQueryBuilder;
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.query.EntityQuery;

public class RestV2EntityQueryBuilder extends PropertyAccessInterceptor {

	private final JunctionBuilder<? extends AbstractQueryBuilder<EntityQuery>> conjunction;
	
	public RestV2EntityQueryBuilder(JunctionBuilder<? extends AbstractQueryBuilder<EntityQuery>> conjunction) {
		this.conjunction = conjunction;
	}
	
	@Override
	public Object getProperty(Property property, GenericEntity entity, boolean isVd) {
		Object value = property.getDirect(entity);
		
		if (!property.getType().isScalar()) {
			throw new IllegalArgumentException("Illegal query parameter: 'where." + property.getName() + "'. Only scalar types are allowed for where-parameters but property '" + property.getName() + "' of an entity with type '" + entity.entityType().getTypeSignature() + "' is itself of type: '" + property.getType().getTypeSignature() + "'.");
		}
		return value;
	}
	
	@Override
	public Object setProperty(Property property, GenericEntity entity, Object value, boolean isVd) {
		this.conjunction.property(property.getName()).eq(value);
		return property.setDirect(entity, value);
	}
}
