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
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.cmd.context.aspects.IsEntityPreliminaryAspect;
import com.braintribe.model.processing.meta.cmd.extended.EntityRelatedMdDescriptor;
import com.braintribe.model.processing.meta.cmd.result.MdResult;

import jsinterop.annotations.JsType;

/**
 * Common superclass for {@link EntityMdResolver} and {@link PropertyMdResolver}.
 */
@JsType(namespace=GmCoreApiInteropNamespaces.metadata)
@SuppressWarnings("unusable-by-js")
public interface EntityRelatedMdResolver<B extends EntityRelatedMdResolver<B>> extends MdResolver<B> {

	B entity(GenericEntity genericEntity);

	/** Specifies the {@link IsEntityPreliminaryAspect}. */
	B preliminary(boolean isPreliminary);

	@Override
	<M extends MetaData> MdResult<M, ? extends EntityRelatedMdDescriptor> meta(EntityType<M> metaDataType);

}
