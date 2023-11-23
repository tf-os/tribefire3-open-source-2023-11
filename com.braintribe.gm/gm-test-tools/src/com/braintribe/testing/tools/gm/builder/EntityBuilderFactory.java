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
package com.braintribe.testing.tools.gm.builder;

import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * @author peter.gazdik
 */
public class EntityBuilderFactory {

	private static final Function<EntityType<?>, GenericEntity> defaultEntityFactory = EntityType::create;
	private static final Consumer<GenericEntity> defaultPostConstructHandler = e -> {
		/* no post construct handling */};

	private final Function<EntityType<?>, GenericEntity> entityFactory;
	private final Consumer<GenericEntity> postConstructHandler;

	public EntityBuilderFactory() {
		this(null, null);
	}

	public EntityBuilderFactory(Function<EntityType<?>, GenericEntity> entityFactory) {
		this(entityFactory, null);
	}

	public EntityBuilderFactory(Function<EntityType<?>, GenericEntity> entityFactory, Consumer<GenericEntity> postConstructHandler) {
		this.entityFactory = entityFactory != null ? entityFactory : defaultEntityFactory;
		this.postConstructHandler = postConstructHandler != null ? postConstructHandler : defaultPostConstructHandler;
	}

	public EntityBuilder make(EntityType<?> entityType) {
		return new EntityBuilder(entityType, entityFactory, postConstructHandler);
	}

}
