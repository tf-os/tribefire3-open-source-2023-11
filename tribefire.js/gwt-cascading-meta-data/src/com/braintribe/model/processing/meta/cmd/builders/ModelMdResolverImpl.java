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

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.proxy.AbstractProxyEntityType;
import com.braintribe.model.generic.proxy.DynamicEntityType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.cmd.context.MutableSelectorContext;
import com.braintribe.model.processing.meta.cmd.context.aspects.EntityTypeAspect;
import com.braintribe.model.processing.meta.cmd.empty.EmptyEntityMdResolver;
import com.braintribe.model.processing.meta.cmd.empty.EmptyEnumMdResolver;
import com.braintribe.model.processing.meta.cmd.resolvers.EntityMdAggregator;
import com.braintribe.model.processing.meta.cmd.resolvers.EnumMdAggregator;
import com.braintribe.model.processing.meta.cmd.resolvers.ModelMdAggregator;
import com.braintribe.model.processing.meta.cmd.result.ModelMdResult;

@SuppressWarnings("unusable-by-js")
public class ModelMdResolverImpl extends MdResolverImpl<ModelMdResolver> implements ModelMdResolver {

	public final ModelMdAggregator modelMdAggregator;

	public ModelMdResolverImpl(ModelMdAggregator modelMdAggregator, MutableSelectorContext mst) {
		super(ModelMdResolverImpl.class, mst, modelMdAggregator);
		this.modelMdAggregator = modelMdAggregator;
	}

	@Override
	public EntityMdResolver entity(GenericEntity entity) {
		EntityType<GenericEntity> et = entity.entityType();
		return entityType(et).entity(entity); // this handles possible proxy type
	}

	@Override
	public EntityMdResolver entityClass(Class<? extends GenericEntity> entityClass) {
		return entityType(GMF.getTypeReflection().getEntityType(entityClass));
	}

	@Override
	public EntityMdResolver entityType(EntityType<? extends GenericEntity> entityType) {
		selectorContext.put(EntityTypeAspect.class, entityType);

		if (entityType instanceof AbstractProxyEntityType) {
			return proxyEntityType((AbstractProxyEntityType) entityType);
		}

		return entityTypeSignature(entityType.getTypeSignature());
	}

	private EntityMdResolver proxyEntityType(AbstractProxyEntityType entityType) {
		if (entityType instanceof DynamicEntityType) {
			DynamicEntityType dynamicEntityType = (DynamicEntityType) entityType;

			EntityMdAggregator entityMdAggregator = modelMdAggregator.getDynamicEntityMdAggregator(dynamicEntityType);
			entityMdAggregator.addLocalContextTo(selectorContext);

			return getEntityMetaDataBuilder(entityMdAggregator);
		}

		return EmptyEntityMdResolver.INSTANCE;
	}

	@Override
	public EntityMdResolver entityType(GmEntityTypeInfo gmEntityTypeInfo) {
		return entityTypeSignature(gmEntityTypeInfo.addressedType().getTypeSignature());
	}

	@Override
	public EntityMdResolver entityTypeSignature(String typeSignature) {
		if (getModelOracle().findEntityTypeOracle(typeSignature) == null) {
			return lenientOrThrowException(() -> EmptyEntityMdResolver.INSTANCE, () -> "Entity type not found: " + typeSignature);
		}

		EntityMdAggregator entityMdAggregator = modelMdAggregator.acquireEntityMdAggregator(typeSignature);
		entityMdAggregator.addLocalContextTo(selectorContext);

		return getEntityMetaDataBuilder(entityMdAggregator);
	}

	@Override
	public ConstantMdResolver enumConstant(Enum<?> constant) {
		return enumTypeSignature(constant.getDeclaringClass().getName()).constant(constant.name());
	}

	@Override
	public ConstantMdResolver enumConstant(GmEnumConstant constant) {
		return enumType(constant.getDeclaringType()).constant(constant.getName());
	}

	@Override
	public EnumMdResolver enumClass(Class<? extends Enum<?>> entityClass) {
		return enumTypeSignature(entityClass.getName());
	}

	@Override
	public EnumMdResolver enumType(EnumType enumType) {
		return enumTypeSignature(enumType.getTypeSignature());
	}

	@Override
	public EnumMdResolver enumType(GmEnumType gmEnumType) {
		return enumTypeSignature(gmEnumType.getTypeSignature());
	}

	@Override
	public EnumMdResolver enumTypeSignature(String typeSignature) {
		if (getModelOracle().findEnumTypeOracle(typeSignature) == null) {
			return lenientOrThrowException(() -> EmptyEnumMdResolver.INSTANCE, () -> "Enum type not found: " + typeSignature);
		}

		EnumMdAggregator enumMdAggregator = modelMdAggregator.acquireEnumMdAggregator(typeSignature);
		enumMdAggregator.addLocalContextTo(selectorContext);

		return getEnumMetaDataContextBuilder(enumMdAggregator);
	}

	@Override
	public PropertyMdResolver property(GmPropertyInfo gmPropertyInfo) {
		return entityType(gmPropertyInfo.declaringTypeInfo()).property(gmPropertyInfo);
	}

	@Override
	public PropertyMdResolver property(Property property) {
		return entityType(property.getDeclaringType()).property(property);
	}

	protected EntityMdResolver getEntityMetaDataBuilder(EntityMdAggregator entityMdAggregator) {
		return new EntityMdResolverImpl(entityMdAggregator, selectorContext);
	}

	protected EnumMdResolver getEnumMetaDataContextBuilder(EnumMdAggregator enumMdAggregator) {
		return new EnumMdResolverImpl(enumMdAggregator, selectorContext);
	}

	@Override
	public ModelMdResolver fork() {
		return new ModelMdResolverImpl(modelMdAggregator, selectorContext.copy());
	}

	@Override
	public <M extends MetaData> ModelMdResult<M> meta(EntityType<M> metaDataType) {
		return new MdResultImpl.ModelMdResultImpl<>(metaDataType, modelMdAggregator, selectorContext);
	}

}
