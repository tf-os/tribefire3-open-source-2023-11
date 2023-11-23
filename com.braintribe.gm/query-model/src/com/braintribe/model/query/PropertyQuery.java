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
package com.braintribe.model.query;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.query.conditions.Condition;

/**
 * Special type of query which is evaluated as the value of the property in the same way as if one would call the getter
 * (short of a possible {@link Restriction}). So for example when querying a Set property, the returned result is an
 * instance of {@link java.util.Set}.
 */
public interface PropertyQuery extends Query {

	EntityType<PropertyQuery> T = EntityTypes.T(PropertyQuery.class);

	PersistentEntityReference getEntityReference();
	void setEntityReference(PersistentEntityReference entityReference);

	String getPropertyName();
	void setPropertyName(String propertyName);

	default EntityType<?> ownerType() {
		return getEntityReference().valueType();
	}

	default Property property() {
		return ownerType().getProperty(getPropertyName());
	}
	
	
	public static PropertyQuery create(EntityType<?> entityType, Object id, String propertyName) {
		return create(entityType.getTypeSignature(), id, null, propertyName);
	}
	
	public static PropertyQuery create(EntityType<?> entityType, Object id, Property property) {
		return create(entityType.getTypeSignature(), id, property.getName());
	}
	
	public static PropertyQuery create(EntityType<?> entityType, Object id, String partition, String propertyName) {
		return create(entityType.getTypeSignature(), id, partition, propertyName);
	}
	
	public static PropertyQuery create(EntityType<?> entityType, Object id, String partition, Property property) {
		return create(entityType, id, partition, property.getName());
	}
	
	public static PropertyQuery create(String entityTypeSignature, Object id, String propertyName) {
		return create(entityTypeSignature, id, null, propertyName);
	}
	
	public static PropertyQuery create(String entityTypeSignature, Object id, Property property) {
		return create(entityTypeSignature, id, property.getName());
	}
	
	public static PropertyQuery create(String entityTypeSignature, Object id, String partition, Property property) {
		return create(entityTypeSignature, id, partition, property.getName());
	}
	
	public static PropertyQuery create(String entityTypeSignature, Object id, String partition, String propertyName) {
		PersistentEntityReference reference = PersistentEntityReference.T.create();
		reference.setRefPartition(partition);
		reference.setRefId(id);
		reference.setTypeSignature(entityTypeSignature);
		return create(reference, propertyName);
	}
	
	public static PropertyQuery create(GenericEntity instance, String propertyName) {
		return create(instance.globalReference(), propertyName);
	}
	
	public static PropertyQuery create(GenericEntity instance, Property property) {
		return create(instance, property.getName());
	}
	
	public static PropertyQuery create(PersistentEntityReference reference, String propertyName) {
		PropertyQuery query = PropertyQuery.T.create();
		query.setEntityReference(reference);
		query.setPropertyName(propertyName);
		return query;
	}
	
	public static PropertyQuery create(PersistentEntityReference reference, Property property) {
		return create(reference, property.getName());
	}
	
	public static PropertyQuery create(EntityProperty entityProperty) {
		return create((PersistentEntityReference)entityProperty.getReference(), entityProperty.getPropertyName());
	}
	
	@Override
	default PropertyQuery where(Condition condition) {
		Query.super.where(condition);
		return this;
	}
	
	@Override
	default PropertyQuery distinct() {
		Query.super.distinct();
		return this;
	}
	
	@Override
	default PropertyQuery orderBy(Ordering ordering) {
		Query.super.orderBy(ordering);
		return this;
	}
	
	@Override
	default PropertyQuery orderBy(Object orderValue) {
		Query.super.orderBy(orderValue);
		return this;
	}
	
	@Override
	default PropertyQuery orderBy(OrderingDirection direction, Object orderValue) {
		Query.super.orderBy(direction, orderValue);
		return this;
	}
	
	@Override
	default PropertyQuery paging(Paging paging) {
		Query.super.paging(paging);
		return this;
	}
	
	@Override
	default PropertyQuery paging(int startIndex, int pageSize) {
		Query.super.paging(Paging.create(startIndex, pageSize));
		return this;
	}
	
	@Override
	default PropertyQuery limit(int limit) {
		Query.super.paging(Paging.create(0, limit));
		return this;
	}
	
	@Override
	default PropertyQuery tc(TraversingCriterion tc) {
		Query.super.tc(tc);
		return this;
	}
	
	@Override
	default PropertyQuery restriction(Restriction restriction) {
		Query.super.restriction(restriction);
		return this;
	}


}
