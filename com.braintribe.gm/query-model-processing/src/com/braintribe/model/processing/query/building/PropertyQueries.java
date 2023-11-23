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
package com.braintribe.model.processing.query.building;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.PropertyQuery;

public abstract class PropertyQueries extends Queries {
	
	
	public static PropertyQuery from(EntityType<?> entityType, Object id, String propertyName) {
		return PropertyQuery.create(entityType, id, propertyName);
	}
	
	public static PropertyQuery from(EntityType<?> entityType, Object id, Property property) {
		return PropertyQuery.create(entityType, id, property);
	}
	
	public static PropertyQuery from(EntityType<?> entityType, Object id, String partition, String propertyName) {
		return PropertyQuery.create(entityType, id, partition, propertyName);
	}
	
	public static PropertyQuery from(EntityType<?> entityType, Object id, String partition, Property property) {
		return PropertyQuery.create(entityType, id, partition, property);
	}
	
	public static PropertyQuery from(String entityTypeSignature, Object id, String propertyName) {
		return PropertyQuery.create(entityTypeSignature, id, propertyName);
	}
	
	public static PropertyQuery from(String entityTypeSignature, Object id, Property property) {
		return PropertyQuery.create(entityTypeSignature, id, property);
	}
	
	public static PropertyQuery from(String entityTypeSignature, Object id, String partition, String propertyName) {
		return PropertyQuery.create(entityTypeSignature, id, partition, propertyName);
	}
	
	public static PropertyQuery from(String entityTypeSignature, Object id, String partition, Property property) {
		return PropertyQuery.create(entityTypeSignature, id, partition, property);
	}
	
	public static PropertyQuery from(GenericEntity instance, String propertyName) {
		return PropertyQuery.create(instance, propertyName);
	}
	
	public static PropertyQuery from(GenericEntity instance, Property property) {
		return PropertyQuery.create(instance, property);
	}
	
	public static PropertyQuery from(PersistentEntityReference reference, String propertyName) {
		return PropertyQuery.create(reference, propertyName);
	}
	
	public static PropertyQuery from(PersistentEntityReference reference, Property property) {
		return PropertyQuery.create(reference, property);
	}
	
	public static PropertyQuery from(EntityProperty entityProperty) {
		return PropertyQuery.create(entityProperty);
	}
	
	public static PropertyOperand property(String name) {
		return PropertyOperand.create(name);
	}
	
	public static PropertyOperand property(Property property) {
		return PropertyOperand.create(property);
	}
	
	public static Operand source() {
		return null;
	}
}