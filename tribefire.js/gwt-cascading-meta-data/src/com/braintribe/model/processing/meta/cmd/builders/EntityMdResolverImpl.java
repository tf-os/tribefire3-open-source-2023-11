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
package com.braintribe.model.processing.meta.cmd.builders;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.cmd.context.MutableSelectorContext;
import com.braintribe.model.processing.meta.cmd.empty.EmptyPropertyMdResolver;
import com.braintribe.model.processing.meta.cmd.resolvers.EntityMdAggregator;
import com.braintribe.model.processing.meta.cmd.resolvers.PropertyMdAggregator;
import com.braintribe.model.processing.meta.cmd.result.EntityMdResult;
import com.braintribe.model.processing.meta.oracle.proxy.DynamicEntityTypeOracle;

/**
 * 
 */
@SuppressWarnings("unusable-by-js")
public class EntityMdResolverImpl extends EntityRelatedMdResolverImpl<EntityMdResolver> implements EntityMdResolver {

	protected EntityMdAggregator entityMdAggregator;

	protected EntityMdResolverImpl(EntityMdAggregator entityMdAggregator, MutableSelectorContext selectorContext) {
		super(EntityMdResolverImpl.class, selectorContext, entityMdAggregator);

		this.entityMdAggregator = entityMdAggregator;
	}

	@Override
	public PropertyMdResolver property(GmPropertyInfo gmPropertyInfo) {
		return property(gmPropertyInfo.relatedProperty().getName());
	}

	@Override
	public PropertyMdResolver property(Property property) {
		return property(property.getName());
	}

	@Override
	public PropertyMdResolver property(String propertyName) {
		if (entityMdAggregator.getEntityOracle().findProperty(propertyName) == null) {
			return lenientOrThrowException(() -> EmptyPropertyMdResolver.INSTANCE, () -> "Property not found: " + propertyName);
		}

		// I am not sure how else to check if our entity type is a dynamic one
		if (entityMdAggregator.getEntityOracle() instanceof DynamicEntityTypeOracle) {
			PropertyMdAggregator propertyMdAggregator = entityMdAggregator.getDynamicPropertyMdAggregator(propertyName);
			propertyMdAggregator.addLocalContextTo(selectorContext);

			return getPropertyMetaDataContextBuilder(propertyMdAggregator);
		}

		PropertyMdAggregator propertyMdAggregator = entityMdAggregator.acquirePropertyMdAggregator(propertyName);
		propertyMdAggregator.addLocalContextTo(selectorContext);

		return getPropertyMetaDataContextBuilder(propertyMdAggregator);
	}

	@Override
	public GmEntityType getGmEntityType() {
		return entityMdAggregator.getGmEntityType();
	}

	protected PropertyMdResolver getPropertyMetaDataContextBuilder(PropertyMdAggregator propertyMdAggregator) {
		return new PropertyMdResolverImpl(propertyMdAggregator, selectorContext.copy());
	}

	@Override
	public EntityMdResolver fork() {
		return new EntityMdResolverImpl(entityMdAggregator, selectorContext.copy());
	}

	@Override
	public <M extends MetaData> EntityMdResult<M> meta(EntityType<M> metaDataType) {
		return new MdResultImpl.EntityMdResultImpl<>(metaDataType, entityMdAggregator, selectorContext);
	}

}
