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
package com.braintribe.model.processing.meta.cmd.empty;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.meta.cmd.result.EntityMdResult;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class EmptyEntityMdResolver extends EmptyEntityRelatedMdResolver<EntityMdResolver> implements EntityMdResolver {

	public static final EmptyEntityMdResolver INSTANCE = new EmptyEntityMdResolver();

	private EmptyEntityMdResolver() {
	}

	@Override
	public final PropertyMdResolver property(GmPropertyInfo gmPropertyInfo) {
		return EmptyPropertyMdResolver.INSTANCE;
	}

	@Override
	public final PropertyMdResolver property(Property property) {
		return EmptyPropertyMdResolver.INSTANCE;
	}

	@Override
	public final PropertyMdResolver property(String propertyName) {
		return EmptyPropertyMdResolver.INSTANCE;
	}

	@Override
	public final <M extends MetaData> EntityMdResult<M> meta(EntityType<M> metaDataType) {
		return EmptyMdResult.EmptyEntityMdResult.singleton();
	}

	@Override
	public final GmEntityType getGmEntityType() {
		throw new UnsupportedOperationException("Method 'EmptyEntityMdResolver.getGmEntityType' is not supported!");
	}

}
