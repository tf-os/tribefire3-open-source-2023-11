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
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.cmd.result.ModelMdResult;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Resolver for meta data defined on a {@link GmMetaModel}.
 * 
 * @see MdResolver
 */
@JsType(namespace=GmCoreApiInteropNamespaces.metadata)
@SuppressWarnings("unusable-by-js")
public interface ModelMdResolver extends MdResolver<ModelMdResolver> {

	EntityMdResolver entity(GenericEntity genericEntity);

	EntityMdResolver entityClass(Class<? extends GenericEntity> entityClass);

	EntityMdResolver entityType(EntityType<? extends GenericEntity> entityType);

	@JsMethod(name="entityTypeInfo")
	EntityMdResolver entityType(GmEntityTypeInfo gmEntityTypeInfo);

	EntityMdResolver entityTypeSignature(String typeSignature);

	ConstantMdResolver enumConstant(Enum<?> constant);

	@JsMethod(name="gmEnumConstant")
	ConstantMdResolver enumConstant(GmEnumConstant constant);

	EnumMdResolver enumClass(Class<? extends Enum<?>> entityClass);

	EnumMdResolver enumType(EnumType enumType);

	@JsMethod(name="gmEnumType")
	EnumMdResolver enumType(GmEnumType gmEnumType);

	EnumMdResolver enumTypeSignature(String typeSignature);

	@JsMethod(name="propertyInfo")
	PropertyMdResolver property(GmPropertyInfo gmPropertyInfo);

	PropertyMdResolver property(Property property);

	@Override
	<M extends MetaData> ModelMdResult<M> meta(EntityType<M> metaDataType);

}
