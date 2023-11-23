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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.context.MutableSelectorContext;
import com.braintribe.model.processing.meta.cmd.context.aspects.EntityAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.EntityTypeAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.IsEntityPreliminaryAspect;
import com.braintribe.model.processing.meta.cmd.resolvers.MdAggregator;

abstract class EntityRelatedMdResolverImpl<B extends EntityRelatedMdResolver<B>> extends MdResolverImpl<B> implements EntityRelatedMdResolver<B> {

	EntityRelatedMdResolverImpl(Class<? extends B> myType, MutableSelectorContext selectorContext, MdAggregator mdAggregator) {
		super(myType, selectorContext, mdAggregator);
	}

	@Override
	public B entity(GenericEntity entity) {
		EntityType<?> et = entity.entityType();

		// this might override the EntityTypeAspect defined ModelMdResolverImpl.entityType(...)
		selectorContext.put(EntityAspect.class, entity);
		selectorContext.put(EntityTypeAspect.class, et);

		return myself;
	}

	@Override
	public B preliminary(boolean isPreliminary) {
		selectorContext.put(IsEntityPreliminaryAspect.class, isPreliminary);
		return myself;
	}

}
