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
package com.braintribe.model.processing.query.fluent;

import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.CriterionBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Source;

public class PropertyQueryBuilder extends AbstractQueryBuilder<PropertyQuery> {

	protected PropertyQueryBuilder() {
		super(PropertyQuery.T.create());
	}

	public static PropertyQueryBuilder forProperty(EntityType<?> type, Object id, String property) {
		return forProperty(type.getTypeSignature(), id, property);
	}

	public static PropertyQueryBuilder forProperty(EntityType<?> type, Object id, String partition, String property) {
		return forProperty(type.getTypeSignature(), id, partition, property);
	}

	public static PropertyQueryBuilder forProperty(String typeSignature, Object id, String property) {
		return forProperty(typeSignature, id, null, property);
	}

	public static PropertyQueryBuilder forProperty(String typeSignature, Object id, String partition, String property) {
		PersistentEntityReference reference = PersistentEntityReference.T.create();
		reference.setTypeSignature(typeSignature);
		reference.setRefId(id);
		reference.setRefPartition(partition);
		return forProperty(reference, property);
	}

	public static PropertyQueryBuilder forProperty(PersistentEntityReference entityReference, String property) {
		PropertyQueryBuilder builder = new PropertyQueryBuilder();
		builder.query.setEntityReference(entityReference);
		builder.query.setPropertyName(property);
		return builder;
	}

	public static PropertyQueryBuilder forProperty(EntityProperty entityProperty) {
		return forProperty((PersistentEntityReference) entityProperty.getReference(), entityProperty.getPropertyName());
	}

	@Override
	public ConditionBuilder<PropertyQueryBuilder> where() {
		return (ConditionBuilder<PropertyQueryBuilder>) super.where();
	}

	@Override
	public PropertyQueryBuilder limit(int limit) {
		aquirePaging().setPageSize(limit);
		return this;
	}

	@Override
	public PropertyQueryBuilder paging(int pageSize, int pageStart) {
		Paging paging = aquirePaging();
		paging.setPageSize(pageSize);
		paging.setStartIndex(pageStart);
		return this;
	}

	@Override
	public OperandBuilder<PropertyQueryBuilder> orderBy() {
		return (OperandBuilder<PropertyQueryBuilder>) super.orderBy();
	}

	@Override
	public OperandBuilder<PropertyQueryBuilder> orderBy(OrderingDirection orderingDirection) {
		return (OperandBuilder<PropertyQueryBuilder>) super.orderBy(orderingDirection);
	}

	@Override
	public PropertyQueryBuilder orderBy(String propertyName) {
		return (PropertyQueryBuilder) super.orderBy(propertyName);
	}

	@Override
	public PropertyQueryBuilder orderBy(String propertyName, OrderingDirection orderingDirection) {
		return (PropertyQueryBuilder) super.orderBy(orderingDirection).property(null, propertyName);
	}

	@Override
	public PropertyQueryBuilder orderingDirection(OrderingDirection orderingDirection) {
		return (PropertyQueryBuilder) super.orderingDirection(orderingDirection);
	}

	@Override
	public CascadedOrderingBuilder<PropertyQueryBuilder> orderByCascade() {
		return (CascadedOrderingBuilder<PropertyQueryBuilder>) super.orderByCascade();
	}

	@Override
	public CriterionBuilder<PropertyQueryBuilder> tc() {
		return (CriterionBuilder<PropertyQueryBuilder>) super.tc();
	}

	@Override
	public PropertyQueryBuilder tc(TraversingCriterion traversingCriterion) {
		return (PropertyQueryBuilder) super.tc(traversingCriterion);
	}

	@Override
	public Source getFirstSource() {
		return null;
	}

}
