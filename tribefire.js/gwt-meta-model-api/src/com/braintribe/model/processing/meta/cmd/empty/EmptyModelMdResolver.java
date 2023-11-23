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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.data.ExplicitPredicate;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.cmd.builders.ConstantMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EnumMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.meta.cmd.result.ModelMdResult;

@SuppressWarnings("unusable-by-js")
public class EmptyModelMdResolver extends EmptyMdResolver<ModelMdResolver> implements ModelMdResolver {

	public static final EmptyModelMdResolver INSTANCE = new EmptyModelMdResolver();

	private EmptyModelMdResolver() {
	}

	@Override
	public boolean is(EntityType<? extends Predicate> predicateType) {
		boolean isExplicit = ExplicitPredicate.T.isAssignableFrom(predicateType);
		return !isExplicit; // if explicit, default is false
	}

	@Override
	public EntityMdResolver entity(GenericEntity genericEntity) {
		return EmptyEntityMdResolver.INSTANCE;
	}

	@Override
	public EntityMdResolver entityClass(Class<? extends GenericEntity> entityClass) {
		return EmptyEntityMdResolver.INSTANCE;
	}

	@Override
	public EntityMdResolver entityType(EntityType<? extends GenericEntity> entityType) {
		return EmptyEntityMdResolver.INSTANCE;
	}

	@Override
	public EntityMdResolver entityType(GmEntityTypeInfo gmEntityTypeInfo) {
		return EmptyEntityMdResolver.INSTANCE;
	}

	@Override
	public EntityMdResolver entityTypeSignature(String typeSignature) {
		return EmptyEntityMdResolver.INSTANCE;
	}

	@Override
	public ConstantMdResolver enumConstant(Enum<?> constant) {
		return EmptyConstantMdResolver.INSTANCE;
	}

	@Override
	public ConstantMdResolver enumConstant(GmEnumConstant constant) {
		return EmptyConstantMdResolver.INSTANCE;
	}

	@Override
	public EnumMdResolver enumClass(Class<? extends Enum<?>> entityClass) {
		return EmptyEnumMdResolver.INSTANCE;
	}

	@Override
	public EnumMdResolver enumType(EnumType enumType) {
		return EmptyEnumMdResolver.INSTANCE;
	}

	@Override
	public EnumMdResolver enumType(GmEnumType gmEnumType) {
		return EmptyEnumMdResolver.INSTANCE;
	}

	@Override
	public EnumMdResolver enumTypeSignature(String typeSignature) {
		return EmptyEnumMdResolver.INSTANCE;
	}

	@Override
	public PropertyMdResolver property(GmPropertyInfo gmPropertyInfo) {
		return EmptyPropertyMdResolver.INSTANCE;
	}

	@Override
	public PropertyMdResolver property(Property property) {
		return EmptyPropertyMdResolver.INSTANCE;
	}

	@Override
	public final <M extends MetaData> ModelMdResult<M> meta(EntityType<M> metaDataType) {
		return EmptyMdResult.EmptyModelMdResult.singleton();
	}

}
