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
package com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers;

import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.ddra.endpoints.api.rest.v2.RestV2EndpointContext;
import com.braintribe.model.ddra.endpoints.v2.RestV2Endpoint;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.RestV2EntityQueryBuilder;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.query.fluent.AbstractQueryBuilder;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.web.rest.HttpRequestEntityDecoderOptions;
import com.braintribe.model.processing.web.rest.PropertyTypeResolver;
import com.braintribe.model.query.EntityQuery;

public abstract class AbstractEntityQueryingHandler<E extends RestV2Endpoint> extends AbstractQueryingHandler<E> {

	private ModelAccessoryFactory modelAccessoryFactory;
	private final Map<EntityType<?>, EntityType<?>> typeToInstantiableSubType = new ConcurrentHashMap<>();
	
	protected AbstractQueryBuilder<EntityQuery> decodeEntityQueryBuilder(RestV2EndpointContext<E> context) {
		EntityType<?> entityType = context.getEntityType();
		
		EntityType<?> instantiableSybType = findInstantiableSybTypeOf(entityType, context);
		if (instantiableSybType == null) {
			decode(context, null, null, HttpRequestEntityDecoderOptions.defaults().addIgnoredParameter("where"));
			// No instantiable sub type exists, so we simply use a condition that always evaluates to false
			return noEntityMatchingQuerye(entityType);

		} else { 
			return decodeEntityQueryBuilderWithType(context, instantiableSybType);
		}
	}

	private EntityType<?> findInstantiableSybTypeOf(EntityType<?> entityType, RestV2EndpointContext<?> context) {
		if (!entityType.isAbstract())
			return entityType;
		
		return typeToInstantiableSubType.computeIfAbsent(entityType, et -> findInstantiableSubType(et, context));
	}
	
	private EntityType<?> findInstantiableSubType(EntityType<?> et, RestV2EndpointContext<?> context) {
		String accessId = context.getParameters().getAccessId();
		ModelAccessory modelAccessory = modelAccessoryFactory.getForAccess(accessId);
		
		Set<EntityType<?>> instantiableSubTypes = modelAccessory.getOracle().getEntityTypeOracle(et).getSubTypes().onlyInstantiable().asTypes();
		return instantiableSubTypes.isEmpty() ? null : first(instantiableSubTypes);
	}

	private AbstractQueryBuilder<EntityQuery> decodeEntityQueryBuilderWithType(RestV2EndpointContext<E> context,
			EntityType<?> instantiableSybType) {

		EntityQueryBuilder builder = EntityQueryBuilder.from(context.getEntityType());

		JunctionBuilder<? extends AbstractQueryBuilder<EntityQuery>> conjunction = builder.where().conjunction();

		GenericEntity entity = instantiableSybType.createRaw(new RestV2EntityQueryBuilder(conjunction));
		decode(context, "where", entity,
				HttpRequestEntityDecoderOptions.defaults().setPropertyTypeResolver(getPropertyResolver(context.getParameters().getAccessId())));

		conjunction.close();

		return builder;
	}

	private AbstractQueryBuilder<EntityQuery> noEntityMatchingQuerye(EntityType<?> entityType) {
		return EntityQueryBuilder.from(entityType).where().value(0).eq(1);
	}

	private PropertyTypeResolver getPropertyResolver(String accessId) {
		ModelAccessory accessory = modelAccessoryFactory.getForAccess(accessId);

		return (entityType, property) -> {
			PropertyMdResolver mdResolver = accessory.getMetaData().entityType(entityType).property(property.getName());
			TypeSpecification ts = mdResolver.meta(TypeSpecification.T).exclusive();
			return ts != null ? ts.getType().reflectionType() : null;
		};
	}

	@Required
	@Configurable
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}
}
